package cc.tool.record;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import cc.tool.record.TimeRecord.RecordColumns;

public class TimeRecordActivity extends ListActivity implements OnClickListener {
	private TextView mView;
	private Button mBtnTest;
	private Button mBtnDelAll;
	private TextView mTextViewTest;
	CursorAdapter mAdapter;
	
	Cursor mCursor;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mView = (TextView) findViewById(R.id.textview);
		mBtnTest = (Button) findViewById(R.id.btnTest);
		mTextViewTest = (TextView) findViewById(R.id.textViewTest);
		mBtnTest.setOnClickListener(this);
		mBtnDelAll = (Button) findViewById(R.id.btnDelAll);
		mBtnDelAll.setOnClickListener(this);

        getListView().setEmptyView(findViewById(R.id.empty));
        
        mCursor = this.getContentResolver().query(RecordColumns.CONTENT_URI, null, null, null, null);
        startManagingCursor(mCursor);

        mAdapter = new SimpleCursorAdapter(
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

		mContentResolver = getContentResolver();
	}

	private ContentResolver mContentResolver;

	private int cBegin1 = 324;
	private int cBegin2 = 9564;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnTest:
			
			ContentValues values = new ContentValues();
			values.put(RecordColumns.BEGIN, cBegin1);
			values.put(RecordColumns.END, cBegin1);
			values.put(RecordColumns.CATEGORY, cBegin1);
			values.put(RecordColumns.NOTE, String.format("cccc"));
			mContentResolver.insert(RecordColumns.CONTENT_URI, values);
			values.put(RecordColumns.BEGIN, cBegin2);
			mContentResolver.insert(RecordColumns.CONTENT_URI, values);

			break;
			
		case R.id.btnDelAll:
			mContentResolver.delete(RecordColumns.CONTENT_URI, String.format("_id > 0"), null);
			break;

		default:
			break;
		}
	}
}