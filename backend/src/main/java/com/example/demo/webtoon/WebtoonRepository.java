package com.example.demo.webtoon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WebtoonRepository extends JpaRepository<Webtoon, String> {
    Page<Webtoon> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Webtoon> findByGenre_Id(String genreId, Pageable pageable);

    @Query("""
        select w from Webtoon w where
        (:keyword = '' or lower(w.title) like lower(concat('%', :keyword, '%'))
          or lower(coalesce(w.pictureWriter, '')) like lower(concat('%', :keyword, '%'))
          or lower(coalesce(w.storyWriter, '')) like lower(concat('%', :keyword, '%')))
        and (:genre = '' or w.genre.id = :genre)
        and (:platform = '' or exists (select wp from WebtoonPlatform wp where wp.webtoon = w and wp.platform.id = :platform))
        """)
    Page<Webtoon> search(@Param("keyword") String keyword, @Param("genre") String genre, @Param("platform") String platform, Pageable pageable);
}
