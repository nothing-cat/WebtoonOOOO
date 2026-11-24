package com.example.demo.favorite;
import com.example.demo.auth.SessionUser;
import com.example.demo.common.AccessService;
import com.example.demo.webtoon.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
@RestController
public class FavoriteController {
    private final FavoriteWebtoonRepository favorites;
    private final WebtoonService webtoons;
    private final AccessService access;
    private final JdbcTemplate jdbc;
    public FavoriteController(FavoriteWebtoonRepository favorites,WebtoonService webtoons,AccessService access,JdbcTemplate jdbc){this.favorites=favorites;this.webtoons=webtoons;this.access=access;this.jdbc=jdbc;}
    @PostMapping("/api/webtoons/{id}/favorite") @Transactional
    public WebtoonDto add(@PathVariable String id,HttpServletRequest request){var u=access.require(SessionUser.require(request));var w=webtoons.require(id);jdbc.update("insert into public.favorite_webtoons(user_id,webtoon_id) values (?,?) on conflict do nothing",u.getId(),id);return webtoons.dto(w,u.getId());}
    @DeleteMapping("/api/webtoons/{id}/favorite") @Transactional
    public WebtoonDto remove(@PathVariable String id,HttpServletRequest request){var u=access.require(SessionUser.require(request));var w=webtoons.require(id);jdbc.update("delete from public.favorite_webtoons where user_id=? and webtoon_id=?",u.getId(),id);return webtoons.dto(w,u.getId());}
    @GetMapping("/api/users/me/favorites") @Transactional(readOnly=true)
    public List<WebtoonDto> mine(HttpServletRequest request){var u=access.require(SessionUser.require(request));return favorites.findByUser_IdOrderByLastClickedAtDesc(u.getId()).stream().map(f->webtoons.dto(f.getWebtoon(),u.getId())).toList();}
}
