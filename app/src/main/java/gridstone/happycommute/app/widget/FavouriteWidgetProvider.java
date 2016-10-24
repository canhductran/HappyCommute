package gridstone.happycommute.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;

import java.util.ArrayList;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.activity.MainActivity;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListPopulator;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.FavoriteJourney;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by CHRIS on 20/09/2014.
 */

public class FavouriteWidgetProvider extends AppWidgetProvider
{
    private SharedPreference sharedPreference;
    private FavouriteListPopulator favourites;
    private ArrayList<FavoriteJourney> favoriteJourneyArrayList;
    private boolean firstRefresh = true;

    private ArrayList<FavouriteListItem> listData;
    private Subscriber<ArrayList<FavouriteListItem>> subscriber;
    private int[] appWidgetIds;
    private AppWidgetManager appWidgetManager;
    private Context context;
    private static final String SYNC_CLICKED = "android.appwidget.action.APPWIDGET_UPDATE";


    @Override
    public void onUpdate(Context pContext, AppWidgetManager pAppWidgetManager, int[] pAppWidgetIds)
    {
        this.appWidgetIds = pAppWidgetIds;
        this.appWidgetManager = pAppWidgetManager;
        this.context = pContext;



        this.sharedPreference = new SharedPreference();
        this.favoriteJourneyArrayList = this.sharedPreference.getFavorite(context);

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        this.subscriber = new Subscriber<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void onNext(ArrayList<FavouriteListItem> favouriteListItems)
            {
                Log.d("Update", "Update");
                listData = favouriteListItems;
                WidgetService widgetService = new WidgetService(listData);
                for (int i = 0; i < appWidgetIds.length; ++i) {
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                            R.layout.app_widget_provider);

                    //RemoteViews Service needed to provide adapter for ListView
                    Intent serviceIntent = new Intent(context, WidgetService.class);
                    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                    serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                    //passing app widget id to that RemoteViews Service
                    //setting adapter to listview of the widget
                    remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.widgetListView,
                            serviceIntent);
                    remoteViews.setOnClickPendingIntent(R.id.action_refresh, getPendingSelfIntent(context, SYNC_CLICKED));
                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    remoteViews.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent);
                    context.startService(serviceIntent);
                    appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
                }
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}
        };


        //observable object will be executed in background thread.
        //afterwards, when the observable object has finished executed its task,
        // it will transfer the result of calculation to its subscriber in main thread and the subscriber will update the UI in onNext()
        getFavouriteListItems().subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);


    }


    public Observable<ArrayList<FavouriteListItem>> getFavouriteListItems()
    {
        return Observable.create(new Observable.OnSubscribe<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void call(Subscriber<? super ArrayList<FavouriteListItem>> sub)
            {

                if (firstRefresh)
                {
                    favourites = new FavouriteListPopulator(context, 0, favoriteJourneyArrayList, false);
                    favourites.updateTransportDepartures();
                    firstRefresh = false;
                }
                else
                {
                    //if it isn't the first refresh, we execute this method to potentially save on net and battery resources
                    favourites.updateTransportDepartures();
                    //                Log.d("Updating transport departures!", "updating transport");
                }
                ArrayList<FavouriteListItem> favouriteListItems;
                favouriteListItems = favourites.getFavouriteList();
                if (favouriteListItems.size() == 0)
                {
                    favouriteListItems = null;
                }
                sub.onNext(favouriteListItems);
                sub.onCompleted();
            }

        });
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance
                (context);

        ComponentName thisAppWidgetComponentName =
                new ComponentName(context.getPackageName(),getClass().getName()
                );
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetListView);
    }


    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}


