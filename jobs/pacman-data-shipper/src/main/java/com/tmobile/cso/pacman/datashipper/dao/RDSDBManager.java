package com.tmobile.cso.pacman.datashipper.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.cso.pacman.datashipper.dto.PolicyTable;
import com.tmobile.cso.pacman.datashipper.util.Constants;

/**
 * The Class RDSDBManager.
 */
public class RDSDBManager {

    /** The Constant dbURL. */
    private static final String DB_URL = System.getProperty(Constants.RDS_DB_URL);

    /** The Constant dbUserName. */
    private static final String DB_USER_NAME = System.getProperty(Constants.RDS_USER);

    /** The Constant dbPassword. */
    private static final String DB_PASSWORD = System.getProperty(Constants.RDS_PWD);

    private static final Logger LOGGER = LoggerFactory.getLogger(RDSDBManager.class);
    
    private RDSDBManager(){
        
    }
    
    /**
     * Gets the connection.
     *
     * @return the connection
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws SQLException
     *             the SQL exception
     */
    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Properties props = new Properties();

        props.setProperty("user", DB_USER_NAME);
        props.setProperty("password", DB_PASSWORD);
        conn = DriverManager.getConnection(DB_URL, props);

        return conn;
    }

    /**
     * Execute query.
     *
     * @param query
     *            the query
     * @return the list
     */
    public static List<Map<String, String>> executeQuery(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        try(
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);){
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
            LOGGER.error("Error Executing Query",ex);
        } 
        return results;
    }
    public static int executeUpdate(String query){
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();){
            return stmt.executeUpdate(query);
        }catch (Exception exception){
            LOGGER.error("Error Executing Query",exception);
        }
        return 0;
    }
    
    
    public static boolean insertNewPolicy(List<PolicyTable> policyList ) {
    	 try(
    	         Connection conn = getConnection();
    			 	
    		     ){
    		 String strQuery = "INSERT  IGNORE INTO cf_PolicyTable (policyId, policyUUID, policyName, policyDisplayName, policyDesc,  targetType, assetGroup,  policyParams, policyType,  severity, category, status)"
			 			+ "VALUES (?,?,?,?,?,?,?,'{\"params\":[{\"encrypt\":false,\"value\":\"hign\",\"key\":\"severity\"},{\"encrypt\":false,\"value\":\"security\",\"key\":\"policyCategory\"}]}','External',?,?,?);";
    		 PreparedStatement preparedStatement = conn.prepareStatement(strQuery);
    		 	
    		 policyList.forEach( policy -> {
    			 try {
					preparedStatement.setString(1, policy.getPolicyId());
					preparedStatement.setString(2, policy.getPolicyUUID());
					preparedStatement.setString(3, policy.getPolicyName());
					preparedStatement.setString(4, policy.getPolicyDisplayName());
					preparedStatement.setString(5, policy.getPolicyDesc());
					preparedStatement.setString(6, policy.getTarget());
					preparedStatement.setString(7, policy.getAssetgroup());
					preparedStatement.setString(8, policy.getSeverity());
					preparedStatement.setString(9, policy.getCategory());
					preparedStatement.setString(10, policy.getStatus());
                    preparedStatement.addBatch();
				} catch (SQLException e) {
					LOGGER.error("sql prepared statement error {}", e);
				}
    			 
    		 });
    		 
    		 int[] insertCounts = preparedStatement.executeBatch();
    		 LOGGER.error("Rows inserted: {}" , insertCounts.length);
    	          
    	        } catch (Exception ex) {
    	            LOGGER.error("Error Executing Query {}",ex);
    	            return false;
    	        } 
    	return true;
    }
}
