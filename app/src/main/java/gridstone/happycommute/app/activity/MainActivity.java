package gridstone.happycommute.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.MainActivityFavouritesAdapter;
import gridstone.happycommute.app.adapter.NavigationAdapter.NavigationDrawerAdapter;
import gridstone.happycommute.app.adapter.NavigationAdapter.NavigationDrawerItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListPopulator;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.FavoriteJourney;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Matt on 1/04/2014.
 * Last edited by Matt on 23/9/2014
 * Edited by Chris on 26/09/2014
 */

public class MainActivity extends ActionBarActivity
{
    //declare constants for the navigation drawer
    private final int TRAIN_NETWORK_MAP = 0;
    private final int BUS_NEARBY = 1;
    private final int TRAM_NEARBY = 2;
    private final int TRAIN_NEARBY = 3;
    private final int NIGHTRIDER_NEARBY = 4;
    private final int TRAIN_JOURNEY_PLANNER = 5;
    private final int REMINDERS = 6;


    //private static BroadcastReceiver tickReceiver;
    //private showFavouritesAsyncTask async;
    private SharedPreference sharedPreference;
    private ArrayList<FavouriteListItem> favouriteListItems = new ArrayList<FavouriteListItem>();
    private Context context;
    private boolean firstRefresh = true;
    private FavouriteListPopulator favourites;
    private ArrayList<FavoriteJourney> favoriteJourneyArrayList;
    private ListView uiListView;
    private boolean internetAccess = true;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ArrayList<NavigationDrawerItem> mNavigationOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_main);
        this.context = this;
        this.uiListView = (ListView) findViewById(R.id.buttonListView);
        this.uiListView.setAdapter(new MainActivityFavouritesAdapter(this.context, null, true));
        showFavourite();
        configureNavDrawer();   //Configure the navigation drawer pane that will open on swipe
    }


        @Override //Handle the menu button presses and open/close the navigation drawer when it's pressed
        public boolean onKeyDown(int keyCode, KeyEvent e) {
            if (keyCode == android.view.KeyEvent.KEYCODE_MENU) {
            // your action...
            if (!mDrawerLayout.isDrawerOpen(mDrawerList)) { //if its closed, open it
                mDrawerLayout.openDrawer(mDrawerList);
            }
            else if (mDrawerLayout.isDrawerOpen(mDrawerList)) { //if its open, then we close it
                mDrawerLayout.closeDrawer(mDrawerList);
            }
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }



    @Override
    public void onResume()
    {
        super.onResume();
        if (activeInternetConnection() == true)
        {
            showFavourite();
        }
    }


    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons, usefull later if we choose to add the actionbar for this activity
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void alertMessageNoInternet()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Happy Commute needs Internet Access enabled, please check your network settings and signal strength.").setCancelable(true).setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialogInterface, @SuppressWarnings("unused") final int id)
            {
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                dialog.cancel();
            }
        }).show();
    }

    private boolean checkNetwork()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null)
        {
            if(activeNetworkInfo.isConnectedOrConnecting())
                return true;
            else
                return false;
        }
        else
            return false;
    }

    private boolean activeInternetConnection()
    {
        if (checkNetwork() == true)
        {
            /*
            try
            {
                HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlConnection.setRequestProperty("User-Agent", "Test");
                urlConnection.setRequestProperty("Connection", "Close");
                urlConnection.setConnectTimeout(1500);
                urlConnection.connect();

                return (urlConnection.getResponseCode() == 200);
            }
            catch(Exception e)
            {

            }
            return false;
            */
            return true;
        } else
        {
            return false;
        }
    }

    public void showFavourite()
    {
        if (this.firstRefresh)
        {
            this.sharedPreference = new SharedPreference();
            this.favoriteJourneyArrayList = this.sharedPreference.getFavorite(context);
            this.favourites = new FavouriteListPopulator(this.context, 0, this.favoriteJourneyArrayList, true);
            this.favouriteListItems = this.favourites.getFavouriteList();
            this.uiListView.setAdapter(new MainActivityFavouritesAdapter(this.context, this.favouriteListItems, true));
        }

        Subscriber<ArrayList<FavouriteListItem>> subscriber = new Subscriber<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void onNext(ArrayList<FavouriteListItem> favouriteListItems)
            {
                if (internetAccess == false)
                {
                    alertMessageNoInternet();
                } else
                {
                    uiListView.setAdapter(new MainActivityFavouritesAdapter(context, favouriteListItems, false));

                }
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error){}

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
                if (activeInternetConnection() == false)
                {
                    internetAccess = false;
                    sub.onNext(favouriteListItems);
                    sub.onCompleted();
                }
                else
                {
                    uiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int position, long id)
                        {
                            startActivity((new Intent(getApplicationContext(), FavouriteJourneys.class)));

                        }
                    });

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
            }
        });


    }

    public void configureNavDrawer() //Configure the layouts for the navigation drawer, and add the list items to it
    {
        //Config the nav drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setDivider(getResources().getDrawable(R.drawable.abc_list_divider_holo_dark));
        mDrawerList.setSelector(getResources().getDrawable(R.drawable.abc_list_selector_holo_dark));
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mNavigationOptions = new ArrayList<NavigationDrawerItem>();

        mNavigationOptions.add(new NavigationDrawerItem("Network maps", R.drawable.ic_action_map)); //Adds the "train network map" item with the ic_action_map icon to the navdrawer
        mNavigationOptions.add(new NavigationDrawerItem("Busses Nearby", R.drawable.toggle_bus));
        mNavigationOptions.add(new NavigationDrawerItem("Trams Nearby", R.drawable.toggle_tram));
        mNavigationOptions.add(new NavigationDrawerItem("Trains Nearby", R.drawable.toggle_train));
        mNavigationOptions.add(new NavigationDrawerItem("Nighrider Nearby", R.drawable.toggle_nightrider));
        mNavigationOptions.add(new NavigationDrawerItem("Journey Planner", R.drawable.journey_planner));
        mNavigationOptions.add(new NavigationDrawerItem("Reminders", R.drawable.alarm_icon));



        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(this, mNavigationOptions); //create a custom adapter instance with the custom navigation menu items

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */)
        {
            public void onDrawerClosed(View view)
            {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView)
            {
//                Uncomment the next line if we use an actionbar
//                getActionBar().setTitle("Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener { //handle the selection of a navigation item
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mDrawerLayout.closeDrawer(mDrawerList); //close the drawer
        Intent showTransportNearbyIntent = null;
        if (position > 0 && position < 5) //if the item is a transport type to show nearby departures of
        {
            showTransportNearbyIntent = new Intent(getApplicationContext(), ShowTransportNearby.class);
        }
            switch (position)
            {
                case TRAIN_NETWORK_MAP:
                    startActivity(new Intent(getApplicationContext(), ShowNetworkMaps.class)); //start the map activity
                    break;
                case BUS_NEARBY:
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "bus"); //add the transport type to the intent
                    startActivity(showTransportNearbyIntent);   //then start the activity
                    break;
                case TRAM_NEARBY:
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "tram");
                    startActivity(showTransportNearbyIntent);
                    break;
                case TRAIN_NEARBY:
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "train");
                    startActivity(showTransportNearbyIntent);
                    break;
                case NIGHTRIDER_NEARBY:
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "nightrider");
                    startActivity(showTransportNearbyIntent);
                    break;
                case TRAIN_JOURNEY_PLANNER:
                    startActivity(new Intent(getApplicationContext(), JourneyPlanner.class)); //start the map activity
                    break;
                case REMINDERS:
                    startActivity(new Intent(getApplicationContext(), ShowReminders.class)); //start the map activity
                    break;
                default:
                    return;
            }
            return;
        }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
