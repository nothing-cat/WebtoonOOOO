package com.example.demo.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface FavoriteWebtoonRepository extends JpaRepository<FavoriteWebtoon, FavoriteWebtoonId> {
    List<FavoriteWebtoon> findByUser_IdOrderByLastClickedAtDesc(UUID userId);
    boolean existsByUser_IdAndWebtoon_Id(UUID userId, String webtoonId);
    long countByWebtoon_Id(String webtoonId);

    @Query(value = """
        select f.webtoon_id as "webtoonId", count(*) as "likeCount"
        from public.favorite_webtoons f join public.webtoons w on w.id = f.webtoon_id
        where (:genre = '' or w.genre_id = :genre)
        and (:platform = '' or exists (select 1 from public.webtoon_platforms wp where wp.webtoon_id = w.id and wp.platform_id = :platform))
        and (:keyword = '' or w.title ilike concat('%', :keyword, '%') or w.picture_writer ilike concat('%', :keyword, '%') or w.story_writer ilike concat('%', :keyword, '%'))
        group by f.webtoon_id order by count(*) desc, f.webtoon_id asc
        """, nativeQuery = true)
    List<PopularWebtoonRow> findPopular(@Param("genre") String genre, @Param("platform") String platform, @Param("keyword") String keyword);

    interface PopularWebtoonRow { String getWebtoonId(); long getLikeCount(); }
}
