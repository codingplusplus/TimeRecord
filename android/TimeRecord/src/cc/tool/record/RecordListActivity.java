package cc.tool.record;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cc.tool.record.GeneralModule.TodayCoursrAdapter;
import cc.tool.record.TimeRecord.RecordColumns;

public abstract class RecordListActivity extends ListActivity {
	
	private TodayCoursrAdapter mAdapter;

	protected void setList() {
		getListView().setEmptyView(findViewById(R.id.empty));

		Cursor cursor = managedQuery(RecordColumns.CONTENT_URI,
				GeneralModule.sSelectCategory, getSelectCondition(), null,
				RecordColumns.BEGIN);

		mAdapter = new TodayCoursrAdapter(this,
				getListItemLayout(), cursor);

		setListAdapter(mAdapter);
		registerForContextMenu(getListView());
	}
	
	protected void updateList() {
		Cursor cursor = managedQuery(RecordColumns.CONTENT_URI,
				GeneralModule.sSelectCategory, getSelectCondition(), null,
				RecordColumns.BEGIN);
		mAdapter.changeCursor(cursor);
	}
	
	protected void changeCursor(Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}
	
	abstract protected String getSelectCondition();
	abstract protected int getListItemLayout();

	protected static final int CONTEXT_MENU_MODIFY = 1;
	protected static final int CONTEXT_MENU_DELETE = 2;

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
}
