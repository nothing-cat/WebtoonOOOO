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
    private static String value(Map<?,?> m,String k){Object v=m.get(k);return v==null?"":v.toString().trim();}
    private static String first(String a,String b){return a.isBlank()?b:a;}
    private static String stable(String prefix,String name){return prefix+UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));}
    @Bean ApplicationRunner importKmas(JdbcTemplate jdbc,TransactionTemplate tx){
        return args->{
            if(key.isBlank()||pages<1||pages>100||start<0)throw new IllegalArgumentException("Check KMAS key and page settings.");
            var client=RestClient.builder().baseUrl("https://www.kmas.or.kr").build();
            for(int i=0;i<pages;i++){
                int page=start+i;
                Map<?,?> body;
                try {
                    body=client.get().uri(b->b.path("/openapi/search/bookAndWebtoonList").queryParam("prvKey",key).queryParam("listSeCd","1").queryParam("pageNo",page).queryParam("viewItemCnt",100).build()).retrieve().body(Map.class);
                } catch(Exception e){throw new IllegalStateException("KMAS request failed; check key and endpoint (page "+page+").");}
                if(body==null)throw new IllegalStateException("Empty KMAS response");
                Map<?,?> result=body.get("result") instanceof Map<?,?> m?m:body;
                if(!"success".equalsIgnoreCase(value(result,"resultState")))throw new IllegalStateException("KMAS returned an unsuccessful response (page "+page+").");
                Object raw=body.get("itemlist");if(raw==null)raw=result.get("itemlist");
                if(!(raw instanceof List<?> items))throw new IllegalStateException("Unexpected KMAS itemlist format.");
                if(items.isEmpty())break;
                Integer count=tx.execute(status->{int n=0;for(Object item:items)if(item instanceof Map<?,?> row && save(jdbc,row))n++;return n;});
                log.info("KMAS page {}: {} rows imported",page,count);
                if(items.size()<100)break;
            }
        };
    }
    private boolean save(JdbcTemplate jdbc,Map<?,?> row){
        String source=value(row,"mastrId"), title=first(value(row,"prdctNm"),value(row,"title")), age=value(row,"ageGradCdNm");
        if(!"1".equals(value(row,"listSeCd"))||source.isBlank()||title.isBlank()||age.isBlank()||age.matches(".*(18|19|성인|청소년이용불가|청소년 관람불가).*"))return false;
        String id="kmas:"+source;
        String genre=value(row,"mainGenreCdNm"), genreId=null;
        if(!genre.isBlank()){
            jdbc.update("insert into public.genres(id,name) values (?,?) on conflict(name) do nothing",stable("kmas-",genre),genre);
            genreId=jdbc.queryForObject("select id from public.genres where name=?",String.class,genre);
        }
        jdbc.update("""
            insert into public.webtoons(id,title,picture_writer,story_writer,description,thumbnail_url,genre_id,age_rating)
            values (?,?,?,?,?,?,?,?)
            on conflict(id) do update set title=excluded.title,picture_writer=excluded.picture_writer,story_writer=excluded.story_writer,description=excluded.description,thumbnail_url=excluded.thumbnail_url,genre_id=excluded.genre_id,age_rating=excluded.age_rating,updated_at=now()
            """,id,title,value(row,"pictrWritrNm"),value(row,"sntncWritrNm"),value(row,"outline"),value(row,"imageDownloadUrl"),genreId,age);
        String platform=value(row,"pltfomCdNm");
        if(!platform.isBlank()){
            jdbc.update("insert into public.platforms(id,name) values (?,?) on conflict(name) do nothing",stable("kmas-",platform),platform);
            String platformId=jdbc.queryForObject("select id from public.platforms where name=?",String.class,platform);
            jdbc.update("insert into public.webtoon_platforms(webtoon_id,platform_id) values (?,?) on conflict do nothing",id,platformId);
        }
        return true;
    }
}
