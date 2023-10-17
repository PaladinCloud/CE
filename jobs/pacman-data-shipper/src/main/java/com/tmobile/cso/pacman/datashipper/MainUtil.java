package com.tmobile.cso.pacman.datashipper;

import com.tmobile.cso.pacman.datashipper.util.ConfigUtil;
import com.tmobile.pacman.commons.utils.Constants;

import java.util.Map;

public class MainUtil {
    private MainUtil() {
        throw new IllegalStateException("MainUtil is a utility class");
    }

    public static void setup(Map<String, String> params) throws Exception {

        ConfigUtil.setConfigProperties(params.get(Constants.CONFIG_CREDS));

        if (!params.isEmpty()) {
            params.forEach(System::setProperty);
        }

        if (params.get(Constants.CONFIG_QUERY) == null) {
            System.setProperty(Constants.CONFIG_QUERY, "select targetName,targetConfig,displayName from cf_Target where domain ='Infra & Platforms'");
        }
    }
}
