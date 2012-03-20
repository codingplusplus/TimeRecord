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
	
	private DatePicker datePicker;
	private TimePicker timePicker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_time);
		
		init();
	}
	
	private void init() {
		Button btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		
		Button btnCannel = (Button) findViewById(R.id.btnCannel);
		btnCannel.setOnClickListener(this);
		
		timePicker = (TimePicker) findViewById(R.id.timePicker);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
		timePicker.setIs24HourView(true);
		Bundle bundle = getIntent().getExtras();
		long time = bundle.getLong(sTime);		

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		
		datePicker.updateDate(year, month, day);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
	}
	
	public static int sResultOk = 1; 
	
	private long getTime() {
		int year = datePicker.getYear();
		int month = datePicker.getMonth();
		int day = datePicker.getDayOfMonth();
		
		int hour = timePicker.getCurrentHour();
		int minute = timePicker.getCurrentMinute();
		
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
			finish();
			break;
			
		default:
			break;
		}
	}
}
