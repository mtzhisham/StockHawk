package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Moataz on 9/7/2016.
 */
public class GraphTaskService extends IntentService {




    private ArrayList<String> highList = new ArrayList<>();
    private ArrayList<String> lowList = new ArrayList<>();
    private ArrayList<String> dateList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private String symHigh;
    private String symLow;
    private String symDate;
    private ArrayList strings;

    static final public String RESULT = "REQUEST_PROCESSED";

    static final public String MESSAGE = "MSG";

    public GraphTaskService() {
        super("GraphTaskService");
    }

    LocalBroadcastManager  broadcaster;
    @Override
    public void onCreate() {
        super.onCreate();

        broadcaster = LocalBroadcastManager.getInstance(this);


    }

    public void sendResult(String message) {
        Intent intent = new Intent(RESULT);
        if(message != null)
            intent.putExtra(MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    public String getGraph(String sym,String startDate,String endDate) throws UnsupportedEncodingException {

//        Log.d("service", startDate + " to " + endDate);
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
        urlStringBuilder.append("select * from yahoo.finance.historicaldata where symbol = ");
        urlStringBuilder.append(URLEncoder.encode("\""+sym+"\"","UTF-8"));
        urlStringBuilder.append(" and startDate = \""+startDate+"\" and endDate = \""+endDate+"\"");
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");
        String urlString = urlStringBuilder.toString();
        Log.d("service URL",urlString);
        return urlString;


    }



    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String sym  = intent.getStringExtra("sym");
        String startDate  = intent.getStringExtra("startDate");
        String endDate  = intent.getStringExtra("endDate");
        symHigh = sym+"High";
        symLow = sym+"Low";
        symDate = sym+"Date";


        try {


            DB snappydb = DBFactory.open(MyStocksActivity.getmContext(),"graphDB");





                try{
                    String graphData = run(getGraph(sym,startDate,endDate));

                    JSONObject data = new JSONObject(graphData);
                    JSONObject query = data.getJSONObject("query");
                    JSONObject results = query.getJSONObject("results");
                    JSONArray quote = results.getJSONArray("quote");

                    for (int i=0; i < quote.length(); i++)
                    {
                        JSONObject oneObject = quote.getJSONObject(i);
                        // Pulling items from the array
                        String high = oneObject.getString("High");
                        highList.add(high);
                        String low = oneObject.getString("Low");
                        lowList.add(low);
                        String date = oneObject.getString("Date");
                        dateList.add(date);
                    }



                }
                catch (Exception e){
                e.printStackTrace();
                }



                snappydb.put(symHigh, highList);
                snappydb.put(symLow, lowList);
                snappydb.put(symDate, dateList);

                strings = snappydb.getObject(symHigh, ArrayList.class);



            snappydb.close();

            sendResult("done");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
