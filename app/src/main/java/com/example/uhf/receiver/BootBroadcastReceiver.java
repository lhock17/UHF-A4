package com.example.uhf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.service.TcpService;

/**
 * Created by Administrator on 2018-12-24.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("zp_add","-------BootBroadcastReceiver--------");
        Intent inte = new Intent(context, TcpService.class);
        context.startService(inte);
    }

}
