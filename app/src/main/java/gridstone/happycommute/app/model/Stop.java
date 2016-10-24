package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 15/05/2014.
 * * Last modified by MATT on 10/9/2014
 */
public class Stop implements Serializable
{
    private String suburb; //the suburb name
    //e.g. “Belgrave”
    private String transport_type;
    //the mode of transport serviced by the stop
    //e.g. can be either “train”, “tram”, “bus”, “V/Line” or “NightRider”
    private int stop_id;
    //the unique identifier of each stop
    //e.g. “1234”
    private String location_name;
    //the name of the stop based on a concise geographic description
    //e.g. "20-Barkly Square/115 Sydney Rd (Brunswick)"
    private double lat;
    //geographic coordinate of latitude
    //e.g. -37.82005
    private double lon;
    //geographic coordinate of longitude
    //e.g. 144.95047
    private double distance;

    public String getSuburb()
    {
        return suburb;
    }

    //returns zero in the context of this API
    public void setSuburb(String aSuburb)
    {
        suburb = aSuburb;
    }

    public String getTransport_type()
    {
        return transport_type;
    }

    public void setTransport_type(String aTransportType)
    {
        transport_type = aTransportType;
    }

    public int getStop_id()
    {
        return stop_id;
    }

    public void setStop_id(int aStopID)
    {
        stop_id = aStopID;
    }

    public String getLocation_name()
    {
        return location_name;
    }

    public void setLocation_name(String aLocationName)
    {
        location_name = aLocationName;
    }

    public double getLat()
    {
        return lat;
    }

    public void setLat(double aLatitude)
    {
        lat = aLatitude;
    }

    public double getLon()
    {
        return lon;
    }

    public void setLon(double aLongitude)
    {
        lon = aLongitude;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double aDistance)
    {
        distance = aDistance;
    }
}
