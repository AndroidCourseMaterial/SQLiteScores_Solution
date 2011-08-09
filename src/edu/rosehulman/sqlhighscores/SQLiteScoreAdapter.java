package edu.rosehulman.sqlhighscores;

import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteScoreAdapter {

    private static final String TAG = "SQLiteScoreAdapter"; 	// Just the tag we use to log
    
    private static final String DATABASE_NAME = "scores.db"; 	// Becomes the filename of the database
    private static final String TABLE_NAME = "scores"; 			// Only one table in this database
    private static final int DATABASE_VERSION = 1; 				// We increment this every time we change the database schema
                                                   				// which will kick off an automatic upgrade
    private SQLiteOpenHelper mOpenHelper; 						// Our special object that helps us open the database
    private SQLiteDatabase mDb; 								// The actual database we're dealing with (once it's open)
    
	public static final String ID_KEY = "_id";
	public static final int ID_COLUMN = 0;
	public static final String NAME_KEY = "name";
	public static final int NAME_COLUMN = 1;
	public static final String SCORE_KEY = "score";
	public static final int SCORE_COLUMN = 2;
	
    // ====================================================================================================================
    // INNER HELPER CLASS
    // ====================================================================================================================
    
    /**
     * This class makes it really easy to open, create, and upgrade databases.
     */
    private static class ScoreDbHelper extends SQLiteOpenHelper {
    	
		private static String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;
		private static String CREATE_STATEMENT;
		static {
			StringBuilder s = new StringBuilder();
			s.append("CREATE TABLE ");
			s.append(TABLE_NAME);
			s.append(" (");
			s.append(ID_KEY);
			s.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
			s.append(NAME_KEY);
			s.append(" TEXT, ");
			s.append(SCORE_KEY);
			s.append(" INTEGER)");
			CREATE_STATEMENT = s.toString();
		}
		
		public ScoreDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Called when the database is first created.
         * This is where we should create our tables.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating table");
			db.execSQL(CREATE_STATEMENT);
        }

        /**
         * Automatically called whenever the version number of the database on
         * the phone does not match the current one. This is where we either migrate
         * our data or just smoke everything and recreate the tables.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // In this case, we'll take the cheap way out and just delete everything
            Log.d(TAG, "Updating from version " + oldVersion + " to " + 
            		newVersion + ", which will destroy old table(s).");
            db.execSQL(DROP_STATEMENT);
            onCreate(db);            // Then we use our already defined create step to recreate
        }
    }
    
    // ====================================================================================================================
    // END INNER HELPER CLASS
    // ====================================================================================================================
    
    public SQLiteScoreAdapter(Context context) {
        // The main thing we need to do here is get access to the database
        // To do that, we use our helper class
        
        Log.d(TAG, "Asking for access to the database by making a database open helper");
        mOpenHelper = new ScoreDbHelper(context);
    }

	private ContentValues getContentValuesFromScore(Score s) {
		ContentValues rowValues = new ContentValues();
		rowValues.put(NAME_KEY, s.getName());
		rowValues.put(SCORE_KEY, s.getScore());
		return rowValues;
	}
	
	private Score getScoreFromCursor(Cursor c) {
		Score s = new Score();
		s.setID(c.getInt(ID_COLUMN));
		s.setName(c.getString(NAME_COLUMN));
		s.setScore(c.getInt(SCORE_COLUMN));
		return s;
	}
	
	/**
	 * Adds a score (which has no _id) to the database.  
	 * Returns the score that is in the database (which has an _id)
	 *  
	 * @param s  The score to add to the database
	 * @return The score that is in the database
	 */
	public Score addScore(Score s) {
		ContentValues rowValues = getContentValuesFromScore(s);
		mDb.insert(TABLE_NAME, null, rowValues);
		
		Cursor c = mDb.query(TABLE_NAME, new String[] { ID_KEY, NAME_KEY, SCORE_KEY },
				null, null, null, null, ID_KEY + " DESC", "1");
		c.moveToFirst();
		return getScoreFromCursor(c);
	}
	
	// CONSIDER: Add an update method
	
	public void removeScore(int id) {
		mDb.delete(TABLE_NAME, ID_KEY + " = ?", new String[] { Integer.toString(id) });
	}
	
	public void removeScore(Score s) {
		mDb.delete(TABLE_NAME, ID_KEY + " = ?", new String[] { Integer.toString(s.getID()) });
	}
	
	public Cursor getScoresCursor() {
		return mDb.query(TABLE_NAME, new String[] { ID_KEY, NAME_KEY, SCORE_KEY },
				null, null, null, null, SCORE_KEY + " DESC");
	}
	
	public Collection<? extends Score> getAllScores() {
		ArrayList<Score> scoreList = new ArrayList<Score>();
		Cursor c = getScoresCursor();
		if (c.moveToFirst()) {
			do {
				scoreList.add(getScoreFromCursor(c));
			} while (c.moveToNext());
		}
		return scoreList;
	}
	
	public void open() {
		mDb = mOpenHelper.getWritableDatabase();
	}
	
	public void close() {
		mDb.close();
	}
}
