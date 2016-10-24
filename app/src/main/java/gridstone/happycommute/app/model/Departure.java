package gridstone.happycommute.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by CHRIS on 15/05/2014.
 * Last modified by MATT on 10/9/2014
 */
public class Departure implements Serializable //JSONObject Value is returned when use
        // the three APIs which return timetable data: Broad Next Departures, Stopping Pattern and Specific Next Departures.
{
    private static final long serialVersionUID = 1L;
    private Platform platform;
    private Run run;
    private Date time_timetable_utc;
    private Date reminder;
    private Date time_realtime_utc; //always 0, not used at the moment
    private String flags;
    private ArrayList<StoppingPattern> stoppingPatternArrayList = null; //used for the removal of duplicate departures

    private Date destinationArrivalDate = null;

    public Platform getPlatform()
    {
        return platform;
    }

    public void setPlatform(Platform aPlatform)
    {
        platform = aPlatform;
    }

    public Run getRun()
    {
        return run;
    }

    public void setRun(Run aRun)
    {
        run = aRun;
    }

    public Date getTime_timetable_utc()
{
    return time_timetable_utc;
}

    public void setTime_timetable_utc(Date aDateTime)
    {
        time_timetable_utc = aDateTime;
    }

    public Date getReminder()
    {
        return reminder;
    }

    public void setReminder(Date aReminder)
    {
        reminder = aReminder;
    }

    public String getDepartureTime()
    {
        return this.getTime_timetable_utc().toString().substring(11, 16);
    }

    public String getFlags()
    {
        return flags;
    }

    public void setFlags(String aFlags)
    {
        flags = aFlags;
    }

    public Date getDestinationArrivalDate()
    {
        return destinationArrivalDate;
    }

    public void setDestinationArrivalDate(Date destinationArrivalDate)
    {
        this.destinationArrivalDate = destinationArrivalDate;
    }

    public ArrayList<StoppingPattern> getStoppingPatternArrayList()
    {
        return stoppingPatternArrayList;
    }

    public void setStoppingPatternArrayList(ArrayList<StoppingPattern> stoppingPatternArrayList)
    {
        this.stoppingPatternArrayList = stoppingPatternArrayList;
    }
}
