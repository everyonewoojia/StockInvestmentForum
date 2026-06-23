package com.medicine.assistant.forum;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ForumDataAccess {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JdbcTemplate jdbcTemplate;

    public ForumDataAccess(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> query(String sql, Object... args) {
        return jdbcTemplate.query(sql, args, (rs, rowNum) -> readRow(rs));
    }

    public Optional<Map<String, Object>> queryOne(String sql, Object... args) {
        List<Map<String, Object>> rows = query(sql, args);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, args, Long.class);
        return value == null ? 0 : value.longValue();
    }

    public int update(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    public long insert(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i += 1) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0L : key.longValue();
    }

    public List<Object> args(Object... values) {
        List<Object> args = new ArrayList<Object>();
        for (Object value : values) {
            args.add(value);
        }
        return args;
    }

    public Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    private Map<String, Object> readRow(ResultSet rs) throws java.sql.SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= meta.getColumnCount(); i += 1) {
            String label = meta.getColumnLabel(i);
            Object value = rs.getObject(i);
            if (value instanceof Timestamp) {
                value = ((Timestamp) value).toLocalDateTime().format(DATE_TIME);
            }
            row.put(toCamel(label), value);
        }
        return row;
    }

    private String toCamel(String label) {
        String normalized = label == null ? "" : label.toLowerCase();
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < normalized.length(); i += 1) {
            char ch = normalized.charAt(i);
            if (ch == '_' || ch == ' ') {
                upperNext = true;
            } else if (upperNext) {
                builder.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}
