package com.tmobile.cso.pacman.datashipper.dao;

import com.tmobile.cso.pacman.datashipper.dto.PolicyTable;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Class RDSDBManager.
 */
public class RDSDBManager {

    /**
     * The Constant dbURL.
     */
    private static final String DB_URL = System.getProperty(Constants.RDS_DB_URL);

    /**
     * The Constant dbUserName.
     */
    private static final String DB_USER_NAME = System.getProperty(Constants.RDS_USER);

    /**
     * The Constant dbPassword.
     */
    private static final String DB_PASSWORD = System.getProperty(Constants.RDS_PWD);

    private static final Logger LOGGER = LoggerFactory.getLogger(RDSDBManager.class);

    private RDSDBManager() {}

    /**
     * Gets the DB connection.
     *
     * @return the connection
     * @throws SQLException           the SQL exception
     */
    private static Connection getConnection() throws SQLException {
        Connection conn;
        Properties props = new Properties();

        props.setProperty("user", DB_USER_NAME);
        props.setProperty("password", DB_PASSWORD);
        conn = DriverManager.getConnection(DB_URL, props);

        return conn;
    }

    /**
     * Execute query.
     *
     * @param query the query
     * @return the list
     */
    public static List<Map<String, String>> executeQuery(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            Map<String, String> data;
            while (rs.next()) {
                data = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    data.put(rsmd.getColumnName(i), rs.getString(i));
                }
                results.add(data);
            }
        } catch (Exception ex) {
            LOGGER.error("Error Executing Query", ex);
        }

        return results;
    }

    public static int executeUpdate(String query) {
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();) {
            return stmt.executeUpdate(query);
        } catch (Exception ex) {
            LOGGER.error("Error Executing Query", ex);
        }

        return 0;
    }

    public static void insertNewPolicy(List<PolicyTable> policyList) {
        String strQuery = "INSERT INTO cf_PolicyTable (" +
                "policyId, " +              // 1
                "policyUUID, " +            // 2
                "policyName, " +            // 3
                "policyDisplayName, " +     // 4
                "policyDesc, " +            // 5
                "targetType, " +            // 6
                "assetGroup, " +            // 7
                "policyParams, " +          // 8
                "policyType, " +            // 9
                "severity, " +              // 10
                "category, " +              // 11
                "status, " +                // 12
                "policyFrequency, " +       // 13
                "userId, " +                // 14
                "createdDate, " +           // 14
                "resolutionUrl" +           // 16
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "policyName=VALUES(policyName)," +
                "policyDisplayName=VALUES(policyDisplayName), " +
                "policyDesc=VALUES(policyDesc), " +
                "targetType=VALUES(targetType), " +
                "assetGroup=VALUES(assetGroup), " +
                "policyParams=VALUES(policyParams), " +
                "severity=VALUES(severity), " +
                "category=VALUES(category), " +
                "resolutionUrl=VALUES(resolutionUrl)";

        String policyParams = "{\"params\":[{\"encrypt\":false,\"value\":\"%s\",\"key\":\"severity\"},"
                + "{\"encrypt\":false,\"value\":\"%s\",\"key\":\"policyCategory\"}]}";
        String createDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(strQuery)) {

            policyList.forEach(policy -> {
                try {
                    String params = String.format(policyParams, policy.getSeverity(), policy.getCategory());
                    preparedStatement.setString(1, policy.getPolicyId());
                    preparedStatement.setString(2, policy.getPolicyUUID());
                    preparedStatement.setString(3, policy.getPolicyName());
                    preparedStatement.setString(4, policy.getPolicyDisplayName());
                    preparedStatement.setString(5, policy.getPolicyDesc());
                    preparedStatement.setString(6, policy.getTarget());
                    preparedStatement.setString(7, policy.getAssetgroup());
                    preparedStatement.setString(8, params);
                    preparedStatement.setString(9, Constants.EXTERNAL_POLICY);                                                           // Type
                    preparedStatement.setString(10, policy.getSeverity());
                    preparedStatement.setString(11, policy.getCategory());
                    preparedStatement.setString(12, policy.getStatus());
                    preparedStatement.setString(13, Constants.DEFAULT_POLICY_FREQUENCY);
                    preparedStatement.setString(14, Constants.ADMIN_MAIL_ID);
                    preparedStatement.setString(15, createDate);
                    preparedStatement.setString(16, policy.getResolutionUrl());
                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    LOGGER.error("sql prepared statement error", e);
                }
            });

            int[] insertCounts = preparedStatement.executeBatch();
            LOGGER.info("Rows inserted: {}", insertCounts.length);
        } catch (Exception ex) {
            LOGGER.error("Error Executing Query", ex);
        }
    }
}
