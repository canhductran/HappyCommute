package gridstone.happycommute.app.model;

import java.io.Serializable;

/**
 * Created by CHRIS on 15/05/2014.
 * Last modified by MATT on 10/9/2014
 */
public class Direction implements Serializable
{
    private int linedir_id;
    //unique identifier of a particular line and direction
    //e.g. “21”
    private int direction_id;
    //unique identifier of a direction
    // e.g. “0”
    private String direction_name;
    //name of the direction of the service (e.g. “0” signifies “city”)
    // e.g. "City (Flinders Street)"
    private Line line;         //JSONObject "Line" is always embedded inside JSONObject "Direction" when returned

    public int getLinedir_id()
    {
        return linedir_id;
    }

    public void setLinedir_id(int aLineDirection)
    {
        linedir_id = aLineDirection;
    }

    public int getDirection_id()
    {
        return direction_id;
    }

    public void setDirection_id(int aDirection)
    {
        direction_id = aDirection;
    }

    public String getDirection_name()
    {
        return direction_name;
    }

    public void setDirection_name(String aDirectionName)
    {
        direction_name = aDirectionName;
    }

    public Line getLine()
    {
        return line;
    }

    public void setLine(Line aLine)
    {
        line = aLine;
    }
}
