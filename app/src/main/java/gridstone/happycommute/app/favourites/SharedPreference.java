package gridstone.happycommute.app.favourites;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.FavoriteJourney;

/**
 * Created by CHRIS on 28/07/2014.
 */
public class SharedPreference
{
    private static final String FAVORITE_NAME = "Favorite";
    private static final String REMINDER_NAME = "Reminder";
    private static final String FAVORITE = "Favorite_Journey";
    private static final String REMINDER = "Reminder";

    public SharedPreference()
    {

    }

    public void saveFavorite(Context context, List<FavoriteJourney> journeys)
    {
        SharedPreferences sharedPreferences;
        Editor editor;

        sharedPreferences = context.getSharedPreferences(FAVORITE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(journeys);

        editor.putString(FAVORITE, jsonFavorites);

        editor.commit();
    }

    public void saveReminder(Context context, List<Departure> departures)
    {
        SharedPreferences sharedPreferences;
        Editor editor;

        sharedPreferences = context.getSharedPreferences(REMINDER_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(departures);

        editor.putString(REMINDER, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, FavoriteJourney newJourney)
    {
        List<FavoriteJourney> favorites = getFavorite(context);

        if (favorites == null)
        {
            favorites = new ArrayList<FavoriteJourney>();
        }

        favorites.add(newJourney);
        saveFavorite(context, favorites);

    }

    public void addReminder(Context context, Departure departure)
    {
        List<Departure> remindedDepartures = getReminders(context);

        if (remindedDepartures == null)
        {
            remindedDepartures = new ArrayList<Departure>();
        }

        remindedDepartures.add(departure);
        saveReminder(context, remindedDepartures);
    }

    public void removeFavorite(Context context, String departureStop, String arrivalStop)
    {
        ArrayList<FavoriteJourney> favorites = getFavorite(context);
        if (favorites != null)
        {
            for(FavoriteJourney favouriteJourney: favorites)
            {
                if(favouriteJourney.getStart().getLocation_name().toUpperCase().equals(departureStop.toUpperCase()) &&
                        favouriteJourney.getArrival().getLocation_name().toUpperCase().equals(arrivalStop.toUpperCase()))
                {
                    favorites.remove(favouriteJourney);
                    break;
                }
            }
            saveFavorite(context, favorites);
        }
    }

    public void removeReminder(Context context, Date time)
    {
        ArrayList<Departure> reminders = getReminders(context);
        if (reminders != null)
        {
            for(Departure reminder: reminders)
            {
                if(reminder.getTime_timetable_utc().compareTo(time) == 0)
                {
                    reminders.remove(reminder);
                    break;
                }
            }
            saveReminder(context, reminders);
        }
    }

    public ArrayList<FavoriteJourney> getFavorite(Context context)
    {
        SharedPreferences sharedPreferences;
        List<FavoriteJourney> getJourneys;

        sharedPreferences = context.getSharedPreferences(FAVORITE_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(FAVORITE))
        {
            String jsonFavorites = sharedPreferences.getString(FAVORITE, null);
            Gson gson = new Gson();
            FavoriteJourney[] favoriteJourneys = gson.fromJson(jsonFavorites, FavoriteJourney[].class);

            getJourneys = Arrays.asList(favoriteJourneys);
            getJourneys = new ArrayList<FavoriteJourney>(getJourneys);
        } else
        {
            return new ArrayList<FavoriteJourney>();
        }

        return (ArrayList<FavoriteJourney>) getJourneys;
    }

    public ArrayList<Departure> getReminders(Context context)
    {
        SharedPreferences sharedPreferences;
        List<Departure> getDepartures;

        sharedPreferences = context.getSharedPreferences(REMINDER_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(REMINDER))
        {
            String jsonReminders = sharedPreferences.getString(REMINDER, null);
            Gson gson = new Gson();
            Departure[] departures = gson.fromJson(jsonReminders, Departure[].class);

            getDepartures = Arrays.asList(departures);
            getDepartures = new ArrayList<Departure>(getDepartures);
        } else
        {
            return new ArrayList<Departure>();
        }

        return (ArrayList<Departure>) getDepartures;
    }
    public void clearAllFavourites(Context context)
    {

        SharedPreferences sharedPreferences = context.getSharedPreferences(FAVORITE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void clearAllReminders(Context context)
    {

        SharedPreferences sharedPreferences = context.getSharedPreferences(REMINDER_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
