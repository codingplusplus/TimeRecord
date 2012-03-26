package cc.tool.record;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.TimePicker;
import cc.tool.record.GeneralModule.TodayCoursrAdapter;
import cc.tool.record.TimeRecord.RecordColumns;

public class MainActivity extends ListActivity implements OnClickListener {

	private TextView mTvTime;
	private Button mBtnStart;

	private boolean mTimed;

	private Handler mHandler;

	private Timer mTimer;

	private long mTimeBegin;
	private long mTimeEnd;

	private NotificationManager mNotificationManager;

	private Dialog mDialogSetTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mTvTime = (TextView) findViewById(R.id.tvTime);
		mTvTime.setOnClickListener(this);

		mBtnStart = (Button) findViewById(R.id.btnStart);
		mBtnStart.setOnClickListener(this);
		mTimed = false;

		mHandler = new Handler() {
			boolean normal = false;

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (normal) {
					mTvTime.setText(GeneralModule
							.timeToStringNoColon(getTimeMill()));
				} else {
					mTvTime.setText(GeneralModule.timeToString(getTimeMill()));
				}
				normal = !normal;
			}
		};
		
		mDialogSetTime = new Dialog(this);
		mDialogSetTime.setContentView(R.layout.dialog_set_time);		
		Button btnSetTime = (Button) mDialogSetTime.findViewById(R.id.btnOk);
		btnSetTime.setOnClickListener(this);
		Button btnSetNoTime = (Button) mDialogSetTime.findViewById(R.id.btnCannel);
		btnSetNoTime.setOnClickListener(this);	
		TimePicker timePicker = (TimePicker) mDialogSetTime.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);

		setList();
	}
	
	@Override
	protected void onResume() {
		initTime();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		mNotificationManager.cancel(R.layout.activity_main);
		super.onDestroy();
	}

	private static final int OPTION_STATS_RECORD = 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_STATS_RECORD, 0, R.string.stats_record);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTION_STATS_RECORD:
			Intent intent = new Intent(this, StatsActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private static final int CONTEXT_MENU_MODIFY = 1;
	private static final int CONTEXT_MENU_DELETE = 2;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_MENU_DELETE, 1, R.string.menu_delete);
		menu.add(0, CONTEXT_MENU_MODIFY, 0, R.string.menu_change);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info;
		info = (AdapterContextMenuInfo) item.getMenuInfo();
		Uri uri = ContentUris
				.withAppendedId(RecordColumns.CONTENT_URI, info.id);

		switch (item.getItemId()) {
		case CONTEXT_MENU_MODIFY:
			Intent intent = new Intent(this, InputActivity.class);
			intent.putExtra(InputActivity.sId, info.id);
			startActivity(intent);
			break;

		case CONTEXT_MENU_DELETE:
			getContentResolver().delete(uri, null, null);
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private String getToday() {
		String today = "";
		Calendar cal = GeneralModule.getCalerdarDay();

		today += RecordColumns.BEGIN;
		today += String.format(" >= %d and ", cal.getTimeInMillis());

		cal.add(Calendar.HOUR_OF_DAY, 24);
		today += RecordColumns.BEGIN;
		today += String.format(" < %d", cal.getTimeInMillis());

		Log.d("main", today);

		return today;
	}

	private void setList() {
		getListView().setEmptyView(findViewById(R.id.empty));

		Cursor mCursor = managedQuery(RecordColumns.CONTENT_URI,
				GeneralModule.sSelectCategory, getToday(), null,
				RecordColumns.BEGIN);
		startManagingCursor(mCursor);

		TodayCoursrAdapter mAdapter = new TodayCoursrAdapter(this,
				R.layout.list_item_all, mCursor);

		setListAdapter(mAdapter);
		registerForContextMenu(getListView());
	}

	private void setDefault(int defaults) {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		CharSequence text = GeneralModule.timeToString(mTimeBegin);

		final Notification notification = new Notification(
				R.drawable.ic_launcher, text, System.currentTimeMillis());

		notification.setLatestEventInfo(this, getString(R.string.app_name), text, contentIntent);

		notification.defaults = defaults;

		mNotificationManager.notify(R.layout.activity_main, notification);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStart:
			String btnName;
			if (mTimed) {
				btnName = getString(R.string.start);
				setTimeEnd();
			} else {
				btnName = getString(R.string.stop);
				mTimer.cancel();
				setTimeBegin(getTimeMill());
			}
			mTimed = !mTimed;

			mBtnStart.setText(btnName);
			break;

		case R.id.tvTime:
			if (mTimed) {
				mDialogSetTime.show();
			}
			break;
		
		case R.id.btnOk:
			TimePicker timePicker = (TimePicker) mDialogSetTime.findViewById(R.id.timePicker);
			DatePicker datePicker = (DatePicker) mDialogSetTime.findViewById(R.id.datePicker);

			int year = datePicker.getYear();
			int month = datePicker.getMonth();
			int day = datePicker.getDayOfMonth();			
			int hour = timePicker.getCurrentHour();
			int minute = timePicker.getCurrentMinute();
			
			Calendar calendar = GeneralModule.getCalerdarDay();
			calendar.set(year, month, day, hour, minute);
			
			setTimeBegin(calendar.getTimeInMillis());
			
			mDialogSetTime.dismiss();
			break;
			
		case R.id.btnCannel:
			mDialogSetTime.dismiss();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent MyIntent = new Intent(Intent.ACTION_MAIN);
			MyIntent.addCategory(Intent.CATEGORY_HOME);
			startActivity(MyIntent);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void setTimeBegin(long time) {
		mTimeBegin = time;
		String timeString = GeneralModule.timeToString(mTimeBegin).toString();
		timeString += getString(R.string.to);
		mTvTime.setText(timeString);
		mTvTime.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		setDefault(Notification.DEFAULT_VIBRATE);
	}

	private void setTimeEnd() {
		mNotificationManager.cancel(R.layout.activity_main);

		mTimeEnd = getTimeMill();
		String time = mTvTime.getText().toString();
		time += GeneralModule.timeToString(mTimeEnd).toString();
		mTvTime.setText(time);

		Intent intent = new Intent(this, InputActivity.class);
		intent.putExtra(InputActivity.sBEGIN, mTimeBegin);
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
		}, 0, 500);
		mTvTime.setGravity(Gravity.CENTER);
		mTimeBegin = 0;
		mTimeEnd = 0;
	}
}
