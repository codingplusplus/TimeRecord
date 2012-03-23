package cc.tool.record;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cc.tool.record.TimeRecord.CategoryColumns;
import cc.tool.record.TimeRecord.RecordColumns;

public class InputActivity extends ExpandableListActivity implements
		OnClickListener {

	public static String sBEGIN = "BEGIN";
	public static String sEnd = "END";

	private Button mBtnBegin;
	private Button mBtnEnd;
	private Button mBtnAdd;
	private Button mBtnCannel;

	private EditText mEditNote;

	private long mTimeBegin;
	private long mTimeEnd;

	private long mCategory;

	private int mGroupIndex;
	private CategoryAdapter mAdapter;

	public static String[] mCategoryItem = new String[] { CategoryColumns._ID,
			CategoryColumns.NAME };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);

		init();
		initExpandable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.menu_add_category);

		return super.onCreateOptionsMenu(menu);
	}

	private static final int sContextMenuModify = 1;
	private static final int sContextMenuDelete = 2;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo info;
		info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		View item = (View) info.targetView;
		TextView itemText = (TextView) item.findViewById(android.R.id.text1);

		String title = itemText.getText().toString();
		menu.setHeaderTitle(title);
		menu.add(0, sContextMenuDelete, 1, R.string.menu_delete);
		menu.add(0, sContextMenuModify, 0, R.string.menu_change);
	}

	private static String mDialogTitle;
	private static long mDialogSelectId;
	private static List<Long> mDialogSelectIds;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();
		long id = info.id;
		Uri selectUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI,
				id);

		switch (item.getItemId()) {
		case sContextMenuDelete:
			ContentResolver cr = getContentResolver();
			Cursor curParent = cr.query(selectUri,
					new String[] { CategoryColumns.TYPE }, null, null, null);
			curParent.moveToFirst();
			if (curParent.getLong(0) == 0) { // delete father category
				Cursor curAllChild = cr.query(CategoryColumns.CONTENT_URI,
						new String[] { CategoryColumns._ID, CategoryColumns.NAME }, 
						CategoryColumns.TYPE + "=" + id, null, null);

				int childCount = curAllChild.getCount();
				if (childCount != 0) {
					mDialogSelectId = id;
					mDialogSelectIds = new ArrayList<Long>();
					String allChildName = "";
					int recordCount = 0;

					curAllChild.moveToFirst();
					do {
						Long childId = curAllChild.getLong(0);
						String childName = curAllChild.getString(1);

						mDialogSelectIds.add(childId);
						allChildName += childName;
						allChildName += ", ";
						Cursor curRecord = cr.query(RecordColumns.CONTENT_URI,
								new String[] { RecordColumns._ID },
								RecordColumns.CATEGORY + "=" + childId, null,
								null);
						recordCount += curRecord.getCount();
						
					} while (curAllChild.moveToNext());
					allChildName = allChildName.substring(0,
							allChildName.length() - 2);

					if (recordCount == 0) {
						mDialogTitle = String.format(
								getString(R.string.will_delete_child),
								childCount, allChildName);
					} else {
						mDialogTitle = String
								.format(getString(R.string.will_delete_child_and_record),
										childCount, allChildName, recordCount);
					}
					removeDialog(DIALOG_DELETE_FATHER);
					showDialog(DIALOG_DELETE_FATHER);
				} else {
					cr.delete(selectUri, null, null);
				}
				
			} else { // delete children category
				Cursor cursor = cr.query(RecordColumns.CONTENT_URI,
						new String[] { RecordColumns._ID },
						RecordColumns.CATEGORY + "=" + id, null, null);

				if (cursor.getCount() != 0) {
					mDialogSelectId = id;
					mDialogTitle = cursor.getCount()
							+ getString(R.string.will_delete_record);
					removeDialog(DIALOG_DELETE_CHILD);
					showDialog(DIALOG_DELETE_CHILD);
				} else {
					cr.delete(selectUri, null, null);
				}
			}
			break;
			
		case sContextMenuModify:
			Intent intent = new Intent(this, CategoryChange.class);
			intent.putExtra(CategoryChange.sID, info.id);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private static final int DIALOG_DELETE_CHILD = 1;
	private static final int DIALOG_DELETE_FATHER = 2;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DELETE_CHILD:
			return new AlertDialog.Builder(this)
					.setTitle(mDialogTitle)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									ContentResolver cr = InputActivity.this
											.getContentResolver();
									Uri selectUri = ContentUris.withAppendedId(
											CategoryColumns.CONTENT_URI,
											mDialogSelectId);

									cr.delete(RecordColumns.CONTENT_URI,
											RecordColumns.CATEGORY + "="
													+ mDialogSelectId, null);
									cr.delete(selectUri, null, null);
								}
							})
					.setNegativeButton(R.string.cannel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();

		case DIALOG_DELETE_FATHER:
			return new AlertDialog.Builder(this)
					.setTitle(mDialogTitle)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									ContentResolver cr = InputActivity.this
											.getContentResolver();
									for (Long childId : mDialogSelectIds) {
										cr.delete(RecordColumns.CONTENT_URI,
												RecordColumns.CATEGORY + "="
														+ childId, null);

										Uri uriChild = ContentUris
												.withAppendedId(
														CategoryColumns.CONTENT_URI,
														childId);

										cr.delete(uriChild, null, null);
										
									}
									cr.delete(CategoryColumns.CONTENT_URI, 
											CategoryColumns._ID + "=" 
												+ mDialogSelectId, 
											null);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent i = new Intent(this, CategoryChange.class);
			i.putExtra(CategoryChange.sID, 0);
			startActivity(i);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mCategory = id;
		CheckedTextView ctv = (CheckedTextView) v;
		ctv.setChecked(true);
		return false;
	}

	private void initExpandable() {
		Cursor groupCursor = getContentResolver().query(
				CategoryColumns.CONTENT_URI, mCategoryItem,
				new String(CategoryColumns.TYPE + "=0"), null, null);
		mGroupIndex = groupCursor.getColumnIndex(CategoryColumns._ID);

		mAdapter = new CategoryAdapter(
				groupCursor,
				this,
				android.R.layout.simple_expandable_list_item_1,
				android.R.layout.simple_list_item_single_choice,
				new String[] { CategoryColumns.NAME },
				new int[] { android.R.id.text1 },
				new String[] { CategoryColumns.NAME },
				new int[] { android.R.id.text1 });

		setListAdapter(mAdapter);

		final ExpandableListView listView = getExpandableListView();
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);

		registerForContextMenu(getExpandableListView());

		output();
	}

	public class CategoryAdapter extends SimpleCursorTreeAdapter {

		public CategoryAdapter(Cursor cursor, Context context, int groupLayout,
				int childLayout, String[] groupFrom, int[] groupTo,
				String[] childrenFrom, int[] childrenTo) {
			super(context, cursor, groupLayout, groupFrom, groupTo,
					childLayout, childrenFrom, childrenTo);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			long id = groupCursor.getLong(mGroupIndex);
			return managedQuery(CategoryColumns.CONTENT_URI, mCategoryItem,
					CategoryColumns.TYPE + String.format("=%d", id), null, null);
		}

		@Override
		public ViewBinder getViewBinder() {
			return super.getViewBinder();
		}
	}

	private void output() {

		Cursor cc = managedQuery(CategoryColumns.CONTENT_URI, null, null, null,
				null);
		int count = cc.getCount();
		Log.d("expand", String.format("cursor count is %d", count));
		int posId = cc.getColumnIndex(CategoryColumns._ID);
		int posName = cc.getColumnIndex(CategoryColumns.NAME);
		int posType = cc.getColumnIndex(CategoryColumns.TYPE);
		cc.moveToFirst();
		for (int i = 0; i < count; ++i) {
			Log.d("all category",
					String.format("%d %s %d", cc.getLong(posId),
							cc.getString(posName), cc.getLong(posType)));

			cc.moveToNext();
		}
	}

	private void init() {
		Bundle bundle = getIntent().getExtras();
		mTimeBegin = bundle.getLong(sBEGIN);
		mTimeEnd = bundle.getLong(sEnd);

		mBtnBegin = (Button) findViewById(R.id.btnBegin);
		mBtnBegin.setText(GeneralModule.dateToString(mTimeBegin));
		mBtnBegin.setOnClickListener(this);

		mBtnEnd = (Button) findViewById(R.id.btnEnd);
		mBtnEnd.setText(GeneralModule.dateToString(mTimeEnd));
		mBtnEnd.setOnClickListener(this);

		mBtnAdd = (Button) findViewById(R.id.btnAdd);
		mBtnAdd.setOnClickListener(this);

		mBtnCannel = (Button) findViewById(R.id.btnCannel);
		mBtnCannel.setOnClickListener(this);

		mEditNote = (EditText) findViewById(R.id.editNote);

		mBtnBegin.setFocusable(true);
	}

	private void setTimeDisplay(Button btnTime, long time) {
		btnTime.setText(GeneralModule.dateToString(time));
	}

	private final static int sRequestBegin = 1;
	private final static int sRequestEnd = 2;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case sRequestBegin:
			if (resultCode == SetTimeActivity.sResultOk) {
				mTimeBegin = data.getLongExtra(SetTimeActivity.sTime, 0);
				setTimeDisplay(mBtnBegin, mTimeBegin);
			}
			break;

		case sRequestEnd:
			if (resultCode == SetTimeActivity.sResultOk) {
				mTimeEnd = data.getLongExtra(SetTimeActivity.sTime, 0);
				setTimeDisplay(mBtnEnd, mTimeEnd);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBegin:
			Intent intentBegin = new Intent(this, SetTimeActivity.class);
			intentBegin.putExtra(SetTimeActivity.sTime, mTimeBegin);
			startActivityForResult(intentBegin, sRequestBegin);
			break;

		case R.id.btnEnd:
			Intent intentEnd = new Intent(this, SetTimeActivity.class);
			intentEnd.putExtra(SetTimeActivity.sTime, mTimeEnd);
			startActivityForResult(intentEnd, sRequestEnd);
			break;

		case R.id.btnAdd:
			add();
			break;

		case R.id.btnCannel:
			finish();
			break;

		default:
			break;
		}
	}

	private void add() {
		if (mTimeBegin > mTimeEnd) {
			Toast.makeText(this, R.string.begin_greater_than_end,
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (mCategory == 0) {
			Toast.makeText(this, R.string.category_no_select,
					Toast.LENGTH_SHORT).show();
			return;
		}

		ContentValues values = new ContentValues();
		values.put(RecordColumns.BEGIN, mTimeBegin);
		values.put(RecordColumns.END, mTimeEnd);

		values.put(RecordColumns.CATEGORY, mCategory);
		values.put(RecordColumns.NOTE, mEditNote.getText().toString());
		getContentResolver().insert(RecordColumns.CONTENT_URI, values);

		finish();
	}
}
