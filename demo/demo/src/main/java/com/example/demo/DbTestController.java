package com.example.demo;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db-test")
    public Map<String, String> testConnection() {
        String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
        String database = version != null && version.startsWith("PostgreSQL")
                ? "PostgreSQL"
                : "other";

        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        return Map.of("database", database, "result", String.valueOf(result));
    }
}