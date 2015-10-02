package fr.hmil.cofee;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static fr.hmil.cofee.ServerAPI.*;

public class NewCountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_count);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Subscribe to count
        final EditText textSecret = (EditText) findViewById(R.id.newCountSecret);
        final Button bAddCount = (Button) findViewById(R.id.addCountButton);
        bAddCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskSubscribeToCount task = new TaskSubscribeToCount();
                task.execute(new SubscribeToCountTaskParameters(textSecret.getText().toString()));
            }
        });

        // Create new count
        final EditText textName = (EditText) findViewById(R.id.newCountName);
        final Button bCreateCount = (Button) findViewById(R.id.createCountButton);
        bCreateCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskCreateCount task = new TaskCreateCount();
                task.execute(new CreateCountTaskParameters(textName.getText().toString()));
            }
        });
    }

    private class TaskSubscribeToCount extends SubscribeToCountTask {

        @Override
        protected void onPostExecute(SubscribeToCountTaskResult result) {
            if (result.success)
                NewCountActivity.this.finish();
            else
                Toast.makeText(NewCountActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private class TaskCreateCount extends CreateCountTask {

        @Override
        protected void onPostExecute(CreateCountTaskResult result) {
            if (result.success)
                NewCountActivity.this.finish();
            else
                Toast.makeText(NewCountActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
        }
    }

}
