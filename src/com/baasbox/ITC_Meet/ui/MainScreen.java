package com.baasbox.ITC_Meet.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;



import com.baasbox.ITC_Meet.R;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author:
 * Roger Marciniak (c00169733)
 * Bartosz Zurawski
 */

public class MainScreen extends Activity {



    void notification(){

        NotificationManager nm = (NotificationManager)getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(getBaseContext());
        Intent notificationIntent = new Intent(getBaseContext(), MainScreen.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,notificationIntent,0);

        //set
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText("You've received a new message from " + send);
        builder.setContentTitle("New Message");
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();
        nm.notify((int)System.currentTimeMillis(),notification);

    }
    void startChat(String userName){
        Intent intent = new Intent(this,Chat.class);
        intent.putExtra("userName", userName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void onProfile(){
        Intent intent = new Intent(this,UserProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void onSeries(){
        Intent intent = new Intent(this,Minigame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void onLogout(){
        Intent intent = new Intent(this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void onScan(){
        Intent intent = new Intent(this,Scan.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private RequestToken logoutToken;
    private final BaasHandler<Void> logoutHandler =
            new BaasHandler<Void>() {
                @Override
                public void handle(BaasResult<Void> voidBaasResult) {
                    logoutToken=null;
                    onLogout();
                }
            };

    String send = "";
    String rec = "";
    int notifC = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen) ;

        getIntent().setAction("Already created");
        final TextView UserName = (TextView) findViewById(R.id.userN);
        UserName.setText(BaasUser.current().getName());
        UserName.setTextColor(Color.parseColor("#CDDC39"));

        final Button mProfile = (Button) findViewById(R.id.lProfile);
        final Button mSeries = (Button) findViewById(R.id.lSeries);
        final Button mLogout = (Button) findViewById(R.id.lLogout);
        final Button mScan = (Button) findViewById(R.id.lScan);
        final ImageButton notif = (ImageButton) findViewById(R.id.messageTracker);
        notif.setBackgroundResource(R.drawable.msg_off);
        notif.setEnabled(false);

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProfile();
            }
        });
        mSeries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSeries();
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaasUser.current().logout(logoutHandler);
            }
        });
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScan();
            }
        });

        final int code = 1;
        Intent notificationIntent = new Intent(this, MainScreen.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                code, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                BaasDocument.fetchAll("ChatLog", new BaasHandler<List<BaasDocument>>() {
                    @Override
                    public void handle(BaasResult<List<BaasDocument>> res) {
                        if (res.isSuccess()) {
                            for (BaasDocument doc : res.value()) {
                                rec = doc.getString("Receiver");
                                send = doc.getString("Sender");
                                String mess = doc.getString("Message");

                                if (BaasUser.current().getName().equals(doc.getString("Receiver"))) {

                                    notif.setBackgroundResource(R.drawable.msg_live);
                                    notif.setEnabled(true);
                                    if (notifC == 0) {
                                        notification();
                                        notifC = 1;
                                    }
                                } else {
                                    Log.e("ERROR", "Fetching failed!");
                                }
                            }
                        }
                    }
                });


            }

        }, 0, 5, TimeUnit.SECONDS);


        notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(send);
            }

        });


    }
    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onResume() {
        Log.v("Example", "onResume");

        String action = getIntent().getAction();
        // Stop endless loop by adding a unique action, no restart
        if(action == null || !action.equals("Already created")) {
            Log.v("Example", "Force restart");
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            finish();
        }
        // Get rid of the unique action, if onResume is called it will restart
        else
            getIntent().setAction(null);

        super.onResume();
    }

}

