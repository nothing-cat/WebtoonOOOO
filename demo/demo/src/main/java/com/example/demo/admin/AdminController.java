package com.example.demo.admin;

import com.example.demo.auth.SessionUser;
import com.example.demo.common.AccessService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final JdbcTemplate jdbc;
    private final AccessService access;

    public AdminController(JdbcTemplate jdbc, AccessService access) {
        this.jdbc = jdbc;
        this.access = access;
    }

    public record WebtoonInput(String title, String pictureWriter, String storyWriter,
                              String description, String thumbnailUrl, String genreId,
                              String ageRating, List<String> platformIds) {}
    public record Member(UUID id, String username, String role, boolean blocked) {}
    public record BlockInput(Boolean blocked) {}
    public record Banner(String imageUrl, String altText, String linkUrl) {}

    @PostMapping("/admin/webtoons")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, String> createWebtoon(@RequestBody WebtoonInput input, HttpServletRequest request) {
        requireAdmin(request);
        String genre = optional(input.genreId());
        if (genre != null) requireLookup("select exists(select 1 from public.genres where id=?)", genre);
        var platforms = new LinkedHashSet<>(input.platformIds() == null ? List.of() : input.platformIds());
        for (String platform : platforms) {
            requireLookup("select exists(select 1 from public.platforms where id=?)", platform);
        }

        String id = "manual-" + UUID.randomUUID();
        jdbc.update("""
            insert into public.webtoons
                (id, title, picture_writer, story_writer, description, thumbnail_url, genre_id, age_rating)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """, id, AccessService.nonblank(input.title()), optional(input.pictureWriter()),
            optional(input.storyWriter()), optional(input.description()), webUrl(input.thumbnailUrl()),
            genre, optional(input.ageRating()));
        for (String platform : platforms) {
            jdbc.update("insert into public.webtoon_platforms (webtoon_id, platform_id) values (?, ?)", id, platform);
        }
        return Map.of("id", id);
    }

    @GetMapping("/admin/users")
    public List<Member> members(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "") String username, HttpServletRequest request) {
        requireAdmin(request);
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "페이지 번호를 확인해 주세요.");
        return jdbc.query("""
            select id, username, role, is_blocked from public.users
            where strpos(username, ?) > 0 order by username limit 31 offset ?
            """, (row, index) -> new Member(row.getObject("id", UUID.class), row.getString("username"),
            row.getString("role"), row.getBoolean("is_blocked")), username.strip(), 30L * page);
    }

    @PutMapping("/admin/users/{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockMember(@PathVariable UUID id, @RequestBody BlockInput input, HttpServletRequest request) {
        requireAdmin(request);
        if (input.blocked() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차단 상태를 입력해 주세요.");
        int updated = jdbc.update("update public.users set is_blocked=? where id=? and role='USER'",
            input.blocked(), id);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "관리할 수 있는 회원이 없습니다.");
    }

    @GetMapping("/banner")
    public Banner banner() {
        return jdbc.query("select image_url, alt_text, link_url from public.banners where id=1",
            (row, index) -> new Banner(row.getString(1), row.getString(2), row.getString(3)))
            .stream().findFirst().orElse(null);
    }

    @PutMapping("/admin/banner")
    public Banner updateBanner(@RequestBody Banner input, HttpServletRequest request) {
        requireAdmin(request);
        Banner banner = new Banner(webUrl(AccessService.nonblank(input.imageUrl())),
            AccessService.nonblank(input.altText()), webUrl(input.linkUrl()));
        jdbc.update("""
            insert into public.banners (id, image_url, alt_text, link_url) values (1, ?, ?, ?)
            on conflict (id) do update set image_url=excluded.image_url, alt_text=excluded.alt_text,
                link_url=excluded.link_url, updated_at=now()
            """, banner.imageUrl(), banner.altText(), banner.linkUrl());
        return banner;
    }

    private void requireAdmin(HttpServletRequest request) {
        if (access.require(SessionUser.require(request)).getRole() != com.example.demo.user.UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private void requireLookup(String sql, String id) {
        if (!Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, id))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "장르 또는 플랫폼을 확인해 주세요.");
        }
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String webUrl(String value) {
        String url = optional(value);
        if (url == null) return null;
        try {
            URI uri = URI.create(url);
            if (uri.getHost() != null && uri.getUserInfo() == null &&
                ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) return url;
        } catch (IllegalArgumentException ignored) {
            // Invalid URI syntax is reported as a bad request below.
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "http 또는 https 주소를 입력해 주세요.");
    }
}
