package cc.tool.record;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cc.tool.record.TimeRecord.RecordColumns;

public class MainActivity extends RecordListActivity implements OnClickListener {

	private TextView mTvTime;
	private Button mBtnStart;

	private boolean mTimed;

	private Handler mHandler;

	private Timer mTimer;

	private long mTimeBegin;
	private long mTimeEnd;

	private NotificationManager mNotificationManager;

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
		
		setList();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateList();
		if (mTimeBegin == 0) {
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mHandler.sendEmptyMessage(0);
				}
			}, 0, 500);
			mTvTime.setGravity(Gravity.CENTER);
		}
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

	@Override
	protected String getSelectCondition() {
		Calendar cal = GeneralModule.getCalerdarDay();
		long begin = cal.getTimeInMillis();
		cal.add(Calendar.DATE, 1);
		long end = cal.getTimeInMillis();

		String today = RecordColumns.BEGIN + ">=" + begin + " and " 
				+ RecordColumns.BEGIN + "<" + end;

		return today;
	}
	
	@Override
	protected int getListItemLayout() {
		return R.layout.list_item_all;
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
				Intent intent = new Intent(this, SetTimeActivity.class);
				intent.putExtra(SetTimeActivity.sTime, mTimeBegin);
				startActivityForResult(intent, sRequestCode);
			}
			break;
		
		default:
			break;
		}
	}
	
	private static final int sRequestCode = 1;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case sRequestCode:
			if (resultCode == SetTimeActivity.sResultOk) {
				long time = data.getLongExtra(SetTimeActivity.sTime, 0);
				if (time != 0) {
					setTimeBegin(time);
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mTimeBegin != 0) {
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

		mTimeBegin = 0;
		mTimeEnd = 0;
	}

	private long getTimeMill() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}
}
