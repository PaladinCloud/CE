package com.tmobile.pacbot.gcp.inventory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmobile.pacbot.gcp.inventory.file.FileGenerator;
import com.tmobile.pacbot.gcp.inventory.file.FileManager;
import com.tmobile.pacbot.gcp.inventory.vo.ErrorVH;


/**
 * The Class ErrorManageUtil.
 */
public class ErrorManageUtil {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(ErrorManageUtil.class);
	
	/** The error map. */
	private static Map<String,List<ErrorVH>> errorMap = new HashMap<>();
	
	/**
	 * Instantiates a new error manage util.
	 */
	private ErrorManageUtil() {
		
	}
	
	/**
	 * Initialise.
	 */
	public static void initialise(){
		try {
			FileGenerator.writeToFile("gcp-loaderror.data", InventoryConstants.OPEN_ARRAY, false);
		} catch (IOException e) {
			log.error("Error in Initialise",e);
		}
	}
	
	/**
	 * Finalise.
	 */
	public static void finalise(){
		try {
			FileGenerator.writeToFile("gcp-loaderror.data",InventoryConstants.CLOSE_ARRAY, true);
		} catch (IOException e) {
			log.error("Error in finalise",e);
		}
	}
	
	/**
	 * Upload error.
	 *
	 * @param account the account
	 * @param region the region
	 * @param type the type
	 * @param exception the exception
	 */
	public static synchronized void  uploadError(String account, String region, String type, String exception) {
		try{
			List<ErrorVH> errorList = errorMap.get(account);
			if(errorList==null){
				errorList =  new ArrayList<>();
				errorMap.put(account, errorList);
			}
			ErrorVH error = new ErrorVH();
			error.setException(exception);
			error.setRegion(region);
			error.setType(type);
			errorList.add(error);
		}catch(Exception e){
		    log.error("Error in uploadError",e);
		}
	}
	
	
	
	public static Map<String,Object> formErrorCode() {
        Map<String,Object> errorCode = new HashMap<>();
        errorCode.put("jobName", System.getProperty("jobName"));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        errorCode.put("executionEndDate", sdf.format(new Date()));
        
        List<Map<String,Object>> errors = new ArrayList<>();
        for(Entry<String, List<ErrorVH>> errorDetail :errorMap.entrySet()) {
            Map<String,Object> error = new HashMap<>();
            List<Map<String,String>> details = new ArrayList<>();
            
            error.put("error", "Error while fetching Inventory for account "+errorDetail.getKey());
            for(ErrorVH errorVH : errorDetail.getValue()) {
                Map<String,String> detail = new HashMap<>();
                detail.put("type",errorVH.getType());
                detail.put("region",errorVH.getRegion());
                detail.put("exception",errorVH.getException());
                detail.put("account",errorDetail.getKey());
                details.add(detail);
            }
            error.put("details",details);
            errors.add(error);
        }
        
        errorCode.put("errors", errors);
        if(errors.isEmpty()) {
            errorCode.put("status","Success");
        } else {
            errorCode.put("status","Partial Success");
        }
        log.info("Return Info {}",errorCode);
        return errorCode;
    }
}
