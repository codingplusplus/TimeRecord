package cc.tool.record;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import cc.tool.record.TimeRecord.CategoryColumns;
import cc.tool.record.TimeRecord.RecordColumns;

public class TimeRecordProvider extends ContentProvider {

	private static final String DATABASE_NAME = "timerecord.db";
	private static final int DATABASE_VERSION = 1;
	private static final String RECORD_TABLE_NAME = "record";
	private static final String CATEGORY_TABLE_NAME = "category";

	private static HashMap<String, String> sRecordMap;
	private static HashMap<String, String> sCategoryMap;

	private static final int RECORD = 1;
	private static final int RECORD_ID = 2;
	private static final int CATEGORY = 4;
	private static final int CATEGORY_ID = 5;

	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(TimeRecord.AUTHORITY, "record", RECORD);
		sUriMatcher.addURI(TimeRecord.AUTHORITY, "record/#", RECORD_ID);
		sUriMatcher.addURI(TimeRecord.AUTHORITY, "category", CATEGORY);
		sUriMatcher.addURI(TimeRecord.AUTHORITY, "category/#", CATEGORY_ID);
		
		sRecordMap = new HashMap<String, String>();
		sRecordMap.put(RecordColumns._ID, RecordColumns._ID);
		sRecordMap.put(RecordColumns.BEGIN, RecordColumns.BEGIN);
		sRecordMap.put(RecordColumns.END, RecordColumns.END);
		sRecordMap.put(RecordColumns.CATEGORY, RecordColumns.CATEGORY);
		sRecordMap.put(RecordColumns.NOTE, RecordColumns.NOTE);
		
		sCategoryMap = new HashMap<String, String>();
		sCategoryMap.put(CategoryColumns._ID, CategoryColumns._ID);
		sCategoryMap.put(CategoryColumns.NAME, CategoryColumns.NAME);
		sCategoryMap.put(CategoryColumns.TYPE, CategoryColumns.TYPE);

	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("sqlhelp", "oncreate begin");
			db.execSQL("CREATE TABLE " + RECORD_TABLE_NAME + " ("
					+ RecordColumns._ID + " INTEGER PRIMARY KEY,"
					+ RecordColumns.BEGIN + " INTEGER,"
					+ RecordColumns.END + " INTEGER,"
					+ RecordColumns.CATEGORY + " INTEGER," + RecordColumns.NOTE
					+ " TEXT" + ");");

			db.execSQL("CREATE TABLE " + CATEGORY_TABLE_NAME + " ("
					+ CategoryColumns._ID + " INTEGER PRIMARY KEY,"
					+ CategoryColumns.NAME + " TEXT," + CategoryColumns.TYPE
					+ " INTEGER" + ");");
			
			Log.d("sqlhelp", "oncreate end");
			
			String insertNew = "INSERT INTO " + CATEGORY_TABLE_NAME 
					+ " (" + CategoryColumns.NAME + ", "
					+ CategoryColumns.TYPE + ") " + " VALUES ";
			db.execSQL(insertNew + "(" + "'工作'," + "0" + ");");
			db.execSQL(insertNew + "(" + "'学习'," + "0" + ");");
			db.execSQL(insertNew + "(" + "'锻炼'," + "0" + ")");
			db.execSQL(insertNew + "(" + "'玩'," + "0" + ")");
			
			db.execSQL(insertNew + "(" + "'正事'," + "1" + ")");
			db.execSQL(insertNew + "(" + "'开会'," + "1" + ")");
			db.execSQL(insertNew + "(" + "'杂事'," + "1" + ")");
			
			db.execSQL(insertNew + "(" + "'专业课程'," + "2" + ")");
			db.execSQL(insertNew + "(" + "'英语'," + "2" + ")");
			
			db.execSQL(insertNew + "(" + "'散步'," + "3" + ")");
			db.execSQL(insertNew + "(" + "'健身'," + "3" + ")");
			db.execSQL(insertNew + "(" + "'爬山'," + "3" + ")");
			
			db.execSQL(insertNew + "(" + "'上网'," + "4" + ")");
			db.execSQL(insertNew + "(" + "'逛街'," + "4" + ")");
			db.execSQL(insertNew + "(" + "'K歌'," + "4" + ")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}

	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		pretreatUri(uri);
		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(mSelectTable);
        
        if (mSelectType == RECORD || mSelectType == RECORD_ID) {
            qb.setProjectionMap(sRecordMap);
        } else {
        	qb.setProjectionMap(sCategoryMap);
        }
        
        if (mIsId) {
        	qb.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case RECORD:
            return RecordColumns.CONTENT_TYPE;

        case RECORD_ID:
            return RecordColumns.CONTENT_ITEM_TYPE;
            
        case CATEGORY:
        	return CategoryColumns.CONTENT_TYPE;
        		
        case CATEGORY_ID:
        	return CategoryColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		pretreatUri(uri);
		
		if (mIsId) {
			throw new IllegalArgumentException("insert Unknown URI " + uri);
		}
		
        ContentValues values;
        values = new ContentValues(initialValues);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(mSelectTable, null, values);
        if (rowId > 0) {
            Uri addUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(addUri, null);
            return addUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		pretreatUri(uri);
		
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        
        if (mIsId) {
            String deleteId = uri.getPathSegments().get(1);
            count = db.delete(mSelectTable, BaseColumns._ID + "=" + deleteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
        } else {
            count = db.delete(mSelectTable, selection, selectionArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		pretreatUri(uri);		
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();        
        int count;
        if (mIsId) {
            String updateId = uri.getPathSegments().get(1);
            count = db.update(mSelectTable, values, BaseColumns._ID + "=" + updateId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
        } else {
        	count = db.update(mSelectTable, values, where, whereArgs);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	private String mSelectTable;
	private boolean mIsId;
	private int mSelectType;
	
	private void pretreatUri(Uri uri) {
		mSelectType = sUriMatcher.match(uri); 
        switch (mSelectType) {
        case RECORD:
        	mSelectTable = RECORD_TABLE_NAME;
        	mIsId = false;
            break;

        case RECORD_ID:
        	mSelectTable = RECORD_TABLE_NAME;
        	mIsId = true;        	
            break;

        case CATEGORY:
        	mSelectTable = CATEGORY_TABLE_NAME;
        	mIsId = false;
        	break;
        
        case CATEGORY_ID:
        	mSelectTable = CATEGORY_TABLE_NAME;
        	mIsId = true;
        	break;
        	
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}
}
