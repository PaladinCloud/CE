package com.tmobile.pacman.dto;

import lombok.Data;

@Data
public class Index {
    private String _index;
    private String _id;
    private int _version;
    private String result;
    private boolean forced_refresh;
    private Shards _shards;
    private int _seq_no;
    private int _primary_term;
    private int status;
}
