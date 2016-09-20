package com.sam_chordas.android.stockhawk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Moataz on 9/7/2016.
 */
public class GraphTaskService extends Service {
    public static final String TASK_TAG_WIFI = "wifi_task";


    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
