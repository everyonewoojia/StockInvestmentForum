package com.stock.forum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ForumDataAccessTest {
    private ForumDataAccess data;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:forum_data_access_" + System.nanoTime() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE sample_rows (id BIGINT PRIMARY KEY AUTO_INCREMENT, user_name VARCHAR(64), post_count INT, created_at TIMESTAMP)");
        data = new ForumDataAccess(jdbcTemplate);
    }

    @Test
    void insertReturnsGeneratedKeyAndQueryMapsColumnsToCamelCase() {
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.of(2026, 6, 25, 20, 30, 0));

        long id = data.insert("INSERT INTO sample_rows (user_name, post_count, created_at) VALUES (?, ?, ?)", "north_research", 3, createdAt);
        Map<String, Object> row = data.queryOne("SELECT id, user_name, post_count, created_at FROM sample_rows WHERE id=?", id).get();

        assertThat(id).isPositive();
        assertThat(row.get("userName")).isEqualTo("north_research");
        assertThat(row.get("postCount")).isEqualTo(3);
        assertThat(row.get("createdAt")).isEqualTo("2026-06-25 20:30:00");
    }

    @Test
    void countUpdateQueryAndQueryOneUseJdbcTemplateConsistently() {
        data.insert("INSERT INTO sample_rows (user_name, post_count, created_at) VALUES (?, ?, ?)", "alpha", 1, data.now());
        data.insert("INSERT INTO sample_rows (user_name, post_count, created_at) VALUES (?, ?, ?)", "beta", 2, data.now());

        int updated = data.update("UPDATE sample_rows SET post_count=? WHERE user_name=?", 8, "alpha");
        long total = data.count("SELECT COUNT(*) FROM sample_rows");
        List<Map<String, Object>> rows = data.query("SELECT user_name, post_count FROM sample_rows ORDER BY user_name ASC");

        assertThat(updated).isEqualTo(1);
        assertThat(total).isEqualTo(2);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("userName")).isEqualTo("alpha");
        assertThat(rows.get(0).get("postCount")).isEqualTo(8);
        assertThat(data.queryOne("SELECT * FROM sample_rows WHERE user_name=?", "missing")).isEmpty();
    }
}
