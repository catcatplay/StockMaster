package com.stockmaster.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public final class PermissionUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PermissionUtils() {
    }

    public static List<String> parsePermissions(String permissionsJson) {
        if (permissionsJson == null || permissionsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(permissionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
