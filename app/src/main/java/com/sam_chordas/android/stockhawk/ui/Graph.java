package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.db.chart.view.LineChartView;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.MyDatePicker;
import com.sam_chordas.android.stockhawk.service.GraphTaskService;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Graph extends Activity {
    final private String TAG_TASK_ONEOFF_LOG ="graph task";
    Context context;
    Context mContext;
    GraphView graph;
    String symHigh;
    String symLow;
    String symDate;
    String sym;
    BroadcastReceiver receiver;
    String startDate ="startDate";
    String endDate="endDate";


    ArrayList<String> high;
    private GcmNetworkManager mGcmNetworkManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        context = getApplicationContext();
        setContentView(R.layout.activity_graph);
        LineChartView mChart = (LineChartView) findViewById(R.id.linechart);



        Intent intent = getIntent();
        sym = intent.getStringExtra(getString(R.string.MENU_SYM));
        symHigh = sym+"High";
        symLow = sym+"Low";
        symDate = sym+"Date";
        startDate = sym+startDate;
        endDate = sym+endDate;
        Log.d("activity",sym);
        mGcmNetworkManager = GcmNetworkManager.getInstance(this);
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(10);
        graph.getViewport().setMaxX(50);

        final TextView fromTv = (TextView) findViewById(R.id.fromDatePicker);
        fromTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatePicker myDatePicker = new MyDatePicker();
                myDatePicker.showPicker(mContext,fromTv);
            }
        });

        final TextView toTv = (TextView) findViewById(R.id.toDatePicker);
        toTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatePicker myDatePicker = new MyDatePicker();
                myDatePicker.showPicker(mContext,toTv);
            }
        });



        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat df = new SimpleDateFormat(myFormat, Locale.US);
        toTv.setText(df.format(getMeYesterday()));
        fromTv.setText("2016-01-01");



       if(isDataExist(symHigh)){

           fromTv.setText(getStart());
           toTv.setText(getEnd());
           loadData(fromTv.getText().toString(),toTv.getText().toString());


       }else {

           fireService(fromTv.getText().toString(),toTv.getText().toString());


       }




        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("fromDate",  fromTv.getText().toString());
                Log.d("toDate", toTv.getText().toString());
                fireService(fromTv.getText().toString(),toTv.getText().toString());
                loadData(fromTv.getText().toString(),toTv.getText().toString());


            }
        });


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(GraphTaskService.MESSAGE);
                // do something here.
                loadData(fromTv.getText().toString(),toTv.getText().toString());
                Log.d("from service",s);

            }
        };





    }



    public void fireService(String fromDate, String toDate){
        Intent intent1 = new Intent(this,GraphTaskService.class);
        intent1.putExtra("sym",sym);
        intent1.putExtra("startDate", fromDate);
        intent1.putExtra("endDate", toDate);
        startService(intent1);

    }



    private Date getMeYesterday(){
        return new Date(System.currentTimeMillis()-24*60*60*1000);
    }

    private boolean isDataExist(String key) {
        boolean isKeyExist = false;
        try {
            DB snappydb = DBFactory.open(context, "graphDB");
            isKeyExist = snappydb.exists(key);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return isKeyExist;
    }

    public String getStart() {

        String start = null;
        try {
            DB snappydb = DBFactory.open(context, "graphDB");
            start = snappydb.get(startDate);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return start;
    }


    public String getEnd() {

        String end = null;
        try {
            DB snappydb = DBFactory.open(context, "graphDB");
            end = snappydb.get(endDate);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return end;
    }


    public void loadData(String fromDate, String toDate){

        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series;

        try {
            DB snappydb = DBFactory.open(context,"graphDB");
            boolean isKeyExist =  snappydb.exists(symHigh);
            ArrayList strings = snappydb.getObject(symHigh, ArrayList.class);

            snappydb.put(startDate,fromDate);
            snappydb.put(endDate,toDate);

            snappydb.close();

            Log.d("ArraySize", strings.size() + "");
            Log.d("ArraySizeService", isKeyExist + "");

            ArrayList<DataPoint> points = new ArrayList<DataPoint>();
            DataPoint[] dataPoints = new DataPoint[strings.size()];

            for (int i = 0; i < strings.size();i++){

                dataPoints[i] = new DataPoint(i, Double.valueOf((String) strings.get(i)));

            }

            series = new LineGraphSeries<DataPoint>(dataPoints
            );
            graph.addSeries(series);

        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(GraphTaskService.RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }


}
