package com.paladincloud.common.aws;

import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Database {

    private Database() {
    }

    private static Connection getConnection() {
        Properties props = new Properties();

        props.setProperty("user", ConfigService.get(ConfigConstants.RDS.USER));
        props.setProperty("password", ConfigService.get(ConfigConstants.RDS.PWD));
        try {
            return DriverManager.getConnection(ConfigService.get(ConfigConstants.RDS.DB_URL),
                props);
        } catch (SQLException e) {
            throw new JobException("Unable to connect to database", e);
        }
    }

    public static List<Map<String, String>> executeQuery(String query) {
        var results = new ArrayList<Map<String, String>>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
            query)) {
            var metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                var data = new LinkedHashMap<String, String>();
                for (int idx = 1; idx <= columnCount; idx++) {
                    data.put(metaData.getColumnName(idx), rs.getString(idx));
                }
                results.add(data);
            }
        } catch (Exception ex) {
            throw new JobException("Error Executing Query", ex);
        }

        return results;
    }
}
