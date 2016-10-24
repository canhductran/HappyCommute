package gridstone.happycommute.app.adapter.favouriteAdapter;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;

import gridstone.happycommute.app.favourites.FavoriteManipulater;
import gridstone.happycommute.app.model.FavoriteJourney;
import gridstone.happycommute.app.model.FavouritePattern;
import gridstone.happycommute.app.model.StoppingPattern;

/**
 * Created by CHRIS on 27/08/2014.
 * Last edited by Matt on 31/8/2014
 * edited by Chris on 14/9/2014
 */
public class FavouriteListPopulator
{
    private boolean firstLoad;
    private FavoriteManipulater favoriteManipulater;
    private int mode;
    private ArrayList<FavouriteListItem> favouriteList;
    private ArrayList<FavoriteJourney> favoriteJourneys;
    private ArrayList<FavouritePattern> favoritePatterns;
    private boolean updateRequired = false;

    public FavouriteListPopulator(Context context, int mode, ArrayList<FavoriteJourney> favoriteJourneys, boolean firstLoad)
    {
        this.favoriteJourneys = favoriteJourneys;
        this.mode = mode;
        this.firstLoad = firstLoad;
        FindFavourites();
    }

    public void FindFavourites()
    {
        this.favouriteList = new ArrayList<FavouriteListItem>();
        this.favoritePatterns = new ArrayList<FavouritePattern>();

        int type = 0;
        this.updateRequired = false;
        if (!favoriteJourneys.isEmpty())
        {
            for (int i = 0; i < favoriteJourneys.size(); i++)
            {
                if (favoriteJourneys.get(i).getArrival().getTransport_type().toUpperCase().equals("TRAIN"))
                {
                    type = 0;
                } else if (favoriteJourneys.get(i).getArrival().getTransport_type().toUpperCase().equals("TRAM"))
                {
                    type = 1;
                } else if (favoriteJourneys.get(i).getArrival().getTransport_type().toUpperCase().equals("BUS"))
                {
                    type = 2;
                } else if (favoriteJourneys.get(i).getArrival().getTransport_type().toUpperCase().equals("NIGHTRIDER"))
                {
                    type = 4;
                }
                FavouritePattern favouritePattern = new FavouritePattern();
                if (!firstLoad) //if the main activity loads the 2nd time, if not then dont execute the api
                {
                    favoriteManipulater = new FavoriteManipulater(favoriteJourneys.get(i).getStart().getStop_id(), favoriteJourneys.get(i).getArrival().getStop_id(), favoriteJourneys.get(i).getLine(), type);

                    ArrayList<StoppingPattern> stoppingPatterns = favoriteManipulater.findFavorite(false);

                    favouritePattern.setDeparturePattern(stoppingPatterns.get(0));
                    favouritePattern.setArrivalPattern(stoppingPatterns.get(1));
                }

                favouritePattern.setDepartureStop(favoriteJourneys.get(i).getStart());
                favouritePattern.setArrivalStop(favoriteJourneys.get(i).getArrival());

                favoritePatterns.add(favouritePattern);
            }
        }

        favouriteList = populateFavourite(favoritePatterns, 2);
    }


    //Populates the arraylist with the ammount of values specified in the constructor
    private ArrayList<FavouriteListItem> populateFavourite(ArrayList<FavouritePattern> favouriteList, int listItemCount)
    {
        //For every row we want to populate, iterate through and get the relevent values from the relevent ArrayLists
        ArrayList<FavouriteListItem> populatedList = new ArrayList<FavouriteListItem>();
        FavouriteListItem departureItem;

        for (FavouritePattern aFavouriteList : favouriteList)
        {
            departureItem = new FavouriteListItem(aFavouriteList);
            populatedList.add(departureItem);
        }
        return populatedList;
    }


    public ArrayList<FavouriteListItem> getFavouriteList()
    {
        return favouriteList;
    }

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

    public void updateTransportDepartures()
    {
        if (updateRequired)
        {
            FindFavourites();
            updateRequired = false;
        } else
        {
            for (FavouriteListItem favouriteListItem : favouriteList)
            {
                if (!favouriteListItem.updateDepartureTime())
                {
                    updateRequired = true;

                }
            }
        }
    }


}
