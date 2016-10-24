package gridstone.happycommute.app.gson;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Direction;
import gridstone.happycommute.app.model.Line;
import gridstone.happycommute.app.model.Platform;
import gridstone.happycommute.app.model.Run;
import gridstone.happycommute.app.model.Stop;
import gridstone.happycommute.app.model.StoppingPattern;

/**
 * Created by CHRIS on 20/05/2014.
 * Last edited by MATT on 6/6/2014
 * Last edited by CHRIS on 24/4/2014
 */
public class JSONHandler
{
    private final Gson gson;

    public JSONHandler()
    {
        gson = new Gson();
    }

    public Stop parseJSONtoStop(String aJSON)
    {
        Stop lStop = new Stop();

        try
        {
            lStop = gson.fromJson(aJSON, Stop.class);
        }
        catch (JsonParseException e)
        {
            e.printStackTrace();
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }

        return lStop;
    }

    public Departure parseJSONtoDeparture(String aJSON)
    {
        Departure lDeparture = new Departure();
        try
        {
            lDeparture = gson.fromJson(aJSON, Departure.class);
            /*
            TimeZone tz = TimeZone.getTimeZone("GMT");
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(tz);
            Date dateobj = sdf.parse(aJSON.getString("time_timetable_utc"));
            lDeparture.setDateTime(dateobj);
            lDeparture.setPlatform(parseJSONtoPlatform(aJSON.getJSONObject("platform")));
            lDeparture.setRun(parseJSONtoRun(aJSON.getJSONObject("run")));
            lDeparture.setFlags(aJSON.getString("flags"));
            */


        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }

        return lDeparture;
    }

    public Platform parseJSONtoPlatform(String aJSON)
    {
        Platform lPlatform = new Platform();
        try
        {
            lPlatform = gson.fromJson(aJSON, Platform.class);
            /*
            lPlatform.setStop(parseJSONtoStop(aJSON.getJSONObject("stop")));
            lPlatform.setDirection(parseJSONtoDirection(aJSON.getJSONObject("direction")));
            */
        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }

        return lPlatform;
    }

    public Run parseJSONtoRun(String aJSON)
    {
        Run lRun = new Run();
        try
        {
            lRun = gson.fromJson(aJSON, Run.class);
            /*
            lRun.setTransportType(TransportType.valueOf(aJSON.getString("transport_type").toUpperCase()));
            lRun.setRunID(aJSON.getInt("run_id"));
            lRun.setNumSkipped(aJSON.getInt("num_skipped"));
            lRun.setDestinationID(aJSON.getInt("destination_id"));
            lRun.setDestinationName(aJSON.getString("destination_name"));
            */
        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }

        return lRun;
    }

    public Direction parseJSONtoDirection(String aJSON)
    {
        Direction lDirection = new Direction();
        try
        {
            lDirection = gson.fromJson(aJSON, Direction.class);
            /*
            lDirection.setDirectionID(aJSON.getInt("direction_id"));
            lDirection.setDirectionName(aJSON.getString("direction_name"));
            lDirection.setLineDirectionID(aJSON.getInt("linedir_id"));
            lDirection.setLine(parseJSONtoLine(aJSON.getJSONObject("line")));
            */
        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }

        return lDirection;
    }

    public Line parseJSONtoLine(String aJSON)
    {
        Line lLine = new Line();
        try
        {
            lLine = gson.fromJson(aJSON, Line.class);
                /*
                lLine.setTransportType(TransportType.valueOf(aJSON.getString("transport_type").toUpperCase()));
                lLine.setLineID(aJSON.getInt("line_id"));
                lLine.setLineName(aJSON.getString("line_name"));
                lLine.setLineNumber(aJSON.getString("line_number"));
                */
        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }
        return lLine;
    }

    public StoppingPattern parseJSONtoStoppingPattern(String aJSON)
    {
        StoppingPattern stoppingPattern = new StoppingPattern();
        try
        {
            stoppingPattern = gson.fromJson(aJSON, StoppingPattern.class);
        }
        catch (JsonParseException e)
        {
            Log.w(getClass().getSimpleName(), "Error: " + e);
        }
        return stoppingPattern;
    }

}
