package com.example.nirbhay.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button signUp;
    private TextView back;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        emailField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);
        signUp = (Button) findViewById(R.id.signUp);
        back = (TextView) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //final String name = nameField.getText().toString();
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                if(!email.isEmpty() && !password.isEmpty() ){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                            SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(SignUpActivity.this, "Sign Up Failed: "+task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putBoolean("finishedSignUp", false);
                                        editor.putBoolean("signedIn", true);
                                        editor.putBoolean("safeNow", true);
                                        editor.apply();
                                        editor.commit();
                                        Intent intent = new Intent(SignUpActivity.this, CompleteSignUp.class);

                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                    );
                }
                else{
                    if(email.isEmpty()){
                        emailField.setError("Email Id Required");
                        emailField.requestFocus();
                    }
                    if(password.isEmpty()){
                        passwordField.setError("Password Required");
                        passwordField.requestFocus();
                    }
                }
            }
        });

    }
}
