package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 27/08/2014.
 * Last modified by MATT on 10/9/2014
 */
public class FavouritePattern implements Serializable
{
    private Stop departureStop;
    private Stop arrivalStop;
    private StoppingPattern departurePattern;
    private StoppingPattern arrivalPattern;

    public Stop getDepartureStop()
    {
        return this.departureStop;
    }

    public void setDepartureStop(Stop departureStop)
    {
        this.departureStop = departureStop;
    }

    public Stop getArrivalStop()
    {
        return this.arrivalStop;
    }

    public void setArrivalStop(Stop arrivalStop)
    {
        this.arrivalStop = arrivalStop;
    }

    public StoppingPattern getDeparturePattern()
    {
        return this.departurePattern;
    }

    public void setDeparturePattern(StoppingPattern departurePattern)
    {
        this.departurePattern = departurePattern;
    }

    public StoppingPattern getArrivalPattern()
    {
        return this.arrivalPattern;
    }

    public void setArrivalPattern(StoppingPattern arrivalPattern)
    {
        this.arrivalPattern = arrivalPattern;
    }


}
