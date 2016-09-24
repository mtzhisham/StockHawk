package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by moataz on 23/09/16.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.Graph;


public class WidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = WidgetProvider.class.getSimpleName();
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update all StockWidgets
        for (int widgetId: appWidgetIds){
            Intent intent = new Intent(context,StockWidgetRemoteViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);

            // create Widget
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.collection_widget_layout);
            views.setRemoteAdapter(R.id.widgetCollectionList,intent);

            // Open Graph on List Item click
            Intent intentStockGraph = new Intent(context, Graph.class);
            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intentStockGraph)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widgetCollectionList,pendingIntent);

            // Update Widget on HomeScreen
            appWidgetManager.updateAppWidget(widgetId,views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(StockTaskService.ACTION_DATA_UPDATED)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widgetCollectionList);

        }
    }
}
