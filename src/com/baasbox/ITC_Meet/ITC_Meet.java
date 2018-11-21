package com.baasbox.ITC_Meet;

import android.app.Application;
import com.baasbox.android.*;

/**
 * @author:
 * Roger Marciniak
 * Bartosz Zurawski
 */
public class ITC_Meet extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BaasBox.builder(this).setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN)
                .setApiDomain("vps.yatsu.eu")
                .setPort(9000)
                .setAppCode("1234567890")
                .init();
    }

}
