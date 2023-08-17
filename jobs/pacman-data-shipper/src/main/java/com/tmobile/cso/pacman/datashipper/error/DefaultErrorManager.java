package com.tmobile.cso.pacman.datashipper.error;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultErrorManager extends ErrorManager {

	protected DefaultErrorManager() {
		
	}
	@Override
	public Map<String, Long> handleError(String index, String type, String loaddate,
			List<Map<String, String>> errorList, boolean checkLatest) {
		// TODO Auto-generated method stub
		return Collections.emptyMap();
	}

}
