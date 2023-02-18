package com.ibm.ace.util;

import java.util.Date; 


public class TimeUtil {

	/**
	 * GetCurrentTimeStamp
	 * Returns a simple timestamp based on date.toString format 
	 * 
	 */
	public static String getCurrentTimestamp() {
		try {
			Date date = new Date();
			return date.toString(); 
		} catch (Exception e) {
			return "issue getting timestamp";
		}
	}

}