package com.example.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.uhf.activity.UHFMainActivity;

/**
 * Created by Administrator on 2018-12-24.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("zp_add","-------BootBroadcastReceiver--------");
        Intent inte = new Intent(context, UHFMainActivity.class);
        context.startActivity(inte);
    }

}
