package com.nubank.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
	
	public static Date convertStringToDate(String dateAsString) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateAsString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
	public static long getDiferenceInSecondsBetweenTwoDates(Date firstDate, Date secondDate) {
		long diffInMilliseconds  = Math.abs(secondDate.getTime() - firstDate.getTime());
		return TimeUnit.SECONDS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
	}
	
}
