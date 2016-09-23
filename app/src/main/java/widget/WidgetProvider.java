package widget;

/**
 * Created by moataz on 23/09/16.
 */
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        if (StockTaskService.ACTION_DATA_UPDATED.equals(intent.getAction())){
//            context.startService(new Intent(context, StockWidgetRemoteViewService.class));
            Log.d("widget","recevied");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_provider_layout;
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

//            // Add the data to the RemoteViews
//            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
//            // Content Descriptions for RemoteViews were only added in ICS MR1
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                setRemoteContentDescription(views, description);
//            }


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(context, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
//        views.setContentDescription(R.id.widget_icon, description);
    }
}
