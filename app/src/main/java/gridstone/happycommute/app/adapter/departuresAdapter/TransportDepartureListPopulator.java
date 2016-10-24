package gridstone.happycommute.app.adapter.departuresAdapter;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Stop;
import gridstone.happycommute.app.model.StoppingPattern;

/**
 * Created by Matt on 6/6/2014.
 * Last modified 9/9 by Matt
 */
public class TransportDepartureListPopulator
{
    private final Context context;
    private LocationManager fLocationManager;
    private Location desiredTransportLocation;//will be set to current location by default
    private APIHandler fAPIHandler;
    private ArrayList<DepartureListItem> departuresList;
    private Stop currentDesiredStop;
    private ArrayList<Stop> stopsNearbyCurrentStation;
    private String stationLocation = "";
    private int transportType;
    private boolean updateRequired = true;
    private ArrayList<Departure> lNextDepartures;

    public TransportDepartureListPopulator(Context aContext, int aType, Stop aStop)
    {
        context = aContext;
        transportType = aType;
        if (aStop != null)
        {
            setCurrentDesiredStop(aStop);
        }
    }
    public void FindDepartureTimesAtTime(Date time)
    {
        departuresList = new ArrayList<DepartureListItem>();
        updateRequired = false;
        if (getCurrentDesiredStop() == null)
        {
            fAPIHandler = new APIHandler();
            if (desiredTransportLocation == null)
            {
                getLocation(); //if the location isn't set specifically then we use the current area
            }

            setCurrentDesiredStop(getStopNearby(getDesiredTransportLocation()));
            //also get the list of stops nearby
            stopsNearbyCurrentStation = fAPIHandler.getSpecifiedNearestStops(getDesiredTransportLocation().getLatitude(), getDesiredTransportLocation().getLongitude(), transportType, 6);
        }

        lNextDepartures = getNextDeparturesAtTimeFromAPI(time);
        departuresList = populateDepartures(lNextDepartures, 2);

        if (getCurrentDesiredStop() != null)
        {
            stationLocation = getCurrentDesiredStop().getLocation_name();
        }
    }
    public void FindDepartureTimes()
    {
        departuresList = new ArrayList<DepartureListItem>();
        updateRequired = false;
        if (getCurrentDesiredStop() == null)
        {
            fAPIHandler = new APIHandler();
            if (desiredTransportLocation == null)
            {
                getLocation(); //if the location isn't set specifically then we use the current area
            }

            setCurrentDesiredStop(getStopNearby(getDesiredTransportLocation()));
            //also get the list of stops nearby
            stopsNearbyCurrentStation = fAPIHandler.getSpecifiedNearestStops(getDesiredTransportLocation().getLatitude(), getDesiredTransportLocation().getLongitude(), transportType, 6);
        }

        lNextDepartures = getNextDeparturesFromAPI();
        departuresList = populateDepartures(lNextDepartures, 2);

        if (getCurrentDesiredStop() != null)
        {
            stationLocation = getCurrentDesiredStop().getLocation_name();
        }
    }

    protected void getLocation()
    {
        fLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (fLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            setDesiredTransportLocation(fLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
        if (fLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            if (getDesiredTransportLocation() == null)
            {
                setDesiredTransportLocation(fLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            }
        }
    }

    protected Stop getStopNearby(Location location)
    {
        return fAPIHandler.getStopNearBy(location.getLatitude(), location.getLongitude(), transportType);
    }

    protected ArrayList<Departure> getNextDeparturesFromAPI()
    {
//        if (currentDesiredStop == null)
//        {
//            return fAPIHandler.getNextDepartures(currentDesiredStop.getStop_id(), 10, transportType);
//        } else if (currentDesiredStop != null)
//        {

//        } else
//        {
//            return null;
//        }
        return fAPIHandler.getBroadNextDepartures(getCurrentDesiredStop().getStop_id(), 10, transportType, true);

    }
    protected ArrayList<Departure> getNextDeparturesAtTimeFromAPI(Date time)
    {
        return fAPIHandler.getBroadNextDeparturesAtTime(getCurrentDesiredStop().getStop_id(), 10, transportType, true, time);

    }


    //Populates the arraylist with the ammount of values specified in the constructor
    private ArrayList<DepartureListItem> populateDepartures(ArrayList<Departure> departureList, int listItemCount)
    {
        if (getCurrentDesiredStop() != null) //if the stop can be returned
        {
            //For every row we want to populate, iterate through and get the relevent values from the relevent ArrayLists
            ArrayList<DepartureListItem> populatedList = new ArrayList<DepartureListItem>();
            DepartureListItem departureItem;
            ArrayList<StoppingPattern> pattern = null;
            for (Departure aDeparture : departureList)
            {
                departureItem = new DepartureListItem(aDeparture, false);
//                if (aDepartureList.getDestinationArrivalDate() == null)
//                {
//                    pattern = fAPIHandler.getStoppingPattern(transportType, aDepartureList.getRun().getRun_id(), aDepartureList.getPlatform().getStop().getStop_id(), null);
//                    aDepartureList.setDestinationArrivalDate(pattern.get(pattern.size() - 1).getTime_timetable_utc());
//                }
                    departureItem.setDestTime(aDeparture.getTime_timetable_utc());

                populatedList.add(departureItem);
            }
            return populatedList;
        } else
        {
            return null; //If the stop isn't returned by the API call
        }
    }

    //Calculate the time to departure based on the difference between the departuretime and the current time
    //This will return a dummy location at the latitude and longtitude passed to it
    public Location getMockLocation(double lat, double lng, float accuracy)
    {
        String PROVIDER = "flp";
    /*
     * From input arguments, create a single Location with provider set to
     * "flp"
     */

        // Create a new Location
        Location newLocation = new Location(PROVIDER);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAccuracy(accuracy);
        return newLocation;
    }


    public ArrayList<DepartureListItem> getDeparturesList()
    {
        return departuresList;
    }

    public Stop getNearestStop()
    {
        return getCurrentDesiredStop();
    }

    public String getStationLocation()
    {
        return stationLocation;
    }

    //for testing purposes only, this queries all stations in victoria for a given transport type
    public void ptvGetPOIVIC()
    {
        fAPIHandler.getTransportPOI(140.908931, -34.040211, 151.323970, -39.458526, 2, 0, 999999);
    }

    public void updateTransportDepartures()
    {
        if (updateRequired)
        {
            FindDepartureTimes();
            updateRequired = false;
        } else
        {
            for (DepartureListItem transportDepartureItem : departuresList)
            {
                if (transportDepartureItem.updateDepartureTime())
                {
                    updateRequired = true;
                }

            }

        }
    }

    public ArrayList<Stop> getStopsNearbyCurrentStation()
    {
        return stopsNearbyCurrentStation;
    }

    public Location getDesiredTransportLocation()
    {
        return desiredTransportLocation;
    }

    public void setDesiredTransportLocation(Location desiredTransportLocation)
    {
        this.desiredTransportLocation = desiredTransportLocation;
    }

    public Stop getCurrentDesiredStop()
    {
        return currentDesiredStop;
    }

    public void setCurrentDesiredStop(Stop currentDesiredStop)
    {
        this.currentDesiredStop = currentDesiredStop;
    }

    public ArrayList<Departure> getlNextDepartures()
    {
        return lNextDepartures;
    }

}