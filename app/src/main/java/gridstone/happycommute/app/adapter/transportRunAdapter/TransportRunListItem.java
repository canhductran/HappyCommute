package gridstone.happycommute.app.adapter.transportRunAdapter;

/**
 * Created by Matt on 10/9/2014.
 * Last modified by Matt on 11/9/2014
 */
public class TransportRunListItem
{
    private String stationName;
    private String stationDepartureTime;

    public String getStationName()
    {
        return stationName;
    }

    public void setStationName(String stationName)
    {
        this.stationName = stationName;
    }

    public String getStationDepartureTime()
    {
        return stationDepartureTime;
    }

    public void setStationDepartureTime(String stationDepartureTime)
    {
        this.stationDepartureTime = stationDepartureTime;
    }
}
