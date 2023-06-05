package com.tmobile.pacman.api.admin.repository.service;

public interface EsCommonService {

    /**
     * Checks if given data stream or index or alias exists or not
     *
     * @param target data stream or index or alias name to check
     * @return boolean
     */
    boolean isDataStreamOrIndexOrAliasExists(String target);
}
