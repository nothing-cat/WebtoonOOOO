package com.example.demo.admin;

import com.example.demo.auth.SessionUser;
import com.example.demo.common.AccessService;
import com.example.demo.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class VisitorStatisticsController {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final JdbcTemplate jdbc;
    private final AccessService access;

    public VisitorStatisticsController(JdbcTemplate jdbc, AccessService access) {
        this.jdbc = jdbc;
        this.access = access;
    }

    public record Visit(UUID visitorId) {}
    public record Statistics(String date, long dailyVisitors, long totalMembers) {}

    @PostMapping("/visits")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void visit(@RequestBody Visit visit) {
        if (visit.visitorId() == null || visit.visitorId().version() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "방문 식별자가 올바르지 않습니다.");
        }
        jdbc.update("""
            insert into public.daily_visitors (visit_date, visitor_id) values (?, ?)
            on conflict (visit_date, visitor_id) do nothing
            """, LocalDate.now(SEOUL), visit.visitorId());
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<Statistics> statistics(HttpServletRequest request) {
        if (access.require(SessionUser.require(request)).getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
        LocalDate today = LocalDate.now(SEOUL);
        Statistics counts = jdbc.queryForObject("""
            select (select count(*) from public.daily_visitors where visit_date=?) as daily_visitors,
                   (select count(*) from public.users) as total_members
            """, (row, index) -> new Statistics(today.toString(), row.getLong(1), row.getLong(2)), today);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(counts);
    }
}
