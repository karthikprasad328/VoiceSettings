package com.example.kprasad.speechrecognizer;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.RemoteViews;
import android.widget.Toast;


/**
 * Implementation of App Widget functionality.
 */
public class speechRecognizerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speech_recognizer_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setImageViewResource(R.id.appwidget_image,R.drawable.microphoneicon2);

        Intent configIntent = new Intent(context, MainActivity.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        views.setOnClickPendingIntent(R.id.widgetParentLayout, configPendingIntent);

        ComponentName thisWidget = new ComponentName(context, speechRecognizerWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, views);

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speech_recognizer_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setImageViewResource(R.id.appwidget_image,R.drawable.microphoneicon2);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

