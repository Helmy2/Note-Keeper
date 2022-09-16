package com.example.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.databinding.ActivityNoteListBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class NoteListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    ActivityNoteListBinding binding;
    NoteListAdapter noteListAdapter;
    CourseListAdapter courseListAdapter;
    LinearLayoutManager notesLayoutManager;
    GridLayoutManager coursesLayoutManager;
    private NoteKeeperOpenHelper dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dbOpenHelper = new NoteKeeperOpenHelper(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        initializeDisplayContent();
        binding.fab.setOnClickListener(this::onFABAddClicked);
    }

    @Override
    protected void onDestroy() {
        dbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES, null, this);
        updateNavHeader();
    }

    private void updateNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textEmailAddress = headerView.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString("user_display_name", "");
        String emailAddress = pref.getString("user_email_address", "");

        textUserName.setText(userName);
        textEmailAddress.setText(emailAddress);
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(dbOpenHelper);

        notesLayoutManager = new LinearLayoutManager(this);
        coursesLayoutManager = new GridLayoutManager(this, 2);

        noteListAdapter = new NoteListAdapter(null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        courseListAdapter = new CourseListAdapter(courses);

        displayNotes();
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayNotes() {
        binding.listNotes.setLayoutManager(notesLayoutManager);
        binding.listNotes.setAdapter(noteListAdapter);
        noteListAdapter.setOnItemClickedListener(this::onNoteItemClicked);

        binding.navView.getMenu().findItem(R.id.nav_notes).setChecked(true);
    }

    private void displayCourses() {
        binding.listNotes.setLayoutManager(coursesLayoutManager);
        binding.listNotes.setAdapter(courseListAdapter);
        courseListAdapter.setOnItemClickedListener(this::onCourseItemClicked);

        binding.navView.getMenu().findItem(R.id.nav_courses).setChecked(true);
    }

    private void handleShare() {
        Snackbar.make(binding.listNotes, "Share to - " +
                        PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", ""),
                Snackbar.LENGTH_LONG).show();
    }

    private void onCourseItemClicked(int position) {
        Snackbar.make(binding.listNotes, Integer.valueOf(position).toString(), Snackbar.LENGTH_LONG).show();
    }


    private void onNoteItemClicked(int id) {
        Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
        intent.putExtra(NoteActivity.NOTE_ID, id);
        startActivity(intent);
    }

    private void onFABAddClicked(View view) {
        Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
        startActivity(intent);
    }

    private void handleSelection(int message_id) {
        Snackbar.make(binding.listNotes, message_id, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES) {
            loader = new CursorLoader(this) {
                @Override
                public Cursor loadInBackground() {
                    SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

                    final String[] noteColumns = {
                            NoteInfoEntry.getQName(NoteInfoEntry._ID),
                            NoteInfoEntry.COLUMN_NOTE_TITLE,
                            CourseInfoEntry.COLUMN_COURSE_TITLE
                    };

                    final String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE +
                            "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

                    String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                            CourseInfoEntry.TABLE_NAME + " ON " +
                            NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                            CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

                    return db.query(tablesWithJoin, noteColumns,
                            null, null, null, null, noteOrderBy);
                }
            };
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            noteListAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (loader.getId() == LOADER_NOTES) {
            noteListAdapter.changeCursor(null);
        }

    }
}