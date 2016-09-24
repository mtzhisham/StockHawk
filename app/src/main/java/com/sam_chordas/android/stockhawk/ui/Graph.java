package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import com.melnykov.fab.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.view.LineChartView;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.MyDatePicker;
import com.sam_chordas.android.stockhawk.service.GraphTaskService;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.wang.avi.AVLoadingIndicatorView;
import com.wang.avi.indicators.BallClipRotateIndicator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Graph extends AppCompatActivity {
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
    private AVLoadingIndicatorView avi;
    TextView name_tv;
    TextView date_tv;
    TextView high_tv;
    TextView low_tv;


    ArrayList<String> high;
    private GcmNetworkManager mGcmNetworkManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        mContext = this;
        context = getApplicationContext();


        name_tv = (TextView) findViewById(R.id.name_tv);
        date_tv = (TextView) findViewById(R.id.date_tv);
        high_tv = (TextView) findViewById(R.id.high_tv);
        low_tv = (TextView) findViewById(R.id.low_tv);

        LineChartView mChart = (LineChartView) findViewById(R.id.linechart);

        String indicator=getIntent().getStringExtra("indicator");
        avi= (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.setIndicator(indicator);

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
        graph.getViewport().setMinX(5);
        graph.getViewport().setMaxX(50);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        name_tv.setText(sym);
        startAnim(graph);
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
        toTv.setText(getMPast(1));
        fromTv.setText(getMPast(365));



       if(isDataExist(symHigh)){

           fromTv.setText(getStart());
           toTv.setText(getEnd());
           loadData(fromTv.getText().toString(),toTv.getText().toString());


       }else {

           fireService(fromTv.getText().toString(),toTv.getText().toString());


       }




       FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.button);
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

    void startAnim(View view){
//        avi.show();
          avi.smoothToShow();
        avi.setIndicatorColor(ContextCompat.getColor(context, R.color.primary));
    }

    void stopAnim(View view){
//        avi.hide();
          avi.smoothToHide();
    }



    public void fireService(String fromDate, String toDate){
        Intent intent1 = new Intent(this,GraphTaskService.class);
        intent1.putExtra("sym",sym);
        intent1.putExtra("startDate", fromDate);
        intent1.putExtra("endDate", toDate);
        startService(intent1);

    }



    private String getMPast(int i ){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -i);
        return dateFormat.format(cal.getTime());


    }
    private Date getLastYear(){return new Date(System.currentTimeMillis()-30*(24*60*60*1000));
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

        LineGraphSeries<DataPoint> highSeries;
        LineGraphSeries<DataPoint> lowSeries;


        try {
            DB snappydb = DBFactory.open(context,"graphDB");
            String gTitle = snappydb.get(sym+"Name");
            name_tv.setText(gTitle);
            boolean isKeyExist =  snappydb.exists(symHigh);
            ArrayList high = snappydb.getObject(symHigh, ArrayList.class);
            ArrayList low = snappydb.getObject(symLow, ArrayList.class);
            final ArrayList dates = snappydb.getObject(symDate, ArrayList.class);

            snappydb.put(startDate,fromDate);
            snappydb.put(endDate,toDate);

            snappydb.close();

            Log.d("ArraySize", high.size() + "");
            Log.d("ArraySizeService", isKeyExist + "");


            DataPoint[] highDataPoints = new DataPoint[high.size()];
            DataPoint[] lowDataPoints = new DataPoint[low.size()];



            for (int i = 0; i < high.size();i++){

                highDataPoints[i] = new DataPoint(i, Double.valueOf((String) high.get(i)));
                lowDataPoints[i] = new DataPoint(i, Double.valueOf((String) low.get(i)));


            }

            highSeries = new LineGraphSeries<DataPoint>(highDataPoints);
            lowSeries = new LineGraphSeries<DataPoint>(lowDataPoints);


            highSeries.setColor(Color.GREEN);
            lowSeries.setColor(Color.RED);
            highSeries.setTitle("High");
            lowSeries.setTitle("low");

            highSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                   double round = (double) Math.round(dataPoint.getY() * 100) / 100;
                    String value = String.format("%.2f", round);
                    Toast.makeText(context,  dates.get((int) dataPoint.getX()) + " "+ value , Toast.LENGTH_SHORT).show();
                    high_tv.setText(value);
                    date_tv.setText(dates.get((int) dataPoint.getX()).toString());
                }
            });

            lowSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    double round = (double) Math.round(dataPoint.getY() * 100) / 100;
                    String value = String.format("%.2f", round);
                    low_tv.setText(value);
                }
            });


            graph.addSeries(highSeries);
            graph.addSeries(lowSeries);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        stopAnim(graph);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
