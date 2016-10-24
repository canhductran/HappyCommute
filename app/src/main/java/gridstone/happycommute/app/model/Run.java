package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 15/05/2014.
 * Last modified by MATT on 10/9/2014
 */
public class Run implements Serializable
{
    private String transport_type;//the mode of transport serviced by the line
    // e.g. can be either “train”, “tram”, “bus”, “V/Line” or “NightRider”
    private int run_id; //the unique identifier of each run
    // e.g. “1464”
    private int num_skipped; //the number of stops skipped for the run, applicable to train;
    // a number greater than zero indicates either a limited express or express service
    //e.g. 0
    private int destination_id;//the stop_id of the destination, i.e. the last stop for the run
    //e.g. “1044”
    private String destination_name;//the location_name of the destination, i.e. the last stop for the run
    //e.g. “Craigieburn”

    public String getTransport_type()
    {
        return transport_type;
    }

    public void setTransport_type(String aTransportType)
    {
        transport_type = aTransportType;
    }

    public int getRun_id()
    {
        return run_id;
    }

    public void setRun_id(int aRunID)
    {
        run_id = aRunID;
    }

    public int getNum_skipped()
    {
        return num_skipped;
    }

    public void setNum_skipped(int aNumSkipped)
    {
        num_skipped = aNumSkipped;
    }

    public int getDestination_id()
    {
        return destination_id;
    }

    public void setDestination_id(int aDestinationID)
    {
        destination_id = aDestinationID;
    }

    public String getDestination_name()
    {
        return destination_name;
    }

    public void setDestination_name(String aDestinationName)
    {
        destination_name = aDestinationName;
    }
}
