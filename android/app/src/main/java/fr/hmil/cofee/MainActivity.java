package fr.hmil.cofee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean skipRedirect = intent.getBooleanExtra(Definitions.IEXTRA_SKIP_REDIRECT, false);

        // Redirect to active count if needed
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        if (prefs.contains(Definitions.PREF_ACTIVE_COUNT) && !skipRedirect) {
            gotoCount(prefs.getString(Definitions.PREF_ACTIVE_COUNT, null));
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
        ListAdapter listAdapter = new ArrayAdapter<String>(this, R.layout.count_list_element, new String[] {"Haha"});
        list.setAdapter(listAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoCount(((TextView)view).getText());
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void gotoCount(CharSequence name) {

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit().putString(Definitions.PREF_ACTIVE_COUNT, name.toString()).commit();

        Intent intent = new Intent(MainActivity.this, CountActivity.class);
        intent.putExtra(Definitions.IEXTRA_COUNT_NAME, name);
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}