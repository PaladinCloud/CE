package com.tmobile.pacman.api.asset.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DatasourceData {

    private List<String> accountIds;

    private Map<String, List<String>> assetGroupDomains;
}