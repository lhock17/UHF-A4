package com.example.uhf.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TcpService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
