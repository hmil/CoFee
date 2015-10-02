package fr.hmil.cofee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

public class CountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        this.setTitle(intent.getStringExtra(Definitions.IEXTRA_COUNT_NAME));

        GridView coffeeGrid = (GridView) findViewById(R.id.coffeeGrid);
        coffeeGrid.setAdapter(new ArrayAdapter<String>(this, R.layout.coffee_list_element, R.id.coffeeListItemName,
                new String[]{"Espresso", "Lungo", "Special"}));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_count, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;

        switch(id) {
            case R.id.action_goto_list:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra(Definitions.IEXTRA_SKIP_REDIRECT, true);
                startActivity(intent);
                return true;
            case R.id.action_buy_capsules:
                intent = new Intent(this, BuyCapsulesActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_history:
                intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                ServerAPI.getInstance(this).logout();
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                prefs.edit().remove(Definitions.PREF_ACTIVE_COUNT).apply();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
