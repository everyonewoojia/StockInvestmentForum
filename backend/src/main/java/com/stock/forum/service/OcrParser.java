package com.stock.forum.service;

import com.stock.forum.dto.OcrDtos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OcrParser {
    private static final Pattern ARABIC_TIMES = Pattern.compile("(?:每日|每天|一日|日服|口服)?\\s*(\\d+)\\s*次");
    private static final Pattern CHINESE_TIMES = Pattern.compile("(?:每日|每天|一日|日服|口服)?\\s*([一二两三四五六七八九十])\\s*次");
    private static final Pattern DOSE = Pattern.compile("每次\\s*([\\d.]+\\s*(?:片|粒|袋|支|丸|颗|滴|ml|mL|毫升|mg|g|克))");
    private static final Pattern CYCLE = Pattern.compile("(\\d+)\\s*(?:天|日)");
    private static final Pattern CHINESE_CYCLE = Pattern.compile("([一二两三四五六七八九十])\\s*(?:天|日)");

    public OcrDtos.MedicineInfoResponse parse(List<String> lines) {
        OcrDtos.MedicineInfoResponse response = new OcrDtos.MedicineInfoResponse();
        if (lines == null || lines.isEmpty()) {
            response.cycle = 7;
            return response;
        }
        String merged = join(lines);
        response.medicineName = pickMedicineName(lines);
        response.dailyTimes = firstInt(ARABIC_TIMES, CHINESE_TIMES, merged);
        response.dose = pickDose(lines, merged);
        response.takeTime = pickTakeTime(merged);
        Integer cycle = firstInt(CYCLE, CHINESE_CYCLE, merged);
        response.cycle = cycle == null ? 7 : cycle;
        return response;
    }

    private String pickMedicineName(List<String> lines) {
        for (String raw : lines) {
            String line = clean(raw);
            if (line.contains("胶囊") || line.contains("片") || line.contains("颗粒")
                    || line.contains("口服液") || line.contains("丸") || line.contains("药")) {
                return line;
            }
        }
        for (String raw : lines) {
            String line = clean(raw);
            if (!line.isEmpty()) {
                return line;
            }
        }
        return "";
    }

    private String pickDose(List<String> lines, String merged) {
        Matcher matcher = DOSE.matcher(merged);
        if (matcher.find()) {
            return "每次" + matcher.group(1).replace(" ", "");
        }
        for (String raw : lines) {
            String line = clean(raw);
            if (line.contains("每次")) {
                return line;
            }
        }
        return "";
    }

    private String pickTakeTime(String merged) {
        if (merged.contains("饭前") || merged.contains("餐前") || merged.contains("空腹")) {
            return "饭前";
        }
        return "饭后";
    }

    private Integer firstInt(Pattern arabic, Pattern chinese, String text) {
        Matcher arabicMatcher = arabic.matcher(text);
        if (arabicMatcher.find()) {
            return Integer.valueOf(arabicMatcher.group(1));
        }
        Matcher chineseMatcher = chinese.matcher(text);
        if (chineseMatcher.find()) {
            return chineseToInt(chineseMatcher.group(1));
        }
        return null;
    }

    private Integer chineseToInt(String value) {
        if ("十".equals(value)) {
            return 10;
        }
        if (value.contains("十")) {
            String[] parts = value.split("十", -1);
            int tens = parts[0].isEmpty() ? 1 : digit(parts[0]);
            int ones = parts.length > 1 && !parts[1].isEmpty() ? digit(parts[1]) : 0;
            return tens * 10 + ones;
        }
        return digit(value);
    }

    private int digit(String value) {
        if ("一".equals(value)) return 1;
        if ("二".equals(value) || "两".equals(value)) return 2;
        if ("三".equals(value)) return 3;
        if ("四".equals(value)) return 4;
        if ("五".equals(value)) return 5;
        if ("六".equals(value)) return 6;
        if ("七".equals(value)) return 7;
        if ("八".equals(value)) return 8;
        if ("九".equals(value)) return 9;
        return 0;
    }

    private String join(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(clean(line)).append(' ');
        }
        return builder.toString();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
