package cc.tool.record;

import java.util.Calendar;

import cc.tool.record.TimeRecord.CategoryColumns;
import cc.tool.record.TimeRecord.RecordColumns;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class GeneralModule {
	
	public static CharSequence dateToString(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return DateFormat.format("MM.dd", cal);
	}
	
	public static CharSequence dateToString(Calendar calendar) {
		return DateFormat.format("yyyy.MM.dd", calendar);
	}
	
	public static CharSequence timeToStringNoColon(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return DateFormat.format("kk mm", cal);
	}
	
	public static CharSequence timeToString(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return timeToString(cal);
	}
	
	public static CharSequence timeToString(Calendar cal) {
		return DateFormat.format("kk:mm", cal);
	}
	
	public static Calendar getCalerdarDay() {
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		return calendar;
	}

	static final String[] sSelectCategory = {
		RecordColumns.BEGIN, 
		RecordColumns.END, 
		RecordColumns.CATEGORY, 
		RecordColumns.NOTE,
		RecordColumns._ID
	};

	public static class TodayCoursrAdapter extends ResourceCursorAdapter {

		public TodayCoursrAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			long begin = cursor.getLong(0);
			long end = cursor.getLong(1);
			String note = cursor.getString(3);
			
			
			long category = cursor.getInt(2);
			
			Uri uri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, category);
			Cursor curCategory = context.getContentResolver().query(uri, 
					new String[] { CategoryColumns.NAME }, null, null, null);
			curCategory.moveToFirst();
			String categoryName = curCategory.getString(0);
						
			LinearLayout layout = (LinearLayout) view;
			TextView beginView = (TextView) layout.findViewById(R.id.begin);
			TextView endView = (TextView) layout.findViewById(R.id.end);
			TextView categoryView = (TextView) layout.findViewById(R.id.category);
			TextView durationView = (TextView) layout.findViewById(R.id.duration);
			TextView noteView = (TextView) layout.findViewById(R.id.note);
			TextView dateView = (TextView) layout.findViewById(R.id.date);
			
			if (dateView != null) {
				dateView.setText(GeneralModule.dateToString(begin));
			}
			
			beginView.setText(GeneralModule.timeToString(begin));
			endView.setText(GeneralModule.timeToString(end));
			categoryView.setText(categoryName);
			durationView.setText(getDuration(begin, end));
			
			LayoutParams lp = noteView.getLayoutParams();
			if (note.equals("")) {
				lp.height = 1;
			} else {
				if (lp.height == 1) {
					lp.height = LayoutParams.WRAP_CONTENT;
				}
			}
			
			noteView.setLayoutParams(lp);
			layout.requestLayout();
			
			noteView.setText(note);
			
		}
		
		private String getDuration(long begin, long end) {
			Calendar calBegin = Calendar.getInstance();
			calBegin.setTimeInMillis(begin);
			calBegin.set(Calendar.SECOND, 0);
			calBegin.set(Calendar.MILLISECOND, 0);
			
			Calendar calEnd = Calendar.getInstance();
			calEnd.setTimeInMillis(end);
			calEnd.set(Calendar.MILLISECOND, 0);
			calEnd.set(Calendar.SECOND, 0);
			
			long durationMills = calEnd.getTimeInMillis() - calBegin.getTimeInMillis();
			long oneMinuteMills = 60 * 1000;
			long oneHourMills = 60 * oneMinuteMills;
			long hour = durationMills / oneHourMills;
			long minute = (durationMills % oneHourMills) / oneMinuteMills; 
			
			String hourString = formatNumber(hour);
			if (hourString.equals("  ")) {
				hourString += " ";
			} else {
				hourString += "h";
			}
			
			String minuteString = formatNumber(minute);
			if (minuteString.equals("  ")) {
				minuteString = " 0m";
			} else {
				minuteString += "m";
			}
			return hourString + minuteString;
		}
		
		private String formatNumber(long number) {
			if (number / 10 == 0) {
				if (number == 0) {
					return "  ";
				} else {
					return " "+number;
				}
			} else {
				return number+"";
			}
		}
	}
}
