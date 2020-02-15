package com.example.nirbhay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nirbhay.Police.Survillance;
import com.example.nirbhay.Users.CompleteSignUp;
import com.example.nirbhay.Users.PostActivity;
import com.example.nirbhay.Users.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailField, passwordField;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
        emailField = (EditText) findViewById(R.id.emailSignIn);
        passwordField = (EditText) findViewById(R.id.passwordSignIn);

        auth = getInstance();




        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Pref", 0);
        if(preferences!=null){
            if(preferences.getBoolean("signedIn", false)){

                if(!preferences.getBoolean("finishedSignUp", false)){
                    Intent intent = new Intent(this, CompleteSignUp.class);
                    startActivity(intent);
                }else {
                    if(preferences.getBoolean("userSignIn", false)) {
                        Intent intent = new Intent(this, PostActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(this, Survillance.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }




    }
    private boolean validate(){
        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();

        if(email.isEmpty()){
            // Toast.makeText(MainActivity.this, "Email "  +email, Toast.LENGTH_SHORT).show();

            emailField.requestFocus();
            emailField.setError("Required!!");
            return false;
        }
        if(password.isEmpty()){
            //Toast.makeText(MainActivity.this, "Pass "  +password, Toast.LENGTH_SHORT).show();

            passwordField.requestFocus();
            passwordField.setError("Required!!");
            return false;
        }
        return true;
    }
    public void signInClick(View view) {

        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();
        if (validate()) {
            signIn(email, password);
        }
    }

    public void policeSignInClick(View view){
        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();
        if (validate()) {
            policeSignIn(email, password);
        }
    }

    public void policeSignIn(String email, String password){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("userSignIn", false);
                    editor.putBoolean("finishedSignUp", true);
                    editor.putBoolean("signedIn", true);
                    editor.apply();
                    editor.commit();

                    Intent intent = new Intent(MainActivity.this, Survillance.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, "Login failed: " + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void signIn(String email, String password){
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("userSignIn", true);
                    editor.putBoolean("finishedSignUp", true);
                    editor.putBoolean("signedIn", true);
                    editor.apply();
                    editor.commit();

                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    progressDialog.hide();
                    Toast.makeText(MainActivity.this, "Login failed: " + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void openSignupPage(View view) {
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
    }
}
