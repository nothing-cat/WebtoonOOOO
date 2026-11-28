package com.example.demo.webtoon;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebtoonPlatformRepository extends JpaRepository<WebtoonPlatform, WebtoonPlatformId> {
    List<WebtoonPlatform> findByWebtoon_Id(String webtoonId);
    List<WebtoonPlatform> findByPlatform_Id(String platformId);
}
