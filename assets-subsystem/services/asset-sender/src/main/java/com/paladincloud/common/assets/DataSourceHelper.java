package com.paladincloud.common.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class DataSourceHelper {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseHelper.class);
    private final ElasticSearchHelper elasticSearch;
    private final DatabaseHelper databaseHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public DataSourceHelper(ElasticSearchHelper elasticSearch, DatabaseHelper databaseHelper) {
        this.elasticSearch = elasticSearch;
        this.databaseHelper = databaseHelper;
    }

    public DataSourceInfo fetch(String dataSource) throws IOException {
        var assetGroups = getVisibleAssetGroups(getAliases(dataSource).stream().toList());
        var accounts = getAccounts(dataSource);
        LOGGER.info("There are {} asset groups and {} accounts", assetGroups.size(), accounts.size());
        return new DataSourceInfo(accounts, assetGroups);
    }

    private List<String> getVisibleAssetGroups(List<String> filter) {
        var query = "select distinct groupName from cf_AssetGroupDetails where isVisible = true "
            + STR."and groupName in ('\{String.join("','", filter)}')";
        var result = databaseHelper.executeQuery(query);
        return result.stream().map(r -> r.get("groupName")).toList();
    }

    private List<String> getAccounts(String dataSource) {
        var query = STR."select accountId from cf_Accounts where platform = '\{dataSource}' " +
            "and accountStatus= 'configured'";
        var result = databaseHelper.executeQuery(query);
        return result.stream().map(r -> r.get("accountId")).toList();
    }

    private Set<String> getAliases(String dataSource) throws IOException {
        var result = elasticSearch.invokeAndCheck(HttpMethod.GET,
            STR."/_alias?filter_path=\{dataSource}_*.aliases", null);
        var root = objectMapper.readTree(result.getBody());
        var uniqueAliases = new HashSet<String>();
        root.fields().forEachRemaining(item -> {
            var aliases = item.getValue().path("aliases");
            for (var it = aliases.fields(); it.hasNext(); ) {
                var alias = it.next();
                uniqueAliases.add(alias.getKey());
            }
        });
        return uniqueAliases;
    }
}