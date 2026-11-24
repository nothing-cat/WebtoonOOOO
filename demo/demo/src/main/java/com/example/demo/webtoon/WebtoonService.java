package com.example.demo.webtoon;
import com.example.demo.favorite.*;
import com.example.demo.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.time.OffsetDateTime;

@Service
@Transactional
public class WebtoonService {
    private final WebtoonRepository webtoons;
    private final WebtoonPlatformRepository platforms;
    private final FavoriteWebtoonRepository favorites;
    public WebtoonService(WebtoonRepository webtoons, WebtoonPlatformRepository platforms, FavoriteWebtoonRepository favorites) {
        this.webtoons=webtoons; this.platforms=platforms; this.favorites=favorites;
    }
    public Webtoon require(String id) { return webtoons.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"웹툰이 없습니다.")); }
    public WebtoonDto dto(Webtoon w, UUID userId) {
        var links=platforms.findByWebtoon_Id(w.getId());
        return new WebtoonDto(w.getId(),w.getTitle(),w.getPictureWriter(),w.getStoryWriter(),w.getGenre()==null?null:w.getGenre().getName(),w.getGenre()==null?null:w.getGenre().getId(),links.stream().map(l->l.getPlatform().getName()).toList(),links.stream().map(l->l.getPlatform().getId()).toList(),w.getDescription(),w.getThumbnailUrl(),w.getAgeRating(),favorites.countByWebtoon_Id(w.getId()),userId!=null&&favorites.existsByUser_IdAndWebtoon_Id(userId,w.getId()));
    }
    @Transactional(readOnly=true)
    public List<WebtoonDto> search(String keyword,String genre,String platform,UUID userId) {
        return webtoons.search(keyword,genre,platform,Pageable.unpaged()).stream().map(w->dto(w,userId)).toList();
    }
    @Transactional(readOnly=true)
    public List<WebtoonDto> popular(String keyword,String genre,String platform,UUID userId) {
        return favorites.findPopular(genre,platform,keyword).stream().map(r->dto(require(r.getWebtoonId()),userId)).toList();
    }
    public WebtoonDto detail(String id,UUID userId) {
        Webtoon w=require(id);
        if(userId!=null) favorites.findById(new FavoriteWebtoonId(userId,id)).ifPresent(f->f.setLastClickedAt(OffsetDateTime.now()));
        return dto(w,userId);
    }
}
