package cc.tool.record;

import java.util.Calendar;

import android.text.format.DateFormat;

public class GeneralModule {
//	
//	public static CharSequence dateToString() {
//		Calendar cal = Calendar.getInstance();
//		return dateToString(cal);
//	}
//	
	
	public static CharSequence dateToString(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return dateToString(cal);
	}
	
	public static CharSequence dateToString(Calendar cal) {
		return DateFormat.format("kk:mm", cal);
	}
	
}
