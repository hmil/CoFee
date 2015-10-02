package fr.hmil.cofee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView tUsername = (TextView) findViewById(R.id.username);
        final TextView tPassword = (TextView) findViewById(R.id.password);

        Button bRegister = (Button) findViewById(R.id.registerButton);
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra(Definitions.IEXTRA_USERNAME, tUsername.getText().toString());
                intent.putExtra(Definitions.IEXTRA_PASSWORD, tPassword.getText().toString());
                startActivity(intent);
            }
        });

        Button bLogin = (Button) findViewById(R.id.loginButton);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoginTask().execute(new ServerAPI.LoginTaskParams(tUsername.getText().toString(), tPassword.getText().toString()));
            }
        });

    }

    private ServerAPI.LoginTask getLoginTask() {
        return ServerAPI.getInstance(this).new LoginTask()
        {
            @Override
            protected void onPostExecute(ServerAPI.LoginTaskResult result) {
                if (result.success) {
                    Intent redirect = new Intent(LoginActivity.this, MainActivity.class);
                    redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(redirect);
                } else {
                    if (result.error == ServerAPI.Error.ERR_WRONG_CREDENTIALS) {
                        Toast.makeText(LoginActivity.this, "Wrong username/password", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Could not log in", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

}
