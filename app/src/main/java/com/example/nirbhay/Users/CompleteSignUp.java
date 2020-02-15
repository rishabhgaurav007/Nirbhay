package com.example.nirbhay.Users;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CompleteSignUp extends AppCompatActivity {

    private TextView greet, contact;
    private EditText gender, age, address, name, phoneNumber;
    private Button addContact, del_contact, finish;
    private Pair<String, String> emergency_contact;
    private final int RQS_PICK_CONTACT = 1;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_sign_up);


        databaseReference = FirebaseDatabase.getInstance().getReference();

        greet = (TextView) findViewById(R.id.greet);
        greet.setText("Hi!");
        contact = (TextView) findViewById(R.id.contact1);
        //contact2 = (TextView) findViewById(R.id.contact2);

        gender = (EditText) findViewById(R.id.gender);
        age = (EditText) findViewById(R.id.age);
        address = (EditText) findViewById(R.id.address);
        name = (EditText) findViewById(R.id.name);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);

        addContact = (Button) findViewById(R.id.addContact);
        del_contact = (Button) findViewById(R.id.del_contact1);
        //del_contact2 = (Button) findViewById(R.id.del_contact2);
        finish = (Button) findViewById(R.id.finish);


        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, RQS_PICK_CONTACT);
            }
        });

        del_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact.setText("Select contact");
                if(emergency_contact!=null)
                    emergency_contact = null;
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkCredentials()){
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String age_user = age.getText().toString();
                    String gender_user = gender.getText().toString();
                    String address_user = address.getText().toString();
                    String userId = firebaseUser.getUid();
                    String name_user = name.getText().toString();
                    String contactNumber = phoneNumber.getText().toString();
                    if(firebaseUser!=null){
                        String email = firebaseUser.getEmail();
                        User user = new User(
                                userId, email, name_user, gender_user, age_user, address_user, emergency_contact.first, emergency_contact.second,contactNumber
                        );

                        databaseReference.child("users").child(userId).setValue(user);
                        databaseReference.child("users").child(userId).child("postByUser");
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("finishedSignUp", true);
                        editor.putBoolean("signedIn", true);
                        editor.putString("userName", name_user);
                        editor.apply();
                        editor.commit();
                        Intent intent = new Intent(CompleteSignUp.this, PostActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    private Boolean checkCredentials(){
        String age_user = age.getText().toString();
        String gender_user = gender.getText().toString();
        String address_user = address.getText().toString();
        String name_user = name.getText().toString();
        boolean flag =true;
        if(name_user.isEmpty()){
            name.setError("Required!!");
            flag = false;
        }
        if(age_user.isEmpty()){
            age.setError("Required!");
            flag = false;
        }
        if(gender_user.isEmpty()){
            gender.setError("Required!");
            flag = false;
        }
        if(address_user.isEmpty()){
            gender.setError("Required!");
            flag = false;
        }
        if(emergency_contact==null){
            Toast.makeText(this, "Contact is required!", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if(phoneNumber.getText().toString().isEmpty()){
            phoneNumber.setError("Required!");
        }
        if(flag){
            return true;
        }
        else{
            return false;
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RQS_PICK_CONTACT){
            if(resultCode == RESULT_OK){
                Uri contactData = data.getData();
                Cursor cursor =  managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                emergency_contact = new Pair<>(name, number);
                contact.setText(name + "\n" + number);
            }
        }
    }
}
