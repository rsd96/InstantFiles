package com.rsd96.instantfiles;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider{

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		 final int N = appWidgetIds.length;

	        // Perform this loop procedure for each App Widget that belongs to this provider
	        for (int i=0; i<N; i++) {
	            int appWidgetId = appWidgetIds[i];

	            // Create an Intent to launch ExampleActivity
	            Intent intent = new Intent(context, MainActivity.class);
	            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

	            // Get the layout for the App Widget and attach an on-click listener
	            // to the button
	            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
	            views.setOnClickPendingIntent(R.id.widget_text_view, pendingIntent);

	            // Tell the AppWidgetManager to perform an update on the current app widget
	            appWidgetManager.updateAppWidget(appWidgetId, views);
	        }
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
	}
	
	
	

}
