package com.baasbox.ITC_Meet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.RequestToken;
import com.baasbox.ITC_Meet.R;

/**
 * @author:
 * Roger Marciniak (c00169733)
 */
public class Login extends FragmentActivity {
    private final static String SIGNUP_TOKEN_KEY = "signup_token_key";
    public static final String EXTRA_USERNAME = "com.baasbox.ITC_Meet.username.EXTRA";

    private String mUsername;
    private String mPassword;

    private EditText mUserView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private TextView mTitle;
    ImageView mLogo;




    private RequestToken mSignupOrLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState!=null){
            mSignupOrLogin = savedInstanceState.getParcelable(SIGNUP_TOKEN_KEY);
        }

        mTitle = (TextView) findViewById(R.id.title);
        mLogo = (ImageView) findViewById(R.id.imageView);
        mLogo.setImageResource(R.drawable.ic_launcher);
        mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        mUserView = (EditText) findViewById(R.id.email);
        mUserView.setText(mUsername);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusMessageView = (TextView)findViewById(R.id.login_status_message);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(false);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin(true);
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                attemptLogin(false);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSignupOrLogin!=null){
            showProgress(false);
            mSignupOrLogin.suspend();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSignupOrLogin!=null){
            showProgress(true);
            mSignupOrLogin.resume(onComplete);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSignupOrLogin!=null){
            outState.putParcelable(SIGNUP_TOKEN_KEY,mSignupOrLogin);
        }
    }

    private void completeLogin(boolean success){
        showProgress(false);
        mSignupOrLogin = null;
        if (success) {
            Intent intent = new Intent(this,MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }



    public void attemptLogin(boolean newUser) {
        //Reset errors
        mUserView.setError(null);
        mPasswordView.setError(null);

        //Store input
        mUsername = mUserView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Password rules
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        //Username rules
        if (TextUtils.isEmpty(mUsername)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            signupWithBaasBox(newUser);
        }
    }

    private void signupWithBaasBox(boolean newUser){
        //todo 3.1
        BaasUser user = BaasUser.withUserName(mUsername);
        user.setPassword(mPassword);
        if (newUser) {
            mSignupOrLogin=user.signup(onComplete);
        } else {
            mSignupOrLogin=user.login(onComplete);
        }
    }

    //todo 3.2
    private final BaasHandler<BaasUser> onComplete =
            new BaasHandler<BaasUser>() {
                @Override
                public void handle(BaasResult<BaasUser> result) {

                    mSignupOrLogin = null;
                    if (result.isFailed()){
                        Log.d("ERROR","ERROR",result.error());
                    }
                    completeLogin(result.isSuccess());
                }
            };

    @TargetApi(Build.VERSION_CODES.M)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
