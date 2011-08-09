package edu.rosehulman.sqlhighscores;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ScoresListActivity extends Activity {
    
	private static final int DIALOG_ID = 1;
    
	private SQLiteScoreAdapter mDbScoreAdapter; // Our special class that does the database magic
    private SimpleCursorAdapter mScoreAdapter;
    private Cursor mScoresCursor;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores_list_activity);
        
        // This is all it takes to set up our database connection in the activity
        mDbScoreAdapter = new SQLiteScoreAdapter(this);
        mDbScoreAdapter.open();
        
        // Now we get our list of scores from the database
        ListView scoresListView = (ListView) findViewById(R.id.scores_list_view);
		mScoresCursor = mDbScoreAdapter.getScoresCursor();
		startManagingCursor(mScoresCursor);  // Note: A Content Provide can notify you of changes to the cursor automagically via the URI!

        //mScoreAdapter = new ArrayAdapter<Score>(this, android.R.layout.simple_list_item_1, mScores);
		String[] fromColumns = new String[] { SQLiteScoreAdapter.NAME_KEY, SQLiteScoreAdapter.SCORE_KEY };
		int[] toTextViews = new int[] { R.id.textViewName, R.id.textViewScore};
		mScoreAdapter = new SimpleCursorAdapter(this, R.layout.score_list_item, mScoresCursor, fromColumns, toTextViews);
        scoresListView.setAdapter(mScoreAdapter);
        scoresListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ScoresListActivity.this, "TODO Launch edit score activity", Toast.LENGTH_SHORT).show();
            }});
        scoresListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(ScoresListActivity.this, "Removed score", Toast.LENGTH_SHORT).show();
				mDbScoreAdapter.removeScore((int)id);
				
				//TODO: Remove this mechanism and use a context menu similar to DiscGolf solution
				
				return false;
			}
		});
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.menu_add) {
			showDialog(DIALOG_ID);
		}
		return false;
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbScoreAdapter.close();
	}
	
	private void addScore(Score s) {
		Toast.makeText(this, "Adding score: " + s, Toast.LENGTH_SHORT);
		s = mDbScoreAdapter.addScore(s);
		mScoresCursor.requery();
		mScoreAdapter.notifyDataSetChanged();
	}
	
	private void removeScore(Score s) {
		Toast.makeText(this, "Removing score: " + s, Toast.LENGTH_SHORT);
		mDbScoreAdapter.removeScore(s);
		mScoresCursor.requery();
		mScoreAdapter.notifyDataSetChanged();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		final Dialog dialog = new Dialog(this);
		if (id == DIALOG_ID) {
			dialog.setContentView(R.layout.add_dialog);
			dialog.setTitle(R.string.add_score);
			
			final EditText nameText = (EditText) dialog.findViewById(R.id.name_entry);
			final EditText scoreText = (EditText) dialog.findViewById(R.id.score_entry);
			final Button addButton = (Button) dialog.findViewById(R.id.add_score_button);
			final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_score_button);
			
			addButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Score s = new Score();
					s.setName(nameText.getText().toString());
					try {
						s.setScore(Integer.parseInt(scoreText.getText().toString()));
					} catch (NumberFormatException e) {
						s.setScore(0);
					}
					addScore(s);
					dialog.dismiss();
				}
			});
			
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		return dialog;
	}
}