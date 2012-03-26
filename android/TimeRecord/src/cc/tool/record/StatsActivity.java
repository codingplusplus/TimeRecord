package cc.tool.record;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;
import cc.tool.record.GeneralModule.TodayCoursrAdapter;
import cc.tool.record.TimeRecord.RecordColumns;

public class StatsActivity extends ListActivity {
	
	Calendar mCalendarBegin;
	Calendar mCalendarEnd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_stats);
		
		init();
	}
	
	private Button btnDate;
	private TodayCoursrAdapter mAdapter;
	
	private void init() {
		mCalendarBegin = GeneralModule.getCalerdarDay();
		mCalendarEnd = GeneralModule.getCalerdarDay();
		
		btnDate = (Button) findViewById(R.id.btnDate);
		btnDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SET_DATE);
			}
		});
		btnDate.setText(getDateSelectString());
		
        getListView().setEmptyView(findViewById(R.id.empty));

		String condition = getCondition();
		Log.d("stats", condition);
		
        Cursor cursor = managedQuery(RecordColumns.CONTENT_URI, 
        		GeneralModule.sSelectCategory, 
        		condition, 
        		null, 
        		RecordColumns.BEGIN);
        
        mAdapter = new TodayCoursrAdapter(this, 
        		R.layout.list_item_all_date, cursor);

        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
	}
	
	private void updateSelectDate(Calendar begin, Calendar end) {
		mCalendarBegin = begin;
		mCalendarEnd = end;
		
		String condition = getCondition();
		Log.d("stats", condition);
        Cursor cursor = managedQuery(RecordColumns.CONTENT_URI, 
        		GeneralModule.sSelectCategory, 
        		condition, 
        		null, 
        		RecordColumns.BEGIN);
        
        mAdapter.changeCursor(cursor);
	}
	
	private String getDateSelectString() {
		return GeneralModule.dateToString(mCalendarBegin) + " - " 
				+ GeneralModule.dateToString(mCalendarEnd);
	}
		
	private String getCondition() {
		Calendar end = Calendar.getInstance();
		end.setTime(mCalendarEnd.getTime());
		end.add(Calendar.DATE, 1);
		return RecordColumns.BEGIN + " >= " + mCalendarBegin.getTimeInMillis()
					+ " and " + RecordColumns.BEGIN + " < " + end.getTimeInMillis();
	}
	
	private static final int DIALOG_SET_DATE = 1;
		
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SET_DATE:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(
					R.layout.dialog_select_date, null);
			return new AlertDialog.Builder(StatsActivity.this)
					.setView(textEntryView)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									LinearLayout layout = (LinearLayout)textEntryView;
									DatePicker begin = (DatePicker) layout.findViewById(R.id.datePickerBegin);
									DatePicker end = (DatePicker) layout.findViewById(R.id.datePickerEnd);
									
									int year = begin.getYear();
									int month = begin.getMonth();
									int day = begin.getDayOfMonth();
									
									Calendar calendarBegin = GeneralModule.getCalerdarDay();
									calendarBegin.set(year, month, day);
									
									year = end.getYear();
									month = end.getMonth();
									day = end.getDayOfMonth();
									Calendar calendarEnd = GeneralModule.getCalerdarDay();
									calendarEnd.set(year, month, day);
									
									updateSelectDate(calendarBegin, calendarEnd);
								}
							})
					.setNegativeButton(R.string.cannel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();

		default:
			break;
		}
		return null;
	}
	
}
