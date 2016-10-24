package gridstone.happycommute.app.widget;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.activity.MainActivity;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListAdapter;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListPopulator;
import gridstone.happycommute.app.database.DatabaseHelper;
import gridstone.happycommute.app.favourites.FavoriteManipulater;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.FavoriteJourney;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by CHRIS on 20/09/2014.
 */
public class FavouriteListProvider implements RemoteViewsFactory
{
    private Context context = null;
    private ArrayList<FavouriteListItem> listData;
    private SharedPreference sharedPreference;
    private FavouriteListPopulator favourites;
    private ArrayList<FavoriteJourney> favoriteJourneyArrayList;
    private ArrayList<FavouriteListItem> favouriteListItems = new ArrayList<FavouriteListItem>();

    public FavouriteListProvider(Context context, Intent intent, ArrayList<FavouriteListItem> listData)
    {
        this.context = context;
        this.listData = listData;
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getCount()
    {
        if (listData != null)
        {
            return listData.size();
        } else
        {
            return 0;
        }
    }


    @Override
    public RemoteViews getViewAt(int position)
    {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.favourite_list_row_layout);
        remoteView.setInt(R.id.widgetTitle, "setBackgroundColor", Color.parseColor("#0000ff"));


        if (position == 1 && listData == null)
        {
            remoteView.setTextViewText(R.id.departure, "No favourite");
        } else if (listData != null)
        {

            remoteView.setTextViewText(R.id.departure, "Departs at: " + listData.get(position).getDepartureStop());
            remoteView.setTextViewText(R.id.arrival, "Arrives at: " + listData.get(position).getArrivalStop());
            remoteView.setTextViewText(R.id.departureTime, listData.get(position).getDepartureTime());
                  //remoteView.departureTime.setTextColor(Color.parseColor("#666666"));
                //holder.stationName.setTextColor(Color.parseColor("#666666"));
            if(listData.get(position).getDepartureTime().toUpperCase().equals("NOW") || listData.get(position).getDepartureTime().toUpperCase().equals("1 MIN"))
            {
                remoteView.setTextColor(R.id.departure, Color.parseColor("#000000"));
                remoteView.setTextColor(R.id.arrival, Color.parseColor("#000000"));
                remoteView.setTextColor(R.id.departureTime, Color.parseColor("#000000"));
                remoteView.setInt(R.id.widgetItem, "setBackgroundColor", Color.parseColor("#FFFFFF"));
            }
            else
            {
                remoteView.setInt(R.id.widgetItem, "setBackgroundColor", Color.parseColor("#81CFE0"));
                remoteView.setTextColor(R.id.departure, Color.parseColor("#FFFFFF"));
                remoteView.setTextColor(R.id.arrival, Color.parseColor("#FFFFFF"));
                remoteView.setTextColor(R.id.departureTime, Color.parseColor("#FFFFFF"));
            }


        }


        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView()
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged()
    {
        listData = null;
        showFavourite();
        for(;;)
        {
            if(listData != null)
            {
                break;
            }
        }
    }

    @Override
    public void onDestroy() {}


    public void showFavourite()
    {
        this.sharedPreference = new SharedPreference();
        this.favoriteJourneyArrayList = this.sharedPreference.getFavorite(this.context);
        this.favourites = new FavouriteListPopulator(this.context, 0, this.favoriteJourneyArrayList, true);
        this.favouriteListItems = favourites.getFavouriteList();

        Subscriber<ArrayList<FavouriteListItem>> subscriber = new Subscriber<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void onNext(ArrayList<FavouriteListItem> favouriteListItems)
            {
                listData = favouriteListItems;
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
                favourites = new FavouriteListPopulator(context, 0, favoriteJourneyArrayList, false);
                favouriteListItems = favourites.getFavouriteList();
                try
                {
                    String lineString;
                    for (FavouriteListItem favouriteListItem : favouriteListItems)
                    {
                        //Gets the substring from index 0 to the index of the second hyphen
                        lineString = favouriteListItem.getLineName();
                        favouriteListItem.setLineName(lineString);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                sub.onNext(favouriteListItems);
                sub.onCompleted();
            }
        });
    }
}