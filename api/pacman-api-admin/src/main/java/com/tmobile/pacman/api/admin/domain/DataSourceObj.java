package com.tmobile.pacman.api.admin.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DataSourceObj {

    private String indexName;

    private String dsName;
    private String targetType;

    private List<Map<String, String>> should = new ArrayList<>();

}
