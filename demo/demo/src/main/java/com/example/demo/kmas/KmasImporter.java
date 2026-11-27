package com.example.demo.kmas;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Configuration
public class KmasImporter {
    private static final org.slf4j.Logger log=LoggerFactory.getLogger(KmasImporter.class);
    @Value("${KMAS_API_KEY:}") private String key;
    @Value("${kmas.import.pages:1}") private int pages;
    @Value("${kmas.import.start-page:0}") private int start;
    private static final int PAGE_SIZE=100;
    @Value("${kmas.import.platforms:네이버웹툰,카카오웹툰,리디}") private String platformNames;
    private static String value(Map<?,?> m,String k){Object v=m.get(k);return v==null?"":v.toString().trim();}
    private static String first(String a,String b){return a.isBlank()?b:a;}
    private static String normalize(String s){return java.text.Normalizer.normalize(s,java.text.Normalizer.Form.NFC).strip().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
    private static String stable(String prefix,String name){return prefix+UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));}
    private Map<?,?> fetch(RestClient client,String platform,int offset){
        for(int attempt=1;attempt<=3;attempt++){
            try{return client.get().uri(b->b.path("/openapi/search/bookAndWebtoonList").queryParam("prvKey",key).queryParam("pltfomCdNm",platform).queryParam("pageNo",offset).queryParam("viewItemCnt",PAGE_SIZE).build()).retrieve().body(Map.class);}
            catch(org.springframework.web.client.RestClientResponseException e){int code=e.getStatusCode().value();if(attempt==3||(code!=429&&code<500))throw e;}
            catch(org.springframework.web.client.ResourceAccessException e){if(attempt==3)throw e;}
            try{Thread.sleep(1000L*attempt);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("KMAS retry interrupted");}
        }
        throw new IllegalStateException("KMAS request failed");
    }
    private static void merge(Map<String,Integer> target,Map<String,Integer> source){source.forEach((k,v)->target.merge(k,v,Integer::sum));}
    public record PlatformResult(String platform, String status, int batches) {}
    public record ImportStatus(String state, Map<String,Integer> counts, List<PlatformResult> platforms) {}

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Object importLock = new Object();
    private volatile ImportStatus status = new ImportStatus("IDLE", Map.of(), List.of());

    public KmasImporter(JdbcTemplate jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    public ImportStatus status() { return status; }

    public synchronized ImportStatus start() {
        if (key.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "서버에 KMAS API 키가 설정되어 있지 않습니다.");
        }
        if ("RUNNING".equals(status.state())) return status;
        status = new ImportStatus("RUNNING", Map.of(), List.of());
        Thread.ofPlatform().daemon().name("kmas-admin-import").start(() -> {
            try {
                synchronized (importLock) { status = runImport(100, 0); }
            } catch (Exception e) {
                log.error("KMAS import stopped: {}", e.getClass().getSimpleName());
                status = new ImportStatus("FAILED", Map.of(), List.of());
            }
        });
        return status;
    }

    @Bean
    @ConditionalOnProperty(name="kmas.import.enabled",havingValue="true")
    ApplicationRunner importKmas() {
        return args -> {
            if (key.isBlank() || pages < 1 || pages > 100 || start < 0) {
                log.warn("Check KMAS key and page settings.");
                return;
            }
            synchronized (importLock) { runImport(pages, start); }
        };
    }

    private ImportStatus runImport(int pages, int start) {
        var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);factory.setReadTimeout(30000);
        var client=RestClient.builder().requestFactory(factory).baseUrl("https://www.kmas.or.kr").build();
        Map<String,Integer> overall=new TreeMap<>();
        List<PlatformResult> results = new ArrayList<>();
        for(String name:platformNames.split(",")){
            final String platform=name.trim();if(platform.isBlank())continue;
            Map<String,Integer> summary=new TreeMap<>(),ratings=new TreeMap<>();
            Set<String> seen=new HashSet<>();Set<List<String>> fingerprints=new HashSet<>();
            int received=0,completed=0,nextOffset=start;String status="LIMIT_REACHED";
            for(int i=0;i<pages;i++){
                int offset=start+i*PAGE_SIZE;
                try{
                    Map<?,?> body=fetch(client,platform,offset);
                    if(body==null){status="EMPTY_RESPONSE";break;}
                    Map<?,?> result=body.get("result") instanceof Map<?,?> m?m:body;
                    if(!"success".equalsIgnoreCase(value(result,"resultState"))){log.warn("KMAS API error: {}, offset {}, message={}",platform,offset,value(result,"resultMessage").replace(key,"[redacted]"));status="API_ERROR";break;}
                    Object raw=body.get("itemList");if(raw==null)raw=body.get("itemlist");if(raw==null)raw=result.get("itemList");if(raw==null)raw=result.get("itemlist");
                    if(!(raw instanceof List<?> items)){status="INVALID_RESPONSE";break;}
                    if(items.isEmpty()){status="COMPLETE";break;}
                    received+=items.size();
                    List<String> fingerprint=items.stream().map(String::valueOf).sorted().toList();
                    if(!fingerprints.add(fingerprint)){status="REPEATED_BATCH";break;}
                    Set<String> batchSeen=new HashSet<>();
                    Map<String,Integer> batchRatings=new TreeMap<>();
                    Map<String,Integer> batch=tx.execute(transaction->{
                        Map<String,Integer> counts=new TreeMap<>();
                        for(Object item:items){
                            String outcome=item instanceof Map<?,?> row?save(jdbc,row,platform,seen,batchSeen):"invalid";
                            counts.merge(outcome,1,Integer::sum);
                            if(item instanceof Map<?,?> row&&(outcome.equals("adult")||outcome.equals("unknownAge")))batchRatings.merge(value(row,"ageGradCdNm"),1,Integer::sum);
                        }
                        return counts;
                    });
                    seen.addAll(batchSeen);merge(summary,batch);merge(ratings,batchRatings);completed++;nextOffset=offset+PAGE_SIZE;
                    log.info("KMAS {} offset {}: received={}, counts={}",platform,offset,items.size(),batch);
                    long total=-1;try{total=Long.parseLong(value(result,"totalCount"));}catch(NumberFormatException ignored){}
                    if(items.size()<PAGE_SIZE||(total>=0&&(long)offset+PAGE_SIZE>=total)){status="COMPLETE";break;}
                }catch(Exception e){status="FAILED";log.error("KMAS import failed: {}, offset {}, error={}",platform,offset,e.getClass().getSimpleName());break;}
            }
            log.info("KMAS SUMMARY platform={}, status={}, batches={}, received={}, counts={}, nextOffset={}, excludedRatings={}",platform,status,completed,received,summary,nextOffset,ratings);
            merge(overall,summary);
            results.add(new PlatformResult(platform, status, completed));
        }
        log.info("KMAS FINAL SUMMARY {}",overall);
        String state = results.isEmpty() ? "FAILED" :
            results.stream().allMatch(result -> "COMPLETE".equals(result.status())) ? "COMPLETE" : "PARTIAL";
        return new ImportStatus(state, Map.copyOf(overall), List.copyOf(results));
    }
    private String save(JdbcTemplate jdbc,Map<?,?> row,String requestedPlatform,Set<String> seen,Set<String> batchSeen){
        String title=first(value(row,"prdctNm"),value(row,"title")), age=value(row,"ageGradCdNm");
        String picture=value(row,"pictrWritrNm"), story=value(row,"sntncWritrNm");
        if(title.isBlank())return "invalid";
        String exclusion=KmasAgeRating.exclusion(age);
        if(exclusion!=null)return exclusion;
        String identity=String.join("\\037",normalize(title),normalize(picture),normalize(story));
        if(picture.isBlank()&&story.isBlank()){String fallback=first(value(row,"isbn"),value(row,"imageDownloadUrl"));if(fallback.isBlank())return "invalid";identity+= "\\037"+requestedPlatform+"\\037"+fallback;}
        String id=stable("kmas:",identity);
        if(seen.contains(id)||!batchSeen.add(id))return "duplicate";
        boolean exists=Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from public.webtoons where id=?)",Boolean.class,id));
        String genre=value(row,"mainGenreCdNm"), genreId=null;
        genre=switch(genre){case "로맨스/순정","순정" -> "로맨스";case "액션/무협","무협" -> "액션";case "생활","일상/생활" -> "일상";default -> genre;};
        if(!genre.isBlank()){
            jdbc.update("insert into public.genres(id,name) values (?,?) on conflict(name) do nothing",stable("kmas-",genre),genre);
            genreId=jdbc.queryForObject("select id from public.genres where name=?",String.class,genre);
        }
        jdbc.update("""
            insert into public.webtoons(id,title,picture_writer,story_writer,description,thumbnail_url,genre_id,age_rating)
            values (?,?,?,?,?,?,?,?)
            on conflict(id) do update set title=excluded.title,picture_writer=excluded.picture_writer,story_writer=excluded.story_writer,description=excluded.description,thumbnail_url=excluded.thumbnail_url,genre_id=excluded.genre_id,age_rating=excluded.age_rating,updated_at=now()
            """,id,title,value(row,"pictrWritrNm"),value(row,"sntncWritrNm"),value(row,"outline"),value(row,"imageDownloadUrl"),genreId,age);
        String platform=first(value(row,"pltfomCdNm"),requestedPlatform);
        if(platform.equals("리디북스"))platform="리디";
        if(!platform.isBlank()){
            jdbc.update("insert into public.platforms(id,name) values (?,?) on conflict(name) do nothing",stable("kmas-",platform),platform);
            String platformId=jdbc.queryForObject("select id from public.platforms where name=?",String.class,platform);
            jdbc.update("insert into public.webtoon_platforms(webtoon_id,platform_id) values (?,?) on conflict do nothing",id,platformId);
        }
        return exists?"updated":"inserted";
    }
}
