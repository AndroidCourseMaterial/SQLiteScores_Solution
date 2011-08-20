package edu.rosehulman.sqlhighscores;

import android.app.Dialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Activity that displays a list view of Names & Scores
 * Currently using no data storage
 * Need to add SQLite for data storage
 * 
 * @author Dave Fisher
 *
 */
public class ScoresListActivity extends ListActivity {

	/**
	 * TAG for debug log messages
	 */
	public static final String TAG = "Scores";
	
	/**
	 * Dialog ID for adding and editing scores (one dialog for both tasks)
	 */
	private static final int DIALOG_ID = 1;

	/**
	 * Constant to indicate that no row is selected for editing
	 * Used when adding a new score entry
	 */
	public static final long NO_ID_SELECTED = -1;

	/**
	 * Index of the score / row selected
	 */
	private long mSelectedId = NO_ID_SELECTED;

	private SQLiteScoreAdapter mDbScoreAdapter; // Our special class that does the database magic
	private SimpleCursorAdapter mScoreAdapter;
	private Cursor mScoresCursor;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scores_list_activity);
		
		// This is all it takes to set up our database
		mDbScoreAdapter = new SQLiteScoreAdapter(this);
		mDbScoreAdapter.open();
		
		int viewResourceId = R.layout.score_list_item;
		mScoresCursor = mDbScoreAdapter.getScoresCursor();
		startManagingCursor(mScoresCursor);  // Note: A Content Provide can notify you of changes to the cursor automagically via the URI!

		String[] fromColumns = new String[] { SQLiteScoreAdapter.KEY_NAME, SQLiteScoreAdapter.KEY_SCORE };
		int[] toTextViews = new int[] { R.id.textViewName, R.id.textViewScore};
		mScoreAdapter = new SimpleCursorAdapter(this, viewResourceId, mScoresCursor, fromColumns, toTextViews);
		setListAdapter(mScoreAdapter);  // Magic function for a ListActivity to set the adapter
		registerForContextMenu(getListView());
	}

	/**
	 * ListActivity sets up the onItemClick listener for the list view automatically via this function
	 */
	@Override
	protected void onListItemClick(ListView listView, View selectedView, int position, long id) {
		super.onListItemClick(listView, selectedView, position, id);
		mSelectedId = id;
		showDialog(DIALOG_ID);
	}

	/**
	 * Standard menu.  Only has one item
	 * CONSIDER: Could add an edit and/or remove option when an item is selected
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	/**
	 * Standard listener for the option menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
		case R.id.menu_add:
			mSelectedId = NO_ID_SELECTED;
			showDialog(DIALOG_ID);
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Create a context menu for the list view
	 * Secretly surprised ListActivity doesn't provide a special magic feature here too. :)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflator = getMenuInflater();
		if(v == getListView()) {
			inflator.inflate(R.menu.scores_list_view_context_menu, menu);
		}
	}

	/**
	 * Standard listener for the context menu item selections
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case R.id.menu_item_list_view_delete:
			removeScore(info.id);
			return true;
		case R.id.menu_item_list_view_edit:
			mSelectedId = info.id;
			showDialog(DIALOG_ID);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Called when the activity is removed from memory (placeholder for later)
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	

    // ======================================================================
    // Data CRUD mechanisms (Create, read, update, and delete)
    // ======================================================================

	/**
	 * Create: Add a new score to the data storage mechanism
	 * @param s New score to add
	 */
	private void addScore(Score s) {
//		mScores.add(s);
//		Collections.sort(mScores);
//		mScoreAdapter.notifyDataSetChanged();
		mDbScoreAdapter.addScore(s);
		mScoresCursor.requery();
	}

	/**
	 * Read: Get a score for the data storage mechanism
	 * @param id Index of the score in the data storage mechanism 
	 */
	private Score getScore(long id) {
//		return mScores.get((int) id);
		return mDbScoreAdapter.getScore(id);
	}
	
	/**
	 * Update: Edit a score in the data storage mechanism
	 * Uses the values in the pass Score to updates the score at the mSelectedId location
	 * @param s Container for the new values to use in the update
	 */
	private void editScore(Score s) {
		if (mSelectedId == NO_ID_SELECTED) {
			Log.e(TAG, "Attempt to update with no score selected.");
		}
		s.setID(mSelectedId);
		mDbScoreAdapter.updateScore(s);
		mScoresCursor.requery();
//		Score selectedScore = getScore(mSelectedId);
//		selectedScore.setName(s.getName());
//		selectedScore.setScore(s.getScore());
//		Collections.sort(mScores);
//		mScoreAdapter.notifyDataSetChanged();
	}

	/**
	 * Delete: Remove a score from the data storage mechanism
	 * @param id Index of the score in the data storage mechanism
	 */
	private void removeScore(long id) {
//		mScores.remove((int)id);
//		Collections.sort(mScores);
//		mScoreAdapter.notifyDataSetChanged();
		mDbScoreAdapter.removeScore(id);
		mScoresCursor.requery();
	}
	

    // ======================================================================
    // Dialog for adding and updating Scores
    // ======================================================================
	
	/**
	 * Create the dialog if it has never been launched
	 * Uses a custom dialog layout
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		final Dialog dialog = new Dialog(this);
		switch (id) {
		case DIALOG_ID:
			dialog.setContentView(R.layout.score_dialog);
			dialog.setTitle(R.string.add_score);
			final EditText nameText = (EditText) dialog.findViewById(R.id.name_entry);
			final EditText scoreText = (EditText) dialog.findViewById(R.id.score_entry);
			final Button confirmButton = (Button) dialog.findViewById(R.id.confirm_score_button);
			final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_score_button);

			confirmButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Score s = new Score();  // Create an object to hold the values
					s.setName(nameText.getText().toString());
					try {
						s.setScore(Integer.parseInt(scoreText.getText().toString()));
					} catch (NumberFormatException e) {
						s.setScore(0);
					}
					if (mSelectedId == NO_ID_SELECTED) {
						addScore(s);	
					} else {
						editScore(s);
					}
					dialog.dismiss();
				}
			});

			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;
		default:
			break;
		}
		return dialog;
	}

	/**
	 * Update the dialog with appropriate text before presenting to the user
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_ID:
			final EditText nameText = (EditText) dialog.findViewById(R.id.name_entry);
			final EditText scoreText = (EditText) dialog.findViewById(R.id.score_entry);
			final Button confirmButton = (Button) dialog.findViewById(R.id.confirm_score_button);
			if (mSelectedId == NO_ID_SELECTED) {
				dialog.setTitle(R.string.add_score);
				confirmButton.setText(R.string.add);
				nameText.setText("");
				scoreText.setText("");
			} else {
				dialog.setTitle(R.string.update_score);
				confirmButton.setText(R.string.update);
				Score selectedScore = getScore(mSelectedId);
				nameText.setText(selectedScore.getName());
				scoreText.setText(""+selectedScore.getScore());
			}
			break;
		}
	}
}