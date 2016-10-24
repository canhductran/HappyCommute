package gridstone.happycommute.app.favourites;

import java.util.ArrayList;

import gridstone.happycommute.app.apiServices.APIHandler;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Line;
import gridstone.happycommute.app.model.StoppingPattern;

/**
 * Created by CHRIS on 22/07/2014.
 */
public class FavoriteManipulater
{
    APIHandler apiHandler;
    private int startStopId;
    private int arrivalStopId;
    private int mode;

    public FavoriteManipulater(int startStopId, int arrivalStopId, ArrayList<Line> lines, int mode)
    {
        apiHandler = new APIHandler();
        this.startStopId = startStopId;
        this.arrivalStopId = arrivalStopId;
        this.mode = mode;
    }

    public ArrayList<StoppingPattern> findFavorite(boolean addFavourite)
    {
        StoppingPattern startPattern = new StoppingPattern();
        StoppingPattern arrivalPattern = new StoppingPattern();

        boolean foundStartPattern = false;
        boolean foundArrivalPattern = false;

        int startOrder = 0;
        int arrivalOrder = 0;

        ArrayList<StoppingPattern> resultPatterns = new ArrayList<StoppingPattern>();

        ArrayList<Departure> nextDepartures = apiHandler.getBroadNextDepartures(startStopId, 10, mode, false);
/*
        if(lineId == 0 || directionId == 0)
        {
            nextDepartures = apiHandler.getNextDepartures(startStopId, 10, mode);
        }
        else
        {
            nextDepartures = apiHandler.getNextSpecificDepartures(startStopId, lineId, directionId, 10, mode);
        }
*/
        //outerloop:

        for (Departure d : nextDepartures)
        {
            ArrayList<StoppingPattern> stoppingPatterns = apiHandler.getStoppingPattern(mode, d.getRun().getRun_id(), d.getPlatform().getStop().getStop_id(), d.getTime_timetable_utc());

            for (int i = 0; i < stoppingPatterns.size(); i++)
            {
                if (stoppingPatterns.get(i).getPlatform().getStop().getStop_id() == startStopId)
                {
                    startPattern = stoppingPatterns.get(i);
                    foundStartPattern = true;
                    startOrder = i;
                }

                if (stoppingPatterns.get(i).getPlatform().getStop().getStop_id() == arrivalStopId)
                {
                    arrivalPattern = stoppingPatterns.get(i);
                    foundArrivalPattern = true;
                    arrivalOrder = i;
                }

            }
            if (foundStartPattern && foundArrivalPattern && (startOrder <= arrivalOrder))
            {
                resultPatterns.add(startPattern);
                resultPatterns.add(arrivalPattern);
                return resultPatterns;
            } else
            {
                startPattern = new StoppingPattern();
                arrivalPattern = new StoppingPattern();

                foundStartPattern = false;
                foundArrivalPattern = false;

                startOrder = 0;
                arrivalOrder = 0;
            }
        }

        return resultPatterns;
    }

}
