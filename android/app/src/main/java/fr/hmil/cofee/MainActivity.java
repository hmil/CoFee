package fr.hmil.cofee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.hmil.cofee.models.Count;

import static fr.hmil.cofee.ServerAPI.*;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<Count> listAdapter;

    // onCreate has requested another activity to take over. Skips fetching counts in onResume
    private boolean willRedirect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean skipRedirect = intent.getBooleanExtra(Definitions.IEXTRA_SKIP_REDIRECT, false);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // If not logged-in, redirect to login
        if (!getInstance(this).isLoggedIn()) {
            Intent redirect = new Intent(this, LoginActivity.class);
            redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(redirect);
            willRedirect = true;
            return;
        }

        // Redirect to active count if needed
        if (prefs.contains(Definitions.PREF_ACTIVE_COUNT) && !skipRedirect) {
            gotoCount(prefs.getString(Definitions.PREF_ACTIVE_COUNT, null));
            willRedirect = true;
            // Don't waste time populating the activity as we will kill it right away
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewCountActivity.class);
                startActivity(intent);
            }
        });

        ListView list = (ListView) findViewById(R.id.countsList);
        listAdapter = new ArrayAdapter<>(this, R.layout.count_list_element, new ArrayList<Count>());
        list.setAdapter(listAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoCount(((TextView)view).getText());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!willRedirect) {
            TaskGetCounts task = new TaskGetCounts();
            task.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        willRedirect = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void gotoCount(CharSequence name) {

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit().putString(Definitions.PREF_ACTIVE_COUNT, name.toString()).apply();

        Intent intent = new Intent(MainActivity.this, CountActivity.class);
        intent.putExtra(Definitions.IEXTRA_COUNT_NAME, name);
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class TaskGetCounts extends GetCountsTask {
        @Override
        protected void onPostExecute(GetCountsTaskResult result) {
            if (result.success) {
                if (listAdapter != null) {
                    listAdapter.clear();
                    listAdapter.addAll(result.getCounts());
                }
            } else {
                Toast.makeText(MainActivity.this, "Could not fetch counts list", Toast.LENGTH_LONG).show();
            }
        }
    }
}
