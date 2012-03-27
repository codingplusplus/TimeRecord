package cc.tool.record;

import android.app.ExpandableListActivity;
import android.content.ContentUris;
import android.content.Context;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import cc.tool.record.TimeRecord.CategoryColumns;

public class CategoryActivity extends ExpandableListActivity {

	private int mGroupIndex;
	private CategoryAdapter mAdapter;

	public static String[] mCategoryItem = new String[] { CategoryColumns._ID,
			CategoryColumns.NAME };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor groupCursor = getContentResolver().query(
				CategoryColumns.CONTENT_URI, mCategoryItem,
				new String(CategoryColumns.TYPE + "=0"), null, null);
		mGroupIndex = groupCursor.getColumnIndex(CategoryColumns._ID);

		mAdapter = new CategoryAdapter(groupCursor, this,
				android.R.layout.simple_expandable_list_item_1,
				android.R.layout.simple_list_item_single_choice,
				new String[] { CategoryColumns.NAME },
				new int[] { android.R.id.text1 },
				new String[] { CategoryColumns.NAME },
				new int[] { android.R.id.text1 });
		setListAdapter(mAdapter);
		registerForContextMenu(getExpandableListView());
		
		output();
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
			Log.d("all category", String.format("%d %s %d", 
					cc.getLong(posId),
					cc.getString(posName),
					cc.getLong(posType)));
			
			cc.moveToNext();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo info;
		info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		View item = (View) info.targetView;
		TextView itemText = (TextView) item.findViewById(android.R.id.text1);

		String title = itemText.getText().toString();
		menu.setHeaderTitle(title);
		menu.add(0, 0, 0, R.string.menu_delete);
		menu.add(0, 1, 1, R.string.menu_change);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();
		Uri selectUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI,
				info.id);

		switch (item.getItemId()) {
		case 0:
			getContentResolver().delete(selectUri, null, null);
			break;
		case 1:
			Intent intent = new Intent(this, CategoryChange.class);
			intent.putExtra(CategoryChange.sID, info.id);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.menu_add_category);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent i = new Intent(this, CategoryChange.class);
			i.putExtra(CategoryChange.sID, 0);
			startActivity(i);

			break;
		case 2:
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
		
        setResult(RESULT_OK, (new Intent()).setAction(String.format("%l", id)));
		finish();
		return true;
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
	}
}
