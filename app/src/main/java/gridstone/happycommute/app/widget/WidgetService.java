package gridstone.happycommute.app.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;

/**
 * Created by CHRIS on 20/09/2014.
 */
public class WidgetService extends RemoteViewsService
{
    private Context context;
    private static ArrayList<FavouriteListItem> listData;

    public WidgetService() {} // still need this empty Constructor for default service

    public WidgetService(ArrayList<FavouriteListItem> listData)
    {
        this.listData = listData;
    } //this will assign the listData

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        this.context = this;
        Log.d("Widget Service", "Widget Service");
        return new FavouriteListProvider(this.getApplicationContext(), intent, listData);

    }
}



