package com.tmobile.pacman.commons.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;


/**
 * The Class RDSDBManager.
 */
@Component
public class RDSDBManager {

    /** The Constant dbURL. */
	@Value("${spring.datasource.url}")
    private  String dbUrl ;
	
	@Value("${spring.datasource.username}")
    private  String dbUser ;
	
	@Value("${spring.datasource.password}")
	private  String dbPassword ;
	
    private static Logger log = LoggerFactory.getLogger(RDSDBManager.class);
    
    
    /**
     * Gets the connection.
     *
     * @return the connection
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws SQLException
     *             the SQL exception
     */
    private Connection getConnection() throws  SQLException {
        Connection conn = null;
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        conn = DriverManager.getConnection(dbUrl, props);

        return conn;
    }

    /**
     * Execute query.
     *
     * @param query
     *            the query
     * @return the list
     */
    public List<Map<String, String>> executeQuery(String query) {
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
            log.error(ex.getMessage());
        } 
        return results;
    }

    public int executeUpdate(String query, List<Object> paramList){
        try(
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(query);){
            for(int i=0;i<paramList.size();i++){
                preparedStatement.setObject(i+1,paramList.get(i));
            }
            return preparedStatement.executeUpdate();
        } catch (Exception ex) {
        log.error(ex.getMessage());
    }
        return 0;
    }

    public List<Map<String, String>> executeQueryWithParam(String query, List<Object> paramList) {
        List<Map<String, String>> results = new ArrayList<>();
        try(
                Connection conn = getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                ){
            for(int i=0;i<paramList.size();i++){
                preparedStatement.setObject(i+1,paramList.get(i));
            }
            ResultSet rs = preparedStatement.executeQuery();
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
            log.error("Error Executing Query",ex);
        }
        return results;
    }

}
