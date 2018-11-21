package com.baasbox.ITC_Meet.ui;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import com.baasbox.ITC_Meet.R;
import com.baasbox.android.BaasACL;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;

import com.baasbox.android.Grant;
import com.baasbox.android.Role;
import com.baasbox.android.json.JsonObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author:
 * Roger Marciniak (c00169733)
 */

public class Scan extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        final Button scanButton = (Button) findViewById(R.id.buttonbutton);
        scanButton.setEnabled(true);

        final ListView matchList = (ListView) findViewById(R.id.matchList);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        matchList.setAdapter(adapter);



        if (scanButton != null) {
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View c) {
                    if (scanButton.isPressed()) {
                        arrayList.clear();
                        setMyLocation();
                        scanForMatches();
                    }
                    scanButton.setEnabled(false);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            Scan.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    scanButton.setEnabled(true);

                                }
                            });
                        }
                    }).start();
                }

            });
        }


        matchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                {
                    String chosen = matchList.getItemAtPosition(position).toString();
                    String userNamee = chosen.substring(0, chosen.indexOf(" "));
                    Log.e("UN", userNamee);

                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, userNamee, duration);
                    toast.show();

                    Intent intent = new Intent(view.getContext(), MatchView.class);
                    intent.putExtra("userName", userNamee);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


                }

            }
        });

    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Connection failed", connectionResult.toString());
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void scanForMatches(){

        String whereString = "distance(Latitude,Longitude," + mLatitudeText + "," + mLongitudeText + ") < 5";
        final BaasQuery PREPARED_QUERY =
                BaasQuery.builder()
                        .collection("geo")
                        .where(whereString)
                        .build();

        PREPARED_QUERY.query(new BaasHandler<List<JsonObject>>() {
            @Override
            public void handle(BaasResult<List<JsonObject>> res) {
                if (res.isSuccess()) {
                    for (JsonObject doc : res.value()) {

                        String userName = doc.getString("Author");
                        String lati = doc.get("Latitude");
                        String longi = doc.get("Longitude");

                        if(userName.equals(BaasUser.current().getName())){

                        }
                        else{
                            int matchedInterests = 1;
                            Location myLocation = new Location("point A");

                            myLocation.setLatitude(Double.parseDouble(mLatitudeText));
                            myLocation.setLongitude(Double.parseDouble(mLongitudeText));

                            Location matchLocation = new Location("point B");

                            matchLocation.setLatitude(Double.parseDouble(lati));
                            matchLocation.setLongitude(Double.parseDouble(longi));

                            float distance = (myLocation.distanceTo(matchLocation));
                            String distanceStr = "location not found";
                            Log.d("Pass", userName);

                            if (distance < 10.0) {distanceStr = "~Wow, less than 10m away!";}
                            else if (distance < 50.0){distanceStr = "~less than 50m away!";}
                            else{

                                int distance2 = Math.round(distance);
                                distanceStr = Integer.toString(distance2) + "m away!";
                            }
                            final ArrayList<String> matchPref = new ArrayList<String>();
                            final ArrayList<String> myPref = new ArrayList<String>();
                            BaasQuery.Criteria filter2 = BaasQuery.builder().pagination(0, 20)
                                    .orderBy("_creation_date desc")
                                    .where("_author='" + userName + "'")
                                    .criteria();


                            BaasDocument.fetchAll("Preferences", filter2,
                                    new BaasHandler<List<BaasDocument>>() {
                                        @Override
                                        public void handle(BaasResult<List<BaasDocument>> res) {
                                            if (res.isSuccess()) {
                                                int count = 0;
                                                for (BaasDocument doc : res.value()) {
                                                    String pref = doc.getString("Interests");
                                                    String[] parts = pref.split(",");
                                                    Collections.addAll(matchPref, parts);
                                                }


                                            } else {
                                            }
                                        }
                                    });

                            BaasQuery.Criteria filter = BaasQuery.builder().pagination(0, 20)
                                    .orderBy("_creation_date desc")
                                    .where("_author='" + BaasUser.current().getName() + "'")
                                    .criteria();


                            BaasDocument.fetchAll("Preferences", filter,
                                    new BaasHandler<List<BaasDocument>>() {
                                        @Override
                                        public void handle(BaasResult<List<BaasDocument>> res) {
                                            if (res.isSuccess()) {
                                                int count = 0;
                                                for (BaasDocument doc : res.value()) {
                                                    String pref = doc.getString("Interests");
                                                    String[] parts = pref.split(",");
                                                    Collections.addAll(myPref, parts);
                                                }


                                            } else {
                                            }
                                        }
                                    });

                            for(int i = 0; i < myPref.size()-1; i++){
                                if(myPref.get(i) == matchPref.get(i)){
                                    matchedInterests ++;
                                }
                            }

                            if(matchedInterests >= 1){
                                String finalOutPut = userName + "  " + distanceStr;
                                arrayList.add(finalOutPut);
                                adapter.notifyDataSetChanged();
                            }

                        }
                    }
                } else {
                    arrayList.add("list error");
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }



    public void setMyLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
        }
        //clear previous location entries
        BaasQuery.Criteria filter = BaasQuery.builder().pagination(0, 20)
                .orderBy("_creation_date desc")
                .where("_author='" + BaasUser.current().getName() + "'")
                .criteria();


        BaasDocument.fetchAll("geo", filter,
                new BaasHandler<List<BaasDocument>>() {
                    @Override
                    public void handle(BaasResult<List<BaasDocument>> res) {
                        if (res.isSuccess()) {
                            for (BaasDocument doc : res.value()) {
                                Log.d("LOG", "Doc: " + doc);
                                doc.delete(new BaasHandler<Void>() {
                                    @Override
                                    public void handle(BaasResult<Void> res) {
                                        if (res.isSuccess()) {
                                            Log.d("LOG", "Document deleted");
                                        } else {
                                            Log.e("LOG", "error", res.error());
                                        }
                                    }
                                });
                                break;
                            }
                        } else {
                        }
                    }
                });

        BaasDocument doc = new BaasDocument("geo");
        doc.put("Author",BaasUser.current().getName())
                .put("Latitude", mLatitudeText)
                .put("Longitude", mLongitudeText);

        doc.save(BaasACL.grantRole(Role.REGISTERED, Grant.READ),new BaasHandler<BaasDocument>() {
            @Override
            public void handle(BaasResult<BaasDocument> res) {
                if (res.isSuccess()) {
                    Log.d("LOG", "Saved: " + res.value());
                } else {
                    Log.e("LOG", "Failed");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}