package com.tmobile.pacman.api.commons.dto;

import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.CommonUtils;

public class ExemptionCommonDetails {

    private String type;
    private Constants.Actions action;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Constants.Actions getAction() {
        return action;
    }

    public void setAction(Constants.Actions action) {
        this.action = action;
    }
}
