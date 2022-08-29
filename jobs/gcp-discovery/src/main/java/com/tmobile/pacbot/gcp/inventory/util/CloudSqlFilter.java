package com.tmobile.pacbot.gcp.inventory.util;

import com.tmobile.pacbot.gcp.inventory.vo.CloudDNSVH;
import com.tmobile.pacbot.gcp.inventory.vo.CloudSqlVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class CloudSqlFilter {
    Logger log= LoggerFactory.getLogger(CloudSqlFilter.class);
    public List<CloudSqlVH>  filterByDatabaseVersion(List<CloudSqlVH> cloudSQLList, DataBaseTypeEnum db_type){
        log.info("db version {}",db_type);
        String dbTypePattern="^"+ db_type.name().trim();
        Pattern pattern = Pattern.compile(dbTypePattern);
       return cloudSQLList.stream().filter(cloudSqlVH ->pattern.matcher(cloudSqlVH.getDatabaseVersion()).find() ).collect(Collectors.toList());
    }
}
