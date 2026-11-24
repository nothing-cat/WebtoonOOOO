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
@ConditionalOnProperty(name="kmas.import.enabled",havingValue="true")
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
    @Bean ApplicationRunner importKmas(JdbcTemplate jdbc,TransactionTemplate tx){
        return args->{
            if(key.isBlank()||pages<1||pages>100||start<0){log.warn("Check KMAS key and page settings.");return;}
            var client=RestClient.builder().baseUrl("https://www.kmas.or.kr").build();
            for(String requestedPlatform:platformNames.split(",")){
            requestedPlatform=requestedPlatform.trim();
            if(requestedPlatform.isBlank())continue;
            final String platform= requestedPlatform;
            for(int i=0;i<pages;i++){
                int page=start+i*PAGE_SIZE;
                Map<?,?> body;
                try {
                    body=client.get().uri(b->b.path("/openapi/search/bookAndWebtoonList").queryParam("prvKey",key).queryParam("pltfomCdNm",platform).queryParam("pageNo",page).queryParam("viewItemCnt",PAGE_SIZE).build()).retrieve().body(Map.class);
                } catch(Exception e){log.warn("KMAS request failed: platform={}, page={}, error={}",platform,page,e.getClass().getSimpleName());break;}
                if(body==null){log.warn("Empty KMAS response: {}, {}",platform,page);break;}
                Map<?,?> result=body.get("result") instanceof Map<?,?> m?m:body;
                if(!"success".equalsIgnoreCase(value(result,"resultState"))){log.warn("KMAS unsuccessful: {}, page {}, message={}",platform,page,value(result,"resultMessage").replace(key,"[redacted]"));break;}
                Object raw=body.get("itemList");
                if(raw==null)raw=body.get("itemlist");
                if(raw==null)raw=result.get("itemList");
                if(raw==null)raw=result.get("itemlist");
                if(!(raw instanceof List<?> items)){log.warn("Unexpected KMAS itemlist format: {}, {}",platform,page);break;}
                if(items.isEmpty())break;
                Integer count;
                try{count=tx.execute(status->{int n=0;for(Object item:items)if(item instanceof Map<?,?> row && save(jdbc,row,platform))n++;return n;});}
                catch(Exception e){log.error("KMAS DB import failed: {}, page {}, error={}",platform,page,e.getClass().getSimpleName());break;}
                log.info("KMAS {} offset {}: {} of {} rows imported",platform,page,count,items.size());
                long total=-1;
                try{total=Long.parseLong(value(result,"totalCount"));}catch(NumberFormatException ignored){}
                if(items.size()<PAGE_SIZE || (total>=0 && (long)page+PAGE_SIZE>=total))break;
            }
            }
        };
    }
    private boolean save(JdbcTemplate jdbc,Map<?,?> row,String requestedPlatform){
        String title=first(value(row,"prdctNm"),value(row,"title")), age=value(row,"ageGradCdNm");
        String picture=value(row,"pictrWritrNm"), story=value(row,"sntncWritrNm");
        if(title.isBlank()||!age.matches("전체(\\s*이용가|\\s*관람가)?|[0-9]세 이상|1[0-7]세 이상"))return false;
        String identity=String.join("\\037",normalize(title),normalize(picture),normalize(story));
        if(picture.isBlank()&&story.isBlank()){String fallback=first(value(row,"isbn"),value(row,"imageDownloadUrl"));if(fallback.isBlank())return false;identity+= "\\037"+requestedPlatform+"\\037"+fallback;}
        String id=stable("kmas:",identity);
        String genre=value(row,"mainGenreCdNm"), genreId=null;
        genre=switch(genre){case "로맨스/순정" -> "로맨스";case "액션/무협" -> "액션";default -> genre;};
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
        return true;
    }
}
