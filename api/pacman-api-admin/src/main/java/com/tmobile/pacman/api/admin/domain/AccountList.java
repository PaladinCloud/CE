package com.tmobile.pacman.api.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class AccountList {
    private List<Map<String,String>> response;
    private long total;
}
