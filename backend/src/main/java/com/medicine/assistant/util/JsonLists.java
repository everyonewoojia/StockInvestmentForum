package com.medicine.assistant.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.assistant.common.ApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JsonLists {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<List<String>>() {
    };

    private JsonLists() {
    }

    public static String toJson(ObjectMapper objectMapper, List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.<String>emptyList() : values);
        } catch (Exception ex) {
            throw ApiException.serverError("Failed to serialize list");
        }
    }

    public static List<String> fromJson(ObjectMapper objectMapper, String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (Exception ex) {
            return new ArrayList<String>();
        }
    }
}
