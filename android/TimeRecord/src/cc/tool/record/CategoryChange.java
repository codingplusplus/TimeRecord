package cc.tool.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import cc.tool.record.TimeRecord.CategoryColumns;

public class CategoryChange extends Activity implements OnClickListener {

	public static String sID = "ID";
	EditText editName;
	RadioButton radioFather;
	RadioButton radioSon;
	Spinner spinnerCategory;
	Button btnOk;
	Button btnCannel;
	HashMap<String, Long> mMapCategoryId = new HashMap<String, Long>();
	HashMap<Long, Integer> mMapCategoryPos = new HashMap<Long, Integer>();
	
	private Uri mUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_change);

		init();
		setCategory();
		setSelectItem();
	}

	private void init() {
		editName = (EditText) findViewById(R.id.editName);

		radioSon = (RadioButton) findViewById(R.id.radioSon);
		radioSon.setOnClickListener(this);

		radioFather = (RadioButton) findViewById(R.id.radioFather);
		radioFather.setOnClickListener(this);

		spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);

		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);

		btnCannel = (Button) findViewById(R.id.btnCannel);
		btnCannel.setOnClickListener(this);
	}

	private void setCategory() {
		String[] mCategoryItem = new String[] { CategoryColumns._ID,
				CategoryColumns.NAME };

		Cursor groupCursor = getContentResolver().query(
				CategoryColumns.CONTENT_URI, mCategoryItem,
				new String(CategoryColumns.TYPE + "=0"), null, null);
		int posId = groupCursor.getColumnIndex(CategoryColumns._ID);
		int posName = groupCursor.getColumnIndex(CategoryColumns.NAME);

		List<String> listNames = new ArrayList<String>();
		
		if (groupCursor.moveToFirst()) {
			int postion = 0;		
			do {
				long id = groupCursor.getLong(posId);
				String name = groupCursor.getString(posName);
				mMapCategoryId.put(name, id);
				mMapCategoryPos.put(id, postion++);
				listNames.add(name);
			} while (groupCursor.moveToNext());
		}

		if (listNames.isEmpty()) {
			radioFather.setChecked(true);
			radioSon.setVisibility(View.INVISIBLE);
			spinnerCategory.setVisibility(View.INVISIBLE);
		} else {
			radioSon.setChecked(true);
		}

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this,
				android.R.layout.simple_spinner_item,
				groupCursor,
				new String[] { CategoryColumns.NAME },
				new int[] { android.R.id.text1 } );
		spinnerCategory.setAdapter(adapter);
		spinnerCategory.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					mCategoryType = id;
				}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	private void setSelectItem() {
		Bundle bundle = getIntent().getExtras();
		long id = bundle.getLong(sID);
		if (id == 0) {
			return;
		}
		mUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, id);
		
		Uri uri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, id);
		Cursor cur = managedQuery(uri, 
				new String[] {CategoryColumns.NAME, 
						CategoryColumns.TYPE},
				null, null, null);
		cur.moveToFirst();
		String name = cur.getString(0);
		long type = cur.getLong(1);
		
		editName.setText(name);
		if (type == 0) {
			radioFather.setChecked(true);
			radioSon.setVisibility(View.INVISIBLE);
			spinnerCategory.setVisibility(View.INVISIBLE);
		} else {
			radioSon.setChecked(true);
			radioFather.setVisibility(View.INVISIBLE);
			spinnerCategory.setSelection(mMapCategoryPos.get(type));
		}
	}

	private long mCategoryType;
	private String mCategoryName;

	private boolean checkName() {
		if (mCategoryName.equals("")) {
			Toast.makeText(this, R.string.category_is_empty, 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (mMapCategoryId.containsKey(mCategoryName)) {
			Toast.makeText(this, R.string.category_is_repeat, 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

	private void add() {
		mCategoryName = editName.getText().toString();
		if (mUri == null && !checkName()) {
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(CategoryColumns.NAME, mCategoryName);
		if (radioFather.isChecked()) {
			values.put(CategoryColumns.TYPE, 0);
		} else {
			values.put(CategoryColumns.TYPE, mCategoryType);
		}
		
		if (mUri != null) {
			getContentResolver().update(mUri, values, null, null); 
		} else {
			getContentResolver().insert(CategoryColumns.CONTENT_URI, values);
		}

		finish();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOk:
			add();
			break;

		case R.id.btnCannel:
			finish();
			break;

		case R.id.radioSon:
			spinnerCategory.setVisibility(View.VISIBLE);
			break;

		case R.id.radioFather:
			spinnerCategory.setVisibility(View.INVISIBLE);
			break;

		default:
			break;
		}
	}

}
