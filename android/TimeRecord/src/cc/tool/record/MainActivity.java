package cc.tool.record;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import cc.tool.record.TimeRecord.RecordColumns;

// just test git
public class MainActivity extends ListActivity implements OnClickListener {

	private TextView mTvTime;
	private Button mBtnStart;
	
	private boolean mTimed;

	private Handler mHandler;
	
	private Timer mTimer;
	
	private long mTimeStart;
	private long mTimeEnd;
	
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
		
		
		Log.d("main", "oncreate");
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		initTime();
		setList();
		Log.d("main", "onstart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("main", "onResume");
	}
	
	private void setList() {
        getListView().setEmptyView(findViewById(R.id.empty));
        
        Cursor mCursor = this.getContentResolver().query(RecordColumns.CONTENT_URI, null, null, null, null);
        startManagingCursor(mCursor);

        CursorAdapter mAdapter = new SimpleCursorAdapter(
                this, 
                R.layout.list_item_all,
                mCursor,                                              
                new String[] {RecordColumns.BEGIN, 
                			RecordColumns.END,
                			RecordColumns.CATEGORY,
                			RecordColumns.NOTE},           
                new int[] {R.id.begin, 
                		R.id.duration,
                		R.id.category,
                		R.id.note});  

        setListAdapter(mAdapter);
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
