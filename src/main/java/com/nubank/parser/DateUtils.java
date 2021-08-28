package com.nubank.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static Date convertStringToDate(String dateAsString) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(dateAsString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
}
