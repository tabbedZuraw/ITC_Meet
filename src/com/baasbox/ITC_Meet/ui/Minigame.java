package com.baasbox.ITC_Meet.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.baasbox.ITC_Meet.R;
import com.baasbox.android.BaasACL;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasQuery;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.Grant;
import com.baasbox.android.Role;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author:
 * Bartosz Zurawski(c00165634)
 */

public class Minigame extends Activity {

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
    void clearDB(){
        BaasQuery.Criteria filter = BaasQuery.builder().pagination(0, 20)
                .orderBy("_creation_date desc")
                .where("_author='" + BaasUser.current().getName() + "'")
                .criteria();


        BaasDocument.fetchAll("Preferences", filter,
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
    }
    void UploadResults(List y){

        clearDB();

        String currentDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String newString = y.toString();

            BaasDocument doc = new BaasDocument("Preferences");
            doc.put("Date", currentDate)
                    .put("Author",BaasUser.current().getName().toString())
                    .put("Interests", newString);
            doc.save(BaasACL.grantRole(Role.REGISTERED, Grant.READ),new BaasHandler<BaasDocument>() {
                @Override
                public void handle(BaasResult<BaasDocument> res) {
                    if (res.isSuccess()) {
                        Log.d("LOG", "Saved: " + res.value());
                    } else {
                        Log.e("LOG", "Error");
                    }
                }
            });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series);

        final ArrayList<String> links = new ArrayList<String>();
        links.add("http://www.klydewarrenpark.org/media/images/Activities/reading.jpg");
        links.add("http://www.roadtogrammar.com/movies/fimls.jpg");
        links.add("http://www.ballingerathleticperformance.com/wp-content/uploads/2012/01/crowie.jpg");
        links.add("http://www.natural-homeremedies.com/fitness/wp-content/uploads/2010/10/Health-Benefits-Of-Swimming.jpg");
        links.add("http://blogs.transparent.com/polish/files/2015/09/sports.jpg");
        links.add("http://i-cdn.phonearena.com/images/article/73702-image/The-5-best-smartphones-for-mobile-gaming.jpg");
        links.add("http://s.hswstatic.com/gif/10-best-family-dog-breeds-6.jpg");
        links.add("http://static1.squarespace.com/static/5575eb95e4b08f79780bfb17/5575f0c9e4b04dfb97b3994d/5575f0e9e4b04dfb97b39cf2/1433792832487/tabby-cat-licking-its-lips.png");

        final ArrayList<String> preferences = new ArrayList<String>();
        preferences.add("Books");
        preferences.add("Movies");
        preferences.add("Running");
        preferences.add("Swimming");
        preferences.add("Sports");
        preferences.add("Gaming");
        preferences.add("Dogs");
        preferences.add("Cats");

        final ArrayList<String> result = new ArrayList<String>();

        final MyInt passes = new MyInt(0);
        final MyInt passes2 = new MyInt(0);
        final int numberOfEntries = 3;

        int temp = passes.getValue();
        new LoadImg((ImageButton) findViewById(R.id.opt1))
                .execute(links.get(temp));
        new LoadImg((ImageButton) findViewById(R.id.opt2))
                .execute(links.get(temp + 1));


        final ImageButton imgButt1 = (ImageButton) findViewById(R.id.opt1);
        final ImageButton imgButt2 = (ImageButton) findViewById(R.id.opt2);

        imgButt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View c) {
                if (imgButt1.isPressed()) {
                    int var = passes2.getValue();
                    if (var <= numberOfEntries - 1) {
                        int temp = passes.getValue();
                        result.add(preferences.get(temp));
                        temp = temp + 2;
                        new LoadImg((ImageButton) findViewById(R.id.opt1))
                                .execute(links.get(temp));
                        new LoadImg((ImageButton) findViewById(R.id.opt2))
                                .execute(links.get(temp + 1));
                        passes.setValue(temp);
                    } else {
                        UploadResults(result);
                        Intent intent = new Intent(Minigame.this, MainScreen.class);
                        startActivity(intent);
                        finish();
                    }

                    var = var + 1;
                    passes2.setValue(var);
                }
            }

        });
        imgButt2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View c) {
                if(imgButt2.isPressed()){
                    int var = passes2 .getValue();
                    if(var <= numberOfEntries -1){
                        int temp = passes.getValue();
                        result.add(preferences.get(temp+1));
                        temp = temp +2;
                        new LoadImg((ImageButton) findViewById(R.id.opt1))
                                .execute(links.get(temp));
                        new LoadImg((ImageButton) findViewById(R.id.opt2))
                                .execute(links.get(temp + 1));
                        passes.setValue(temp);
                    }
                    else{
                        UploadResults(result);
                        Intent intent = new Intent(Minigame.this, MainScreen.class);
                        startActivity(intent);
                        finish();
                    }

                    var = var +1;
                    passes2.setValue(var);
                }
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
