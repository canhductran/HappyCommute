package gridstone.happycommute.app.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListAdapter;
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListItem;
import gridstone.happycommute.app.adapter.departuresAdapter.TransportDepartureListPopulator;
import gridstone.happycommute.app.algo.HaversineAlgorithm;
import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.database.DatabaseHelper;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Stop;
import gridstone.happycommute.app.model.TransportType;
import gridstone.happycommute.app.reminder.ReminderReceiver;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Matt  on 8/05/2014.
 * Last edited by Matt on 14/10/2014
 */
public class ShowTransportNearby extends ActionBarActivity
{
    private static BroadcastReceiver tickReceiver;
    private final Context context = this;
    private AlertDialog alert;
    private APIHandler apiHandler;
    private ActionBar actionBar;
    private boolean performFullRefresh = true;
    private TransportDepartureListPopulator departures;
    private ArrayList<Stop> stopsNearbyCurrentStation;
    private Integer transportType;
    private String actionBarTitleTempString;
    private DatabaseHelper db;
    private ArrayAdapter<String> autoCompleteAdapter;
    private AutoCompleteTextView stationSearch;
    private ArrayList<DepartureListItem> departuresList = new ArrayList<DepartureListItem>();
    private LocationManager locationManager;
    private Location currentLocation;
    private Location oldLocation;
    private ListView DeparturesListView;
    private Subscriber<TransportDepartureListPopulator> showTransportSubscriber;
    private int mCurrentMinute, mCurrentHour;
    private DateTime selectedTime, currentTime;
    private boolean timeChosen = false;
    private Button searchTimeButton = null;
    TimePickerDialog timeSelectionDialog;
    private View headerView;
    private boolean loaded = false;
    private boolean persistentReUpdate = false;
    private String headerText = "Loading...";

    private LocationListener listener = new LocationListener()
    {
        //when the currentLocation changes, stop registering changes and if you have moved more than 200 meters use the new currentLocation
        @Override
        public void onLocationChanged(Location location)
        {
            Log.d("Location Changed","asd");
            Integer distanceBetweenLocations;

            distanceBetweenLocations = HaversineAlgorithm.HaversineInM(oldLocation.getLatitude(), oldLocation.getLongitude(), location.getLatitude(), location.getLongitude());
//            Toast.makeText(context, "DEBUG: distance of " + distanceBetweenLocations + "m since last update", Toast.LENGTH_SHORT).show();
            Log.d("DEBUG: distance of " + distanceBetweenLocations + "m since last update", "hehe");
            if (distanceBetweenLocations > 50) //more than 50 meters between new and old location
            {
                currentLocation = location;
                oldLocation = location;
                Log.d("DEBUG: Change detected, updating current location\"", "locationchange");
                Toast.makeText(context, "Change detected, updating current location", Toast.LENGTH_SHORT).show();
                departures.setDesiredTransportLocation(location);
                departures.setCurrentDesiredStop(null);
                performFullRefresh = true;
                if (!showTransportSubscriber.isUnsubscribed()) //unsubcribe means that the subscriber has already stopped executing the subject
                {
                    showTransportSubscriber.unsubscribe();
                }
                showDepartures(false);

            }

            locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d("provider disabled", "locationManager");
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d("provider enabled", "locationManager");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d("Latitude", "status");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();
        oldLocation = currentLocation;
        requestLocationUpdate();

        this.apiHandler = new APIHandler();
        this.db = new DatabaseHelper(context);
        //get the string from the intent and set the transport type based off this
        String transportTypeString = getIntent().getExtras().getString("TRANSPORT_TYPE");
        if (transportTypeString.equalsIgnoreCase("train"))
        {
            this.transportType = 0;
            this.actionBarTitleTempString = "Showing Trains Nearby...";
        }
        else if (transportTypeString.equalsIgnoreCase("tram"))
        {
            this.transportType = 1;
            this.actionBarTitleTempString = "Showing Trams Nearby...";
        }
        else if (transportTypeString.equalsIgnoreCase("bus"))
        {
            this.transportType = 2;
            this.actionBarTitleTempString = "Showing Busses Nearby...";
        }
        else
        {
            this.transportType = 4;
            this.actionBarTitleTempString = "Showing Nightriders Nearby...";
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_nearby); //set the layout
        this.actionBar = getSupportActionBar();
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + this.actionBarTitleTempString + "</font></b>"));
        this.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));

        this.departures = new TransportDepartureListPopulator(context, this.transportType, null); //allocate a new departures populator

//        create a new broacast receiver and register it every time the minute ticks over, run async at this time
        tickReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
                {
                    showDepartures(false);
                    //increment the current minute/hour to update the header
                    //new showTransportAsync().execute();
                }
            }

        };
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        showDepartures(true);
        showLoadingListView();

    }


    @Override
    protected void onPause()
    {
        super.onPause();
//        if (progressDialog.isShowing())
//            progressDialog.dismiss();
    }

    @Override
    public void onStop()
    {
        super.onStop(); //unregister broadcast receiver
        if (tickReceiver != null)
        {
            try
            {
                unregisterReceiver(tickReceiver);
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        requestLocationUpdate();
        performFullRefresh = true;
        if (showTransportSubscriber.isUnsubscribed()) //if its running the activity probably just started, so we don't need to update it
        {
            showDepartures(false);
            Toast.makeText(context, "refreshing data", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.transport_nearby, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.station_search:
                if (loaded)
                {
                    showStationSearchDialog();//If the station searched button is touched, call the method to show the timeSelectionDialog
                }
                return true;
            case R.id.search_map:
                if (loaded)
                {
                    Intent mapActivity = new Intent(getApplicationContext(), SearchLocation.class);
                    String transportTypeString = getIntent().getExtras().getString("TRANSPORT_TYPE");
                    mapActivity.putExtra("TRANSPORT_TYPE", transportTypeString);
                    startActivityForResult(mapActivity, 0);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        Log.d("menu", "menu");

        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.transport_list)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.time_reminder_options, menu);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DepartureListItem departureListItem = (DepartureListItem) DeparturesListView.getItemAtPosition(info.position);

        Date departureTime = departureListItem.getDepartureItem().getTime_timetable_utc();
        Date now = new Date();

        long diff = Math.abs(departureTime.getTime()/60000 - now.getTime()/60000);

        Intent intent = new Intent(getApplicationContext(), ShowReminders.class);   //set the intent for transportRun class
        switch (item.getItemId())
        {
            case R.id.five:
                if(diff > 5)
                {
                    saveReminder(departureListItem.getDepartureItem(), 5);
                    finish();
                    startActivity(intent);
                    return true;
                }
                else
                {
                    Toast.makeText(context, "The time has passed", Toast.LENGTH_SHORT).show();
                }
            case R.id.ten:
                if(diff > 10)
                {
                    saveReminder(departureListItem.getDepartureItem(), 10);
                    finish();
                    startActivity(intent);
                    return true;
                }
                else
                {
                    Toast.makeText(context, "The time has passed", Toast.LENGTH_SHORT).show();
                }
            case R.id.fifteen:
                if(diff > 15)
                {
                    saveReminder(departureListItem.getDepartureItem(), 15);
                    finish();
                    startActivity(intent);
                    return true;
                }
                else
                {
                    Toast.makeText(context, "The time has passed", Toast.LENGTH_SHORT).show();
                }
            case R.id.twenty:
                if(diff > 20)
                {
                    saveReminder(departureListItem.getDepartureItem(), 20);
                    finish();
                    startActivity(intent);
                    return true;
                }
                else
                {
                    Toast.makeText(context, "The time has passed", Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        LatLng l = (LatLng)data.getParcelableExtra("location");
        if(l != null)
        {
            Location loc = new Location("selected location");
            loc.setLatitude(l.latitude);
            loc.setLongitude(l.longitude);
            Toast.makeText(context, "Change detected, updating current location", Toast.LENGTH_SHORT).show();
            departures.setDesiredTransportLocation(loc);
            departures.setCurrentDesiredStop(null);
            performFullRefresh = true;
            if (!showTransportSubscriber.isUnsubscribed()) //unsubcribe means that the subscriber has already stopped executing the subject
            {
                showTransportSubscriber.unsubscribe();
            }
            showDepartures(true);
        }

    }


    public void showStationSearchDialog()
    {
        //initialize all of the objects from the layout
        final View dialogView = getLayoutInflater().inflate(R.layout.transport_nearby_search_dialog, null);
        final CheckedTextView firstChecked = (CheckedTextView) dialogView.findViewById(R.id.firstCheckedText);
        final CheckedTextView secondChecked = (CheckedTextView) dialogView.findViewById(R.id.secondCheckedText);
        final CheckedTextView thirdChecked = (CheckedTextView) dialogView.findViewById(R.id.thirdCheckedText);
        final CheckedTextView fourthChecked = (CheckedTextView) dialogView.findViewById(R.id.fourthCheckedText);
        searchTimeButton = (Button) dialogView.findViewById(R.id.searchTimeButton);
        final Button goButton = (Button) dialogView.findViewById(R.id.submitButton);
        // getSuggestionsAsync = new getSuggestionsAsyncAdapter(); //initialise the asynctask for the autocomplete
        stationSearch = (AutoCompleteTextView) dialogView.findViewById(R.id.stationSearchAutoComplete);
        stationSearch.setThreshold(1);  //show the autocomplete dropdown after 1 character
        getSuggestions();
        // getSuggestionsAsync.execute();  //fetch and attatch the autocomplete adapter
        getCurrentTime();
        searchTimeButton.setText("NOW");
        alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Search or select a station");
        alert.setView(dialogView);
        alert.setCancelable(true);
        alert.setButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //set the text on the checkfields to the value of the 5 nearest stations
        firstChecked.setText(stopsNearbyCurrentStation.get(0).getLocation_name());
        secondChecked.setText(stopsNearbyCurrentStation.get(1).getLocation_name());
        thirdChecked.setText(stopsNearbyCurrentStation.get(2).getLocation_name());
        fourthChecked.setText(stopsNearbyCurrentStation.get(3).getLocation_name());

        //check the boxes when one of the fields is touched
        if (stopsNearbyCurrentStation.get(0).getLocation_name().equals(departures.getCurrentDesiredStop().getLocation_name()))
        {
            firstChecked.setChecked(true);
        }
        else if (stopsNearbyCurrentStation.get(1).getLocation_name().equals(departures.getCurrentDesiredStop().getLocation_name()))
        {
            secondChecked.setChecked(true);
        }
        else if (stopsNearbyCurrentStation.get(2).getLocation_name().equals(departures.getCurrentDesiredStop().getLocation_name()))
        {
            thirdChecked.setChecked(true);
        }
        else if (stopsNearbyCurrentStation.get(3).getLocation_name().equals(departures.getCurrentDesiredStop().getLocation_name()))
        {
            fourthChecked.setChecked(true);
        }

        //listener for the event where the checktextfields are checked, sets the current desired stop to whatever object is checked
        View.OnClickListener checkedListener = new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Location location = new Location("Stop Location");

                firstChecked.setChecked(false);
                ((CheckedTextView) v).toggle();
                if (v.equals(firstChecked))
                {
                    departures.setCurrentDesiredStop(stopsNearbyCurrentStation.get(0)); //set the desired stop in the populator
                } else if (v.equals(secondChecked))
                {
                    departures.setCurrentDesiredStop(stopsNearbyCurrentStation.get(1));
                } else if (v.equals(thirdChecked))
                {
                    departures.setCurrentDesiredStop(stopsNearbyCurrentStation.get(2));
                } else if (v.equals(fourthChecked))
                {
                    departures.setCurrentDesiredStop(stopsNearbyCurrentStation.get(3));
                }
                location.setLatitude(departures.getCurrentDesiredStop().getLat());  //set the lat and long values to those of the desired stop
                location.setLongitude(departures.getCurrentDesiredStop().getLon());
                departures.setDesiredTransportLocation(location);   //update the departure populator with the desired location
                performFullRefresh = true;  //we now need a full refresh as we need to get the departure times from this station
                //new showTransportAsync().execute(); //run the async to get the departures and fill the listview
                showDepartures(true);
                actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + departures.getCurrentDesiredStop().getLocation_name() + "</font></b>"));

                alert.dismiss(); //now we can get rid of the timeSelectionDialog
            }
        };
        //set the listener we just created to the five checkfields
        firstChecked.setOnClickListener(checkedListener);
        secondChecked.setOnClickListener(checkedListener);
        thirdChecked.setOnClickListener(checkedListener);
        fourthChecked.setOnClickListener(checkedListener);
        stationSearch.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    searchButtonPressed();
                }
                return false;
            }
        });
        //handle the on click event for the "go" button
        goButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                selectedTime = new DateTime(currentTime);
                headerText = ("Departures from: " + getTime12Hr(selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour()));
                searchButtonPressed();
            }
        });
        searchTimeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                showTimePicker();
            }
        });
        alert.show();
    }

    private void searchButtonPressed()
    {

        if (this.stationSearch.getText().toString().contains("("))
        {
            if (!this.db.checkStationExists(this.transportType, this.stationSearch.getText().toString().substring(0,this.stationSearch.getText().toString().indexOf("(")))) //if the textfield is not entered from the suggestions
            {
                //build a new timeSelectionDialog to notify the user
                AlertDialog.Builder notifyInvalidStation = new AlertDialog.Builder(context);
                notifyInvalidStation.setTitle("Station not found");
                TextView failedNotification = new TextView(context);
                failedNotification.setText("Please try using the autocorrect suggestion");
                notifyInvalidStation.setView(failedNotification);
                notifyInvalidStation.setCancelable(true).
                        setPositiveButton("Ok", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                                dialog.dismiss();
                            }
                        });
                notifyInvalidStation.show();
            }
            else
            {
                searchForStationAdapter();
                //new searchForStationAsyncAdapter().execute(); //otherwise execute the asynctask to perform the search
                alert.dismiss();
            }
        }
        else
        {
            //build a new timeSelectionDialog to notify the user
            AlertDialog.Builder notifyInvalidStation = new AlertDialog.Builder(context);
            notifyInvalidStation.setTitle("Station not found");
            TextView failedNotification = new TextView(context);
            failedNotification.setText("Please try using the autocorrect suggestion");
            notifyInvalidStation.setView(failedNotification);
            notifyInvalidStation.setCancelable(true).
                    setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {

                            dialog.dismiss();
                        }
                    });
            notifyInvalidStation.show();
        }


    }

    public void showDepartures(final boolean showProgressDialog)
    {
        if (this.performFullRefresh && showProgressDialog)//if a full update is needed (if we need to reget data from the ptv api)
        {
            getCurrentTime();
            loaded = false;
            showLoadingListView();
        }

        this.showTransportSubscriber = new Subscriber<TransportDepartureListPopulator>() {
            @Override
            public void onNext(final TransportDepartureListPopulator departures)
            {
                actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + departures.getStationLocation() + "</font></b>"));
                if(!timeChosen)
                {
                    DateTime now = new DateTime();
                    headerText = "Departures from: " + getTime12Hr(now.getHourOfDay(), now.getMinuteOfHour());
                }
                DeparturesListView.setAdapter(new DepartureListAdapter(context, departuresList,null,headerText,false));   //populate the departures list view (set the adapter)
                registerForContextMenu(DeparturesListView);
                DeparturesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id)
                    {
//                    Object o = DeparturesListView.getItemAtPosition(position);
//                    DepartureListItem trainData = (DepartureListItem) o;
//                    Toast.makeText(ShowTransportNearby.this, "Selected :" + " " + trainData, Toast.LENGTH_SHORT).show();
                        Intent runActivity = new Intent(getApplicationContext(), ShowTransportRun.class);   //set the intent for transportRun class
                        runActivity.putExtra("Departure Object", departures.getlNextDepartures().get(position -1 )); //add the departure object to intent
                        runActivity.putExtra("Transport Type", transportType); //add the transportType
                        startActivity(runActivity);
//TODO use percelables instead of serializables for a speed increase


                    }
                });

                /*
                DeparturesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        final Intent intent = new Intent(context, ShowReminders.class);
                        DepartureListItem departureListItem = (DepartureListItem) DeparturesListView.getItemAtPosition(i);
                        saveReminder(departureListItem.getDepartureItem());
                        finish();
                        startActivity(intent);
                        return true;
                    }
                });
*/
//                if(showProgressDialog)
//                    try{
//                        if(progressDialog.isShowing()){
//                            progressDialog.dismiss();
//                        }
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    finally
//                    {
//                        progressDialog.dismiss();
//                    }
                if(timeChosen)
                    timeChosen = false;
            }

            @Override
            public void onCompleted() {
                /*
                if (selectedTime != null)
                    addDeparturesHeader("Arrives" + getTime12Hr(selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour()));
                else
                    addDeparturesHeader("Departures from: " + getTime12Hr(mCurrentHour,mCurrentMinute));
                    */
                loaded=true;
            }

            @Override
            public void onError(Throwable error) {}

        };

        //observable object will be executed in background thread.
        //afterwards, when the observable object has finished executed its task,
        // it will transfer the result of calculation to its subscriber in main thread and the subscriber will update the UI in onNext()
        getDepartureList().subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(showTransportSubscriber);
        if(showTransportSubscriber.isUnsubscribed())
        {
            Log.d("UNSUBSCRIBE", "showDepartures");
        }
    }

    public Observable<TransportDepartureListPopulator> getDepartureList()
    {
        return Observable.create(new Observable.OnSubscribe<TransportDepartureListPopulator>()
        {
            @Override
            public void call(Subscriber<? super TransportDepartureListPopulator> sub)
            {
                //If this is the first refresh then we always request from the api, and then turn the flag off
                if (performFullRefresh)
                {
                    performFullRefresh = false;
                    if (timeChosen)
                    {
                        departures.FindDepartureTimesAtTime(selectedTime.toDate());
                    }
                    else
                    {

                        departures.FindDepartureTimes();
                    }
                    stopsNearbyCurrentStation = departures.getStopsNearbyCurrentStation();//get the stops nearest to the current station for use later
                } else
                {
                    //if it isn't the first refresh, we execute this method to potentially save on net and battery resources
                    departures.updateTransportDepartures();
//                Log.d("Updating transport departures!", "updating transport");

                    View tempView;
                    for (int i = 0; i < DeparturesListView.getCount(); i++)
                    {
                        tempView = DeparturesListView.getChildAt(i);
                        if (((TextView)tempView.findViewById(R.id.departureTime)).getText().equals("NOW")
                                || ((TextView)tempView.findViewById(R.id.departureTime)).getText().equals("1 min"));
                        ((ImageView) tempView.findViewById(R.id.progressArrowsImageView)).setImageResource(R.drawable.ui_progress_arrows_grey);
                        ((TextView) tempView.findViewById(R.id.line)).setTextColor(Color.parseColor("#666666"));
                        ((TextView) tempView.findViewById(R.id.direction)).setTextColor(Color.parseColor("#666666"));
                        ((TextView) tempView.findViewById(R.id.departureTime)).setTextColor(Color.parseColor("#666666"));
                        tempView.setBackgroundColor(Color.parseColor("#FFFFFF"));

//                        icon.setImageResource(R.drawable.ui_progress_arrows_grey);
//                        holder.progressionArrowsImageView.setImageResource(R.drawable.ui_progress_arrows_grey);
//                        holder.lineNameView.setTextColor(Color.parseColor("#666666"));
//                        holder.platformNumberView.setTextColor(Color.parseColor("#666666"));
//                        holder.arrivalTimeView.setTextColor(Color.parseColor("#666666"));
//                        if (holder.destTimeView != null)
//                            holder.destTimeView.setTextColor(Color.parseColor("#666666"));
//                        convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                }

                departuresList = departures.getDeparturesList();
                try
                {
                    if (departures.getNearestStop() != null)
                    {
                        String lineString = "";
                        for (DepartureListItem aDeparturesList : departuresList)
                        {
                            //Gets the substring from index 0 to the index of the second hyphen
                            if (lineString.contains("-"))
                                lineString = aDeparturesList.getLineName().substring(0, (aDeparturesList.getLineName().indexOf("-", aDeparturesList.getLineName().indexOf("-") + 1)));
                            else
                                lineString = aDeparturesList.getLineName();
                            aDeparturesList.setLineName(lineString);
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.d("ChooseTime", e.toString());
                }
                sub.onNext(departures);
                sub.onCompleted();
            }
        });
    }


    public void getSuggestions()
    {
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onNext(final String result)
            {
                stationSearch.setAdapter(autoCompleteAdapter);  //set the autocomplete adapter to both departure and arrival
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}

        };

        Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> sub)
            {
                autoCompleteAdapter = db.getAutocompleteAdapter(TransportType.values()[transportType]); //get the right adapter for the transportmode in question
                sub.onNext("operation finished");
                sub.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }

    public void saveReminder(Departure departure, int minusTime)
    {
        Date tempDate = (Date) departure.getTime_timetable_utc().clone();
        tempDate.setMinutes(departure.getTime_timetable_utc().getMinutes() - minusTime);
        departure.setReminder(tempDate);
        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.addReminder(context, departure);

        // Create a new calendar set to the date chosen
        // we set the time to midnight (i.e. the first minute of that day)
        int minute = departure.getTime_timetable_utc().getMinutes() - minusTime;
        int hour = departure.getTime_timetable_utc().getHours();
        int day = departure.getTime_timetable_utc().getDate();
        int month = departure.getTime_timetable_utc().getMonth();
        int year = departure.getTime_timetable_utc().getYear() + 1900;

        Calendar calendar = Calendar.getInstance();
        //calendar.set(year, Calendar.SEPTEMBER, day, hour, minute);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);


        long remainingTime = calendar.getTimeInMillis();
        // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service

        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        Uri data = Uri.withAppendedPath(
                Uri.parse("myapp://myapp/Id/#"),
                String.valueOf(departure.getTime_timetable_utc().toString()));
        intentAlarm.setData(data);

        intentAlarm.putExtra("departure", departure);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) departure.getTime_timetable_utc().getTime(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, remainingTime, pendingIntent);
    }

    public void searchForStationAdapter()
    {
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onNext(String result)
            {
                performFullRefresh = true; //we now need to perform an api query to get the departure times, so we need a full refresh
                showDepartures(true);
                actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + departures.getCurrentDesiredStop().getLocation_name() + "</font></b>"));
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}
        };


        //observable object will be executed in background thread.
        //afterwards, when the observable object has finished executed its task,
        // it will transfer the result of calculation to its subscriber in main thread and the subscriber will update the UI in s()
        searchStation().subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }

    public Observable<String> searchStation()
    {
        return Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> sub)
            {
                //create a temporary stop and location
                Location changedStopLocation = new Location("desired location");
                Stop changedStop = null;
                String suburb = stationSearch.getText().toString().substring(stationSearch.getText().toString().indexOf("(") + 1,
                        stationSearch.getText().toString().indexOf(")"));
                String location = stationSearch.getText().toString().substring(0,stationSearch.getText().toString().indexOf("("));

                int changedStopId = db.getStopIDFromStationName(location,suburb, transportType);
                if (transportType == 0) //if user searches for a train grab the result and set it to the stop
                {
                    ArrayList<Stop> matchingStops = apiHandler.searchStop(stationSearch.getText().toString().substring(0,(stationSearch.getText().toString().indexOf("("))), transportType);
                    for (Stop s : matchingStops)
                    {
                        if(s.getStop_id() == changedStopId)
                        {
                            changedStop = s;
                            break;
                        }
                    }
                } else
                {
                    //otherwise, as we can't search for strings with slashes, we search for all values containing the first half of the
                    //tram/bus/nightrider stop, and then we iterate through these results and set the desired one to our temp stop
                    ArrayList<Stop> matchingStops = apiHandler.searchStop(stationSearch.getText().toString().substring(0, stationSearch.getText().toString().indexOf('/')), transportType);
                    for (Stop s : matchingStops)
                    {
                        if(s.getStop_id() == changedStopId)
                        {
                            changedStop = s;
                            break;
                        }
                    }
                }
                //we then set the desired stops and locations within the departure populator so it knows the new information to fetch
                departures.setCurrentDesiredStop(changedStop);
                changedStopLocation.setLatitude(changedStop.getLat());
                changedStopLocation.setLongitude(changedStop.getLon());
                departures.setDesiredTransportLocation(changedStopLocation);

                sub.onNext("operation finished");
                sub.onCompleted();
            }
        });
    }


    protected void getLocation()
    {
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("GOT GPS LOCATION","asd");

        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            if (currentLocation == null)
            {
                Log.d("GOT NETWORK LOCATION","asd");

                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
    }

    public void requestLocationUpdate()
    {
        Log.d("Requesting Location Update","asd");

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        locationManager.requestSingleUpdate(criteria, listener, null);
    }

    //on time set event listener
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                    getCurrentTime();
                    if (hourOfDay > mCurrentHour ||hourOfDay == mCurrentHour && min >= mCurrentMinute )
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        selectedTime = new DateTime(currentTime);
                        selectedTime = selectedTime.plusHours(hourOfDay - selectedTime.getHourOfDay()); //add the additional hours from the picker
                        selectedTime = selectedTime.plusMinutes(min - selectedTime.getMinuteOfHour()); //add the minutes
                        Log.d("SelectedTime = ",selectedTime.toString());
                        performFullRefresh = true; //we need to get values from the api again
                        timeChosen = true; //get the values at a given time instead of the current time
                        headerText = ("Departures from: " + getTime12Hr(selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour()));
//                    showDepartures(true); //run the async method
                        if (selectedTime.getMinuteOfHour() < 10)
                            searchTimeButton.setText(selectedTime.getHourOfDay() + ":0" + selectedTime.getMinuteOfHour());
                        else
                            searchTimeButton.setText(selectedTime.getHourOfDay() + ":" + selectedTime.getMinuteOfHour());
                    }
                    else
                    {
                        Toast.makeText(context,"Time must be in the future", Toast.LENGTH_SHORT).show(); //a time was selected that is in the past
                    }
                }
            };
    //displays a time selection dialog to allow users to select the time
    private void getCurrentTime()
    {
        currentTime = new DateTime();

        mCurrentHour = currentTime.getHourOfDay();
        mCurrentMinute = currentTime.getMinuteOfHour();
    }
    public void showTimePicker() //draws the time picker with the values set to the current time
    {
        getCurrentTime();
        timeSelectionDialog =  new TimePickerDialog(this,
                mTimeSetListener, mCurrentHour, mCurrentMinute, false);
        timeSelectionDialog.show();

    }
    public String getTime12Hr(int hour, int minute) //returns the time in the format hh:mm:pm in 12 hour time from 12 hour
    {
        if (hour <= 12)
        {
            if (minute < 10)
                return hour + ":0" + minute + "am";
            return hour + ":" + minute + "am";
        } else
        {
            if (minute < 10)
                return hour - 12 + ":0" + minute + "pm";
            return hour - 12 + ":" + minute + "pm";
        }
    }
    //    private void addDeparturesHeader(String title) //adds a header to the departureslistview with the given title
//    {
//        if (DeparturesListView.getHeaderViewsCount() > 0)
//            DeparturesListView.removeHeaderView(headerView);
//        headerView = getLayoutInflater().inflate(R.layout.departures_listview_header, null);
//        TextView headerText = (TextView) headerView.findViewById(R.id.departuresHeaderString);
//        headerText.setText(title);
//        DeparturesListView.addHeaderView(headerView);
//    }
    private void showLoadingListView()
    {
        ArrayList<DepartureListItem> tempList = new ArrayList<DepartureListItem>();
        DepartureListItem item = new DepartureListItem(null, false);
        tempList.add(item);
        DeparturesListView = (ListView) findViewById(R.id.transport_list);
        DeparturesListView.setAdapter(new DepartureListAdapter(context, tempList,null,headerText,true));
    }
}