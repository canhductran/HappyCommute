package gridstone.happycommute.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.transportRunAdapter.TransportRunAdapter;
import gridstone.happycommute.app.adapter.transportRunAdapter.TransportRunListItem;
import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.StoppingPattern;

/**
 * Created by Matt on 10/9/2014.
 * Lasted edited by Matt on 19/9/2014
 */
public class ShowTransportRun extends Activity
{
    private populateStopsFromStoppingPattern populateStops;
    private Integer transportType;
    private int runID;
    private int stopID;
    private Date departureDate;
    private String currentStationName;
    private APIHandler handler;
    private Context mContext;
    private ListView stopsListView;
    private Departure departure;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_run);
        //get the departure object and the transport type from the intent, then set the variables from these
        departure = (Departure) getIntent().getSerializableExtra("Departure Object");
        this.transportType = (Integer) getIntent().getExtras().getInt("Transport Type");
        stopID = departure.getPlatform().getStop().getStop_id();
        runID = departure.getRun().getRun_id();
        currentStationName = departure.getPlatform().getStop().getLocation_name();
        departureDate = departure.getTime_timetable_utc();

        stopsListView = (ListView) findViewById(R.id.runListView);
        mContext = this;
        handler = new APIHandler();
        populateStops = new populateStopsFromStoppingPattern();
        populateStops.execute();
    }


    private class populateStopsFromStoppingPattern extends AsyncTask<Void, Void, ArrayList<StoppingPattern>>
    {
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(ShowTransportRun.this);  //configure and show a progressdialog while we make the requests
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Loading Departures...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        protected ArrayList<StoppingPattern> doInBackground(Void... params)
        {
            //get the stopping pattern objects from the api
            ArrayList<StoppingPattern> stoppingPattern = handler.getStoppingPattern(transportType, runID, stopID, departureDate);
            return stoppingPattern;
        }


        protected void onPostExecute(ArrayList<StoppingPattern> stoppingPattern)
        {
            progressDialog.dismiss();
            Integer currentStationIndex = 0;
            ArrayList<TransportRunListItem> runListItems = new ArrayList();
            TransportRunListItem runItem;

            //for every location in the stoppingpatterns add this to the arraylist of transportRunListItems
            for (StoppingPattern s : stoppingPattern)
            {
                runItem = new TransportRunListItem();
                runItem.setStationName(s.getPlatform().getStop().getLocation_name());
                runItem.setStationDepartureTime(convertTimeToString(calculateDepartureTime(s.getTime_timetable_utc())));
                if (runItem.getStationName().equals(currentStationName))
                {
                    currentStationIndex = runListItems.size();
                }
                runListItems.add(runItem);
            }
//            stopsListView.setDivider(null); //hide the row dividing line
            stopsListView.setAdapter(new TransportRunAdapter(mContext, runListItems, currentStationIndex)); //set the custom adapter

            stopsListView.setSelection(currentStationIndex); // center the listview with the current station at the top
        }

        public ArrayList<String> calculateDepartureTime(Date departureDateTime)
        {
            Integer departHour, departMinute, currentHour, currentMinute, calculatedHour, calculatedMinute;
            ArrayList<String> DepartureArray = new ArrayList();
            Date currentDateTime = new Date();
            String departureTime = departureDateTime.toString().substring(11, 16);
//        Tue Jun 10 23:17:00 AEST 2014
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss d/M/yyyy");
            TimeZone tz = TimeZone.getTimeZone("GMT+10:00");
            dateFormat.setTimeZone(tz);
            //Sets the current time and date based on the device
            dateFormat.format(currentDateTime);
            dateFormat.format(departureDateTime);
            try
            {
                currentDateTime = dateFormat.parse(currentDateTime.toString());
                departureDateTime = dateFormat.parse(departureDateTime.toString());
            }
            catch (Exception e)
            {
                //
            }
//        currentDate = currentDateTime.substring(8, 16);
            currentHour = Integer.parseInt(currentDateTime.toString().substring(11, 13));
            currentMinute = Integer.parseInt(currentDateTime.toString().substring(14, 16));
            //Breaks up the DepartureTime into Hours and Minutes
            departHour = Integer.parseInt(departureTime.substring(0, 2));
            departMinute = Integer.parseInt(departureTime.substring(3, 5));
//                                                                                    JUN 11 2014
//                                                                                    11/Jun/2014
            // If the departure hour is a day in the future, add a day to the departure time to get the accurate time
            if ((currentHour > 12 && currentHour < 24) && departHour <= 12)
            {
                departHour += 24;
            }
            //Calculates how long in hours and minutes until the soonest departure. Normally this will simply be a case of minutes.
            calculatedHour = departHour - currentHour;

            calculatedMinute = departMinute - currentMinute;

            //If the departure is over an hour away, the departureminute may be > currentminute, so we account for this and subtract one from the hour if this is the case
            if (calculatedMinute < 0)
            {
                calculatedMinute = 60 + calculatedMinute;
                calculatedHour--;
            }

            //Add and return the values, if the minutes are less than 10 and there is more than 1 hour until
            // departure then fill the string with a "0" buffer so it displays correctly
            if (calculatedMinute < 10 && calculatedMinute > 0 && calculatedHour != 0)
            {
                DepartureArray.add("0" + calculatedMinute.toString());
            } else
            {
                DepartureArray.add(calculatedMinute.toString());
            }
            DepartureArray.add(calculatedHour.toString());
            //Calculates the days between the departure and current date, primarily useful for nightriders that don't run every day

            DateTime startDate = new DateTime(currentDateTime);
            DateTime endDate = new DateTime(departureDateTime);

            Days d = Days.daysBetween(startDate, endDate);
            int days = d.getDays();
//        days--;
            DepartureArray.add(Integer.toString(days));
            return DepartureArray;
        }

        public String convertTimeToString(ArrayList<String> arrivalTimesList)
        {
            String time = "";
            if (Integer.parseInt(arrivalTimesList.get(2)) < 0 || Integer.parseInt(arrivalTimesList.get(1)) < 0 || Integer.parseInt(arrivalTimesList.get(0)) < 0)
                return "Passed";
            if (Integer.parseInt(arrivalTimesList.get(2)) >= 1)
            {
                if (Integer.parseInt(arrivalTimesList.get(2)) == 1)
                {
                    time = (arrivalTimesList.get(2) + " day"); //set 1 day or
                } else if (Integer.parseInt(arrivalTimesList.get(2)) >= 2)
                {
                    time = (arrivalTimesList.get(2) + " days"); //xx days
                }
            }
            //else if less than one day, less than one hour
            else if (Integer.parseInt(arrivalTimesList.get(2)) < 1 &&
                    Integer.parseInt(arrivalTimesList.get(1)) == 0 &&
                    Integer.parseInt(arrivalTimesList.get(0)) < 60)
            {
                if (Integer.parseInt(arrivalTimesList.get(0)) == 1)
                    return "1 min";
                time = (arrivalTimesList.get(0) + " mins"); //xx minutes
            }
            //else if less than one day, more than one hour
            else if (Integer.parseInt(arrivalTimesList.get(1)) > 0 &&
                    Integer.parseInt(arrivalTimesList.get(2)) < 1 &&
                    Integer.parseInt(arrivalTimesList.get(0)) == 0)
            {
                //if 1 hour exactly
                if (Integer.parseInt(arrivalTimesList.get(1)) == 1 &&
                        Integer.parseInt(arrivalTimesList.get(0)) == 0 &&
                        Integer.parseInt(arrivalTimesList.get(2)) < 1)
                {
                    time = (arrivalTimesList.get(1) + " hour");
                }
                //if more than one hour exactly
                else if (Integer.parseInt(arrivalTimesList.get(1)) > 1 &&
                        Integer.parseInt(arrivalTimesList.get(0)) == 0 &&
                        Integer.parseInt(arrivalTimesList.get(2)) < 1)
                {
                    time = (arrivalTimesList.get(1) + " hours");
                }
            } else
            {
                time = (arrivalTimesList.get(1) + ":" + arrivalTimesList.get(0) + " hours");
            }
            if ((Integer.parseInt(arrivalTimesList.get(2)) == 0 && Integer.parseInt(arrivalTimesList.get(1)) == 0 && Integer.parseInt(arrivalTimesList.get(0)) == 0))
            {
                time = "NOW";
            }

            return time;
        }

    }
}