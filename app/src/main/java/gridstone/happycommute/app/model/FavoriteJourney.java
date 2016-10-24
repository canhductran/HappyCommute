package gridstone.happycommute.app.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by CHRIS on 28/07/2014.
 * Last modified by MATT on 10/9/2014
 */
public class FavoriteJourney implements Serializable
{
    Stop start;
    Stop arrival;
    ArrayList<Line> line = new ArrayList<Line>();

    public Stop getStart()
    {
        return this.start;
    }

    public void setStart(Stop start)
    {
        this.start = start;
    }

    public Stop getArrival()
    {
        return this.arrival;
    }

    public void setArrival(Stop arrival)
    {
        this.arrival = arrival;
    }

    public ArrayList<Line> getLine() {return this.line;}

    public void setLine(ArrayList<Line> line) {this.line = line;}

    public void addLine(Line line) {this.line.add(line);}


}
