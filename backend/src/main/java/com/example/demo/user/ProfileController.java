package com.example.demo.user;

import com.example.demo.auth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final JdbcTemplate jdbc;
    private final UserRepository users;

    public ProfileController(JdbcTemplate jdbc, UserRepository users) {
        this.jdbc = jdbc;
        this.users = users;
    }

    public record Visibility(Boolean favorites, Boolean posts, Boolean comments) {}
    public record Work(String id, String title, String thumbnailUrl) {}
    public record Entry(String id, String targetId, String text) {}
    public record Profile(String name, List<Work> favorites, List<Entry> posts,
                          List<Entry> webtoonComments, List<Entry> postComments) {}
    private record ProfileDetails(String name, Visibility visibility) {}

    @GetMapping("/users/me/profile-visibility")
    public Visibility visibility(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return details(SessionUser.require(request)).visibility();
    }

    @PutMapping("/users/me/profile-visibility")
    public Visibility updateVisibility(@RequestBody Visibility visibility, HttpServletRequest request) {
        UUID id = SessionUser.require(request);
        if (visibility.favorites() == null || visibility.posts() == null || visibility.comments() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "공개 설정을 모두 선택해 주세요.");
        }
        int updated = jdbc.update("""
            update public.users set show_favorites=?, show_posts=?, show_comments=?
            where id=? and not is_blocked
            """, visibility.favorites(), visibility.posts(), visibility.comments(), id);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "설정을 변경할 수 없는 계정입니다.");
        return visibility;
    }

    @GetMapping("/profiles/{id}")
    @Transactional(readOnly = true)
    public Profile profile(@PathVariable UUID id, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-store");
        ProfileDetails target = details(id);
        UUID viewer = SessionUser.id(request);
        boolean privileged = viewer != null && users.findById(viewer)
            .filter(user -> !user.isBlocked())
            .map(user -> id.equals(viewer) || user.getRole() == UserRole.ADMIN)
            .orElse(false);
        Visibility visibility = target.visibility();

        List<Work> favorites = privileged || visibility.favorites() ? jdbc.query("""
            select w.id, w.title, w.thumbnail_url
            from public.favorite_webtoons f join public.webtoons w on w.id=f.webtoon_id
            where f.user_id=? order by f.last_clicked_at desc, f.webtoon_id
            """, (row, index) -> new Work(row.getString(1), row.getString(2), row.getString(3)), id) : null;

        List<Entry> posts = privileged || visibility.posts() ? entries("""
            select id, id, title from public.posts where user_id=? order by created_at desc, id desc
            """, id) : null;

        boolean showComments = privileged || visibility.comments();
        List<Entry> webtoonComments = showComments ? entries("""
            select id, webtoon_id, text from public.comments where user_id=? order by created_at desc, id desc
            """, id) : null;
        List<Entry> postComments = showComments ? entries("""
            select id, post_id, content from public.post_comments where user_id=? order by created_at desc, id desc
            """, id) : null;
        return new Profile(target.name(), favorites, posts, webtoonComments, postComments);
    }

    private ProfileDetails details(UUID id) {
        return jdbc.query("""
            select coalesce(nickname, username), show_favorites, show_posts, show_comments
            from public.users where id=?
            """, (row, index) -> new ProfileDetails(row.getString(1),
                new Visibility(row.getBoolean(2), row.getBoolean(3), row.getBoolean(4))), id)
            .stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."));
    }

    private List<Entry> entries(String query, UUID id) {
        return jdbc.query(query, (row, index) -> new Entry(row.getString(1), row.getString(2), row.getString(3)), id);
    }
}
