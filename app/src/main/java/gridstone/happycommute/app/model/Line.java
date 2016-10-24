package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 15/05/2014.
 */
public class Line implements Serializable
{
    private String transport_type;//the mode of transport serviced by the line
    // e.g. can be either “train”, “tram”, “bus”, “V/Line” or “NightRider”
    private int line_id; // the unique identifier of each line
    // e.g. “1818”
    private String line_name; //the name of the line
    // e.g. "970 - City - Frankston - Mornington - Rosebud via Nepean Highway & Frankston Station "

    private String line_number; //the line number that is presented to the public (i.e. not the “line_id”)
    // e.g. “970”

    public String getTransport_type()
    {
        return transport_type;
    }

    public void setTransport_type(String aTransportType)
    {
        transport_type = aTransportType;
    }

    public int getLine_id()
    {
        return line_id;
    }

    public void setLine_id(int aLineID)
    {
        line_id = aLineID;
    }

    public String getLine_name()
    {
        return line_name;
    }

    public void setLine_name(String aLineName)
    {
        line_name = aLineName;
    }

    public String getLine_number()
    {
        return line_number;
    }

    public void setLine_number(String aLineNumber)
    {
        line_number = aLineNumber;
    }

}
