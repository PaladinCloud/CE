package com.paladincloud.common.config;

import java.util.Map;

public class ConfigApiResponse {

    public PropertySource[] propertySources;

    public static class PropertySource {

        public String name;
        public Map<String, String> source;
    }
}
