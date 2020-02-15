package com.example.nirbhay.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.MainActivity;
import com.example.nirbhay.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private FloatingActionButton createPost;
    private List<PostDetails> posts = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private DatabaseReference databaseReference;
    private TextView postHeader;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        createPost = (FloatingActionButton) findViewById(R.id.create_post);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        postHeader = (TextView) findViewById(R.id.postHeader);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAdapter = new RecyclerViewAdapter(posts, new RecyclerViewAdapter.ClickHandler() {
            @Override
            public void onButtonClicked(View view, int position) {
                final PostDetails selectedPost = posts.get(position);
                if(view.getId() == R.id.upvote){
                    Toast.makeText(PostActivity.this, "Upvote clicked " + selectedPost.getPincode(), Toast.LENGTH_SHORT).show();
                    final DatabaseReference dbRef = databaseReference.child("posts").child(selectedPost.getPincode()).child(selectedPost.getPostId());
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            PostDetails fetchedPost = dataSnapshot.getValue(PostDetails.class);
                            int flag= 1;
                            for(DataSnapshot upVoteSnapShot : dataSnapshot.child("upVoteUsers").getChildren()){
                                if(upVoteSnapShot.getValue().equals(selectedPost.getOwnerUserId()))
                                    flag=0;
                            }
                            if(fetchedPost!=null && flag ==1) {
                                long upvoteCount = fetchedPost.getUpVoteCount();
                                dbRef.child("upVoteCount").setValue(upvoteCount + 1);
                                dbRef.child("upVoteUsers").push().setValue(selectedPost.getOwnerUserId());
                            }
                            else{
                                //Toast.makeText(PostActivity.this, "Error up voting", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else if(view.getId() == R.id.downvote){
                    //Toast.makeText(PostActivity.this, "Downvote clicked" + position, Toast.LENGTH_SHORT).show();
                    final DatabaseReference dbRef = databaseReference.child("posts").child(selectedPost.getPincode()).child(selectedPost.getPostId());
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            PostDetails fetchedPost = dataSnapshot.getValue(PostDetails.class);
                            int flag= 1;
                            for(DataSnapshot upVoteSnapShot : dataSnapshot.child("upVoteUsers").getChildren()){
                                if(upVoteSnapShot.getValue().equals(selectedPost.getOwnerUserId()))
                                    flag=0;
                            }
                            if(fetchedPost!=null && flag==1) {
                                long downVoteCount = fetchedPost.getDownVoteCount();
                                dbRef.child("downVoteCount").setValue(downVoteCount + 1);
                                dbRef.child("downVoteUsers").push().setValue(selectedPost.getOwnerUserId());
                            }
                            else{
                                //Toast.makeText(PostActivity.this, "Error down voting", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference();
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        //getCurrentLocation();

        Button signOut = (Button) findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0); //Mode_private
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("finishedSignUp", true);
                editor.putBoolean("signedIn", false);
                editor.apply();
                editor.commit();
                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        createPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, CreatePost.class);
                startActivity(intent);
            }
        });

        GPSTracker gps = new GPSTracker(PostActivity.this);

        if(gps.canGetLocation()){
            LatLng currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
            String pincodeUrl = getDirectionsUrl(currentLocation);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(pincodeUrl);
            // \n is for new line
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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

            JSONObject jsonObject = null;
            Log.i("info", result);

            try{
                jsonObject = new JSONObject(result);
                int responseCode = jsonObject.getInt("responseCode");
                if(responseCode !=200){
                    Toast.makeText(PostActivity.this, "Error accessing your location", Toast.LENGTH_SHORT).show();
                }
                else{
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    JSONObject resultObject =jsonArray.getJSONObject(0);
                    String pincode = resultObject.getString("pincode");
                    Log.i("info", pincode);
                    postHeader.setText(postHeader.getText() + " (" +pincode + ") ");
                    Toast.makeText(PostActivity.this, pincode, Toast.LENGTH_SHORT).show();

                    databaseReference.child("posts").child(pincode).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if(dataSnapshot.getValue() == null){
                                Toast.makeText(PostActivity.this, "No records found!!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                posts.clear();
                                Toast.makeText(PostActivity.this, "" + dataSnapshot.getChildrenCount(), Toast.LENGTH_LONG).show();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    PostDetails postDetails = snapshot.getValue(PostDetails.class);
                                    posts.add(postDetails);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
