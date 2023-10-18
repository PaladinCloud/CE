package com.tmobile.cso.pacman.datashipper;

import com.tmobile.cso.pacman.datashipper.util.ConfigUtil;
import com.tmobile.cso.pacman.datashipper.util.Constants;

import java.util.Map;

public class MainUtil {

    /**
     * Setup.
     *
     * @param params the params
     */
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
