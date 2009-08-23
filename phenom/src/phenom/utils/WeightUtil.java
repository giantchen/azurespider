package phenom.utils;

public class WeightUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static String parseDate(String date_) {					
		int iMonth = Integer.parseInt(date_.substring(4, 6));
		int year = Integer.parseInt(date_.substring(0, 4));
		int day = Integer.parseInt(date_.substring(6));
		
		if(iMonth > 0 && iMonth <= 12) {
			return date_;
		}
		
		iMonth = iMonth % 12;
		
		if(iMonth == 0) {
			iMonth = 12;
		}
		
		String sMonth = String.valueOf(iMonth);
		String sDay = String.valueOf(day);
		
		if(sMonth.length() == 1) {
			sMonth = "0" + sMonth;
		}
		
		if(sDay.length() == 1) {
			sDay = "0" + sDay;
		}
		
		date_ = String.valueOf(year + 1) + sMonth + sDay;
		
		return date_;
	}
	
	public static int parseDate(int date_) {
		return Integer.parseInt(parseDate(String.valueOf(date_)));
	}
}
