package com.baasbox.ITC_Meet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.baasbox.android.*;

/**
 * @authors:
 * Roger Marciniak (c00169733)
 * Bartosz Zurawski(c00165634)
 */
public class SessionTracker extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo 2
        if (BaasUser.current() == null){
            startLoginScreen();
            return;
        }
        else{
            Intent intent = new Intent(this,MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }


    }

    private void startLoginScreen(){
        Intent intent = new Intent(this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }





}