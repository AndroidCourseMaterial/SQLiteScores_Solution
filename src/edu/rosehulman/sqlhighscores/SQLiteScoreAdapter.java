package edu.rosehulman.sqlhighscores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Wrapper class that provides access to the SQLite database.
 * All CRUD methods are based on a Score object for each row.
 * 
 * @author fisherds
 *
 */
public class SQLiteScoreAdapter {
	private static final String TAG = "SQLiteScoreAdapter"; 	// Just the tag we use to log
	private static final String DATABASE_NAME = "scores.db"; 	// Becomes the filename of the database
	private static final String TABLE_NAME = "scores"; 			// Only one table in this database
	private static final int DATABASE_VERSION = 1; 				// We increment this every time we change the database schema

	private SQLiteOpenHelper mOpenHelper; 						// Our special object that helps us open the database
	private SQLiteDatabase mDb; 		

	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_SCORE = "score";

	// Optional
	public static final int COLUMN_INDEX_ID = 0;
	public static final int COLUMN_INDEX_NAME = 1;
	public static final int COLUMN_INDEX_SCORE = 2;

	private static class ScoreDbHelper extends SQLiteOpenHelper {

		private static String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;
		private static String CREATE_STATEMENT;
		static {
			StringBuilder s = new StringBuilder();
			s.append("CREATE TABLE ");
			s.append(TABLE_NAME);
			s.append(" (");
			s.append(KEY_ID);
			s.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
			s.append(KEY_NAME);
			s.append(" TEXT, ");
			s.append(KEY_SCORE);
			s.append(" INTEGER)");
			CREATE_STATEMENT = s.toString();
		}

		public ScoreDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating table");
			db.execSQL(CREATE_STATEMENT);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Updating from version " + oldVersion + " to " + 
					newVersion + ", which will destroy old table(s).");
			db.execSQL(DROP_STATEMENT);
			onCreate(db);            // Then we use our already defined create step to recreate
		}
	}

	public SQLiteScoreAdapter(Context context) {
		// Create a SQLiteOpenHelper 
		Log.d(TAG, "Create a helper that will open/close database");
        mOpenHelper = new ScoreDbHelper(context);
	}

	public void open() {
		// Open the database
		mDb = mOpenHelper.getWritableDatabase();
	}

	public void close() {
		// Close the database
		mDb.close();
	}

	/**
	 * Add a score to the table using the name and score provided. If the Score is
	 * successfully created return the new id for that Score, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param score the body of the note
	 * @return id of the inserted row or -1 if failed
	 */
	public long addScore(Score score) {
		ContentValues rowValues = getContentValuesFromScore(score);
		return mDb.insert(TABLE_NAME, null, rowValues);
	}

	private ContentValues getContentValuesFromScore(Score score) {
		ContentValues rowValues = new ContentValues();
		rowValues.put(KEY_NAME, score.getName());
		rowValues.put(KEY_SCORE, score.getScore());
		return rowValues;
	}

	/**
	 * Return a Cursor over the list of all scores in the database
	 * 
	 * @return Cursor over all scores
	 */
	public Cursor getScoresCursor() {
		String[] projection = new String[] { KEY_ID, KEY_NAME, KEY_SCORE };
		return mDb.query(TABLE_NAME, projection,
				null, null, null, null, KEY_SCORE + " DESC");
	}
	
	/**
	 * Getting an individual score from the table
	 * 
	 * @param id Index of the row to get
	 * @return A new Score object with the data from the table row
	 */
	public Score getScore(long id) {
		String[] projection = new String[] { KEY_ID, KEY_NAME, KEY_SCORE };
		String selection = KEY_ID + "=" + id;
		Cursor c = mDb.query(true, TABLE_NAME, projection, selection, null,null,null,null,null);
		if(c != null && c.moveToFirst()) {
			Score s = new Score();
			s.setID(c.getInt(COLUMN_INDEX_ID));
			s.setName(c.getString(COLUMN_INDEX_NAME));
			s.setScore(c.getInt(COLUMN_INDEX_SCORE));
			return s;
		}
		return null;
	}

	/**
	 * Update the table with the new score data
	 * 
	 * @param score Score with an id that is already in the table.
	 */
	public void updateScore(Score score) {
		ContentValues rowValues = getContentValuesFromScore(score);
		String whereClause = KEY_ID + "=" + score.getID();
		mDb.update(TABLE_NAME, rowValues, whereClause, null);
	}
	
	/**
	 * Remove a score from the table using the id.
	 * 
	 * @param id Index of the row to remove
	 * @return true if the id is in the table and removed
	 */
	public boolean removeScore(long id) {
		return mDb.delete(TABLE_NAME, KEY_ID + "=" + id, null) > 0;
	}
	

	/**
	 * Remove a score from the table.
	 * 
	 * @param id Index of the row to remove
	 * @return true if the id is in the table and removed
	 */
	public boolean removeScore(Score s) {
		return removeScore(s.getID());
	}
}
