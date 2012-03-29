package cc.tool.record;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class SetTimeActivity extends Activity implements OnClickListener{

	public static String sTime = "time";
	
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_set_time);
		
		init();
	}
	
	private void init() {
		Button btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		
		Button btnCannel = (Button) findViewById(R.id.btnCannel);
		btnCannel.setOnClickListener(this);
		
		mTimePicker = (TimePicker) findViewById(R.id.timePicker);
		mDatePicker = (DatePicker) findViewById(R.id.datePicker);
		mTimePicker.setIs24HourView(true);
		Bundle bundle = getIntent().getExtras();
		long time = bundle.getLong(sTime);		

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		
		mDatePicker.updateDate(year, month, day);
		mTimePicker.setCurrentHour(hour);
		mTimePicker.setCurrentMinute(minute);
	}
	
	public static int sResultOk = 1; 
	public static int sResultCannel = 2;
	
	private long getTime() {
		mDatePicker.clearFocus();
		mTimePicker.clearFocus();
		
		int year = mDatePicker.getYear();
		int month = mDatePicker.getMonth();
		int day = mDatePicker.getDayOfMonth();
		
		int hour = mTimePicker.getCurrentHour();
		int minute = mTimePicker.getCurrentMinute();
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute);
		return calendar.getTimeInMillis();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOk:			
			long time = getTime();
			setResult(sResultOk, new Intent().putExtra(sTime, time));
			finish();
			break;
			
		case R.id.btnCannel:
			setResult(sResultCannel);
			finish();
			break;
			
		default:
			break;
		}
	}
}
