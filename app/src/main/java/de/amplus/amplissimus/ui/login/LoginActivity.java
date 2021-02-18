package de.amplus.amplissimus.ui.login;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.data.Functions;
import de.amplus.amplissimus.services.DSBService;
import de.amplus.amplissimus.services.Prefs;
import de.amplus.amplissimus.ui.app.MainActivity;

public class LoginActivity extends AppCompatActivity
        implements SaveDataModalBottomSheetDialog.BottomSheetListener {

    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;

    private boolean portable = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("redirect", true)) {
            try {
                if(DSBService.getPlans() != null) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } catch(Exception ignored) {}
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.login_progress_bar);
        loginButton = findViewById(R.id.login_btn);

        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                Log.d("LoginActivity", "Started new Thread!");

                Prefs prefs = new Prefs(this);
                List<DSBService.Plan> plans = prefs.getPlans();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if(plans != null && prefs.getUsername().equals(username)
                        && prefs.getPassword().equals(password)
                        && Functions.isOffline(this)) {
                    portable = false;
                    new DSBService(username, password);
                    DSBService.setPlans(plans);
                    runOnUiThread(this::launchMainActivity);
                } else if(new DSBService(username, password).credentialsOK()) {
                    if(!username.equals(prefs.getUsername())
                            || !password.equals(prefs.getPassword())) {
                        SaveDataModalBottomSheetDialog bottomSheetDialog =
                                new SaveDataModalBottomSheetDialog();
                        bottomSheetDialog.show(getSupportFragmentManager(), "saveDataBottomSheet");
                    } else {
                        portable = false;
                        if(!prefs.bgServicesOn() || plans == null) {
                            try {
                                new DSBService(username, password).parseTimetables();
                            } catch (Exception e) {
                                runOnUiThread(this::onLoginFailed);
                                return;
                            }
                        } else DSBService.setPlans(plans);
                        runOnUiThread(this::launchMainActivity);
                    }
                } else runOnUiThread(this::onLoginFailed);
            }).start();
        });

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        Prefs prefs = new Prefs(this);
        if(prefs.getUsername() != null && prefs.getPassword() != null) {
            usernameEditText.setText(prefs.getUsername());
            passwordEditText.setText(prefs.getPassword());
            loginButton.setEnabled(true);
        }

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0)
                    usernameEditText.setError(getString(R.string.field_empty));
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginButton.setEnabled(!passwordEditText.getText().toString().trim().isEmpty()
                        && !usernameEditText.getText().toString().trim().isEmpty());
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0)
                    passwordEditText.setError(getString(R.string.field_empty));
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginButton.setEnabled(!passwordEditText.getText().toString().trim().isEmpty()
                        && !usernameEditText.getText().toString().trim().isEmpty());
            }
        });

    }

    @Override
    public void dialogAnswered(int i) {
        progressBar.setVisibility(View.VISIBLE);
        if(i == -1) {
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if(i == 1) {
            portable = false;
            Prefs prefs = new Prefs(this);
            prefs.setUsername(username);
            prefs.setPassword(password);
        }
        new Thread(() -> {
            try {
                new DSBService(username, password).parseTimetables();
            } catch (Exception e) { runOnUiThread(this::onLoginFailed); }
            runOnUiThread(this::launchMainActivity);
        }).start();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("portable", portable);
        startActivity(intent);
        finish();
    }

    private void onLoginFailed() {
        progressBar.setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.error_text)).setText(getString(R.string.login_failed));
    }
}
