package phenom.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * TODO: type comment.
 *
 */
public class DateUtil {
    private static final DateFormat YYMMDD = new SimpleDateFormat("yyyyMMdd");   
    
    /**
     * 
     * @param date_ etc: 20080101
     * @return 20080101
     */
    public static Calendar getTime(int date_) {
        return getTime(String.valueOf(date_));
    }
    
    /**
     * 
     * @param date_ etc: 20080101
     * @return 20080101
     */
    public static Calendar getTime(String date_) {
        Calendar gc = null;
        int year, month, day;        
        if(date_.length() == 8) {        
            year = Integer.parseInt(date_.substring(0, 4));
            month = Integer.parseInt(date_.substring(4, 6)) - 1; //January = 0
            day = Integer.parseInt(date_.substring(6, 8));
            gc = new GregorianCalendar(year, month, day);
        }       
        return gc;
    }
    
    /**
     * 
     * @param date_
     * @return last date of date_
     * eg: 20080101 -- 20080131
     *     20080401 -- 20080430
     */ 
    public static String previousDay(String date_) {
        Calendar cal = getTime(date_);       
        cal.add(Calendar.DATE, -1);
        return YYMMDD.format(cal.getTime());
    }
    
    public static String nextDay(String date_) {
        Calendar cal = getTime(date_);
        cal.add(Calendar.DATE, 1);
        return YYMMDD.format(cal.getTime());
    }
    
    public static String nextDay(int date_) {
        return nextDay(String.valueOf(date_));
    }
    
    public static String previousTradeDate(String symbol_, String date_) {
    	return phenom.stock.Stock.previousTradeDate(symbol_, date_);    	
    }
    
    public static String previosTradeDate(String date_) {
    	return phenom.stock.Stock.previousTradeDate(date_);
    }
    
    public static List<String> tradeDates(String startDate_, String endDate_) {
    	return phenom.stock.Stock.tradeDates(startDate_, endDate_);
    }
}
