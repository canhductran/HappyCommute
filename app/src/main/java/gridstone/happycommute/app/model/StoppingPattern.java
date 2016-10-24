package gridstone.happycommute.app.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by CHRIS on 22/07/2014.
 * Last modified by MATT on 10/9/2014
 */
public class StoppingPattern implements Serializable
{
    private Platform platform;
    private Run run;
    private Date time_timetable_utc;
    private Date time_realtime_utc; //always 0, not used at the moment

    public Platform getPlatform()
    {
        return platform;
    }

    public void setPlatform(Platform aPlatform)
    {
        this.platform = aPlatform;
    }

    public Run getRun()
    {
        return run;
    }

    public void setRun(Run aRun)
    {
        this.run = aRun;
    }

    public Date getTime_timetable_utc()
    {
        return time_timetable_utc;
    }

    public void setTime_timetable_utc(Date aDateTime)
    {
        time_timetable_utc = aDateTime;
    }

    public Date getTime_realtime_utc()
    {
        return time_realtime_utc;
    }

    public void setTime_realtime_utc(Date aRealtime)
    {
        time_realtime_utc = aRealtime;
    }

}
