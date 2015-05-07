package com.tumblr.breadcrumbs492.testapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;


import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Date;

//TODO: We need to retrieve hasVoted from the DB to determine the button logic
//TODO: upvotes needs to be passed back in the JSON request to the web server
public class CrumbDetails extends ActionBarActivity /*implements OnStreetViewPanoramaReadyCallback*/{
    //private StreetViewPanoramaFragment panorama;
    private GoogleMap map;
    private String search, name, description, username, date, tags, email, crumbID;
    private int upvotes;
    private double longitude, latitude;
    private boolean hasVoted;

    public final static String CRUMB_NAME = "crumbName";
    public final static String CRUMB_COMMENT = "crumbComment";
    public final static String CRUMB_TAGS = "crumbsTags";
    public final static String CRUMB_ID = "crumbID";
    public final static String CRUMB_UPVOTES = "crumbUpvotes";
    public final static String CRUMB_DATE = "crumbDate";
    public final static String CRUMB_LONGITUDE = "longitude";
    public final static String CRUMB_LATITUDE = "latitude";
    public final static String SEARCH = "search";
    public final static String USERNAME = "username";
    public final static String EMAIL = "email";
    public final static int REQUEST_FIND_CRUMB = 4;

    private TextView crumbUsername, crumbDate, crumbName, crumbUpvotes, crumbTags, crumbComment;
    private String whichActivity = "";

    private MyRequestReceiver9 receiver;

    public void userDetails(View view){
        Intent intent = new Intent(CrumbDetails.this, UserProfile.class);
        intent.putExtra(EMAIL, getIntent().getStringExtra(SearchResults.EMAIL));
        intent.putExtra(USERNAME, getIntent().getStringExtra(SearchResults.USERNAME));
        intent.putExtra(SEARCH, getIntent().getStringExtra(SearchResults.SEARCH));
        intent.putExtra(CRUMB_NAME, getIntent().getStringExtra(SearchResults.CRUMB_NAME));
        intent.putExtra(CRUMB_COMMENT, getIntent().getStringExtra(SearchResults.CRUMB_COMMENT));
        intent.putExtra(CRUMB_UPVOTES, getIntent().getIntExtra(SearchResults.CRUMB_UPVOTES, 0));
        intent.putExtra(CRUMB_DATE, getIntent().getStringExtra(SearchResults.CRUMB_DATE));
        intent.putExtra(CRUMB_TAGS, getIntent().getStringExtra(SearchResults.CRUMB_TAGS));
        intent.putExtra(CRUMB_LATITUDE, getIntent().getDoubleExtra(SearchResults.CRUMB_LATITUDE, 0.0));
        intent.putExtra(CRUMB_LONGITUDE, getIntent().getDoubleExtra(SearchResults.CRUMB_LONGITUDE, 0.0));
        startActivity(intent);
        finish();

    }

    public void backToResults(View view) {
        //back to searchresults, passing same search query back to searchresults activity
        search = getIntent().getStringExtra(SearchResults.SEARCH);
        Intent intent = new Intent(CrumbDetails.this, SearchResults.class);
        intent.putExtra(SEARCH, search);
        startActivityForResult(intent, REQUEST_FIND_CRUMB);
        finish();
    }

    public void voteCrumb(View view)
    {

        // change button text
        Button voteButton = (Button)findViewById(R.id.button);
        if(hasVoted)
        {
            Intent msgIntent = new Intent(this, JSONRequest.class);
            msgIntent.putExtra(JSONRequest.IN_MSG, "unvote");
            msgIntent.putExtra("queryID", "unvote");
            msgIntent.putExtra("jsonObject", "{\"username\":\"" + GlobalContainer.user.getInfo()[0] + "\",\"email\":\""
                    + GlobalContainer.user.getInfo()[1] + "\",\"crumbID\":\"" + crumbID + "\"}");

            startService(msgIntent);

        }
        else
        {
            Intent msgIntent = new Intent(this, JSONRequest.class);
            msgIntent.putExtra(JSONRequest.IN_MSG, "upvote");
            msgIntent.putExtra("queryID", "upvote");
            msgIntent.putExtra("jsonObject", "{\"username\":\"" + GlobalContainer.user.getInfo()[0] + "\",\"email\":\""
                    + GlobalContainer.user.getInfo()[1] + "\",\"crumbID\":\"" + crumbID + "\"}");


            startService(msgIntent);
        }

        // pass crumb back into database

        String comment = crumbComment.toString();
        Intent intent = new Intent(this, MapsActivity.class);
        String id = getIntent().getStringExtra(MyCrumbsActivity.CRUMB_ID);



        // I dont see upvotes here, so maybe its not being commited back?

        //place into intent to pass back to MapsActivity
        intent.putExtra(MapsActivity.NAME, name);
        intent.putExtra(MapsActivity.COMMENT, comment);
        setResult(RESULT_OK, intent);//send result code
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crumb_details);
        whichActivity = getIntent().getStringExtra("activity");

        IntentFilter filter = new IntentFilter(MyRequestReceiver9.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyRequestReceiver9();
        registerReceiver(receiver, filter);

        if(whichActivity.equals("SearchResults")) {
            //retrieve extras from previous SearchResults class
            search = getIntent().getStringExtra(SearchResults.SEARCH);
            name = getIntent().getStringExtra(SearchResults.CRUMB_NAME);
            email = getIntent().getStringExtra(SearchResults.EMAIL);
            description = getIntent().getStringExtra(SearchResults.CRUMB_COMMENT);
            username = getIntent().getStringExtra(SearchResults.USERNAME);
            crumbID = getIntent().getStringExtra(SearchResults.CRUMB_ID);
            upvotes = getIntent().getIntExtra(SearchResults.CRUMB_UPVOTES, 0);
            date = getIntent().getStringExtra(SearchResults.CRUMB_DATE);
            tags = getIntent().getStringExtra(SearchResults.CRUMB_TAGS);
            longitude = getIntent().getDoubleExtra(SearchResults.CRUMB_LONGITUDE, 0.0);
            latitude = getIntent().getDoubleExtra(SearchResults.CRUMB_LATITUDE, 0.0);
            System.out.println("Tags: " + tags);
        }

        else if(whichActivity.equals("MapActivity")) {
            //retrieve extras from previous SearchResults class
            search = getIntent().getStringExtra(MapsActivity.SEARCH);
            email = getIntent().getStringExtra(MapsActivity.CRUMB_EMAIL);
            name = getIntent().getStringExtra(MapsActivity.CRUMB_NAME);
            description = getIntent().getStringExtra(MapsActivity.CRUMB_COMMENT);
            username = getIntent().getStringExtra(MapsActivity.USERNAME);
            crumbID = getIntent().getStringExtra(MapsActivity.CRUMB_ID);
            upvotes = getIntent().getIntExtra(MapsActivity.CRUMB_UPVOTES, 0);
            date = getIntent().getStringExtra(MapsActivity.CRUMB_DATE);
            tags = getIntent().getStringExtra(MapsActivity.CRUMB_TAGS);
            longitude = getIntent().getDoubleExtra(MapsActivity.CRUMB_LONGITUDE, 0.0);
            latitude = getIntent().getDoubleExtra(MapsActivity.CRUMB_LATITUDE, 0.0);
            System.out.println("Tags: " + tags);
        }
        else if(whichActivity.equals("UserProfile")) {
            //retrieve extras from previous SearchResults class
            search = getIntent().getStringExtra(UserProfile.SEARCH);
            email = getIntent().getStringExtra(UserProfile.EMAIL);
            name = getIntent().getStringExtra(UserProfile.CRUMB_NAME);
            description = getIntent().getStringExtra(UserProfile.CRUMB_COMMENT);
            username = getIntent().getStringExtra(UserProfile.USERNAME);
            crumbID = getIntent().getStringExtra(UserProfile.CRUMB_ID);
            upvotes = getIntent().getIntExtra(UserProfile.CRUMB_UPVOTES, 0);
            date = getIntent().getStringExtra(UserProfile.CRUMB_DATE);
            tags = getIntent().getStringExtra(UserProfile.CRUMB_TAGS);
            longitude = getIntent().getDoubleExtra(UserProfile.CRUMB_LONGITUDE, 0.0);
            latitude = getIntent().getDoubleExtra(UserProfile.CRUMB_LATITUDE, 0.0);
            System.out.println("Tags: " + tags);
        }
        System.out.println(crumbID);
        //check to see if user has voted before
        Intent msgIntent1 = new Intent(this, JSONRequest.class);
        msgIntent1.putExtra(JSONRequest.IN_MSG, "hasVoted");
        msgIntent1.putExtra("queryID", "hasVoted");
        msgIntent1.putExtra("jsonObject", "{\"username\":\"" + GlobalContainer.user.getInfo()[0] + "\",\"email\":\""
                + GlobalContainer.user.getInfo()[1] + "\",\"crumbID\":\"" + crumbID + "\"}");

        /*StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.panorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);*/


        //setup Google map to mark where crumb is
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        map = supportMapFragment.getMap();

        //get location of crumb, set to a LatLng object
        System.out.println("Latitude: " + latitude + "Longitude: " + longitude);
        LatLng location = new LatLng(latitude, longitude);

        //mark crumb location on map
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        map.animateCamera(CameraUpdateFactory.zoomTo(14));
        map.addMarker(new MarkerOptions().position(location).
                title(name));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(location, 12);
        map.animateCamera(yourLocation);

        //get reference to TextViews and populate them with extras from variables above
        crumbDate = (TextView) findViewById(R.id.dateTextView);
        crumbDate.setText("Crumb dropped on: " + date);
        crumbUpvotes = (TextView) findViewById(R.id.upvotesTextView);
        crumbUpvotes.setText("Upvotes: " + upvotes);
        //crumbUpvotes.setText("Upvotes: " + getIntent().getIntExtra(SearchResults.CRUMB_UPVOTES, 1));
        crumbUsername = (TextView) findViewById(R.id.userTextView);
        crumbUsername.setText("Crumb dropped by: " + username);
        crumbName = (TextView) findViewById(R.id.crumb_name);
        crumbName.setText("Name: " + name);
        crumbComment = (TextView) findViewById(R.id.crumb_description);
        /*if (description == "") {
            crumbComment.setTypeface(null, Typeface.ITALIC);
            description = "No description found for this crumb.";
            crumbComment.setText("Description: " + description);
        }*/
        crumbComment.setText("Description: " + description);
        crumbTags = (TextView) findViewById(R.id.crumb_tags);
        /*if (tags == "") {
            crumbTags.setTypeface(null, Typeface.ITALIC);
            tags = "No tags found for this crumb.";
            crumbTags.setText(tags);
        }*/

        crumbTags.setText("Tags: " + tags);
    }

   /* @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        System.out.println(latitude + " " + longitude);
        panorama.setPosition(new LatLng(latitude, longitude));
    }*/

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MapsActivity.class);
        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crumb_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //broadcast receiver to receive messages sent from the JSON IntentService
    public class MyRequestReceiver9 extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.tumblr.breadcrumbs492.testapplication.CrumbDetails.MyRequestReceiver";
        public String response = null;
        @Override
        public void onReceive(Context context, Intent intent) {

            String responseType = intent.getStringExtra(JSONRequest.IN_MSG);

            Button voteButton = (Button)findViewById(R.id.button);

            if(responseType.trim().equalsIgnoreCase("upvote")){

                this.response = intent.getStringExtra(JSONRequest.OUT_MSG);

                JSONObject tempJSON = new JSONObject();
                try {
                    tempJSON = new JSONObject(response);
                    if(tempJSON.getString("upvoteResult").equals("true"))
                    {
                        crumbUpvotes.setText("Upvotes: " + (upvotes + 1));
                        upvotes++;
                        hasVoted = true;
                        Toast.makeText(getApplicationContext(), "Vote successful", Toast.LENGTH_SHORT).show();
                        voteButton.setText("Unlike!");
                    }
                }
                catch(JSONException e)
                {
                    Toast.makeText(getApplicationContext(), "Voting error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


            }
            else if(responseType.trim().equalsIgnoreCase("unvote")){

                this.response = intent.getStringExtra(JSONRequest.OUT_MSG);

                JSONObject tempJSON = new JSONObject();
                try {
                    tempJSON = new JSONObject(response);
                    if(tempJSON.getString("unvoteResult").equals("true"))
                    {
                        crumbUpvotes.setText("Upvotes: " + (upvotes - 1));
                        upvotes--;
                        hasVoted = false;
                        Toast.makeText(getApplicationContext(), "Un-vote successful", Toast.LENGTH_SHORT).show();
                        voteButton.setText("Like!");
                    }
                }
                catch(JSONException e)
                {
                    Toast.makeText(getApplicationContext(), "Un-voting error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


            }
            else if(responseType.trim().equalsIgnoreCase("hasVoted")){

                this.response = intent.getStringExtra(JSONRequest.OUT_MSG);

                JSONObject tempJSON = new JSONObject();
                try {
                    tempJSON = new JSONObject(response);
                    if(tempJSON.getString("hasVoted").equals("true"))
                    {
                        hasVoted = true;
                        voteButton.setText("Unlike!");
                    }
                    else if(tempJSON.getString("hasVoted").equals("false"))
                    {
                        hasVoted = false;
                        voteButton.setText("Like!");
                    }
                }
                catch(JSONException e)
                {
                   e.printStackTrace();
                }


            }

        }
        public String getResponse()
        {
            return response;
        }
    }
}