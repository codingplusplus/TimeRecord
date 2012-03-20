package cc.tool.record;

import android.net.Uri;
import android.provider.BaseColumns;

public final class TimeRecord {

    public static final String AUTHORITY = "cc.tool.record.provider";

    public static final class RecordColumns implements BaseColumns {

    	private RecordColumns() { }
    	
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/record");
	
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.record";
	
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.record";
	
	    public static final String BEGIN = "begin";
	    
	    public static final String END = "end";
	
	    public static final String CATEGORY = "category";
	    
	    public static final String NOTE = "note";
	
    }

    public static final class CategoryColumns implements BaseColumns {

    	private CategoryColumns() {}
    	
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/category");
	
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.category";
	
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.category";
	
	    public static final String NAME = "name";
	
	    public static final String TYPE = "type";
    } 
    
}
