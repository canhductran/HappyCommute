package gridstone.happycommute.app.adapter.reminderAdapter;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.ArrayList;

import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListItem;
import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.FavoriteJourney;
import gridstone.happycommute.app.model.Stop;

/**
 * Created by CHRIS on 28/09/2014.
 */
public class ReminderListPopulator
{
    private LocationManager fLocationManager;
    private Location desiredTransportLocation;//will be set to current location by default
    private APIHandler fAPIHandler;
    private ArrayList<DepartureListItem> departuresList;
    private Stop currentDesiredStop;
    private ArrayList<Stop> stopsNearbyCurrentStation;
    private String stationLocation = "";
    private int transportType;
    private boolean updateRequired = true;


    private ArrayList<Departure> reminders;

    public ReminderListPopulator(ArrayList<Departure> reminders)
    {
        this.reminders = reminders;
        departuresList = populateReminders(reminders);
    }

    //Populates the arraylist with the ammount of values specified in the constructor
    private ArrayList<DepartureListItem> populateReminders(ArrayList<Departure> remindersList)
    {
        //For every row we want to populate, iterate through and get the relevent values from the relevent ArrayLists
        ArrayList<DepartureListItem> populatedList = new ArrayList<DepartureListItem>();
        DepartureListItem departureItem;
        for (Departure aDepartureList : remindersList)
        {
            departureItem = new DepartureListItem(aDepartureList, true);
            populatedList.add(departureItem);
        }
        return populatedList;
    }

    public ArrayList<DepartureListItem> getDeparturesList()
    {
        return departuresList;
    }
}
