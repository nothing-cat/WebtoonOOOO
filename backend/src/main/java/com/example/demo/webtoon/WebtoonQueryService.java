package com.example.demo.webtoon;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class WebtoonQueryService {
    private final NamedParameterJdbcTemplate jdbc;
    public WebtoonQueryService(NamedParameterJdbcTemplate jdbc){this.jdbc=jdbc;}
    public WebtoonPage search(String keyword,String genre,String platform,int page,int size,boolean popular,UUID user){
        if(page<1||size<1||size>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"페이지는 1 이상, 크기는 1~100이어야 합니다.");
        var params=new MapSqlParameterSource().addValue("keyword","%"+keyword.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%")
            .addValue("genre",genre).addValue("platform",platform).addValue("size",size).addValue("offset",(long)(page-1)*size).addValue("user",user,java.sql.Types.OTHER);
        String where="""
             where (lower(w.title) like :keyword escape '!' or lower(coalesce(w.picture_writer,'')) like :keyword escape '!' or lower(coalesce(w.story_writer,'')) like :keyword escape '!')
             and (:genre='' or w.genre_id=:genre)
             and (:platform='' or exists(select 1 from public.webtoon_platforms x where x.webtoon_id=w.id and x.platform_id=:platform))
            """;
        if(popular)where+=" and exists(select 1 from public.favorite_webtoons f where f.webtoon_id=w.id)";
        Long total=jdbc.queryForObject("select count(*) from public.webtoons w"+where,params,Long.class);
        String rank=popular?"(select count(*) from public.favorite_webtoons f where f.webtoon_id=w.id) desc, w.id":"w.title, w.id";
        String sql="""
            with selected as (
              select w.*, row_number() over(order by %s) as position
              from public.webtoons w %s order by %s limit :size offset :offset
            )
            select w.*,g.name as genre_name,
              coalesce(p.names,ARRAY[]::text[]) as platform_names,
              coalesce(p.ids,ARRAY[]::text[]) as platform_ids,
              (select count(*) from public.favorite_webtoons f where f.webtoon_id=w.id) as like_count,
              exists(select 1 from public.favorite_webtoons f where f.webtoon_id=w.id and f.user_id=:user) as is_favorite
            from selected w left join public.genres g on g.id=w.genre_id
            left join lateral (
              select array_agg(p.name order by p.id) names,array_agg(p.id order by p.id) ids
              from public.webtoon_platforms wp join public.platforms p on p.id=wp.platform_id where wp.webtoon_id=w.id
            ) p on true order by w.position
            """.formatted(rank,where,rank);
        List<WebtoonDto> rows=jdbc.query(sql,params,(rs,i)->new WebtoonDto(rs.getString("id"),rs.getString("title"),rs.getString("picture_writer"),rs.getString("story_writer"),rs.getString("genre_name"),rs.getString("genre_id"),Arrays.asList((String[])rs.getArray("platform_names").getArray()),Arrays.asList((String[])rs.getArray("platform_ids").getArray()),rs.getString("description"),rs.getString("thumbnail_url"),rs.getString("age_rating"),rs.getLong("like_count"),rs.getBoolean("is_favorite")));
        long count=total==null?0:total;
        return new WebtoonPage(rows,page,size,count,(count+size-1)/size);
    }
}
