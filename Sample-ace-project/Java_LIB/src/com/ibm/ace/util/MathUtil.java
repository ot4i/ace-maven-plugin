package com.ibm.ace.util;

import org.apache.commons.math3.primes.Primes;


public class MathUtil {

	/**
	 * GetCurrentTimeStamp
	 * Returns a simple timestamp based on date.toString format 
	 * 
	 */
	
	public static boolean isPrime(int value) {
		
		return Primes.isPrime(value);
		
	}
	
	public static Boolean test() {
		
		return new Boolean(false);
		
	}

}