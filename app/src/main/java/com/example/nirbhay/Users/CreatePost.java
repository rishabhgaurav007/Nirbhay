package com.example.nirbhay.Users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class CreatePost extends AppCompatActivity {

    private TextView location, back;
    private Button chooseLocation, postButton;
    private EditText postBody;
    private LatLng loc;
    private static final int REQUEST_CODE_LOC = 1;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        location = (TextView) findViewById(R.id.location);
        chooseLocation = (Button) findViewById(R.id.chooseLocation);
        postButton = (Button) findViewById(R.id.postButton);
        postBody = (EditText) findViewById(R.id.postBody);
        back = (TextView) findViewById(R.id.back);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreatePost.this, ChooseLocation.class);
                startActivityForResult(intent, REQUEST_CODE_LOC);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkDetails()){
                    //store record on firebase
                    //retrieve pincode first
                    String pincodeUrl = getDirectionsUrl(loc);
                    CreatePost.DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(pincodeUrl);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean checkDetails(){
        if(loc!=null && !postBody.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_LOC){
            if(data==null){
                Toast.makeText(CreatePost.this, "Could not obtain location!", Toast.LENGTH_LONG).show();
            }else {
                Bundle bundle = data.getExtras();
                String latitude = bundle.getString("latitude");
                String longitude = bundle.getString("longitude");
                location.setText("lat:" + latitude + "lng: " + longitude);
                loc = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            }
        }
        else{
            Toast.makeText(CreatePost.this, "Some Error occurred. Try again!", Toast.LENGTH_LONG).show();
        }
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CreatePost.this);
            progressDialog.setMessage("Loading");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            JSONObject jsonObject = null;
            Log.i("info", result);

            try{
                jsonObject = new JSONObject(result);
                int responseCode = jsonObject.getInt("responseCode");
                if(responseCode !=200){
                    Toast.makeText(CreatePost.this, "Error processing location", Toast.LENGTH_SHORT).show();
                }
                else{
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    JSONObject resultObject =jsonArray.getJSONObject(0);
                    final String pincode = resultObject.getString("pincode");
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String ownerUserId = firebaseUser.getUid();
                    final String postId = databaseReference.push().getKey();
                    final Date date = new Date(System.currentTimeMillis());
                    Toast.makeText(CreatePost.this, pincode, Toast.LENGTH_SHORT).show();
                    databaseReference.child("users").child(ownerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            PostDetails postDetails = new PostDetails();
                            postDetails.setDate(date);
                            postDetails.setOwnerUserId(ownerUserId);
                            postDetails.setPostId(postId);
                            postDetails.setPincode(pincode);
                            postDetails.setPostBody(postBody.getText().toString());
                            postDetails.setOwnerName(user.getName());
                            databaseReference.child("posts").child(pincode).child(postId).setValue(postDetails);
                            databaseReference.child("users").child(ownerUserId).child("postByUser").push().setValue(postId);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(CreatePost.this, "Error updating database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private String getDirectionsUrl(LatLng origin) {
        String url = "https://apis.mapmyindia.com/advancedmaps/v1/zdhlcatxe8tt5mr6c9w3wrhrdfbbr8h3/rev_geocode?lat=" +origin.latitude+"&lng="+origin.longitude;
        Log.i("info", url);

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
