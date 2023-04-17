package com.tmobile.cso.pacman.tenable.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExportStatus {

    private String status;

    @SerializedName(value = "chunks_available")
    private List<Integer> chunkIds;


    public ExportStatus(String status, List<Integer> chunkIds) {
        this.status = status;
        this.chunkIds = chunkIds;
    }

    public ExportStatus() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getChunkIds() {
        return chunkIds;
    }

    public void setChunkIds(List<Integer> chunkIds) {
        this.chunkIds = chunkIds;
    }
}
