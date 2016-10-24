package gridstone.happycommute.app.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListAdapter;
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListItem;
import gridstone.happycommute.app.adapter.departuresAdapter.TransportDepartureListPopulator;
import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.database.DatabaseHelper;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Line;
import gridstone.happycommute.app.model.Stop;
import gridstone.happycommute.app.model.StoppingPattern;
import gridstone.happycommute.app.model.TransportType;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Matt  on 8/05/2014.
 * Last edited by Matt on 21/10/2014
 */
public class JourneyPlanner extends ActionBarActivity
{
    private final Context context = this;
    private ActionBar actionBar;
    private DatabaseHelper db;
    private ArrayAdapter<String> autoCompleteAdapter;
    private AutoCompleteTextView departureTextView, arrivalTextView;
    private APIHandler apiHandler;
    private TransportDepartureListPopulator departures;
    private ProgressDialog progressDialog;
    private Location originLocation = null, arrivalLocation = null;
    private ListView DeparturesListView;
    private ArrayList<Departure> originDepartures, arrivalDepartures, foundDepartures;
    private ArrayList<Line> originLines, arrivalLines;
    private Date originTime, midPointArrivalTime,finalArrivalTime;
    private Stop connectingStop = null;
    private ArrayList<DepartureListItem> departuresList = new ArrayList<DepartureListItem>();
    private ArrayList<Departure> connectingStopDepartures = new ArrayList<Departure>();
    private ArrayList<Departure> subsequentDepartures = new ArrayList<Departure>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.apiHandler = new APIHandler();
        this.db = new DatabaseHelper(context);
        this.foundDepartures = new ArrayList<Departure>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_planner); //set the layout
        this.actionBar = getSupportActionBar();
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + "Train Journey Planner" + "</font></b>"));
        this.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));
        this.departures = new TransportDepartureListPopulator(context, 0, null); //allocate a new departures populator
        this.DeparturesListView = (ListView) findViewById(R.id.train_list);

        DeparturesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
//                    Object o = DeparturesListView.getItemAtPosition(position);
//                    DepartureListItem trainData = (DepartureListItem) o;
//                    Toast.makeText(ShowTransportNearby.this, "Selected :" + " " + trainData, Toast.LENGTH_SHORT).show();
                Intent runActivity = new Intent(getApplicationContext(), ShowTransportRun.class);   //set the intent for transportRun class
                runActivity.putExtra("Departure Object", foundDepartures.get(position -1)); //add the departure object to intent
                runActivity.putExtra("Transport Type", 0); //add the transportType
                startActivity(runActivity);
//TODO use percelables instead of serializables for a speed increase
            }
        });
        showStationSearchDialog();

    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.journey_planner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item.getItemId() == R.id.station_search)
        {
            showStationSearchDialog();//If the station seached button is touched, call the method to show the timeSelectionDialog
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showStationSearchDialog()
    {
        //declare all of the views, buttons, and autocompletetextviews needed
        final View addView = getLayoutInflater().inflate(R.layout.journey_selection_dialog, null);

        this.departureTextView = (AutoCompleteTextView) addView.findViewById(R.id.autoCompleteDeparture);
        this.arrivalTextView = (AutoCompleteTextView) addView.findViewById(R.id.autoCompleteArrival);

        //set the adapter here for the default state before a selection has been made
        getSuggestion();
        this.departureTextView.setAdapter(this.autoCompleteAdapter);
        this.arrivalTextView.setAdapter(this.autoCompleteAdapter);

        this.departureTextView.requestFocus();   //set focus on the departureTextView field
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);  //show the keyboard
        this.departureTextView.setThreshold(0);  //show the autocorrect suggestions when >0 characters are entered
        this.arrivalTextView.setThreshold(0);

        //when switching between text fields
        this.departureTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    departureTextView.showDropDown();
                    autoCompleteAdapter.notifyDataSetChanged();

                }
            }
        });

        this.arrivalTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    arrivalTextView.showDropDown();
                    autoCompleteAdapter.notifyDataSetChanged();
                }
            }
        });

        arrivalTextView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    savePressed();
                    Toast.makeText(context,"asd",Toast.LENGTH_SHORT);
                }
                return false;
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Plan a train trip").setView(addView);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                savePressed();
                dialog.dismiss();
                Toast.makeText(context,"asd",Toast.LENGTH_SHORT);
            }
        }).setNegativeButton("Cancel", null).show();
    }

    public void Query()
    {
        this.departures.setDesiredTransportLocation(originLocation);
        this.departures.FindDepartureTimes();
        this.originDepartures = departures.getlNextDepartures();

        this.departures.setDesiredTransportLocation(arrivalLocation);
        this.departures.setCurrentDesiredStop(null);
        this.departures.FindDepartureTimes();
        this.arrivalDepartures = departures.getlNextDepartures();

        this.arrivalLines = getUniqueTrainLines(this.arrivalDepartures);
        originLines = getUniqueTrainLines(originDepartures);
        Line connectedLine = checkDirectConnection(arrivalLines, originLines);
        if (connectedLine != null)
        {
            findConnectingStop();
            populateList();
        } else
        {
            findConnectingStop();
            findSecondaryDeparture();
            Log.d("Origin:", originDepartures.get(0).getPlatform().getStop().getLocation_name());
            Log.d("Connecting stop ", connectingStop.getLocation_name());
            Log.d("Destination:", arrivalDepartures.get(0).getPlatform().getStop().getLocation_name());
            Log.d("First Departure Leg", foundDepartures.get(0).getTime_timetable_utc() + " arrives " + midPointArrivalTime.toString());
            Log.d("Secondary Departure Leg",foundDepartures.get(1).getPlatform().getDirection().getDirection_name() + " " + foundDepartures.get(1).getTime_timetable_utc() + " arrives " + finalArrivalTime.toString());
            populateList();
            Log.d("BETWEEN", "BETWEEN");
            getAdditionalDepartures();
            Log.d("FINISH", "FINISH");
        }

    }

    public Line checkDirectConnection(ArrayList<Line> lineList1, ArrayList<Line> lineList2)
    {
        for (Line l : lineList1)
        {
            for (Line li : lineList2)
            {
                if (li.getLine_name().equals(l.getLine_name()))
                    return li;
            }
        }
        return null;
    }
    public void findConnectingStop()
    {
        ArrayList<Departure> stopDepartures = null;
        ArrayList<Line> stopLines = null;
        ArrayList<StoppingPattern> stoppingPattern;
        boolean connected = false;

        for (Departure d : originDepartures)
        {
            originTime = d.getTime_timetable_utc();
            stoppingPattern = apiHandler.getStoppingPattern(0, d.getRun().getRun_id(), d.getPlatform().getStop().getStop_id(), d.getTime_timetable_utc());
            for (StoppingPattern s : stoppingPattern)
            {
                if (compareDateHours(s.getTime_timetable_utc(), originTime) > 0)
                {
                    if (!connected)
                    {
                        stopDepartures = apiHandler.getBroadNextDepartures(s.getPlatform().getStop().getStop_id(), 30, 0, false);
                        stopLines = getUniqueTrainLines(stopDepartures);
                        if (!connected)
                        {
                            for (Line l : stopLines)
                            {
                                if (!connected)
                                    for (Line li : arrivalLines)
                                    {
                                        if (li.getLine_name().equals(l.getLine_name()) && compareDateHours(s.getTime_timetable_utc(),originTime) > 0)
                                        {
                                            compareDateHours(s.getTime_timetable_utc(),originTime);
                                            connectingStop = s.getPlatform().getStop();
                                            foundDepartures.add(d);
                                            midPointArrivalTime = s.getTime_timetable_utc();
                                            connected = true;
                                        }
                                    }
                            }
                        } else
                        {
                            return;
                        }


                        if (connected)
                            return;
                    } else
                        return;
                }
            }
            if (connected)
                return;
        }
    }
    public void findSecondaryDeparture()
    {
        //TODO use a temp stop, iterate through all lines and udpate the stop if the location departure time is sooner, the nreturn ath the en
        connectingStopDepartures =apiHandler.getBroadNextDepartures(connectingStop.getStop_id(), 50, 0, false);
        boolean foundDeparture = false;
        ArrayList<StoppingPattern> stoppingPattern;

        for (Departure d: connectingStopDepartures)
        {
            if (foundDeparture)
                break;
            for (Line l : arrivalLines)
            {
                if (d.getPlatform().getDirection().getLine().getLine_name().equals(l.getLine_name())&& compareDateHours(d.getTime_timetable_utc(),midPointArrivalTime) > 0)
                {
                    stoppingPattern = apiHandler.getStoppingPattern(0, d.getRun().getRun_id(), d.getPlatform().getStop().getStop_id(), d.getTime_timetable_utc());
                    for (StoppingPattern s : stoppingPattern)
                    {
                        if (s.getPlatform().getStop().getLocation_name().equals(arrivalTextView.getText().toString().substring(0,this.arrivalTextView.getText().toString().indexOf("("))) && compareDateHours(s.getTime_timetable_utc(),midPointArrivalTime) > 0)
                        {
                            finalArrivalTime = s.getTime_timetable_utc();
                            foundDepartures.add(d);
                            foundDeparture = true;
                        }
                    }
                }
            }

        }


    }

    public ArrayList<Line>getUniqueTrainLines(ArrayList<Departure> departureArrayList)
    {
        ArrayList<Line> lineList = new ArrayList<Line>();
        Line currentLine;
        boolean exists = false;
        for (Departure d : departureArrayList)
        {
            currentLine = d.getPlatform().getDirection().getLine();
            exists = false;
            for (Line l: lineList)
            {
                if (l.getLine_name().equals(currentLine.getLine_name()))
                    exists = true;
            }
            if (!exists)
                lineList.add(currentLine);
        }
        return lineList;
    }

    private void savePressed()
    {
        resetFields();
//        if (departuresList.size() > 0)
//            departuresList= new ArrayList<DepartureListItem>();

        // if the stations read in from the autocomplete fields both don't exist
        if (this.departureTextView.getText().toString().contains("(") &&
            this.arrivalTextView.getText().toString().contains("("))
        {
            if (!db.checkStationExists(0, departureTextView.getText().toString().substring(0, departureTextView.getText().toString().indexOf("("))) || !db.checkStationExists(0, arrivalTextView.getText().toString().substring(0, arrivalTextView.getText().toString().indexOf("("))))
            {
                //build a new timeSelectionDialog to notify the user
                AlertDialog.Builder notifyFailureToSave = new AlertDialog.Builder(JourneyPlanner.this);
                notifyFailureToSave.setTitle("Station not found");
                TextView failedNotification = new TextView(JourneyPlanner.this);
                failedNotification.setText("Please try using the auto correct suggestion");
                notifyFailureToSave.setView(failedNotification);
                notifyFailureToSave.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //hide the keyboard when we dismiss the timeSelectionDialog
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                });
                notifyFailureToSave.show();
            } else //otherwise the stations are valid
            {
                originLocation = db.getLocationFromStopName(departureTextView.getText().toString().substring(0, this.departureTextView.getText().toString().indexOf("(")), 0);
                arrivalLocation = db.getLocationFromStopName(arrivalTextView.getText().toString().substring(0, this.arrivalTextView.getText().toString().indexOf("(")).toString(), 0);
                ArrayList<Location> LocationList = new ArrayList();
                LocationList.add(originLocation);
                LocationList.add(arrivalLocation);
                queryJourneyPlanner();
            }
        }
        else
        {
            //build a new timeSelectionDialog to notify the user
            AlertDialog.Builder notifyFailureToSave = new AlertDialog.Builder(JourneyPlanner.this);
            notifyFailureToSave.setTitle("Station not found");
            TextView failedNotification = new TextView(JourneyPlanner.this);
            failedNotification.setText("Please try using the auto correct suggestion");
            notifyFailureToSave.setView(failedNotification);
            notifyFailureToSave.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //hide the keyboard when we dismiss the timeSelectionDialog
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);
                    dialog.dismiss();
                }
            });
            notifyFailureToSave.show();
        }

    }

    public void queryJourneyPlanner()
    {
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMessage("Finding journey...");
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setProgress(0);
        this.progressDialog.show();

        Subscriber<String> subscriber = new Subscriber<String>()
        {
            @Override
            public void onNext(String result) {
                progressDialog.dismiss();
                Toast.makeText(context,"SUCCESS", Toast.LENGTH_SHORT);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);
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
                Log.d("lelstart", "lelstart");
                Query(); //get the right adapter for the transportmode in question
                sub.onNext("operation finished");
                sub.onCompleted();
                Log.d("lel", "lel");
            }
        }).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }


    public void getSuggestion()
    {
        Subscriber<String> subscriber = new Subscriber<String>()
        {
            @Override
            public void onNext(String result)
            {
                departureTextView.setAdapter(autoCompleteAdapter);  //set the autocomplete adapter to both departure and arrival
                arrivalTextView.setAdapter(autoCompleteAdapter);
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
                autoCompleteAdapter = db.getAutocompleteAdapter(TransportType.TRAIN); //get the right adapter for the transportmode in question
                sub.onNext("operation finished");
                sub.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }

    public void populateList()
    {
        Subscriber<Object> subscriber = new Subscriber<Object>()
        {
            @Override
            public void onNext(Object arrivalTimes)
            {
                DeparturesListView.setAdapter(new DepartureListAdapter(context, departuresList, (ArrayList<String>) arrivalTimes,(departureTextView.getText().toString().substring(0,departureTextView.getText().toString().indexOf("(")) + " to "  + (arrivalTextView.getText().toString().substring(0,arrivalTextView.getText().toString().indexOf("("))) + " with " + String.valueOf(foundDepartures.size() -1) + " exchange(s)"), false));
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}
        };

        getDepartureList().subscribeOn(Schedulers.newThread()).
            observeOn(AndroidSchedulers.mainThread()).
            subscribe(subscriber);
    }

    public Observable<Object> getDepartureList()
    {
        return Observable.create(new Observable.OnSubscribe<Object>()
        {
            @Override
            public void call(Subscriber<? super Object> sub)
            {
                for (Departure d : foundDepartures)
                {
                    departuresList.add(new DepartureListItem(d, false));
                }
                departuresList.get(departuresList.size() - 1).setTransportDirection("To: " + arrivalTextView.getText().toString().substring(0, arrivalTextView.getText().toString().indexOf("(")));


                ArrayList<String> arrivalTimes = new ArrayList<String>();

                arrivalTimes.add("Arrives: " + midPointArrivalTime.toString().substring(11, 16));
                if (finalArrivalTime != null)
                    arrivalTimes.add("Arrives: " + finalArrivalTime.toString().substring(11, 16));
                else
                    arrivalTimes.add("Arrives: ??????????? ");


                sub.onNext(arrivalTimes);
                sub.onCompleted();
            }
        });



    }
    //TODO: make this into a util class because i use it in several places
    public int compareDateHours(Date date1,Date date2) //Note: this function is necessary because sometimes the api likes to think dates that are supposedly a day in the past.
    // As trains run daily, we simply ignore the date all together and check whether the dates are different in terms of hours and minutes.
    {
        DateTime date1Time = new DateTime(date1);
        DateTime date2Time = new DateTime(date2);

        int Comparison =
                (((date1Time.getHourOfDay() * 60) + date1Time.getMinuteOfHour())  -
                        ((date2Time.getHourOfDay() * 60) + date2Time.getMinuteOfHour())); //if return is > 0 date1 is in the future of date2, otherwise vice versa.

        return Comparison;
    }
    private void resetFields()
    {

        try
        {
            originLocation = null;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            arrivalLocation = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            originDepartures.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            arrivalDepartures.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            foundDepartures.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        try
        {
            originLines.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            arrivalLines.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            originTime = null;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            midPointArrivalTime = null;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            finalArrivalTime = null;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            connectingStop = null;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            departuresList.clear();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            departures = new TransportDepartureListPopulator(context,0, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }
    private void getAdditionalDepartures()
    {
        Log.d("Specific next departure for clifton hill","-----------------");
        for (Line l : originLines)
        {
            ArrayList<Departure> lineDepartrues = apiHandler.getNextSpecificDepartures(foundDepartures.get(0).getPlatform().getStop().getStop_id(), l.getLine_id(), getConnectingLineDirection(l.getLine_name()), 10, 0);
            for (Departure d: lineDepartrues)
            {
                subsequentDepartures.add(d);
            }
        }

        for (Line l : arrivalLines)
        {
            ArrayList<Departure> lineDepartrues = apiHandler.getNextSpecificDepartures(foundDepartures.get(1).getPlatform().getStop().getStop_id(), l.getLine_id(), getConnectingLineDirection(l.getLine_name()), 10, 0);
            for (Departure d: lineDepartrues)
            {
                subsequentDepartures.add(d);
            }
        }
    }
    private int getConnectingLineDirection(String lineName)
    {
        for (Departure d: connectingStopDepartures)
        {
            if (d.getPlatform().getDirection().getLine().getLine_name().equals(lineName))
                return d.getPlatform().getDirection().getDirection_id();
        }
        return -1;
    }
}
