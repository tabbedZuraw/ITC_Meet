package com.baasbox.ITC_Meet.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.baasbox.ITC_Meet.R;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasFile;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;

import java.io.InputStream;
import java.util.List;

/**
 * @author:
 * Bartosz Zurawski(c00165634)
 */

public class MatchView extends Activity {

    final MyInt req = new MyInt(0);

    public class MyInt {
        private int value;
        public MyInt(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }
    private class LoadImg extends AsyncTask<String, Void, Bitmap> {
        ImageButton bmImage;

        public LoadImg(ImageButton bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... uris) {
            String uri = uris[0];
            Bitmap myImg = null;
            try {
                InputStream inputStream = new java.net.URL(uri).openStream();
                myImg = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return myImg;
        }

        protected void onPostExecute(Bitmap result) {
            Bitmap resized = Bitmap.createScaledBitmap(result, 500, 350, false);
            bmImage.setImageBitmap(resized);
        }
    }
    void newActiv(){
        Intent intent = new Intent(this,Minigame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    void onChat(String userName){
        Intent intent = new Intent(this,Chat.class);
        intent.putExtra("userName", userName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_view);
        final String pkgn = getIntent().getExtras().getString("userName");

        BaasQuery.Criteria filter = BaasQuery.builder().pagination(0, 20)
                .orderBy("_creation_date desc")
                .where("_author='" + pkgn + "'")
                .criteria();

        BaasFile.fetchAll(filter, new BaasHandler<List<BaasFile>>() {
            @Override
            public void handle(BaasResult<List<BaasFile>> res) {
                if (res.isSuccess()) {
                    final ImageButton pic = (ImageButton) findViewById(R.id.profPic);
                    pic.setImageResource(R.drawable.placeh);
                    for (BaasFile doc : res.value()) {
                        Log.d("Pass", doc.getStreamUri().toString());
                        new LoadImg((ImageButton) findViewById(R.id.profPic))
                                .execute(doc.getStreamUri().toString());

                        break;
                    }
                } else {
                    Log.e("LOG", "Error", res.error());
                }
            }
        });



        final TextView txt1 = (TextView) findViewById(R.id.userName);
        txt1.setText(pkgn);
        txt1.setTextColor(Color.parseColor("#CDDC39"));

        final TextView inte1 = (TextView) findViewById(R.id.textView5);
        final TextView inte2 = (TextView) findViewById(R.id.textView6);
        final TextView inte3 = (TextView) findViewById(R.id.textView7);

        BaasQuery.Criteria filter2 = BaasQuery.builder().pagination(0, 20)
                .orderBy("_creation_date desc")
                .where("_author='" + BaasUser.current().getName() + "'")
                .criteria();


        BaasDocument.fetchAll("Preferences", filter2,
                new BaasHandler<List<BaasDocument>>() {
                    @Override
                    public void handle(BaasResult<List<BaasDocument>> res) {
                        if (res.isSuccess()) {

                            for (BaasDocument doc : res.value()) {
                                String pref = doc.getString("Interests");
                                pref = pref.substring(1, pref.length() - 1);
                                String[] parts = pref.split(",", 4);
                                inte1.setText(parts[0]);
                                inte2.setText(parts[1]);
                                inte3.setText(parts[2]);
                            }


                        } else {
                            newActiv();
                        }
                    }
                });
        final Button chatBtn = (Button) findViewById(R.id.sendMessageButton);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChat(pkgn);
            }

        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}