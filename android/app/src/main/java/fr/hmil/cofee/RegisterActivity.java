package fr.hmil.cofee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static fr.hmil.cofee.ServerAPI.*;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        String username = intent.getStringExtra(Definitions.IEXTRA_USERNAME);
        String password = intent.getStringExtra(Definitions.IEXTRA_PASSWORD);

        final EditText tUsername = (EditText) findViewById(R.id.username);
        final EditText tPassword = (EditText) findViewById(R.id.password);
        final EditText tPasswordConfirm = (EditText) findViewById(R.id.passwordConfirm);

        tUsername.setText(username);
        tPassword.setText(password);


        Button bRegister = (Button) findViewById(R.id.registerButton);
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = tUsername.getText().toString();
                final String password = tPassword.getText().toString();

                final ServerAPI.LoginTask loginTask = ServerAPI.getInstance(RegisterActivity.this).new LoginTask()
                {
                    @Override
                    protected void onPostExecute(LoginTaskResult result) {
                        if (result.success) {
                            Intent redirect = new Intent(RegisterActivity.this, MainActivity.class);
                            redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(redirect);
                        } else {
                            RegisterActivity.this.finish();
                        }
                    }
                };

                final RegisterTask registerTask = new RegisterTask()
                {
                    @Override
                    protected void onPostExecute(RegisterTaskResult result) {
                        if (result.success) {
                            loginTask.execute(new LoginTaskParams(username, password));
                        } else {
                            switch (result.error) {
                                case ERR_NICK_USED:
                                    Toast.makeText(RegisterActivity.this, "failure : username used", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(RegisterActivity.this, "failure : unknown error", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                };

                if (!password.equals(tPasswordConfirm.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
                } else {
                    registerTask.execute(new RegisterTaskParams(username, password));
                }
            }
        });
    }

}
