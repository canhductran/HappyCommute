package gridstone.happycommute.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.model.TransportType;

/**
 * Created by Matt on 26/8/2014.
 * Last edited by Matt on 28/8/2014
 */

public class DatabaseHelper extends SQLiteAssetHelper
{
    private static final String DATABASE_NAME = "happydb.db";
    private static final int DATABASE_VERSION = 1;
    private String tableName = "";
    private int[] stopIdArray;
    private Context mContext;


    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public String[] getStationNames(int aType)
    {
        switch (aType)
        {
            case 0:
                tableName = "trainstops";
                break;
            case 1:
                tableName = "tramstops";
                break;
            case 2:
            case 4:
                tableName = "busstops";
                break;
        }
        String[] columnsToSelect = {"0 _id", "stopid", "suburb", "location", "lat", "long"};
        Cursor cursor = returnQueryCursor(tableName, columnsToSelect, null, null, null, null, null);
        if (cursor.getCount() > 0)
        {
            String[] locationArray = new String[cursor.getCount()];
            int i = 0;

            while (cursor.moveToNext())
            {
                locationArray[i] = cursor.getString(cursor.getColumnIndex("location")) + "(" + cursor.getString(cursor.getColumnIndex(("suburb"))) +")";
                i++;
            }
            return locationArray;
        } else
        {
            return new String[]{};
        }

    }

    public Cursor returnQueryCursor(String tableName, String[] tableColumns, String tableSelection, String[] selectionArgs, String groupBy, String having, String orderBy)
    {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(tableName, tableColumns, tableSelection, selectionArgs, groupBy, having, orderBy);
    }

    public int getStopIdFromArrayIndex(int index)
    {
        return stopIdArray[index];
    }
    public Location getLocationFromStopName(String stationName, Integer transportType)
    {
        Location stopLocation = new Location("db location");
        String tableName = "trainstops";
        switch (transportType)
        {
            case 0:
                tableName = "trainstops";
                break;
            case 1:
                tableName = "tramstops";
                break;
            case 2:
            case 4:
                tableName = "busstops";
                break;
        }
        String[] columnsToSelect = {"0 _id", "lat", "long", "location"};
        Cursor cursor = returnQueryCursor(tableName, columnsToSelect, "UPPER(location) = UPPER('" + stationName + "')", null, null, null, null);

        if (cursor.getCount() > 0)
        {

            while (cursor.moveToNext())
            {
                stopLocation.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex("lat"))));
                stopLocation.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex("long"))));
            }

            return stopLocation;


        } else
        {
            Log.d("error", "Location not found");
            return null;
        }


    }

    public Integer getStopIDFromStationName(String location, String suburb, Integer transportType)
    {
        String tableName = "trainstops";
        switch (transportType)
        {
            case 0:
                tableName = "trainstops";
                break;
            case 1:
                tableName = "tramstops";
                break;
            case 2:
            case 4:
                tableName = "busstops";
                break;
        }
        String[] columnsToSelect = {"0 _id", "stopid", "location"};
        String where = "UPPER(location) = UPPER('" + location + "') AND UPPER(suburb)= UPPER('" + suburb + "')";
        Cursor cursor = returnQueryCursor(tableName, columnsToSelect, where,null, null, null, null);

        if (cursor.getCount() > 0)
        {
            String[] locationArray = new String[cursor.getCount()];
            stopIdArray = new int[cursor.getCount()];

            int i = 0;

            while (cursor.moveToNext())
            {
                stopIdArray[i] = cursor.getInt(cursor.getColumnIndex("stopid"));
                i++;
            }
            return stopIdArray[0];

        } else
        {
            Log.d("error", "stopid not found");
            return 0;
        }


    }

    public boolean checkStationExists(Integer transportType, String stationName)
    {
        String tableName = "trainstops";
        switch (transportType)
        {
            case 0:
                tableName = "trainstops";
                break;
            case 1:
                tableName = "tramstops";
                break;
            case 2:
            case 4:
                tableName = "busstops";
                break;
        }
        String[] columnsToSelect = {"0 _id", "location"};
        Cursor cursor = returnQueryCursor(tableName, columnsToSelect, "UPPER(location) = UPPER('" + stationName + "')", null, null, null, null);

        if (cursor.getCount() > 0)
        {
            String[] locationArray = new String[cursor.getCount()];
            int i = 0;

            while (cursor.moveToNext())
            {
                String tempString = cursor.getString(cursor.getColumnIndex("location"));
                if (stationName.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex("location"))))
                {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    //query the database for the stations of the specified trasport mode and then populate an adapter with this
    public ArrayAdapter<String> getAutocompleteAdapter(TransportType transportMode)
    {
        String[] transportStations;
        transportStations = this.getStationNames(transportMode.ordinal());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.autocompletelist_item, transportStations);
        return adapter;
    }
}


