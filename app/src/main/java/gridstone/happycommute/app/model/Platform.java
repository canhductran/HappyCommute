package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 15/05/2014.
 */
public class Platform implements Serializable
{
    private int realtime_id;//this will always be 0 at the moment because no real-time feeds are provided at this time by the PTV API
    private Stop stop; //JSONObject Stop is always embedded in JSONObject Platform when returned
    private Direction direction; //JSONObject Direction is always embedded in JSONObject Platform when returned

    public int getRealtime_id()
    {
        return realtime_id;
    }

    public void setRealtime_id(int aRealtimeID)
    {
        realtime_id = aRealtimeID;
    }

    public Stop getStop()
    {
        return stop;
    }

    public void setStop(Stop aStop)
    {
        stop = aStop;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public void setDirection(Direction aDirection)
    {
        direction = aDirection;
    }
}
