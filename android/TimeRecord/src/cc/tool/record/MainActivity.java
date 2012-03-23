package cc.tool.record;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import cc.tool.record.TimeRecord.CategoryColumns;
import cc.tool.record.TimeRecord.RecordColumns;

public class MainActivity extends ListActivity implements OnClickListener {

	private TextView mTvTime;
	private Button mBtnStart;
	
	private boolean mTimed;

	private Handler mHandler;
	
	private Timer mTimer;
	
	private long mTimeStart;
	private long mTimeEnd;
	
	static final String[] sSelectCategory = {
		RecordColumns.BEGIN, RecordColumns.END, 
		RecordColumns.CATEGORY, RecordColumns.NOTE,
		RecordColumns._ID
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTvTime = (TextView) findViewById(R.id.tvTime);
		mTvTime.setOnClickListener(this);
		
		mBtnStart = (Button) findViewById(R.id.btnStart);
		mBtnStart.setOnClickListener(this);
		mTimed = false;
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				//mTvTime.setText(GeneralModule.dateToString(cal));
				mTvTime.setText(String.format("%d", getTimeMill()));
			}
		};
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		initTime();
		setList();
	}

	private static final int sContextMenuModify = 1;
	private static final int sContextMenuDelete = 2;
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, sContextMenuDelete, 1, R.string.menu_delete);
		menu.add(0, sContextMenuModify, 0, R.string.menu_change);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info;
		info = (AdapterContextMenuInfo) item.getMenuInfo();
		Uri uri = ContentUris.withAppendedId(RecordColumns.CONTENT_URI, info.id);
		
		switch (item.getItemId()) {
		case sContextMenuModify:
			
			break;
			
		case sContextMenuDelete:
			getContentResolver().delete(uri, null, null);
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	private String getToday() {
		String today = "";
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		today += RecordColumns.BEGIN;
		today += String.format(" >= %d and ", cal.getTimeInMillis());
		
		cal.add(Calendar.HOUR_OF_DAY, 24);
		today += RecordColumns.BEGIN;
		today += String.format(" < %d", cal.getTimeInMillis());
		
		return today;
	}
		
	private void setList() {
        getListView().setEmptyView(findViewById(R.id.empty));
        
        Cursor mCursor = this.getContentResolver().query(RecordColumns.CONTENT_URI, 
        		sSelectCategory, getToday(), null, RecordColumns.BEGIN);
        startManagingCursor(mCursor);

        TodayCoursrAdapter mAdapter = new TodayCoursrAdapter(this, 
        		R.layout.list_item_all, mCursor);

        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
	}

	class TodayCoursrAdapter extends ResourceCursorAdapter {

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
			
			beginView.setText(GeneralModule.dateToString(begin));
			endView.setText(GeneralModule.dateToString(end));
			categoryView.setText(categoryName);
			durationView.setText(getDuration(begin, end));
			if (note.equals("")) {
				LayoutParams lp = noteView.getLayoutParams();
				lp.height = 1;
				noteView.setLayoutParams(lp);
				layout.requestLayout();
			}
			
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
			
			
			
			return formatNumber(hour) + "." + formatNumber(minute);
		}
		
		private String formatNumber(long number) {
			if (number / 10 == 0) {
				if (number == 0) {
					return "00";
				} else {
					return "0"+number;
				}
			} else {
				return number+"";
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStart:
			String btnName;
			if (mTimed) {
				btnName = getResources().getString(R.string.start);
				setEndTime();
			} else {
				btnName = getResources().getString(R.string.stop);
				mTimer.cancel();
				setStartTime(getTimeMill());
			}
			mTimed = !mTimed;
			
			mBtnStart.setText(btnName);
			break;
			
		case R.id.tvTime:
			if (mTimed) {
				Intent intent = new Intent(this, SetTimeActivity.class);
				intent.putExtra(SetTimeActivity.sTime, mTimeStart);
				startActivityForResult(intent, sRequestSetTime);
			}
			break;

		default:
			break;
		}
	}
		
	private void setStartTime(long time) {
		mTimeStart = time;
		String timeString = GeneralModule.dateToString(mTimeStart).toString();
		timeString += getString(R.string.to);
		mTvTime.setText(timeString);
	}

	private static final int sRequestSetTime = 1;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case sRequestSetTime:		
			if (resultCode == SetTimeActivity.sResultOk) {
				long time = data.getLongExtra(SetTimeActivity.sTime, 0);
				setStartTime(time);				
			}
			break;

		default:
			break;
		}
		Log.d("main", "onresult");
	}
	
	private void setEndTime() {
		mTimeEnd = getTimeMill();
		String time = mTvTime.getText().toString();
		time += GeneralModule.dateToString(mTimeEnd).toString();
		mTvTime.setText(time);
		
		Intent intent = new Intent(this, InputActivity.class);
		intent.putExtra(InputActivity.sBEGIN, mTimeStart);
		intent.putExtra(InputActivity.sEnd, mTimeEnd);
		this.startActivity(intent);
	}
	
	private long getTimeMill() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}
	
	private void initTime() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
			}
		}, 0, 1000);
	}
	
}
