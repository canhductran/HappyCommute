package gridstone.happycommute.app.adapter.departuresAdapter;
/**
 * Created by Matt on 6/5/2014.
 * Last modified 28/8 by Matt
 */

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import gridstone.happycommute.app.model.Departure;

public class DepartureListItem
{


    private String lineName;
    private String transportDirection;
    private String arrivalTime;

    private boolean isReminder;
    private String departureTime;
    private String destTime; //time of final arrival at destination
    private ArrayList<String> arrivalTimesArrayList;
    private ArrayList<String> destTimesArrayList;

    private String date; //for reminders only
    private String stop;


    private Departure departureItem;

    public DepartureListItem(Departure departure, boolean isReminder)
    {
        if (departure != null)
        {
            this.isReminder = isReminder;
            this.departureItem = departure;
            this.arrivalTimesArrayList = new ArrayList();
            this.arrivalTimesArrayList = CalculateDepartureTime(departureItem.getTime_timetable_utc());
            this.setFields(arrivalTimesArrayList);
        }
    }


    private void setFields(ArrayList<String> arrivalTimesList)
    {
        arrivalTime = convertTimeToString(arrivalTimesList);
            transportDirection = ("Towards: " + (departureItem.getRun().getDestination_name()).replaceAll("Street","St"));
        //set the transport line name

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if(isReminder)
        {
            this.date = "Reminds at " + df.format(departureItem.getReminder());
            this.departureTime = "Departures at " + df.format(departureItem.getTime_timetable_utc());
        }

        this.stop = departureItem.getPlatform().getStop().getLocation_name();

        setLineName((departureItem.getPlatform().getDirection().getLine().getLine_name()));

    }

    public boolean updateDepartureTime()
    {
        Integer tempInt;
        //0 is mins, 1 is hours, 2 is days
        if (Integer.parseInt(arrivalTimesArrayList.get(2)) == 0 && Integer.parseInt(arrivalTimesArrayList.get(1)) == 0 && Integer.parseInt(arrivalTimesArrayList.get(0)) == 0)
        {
        } else if (Integer.parseInt(arrivalTimesArrayList.get(0)) > 0)
        {
            //minus one from the minute field
            tempInt = (Integer.parseInt(arrivalTimesArrayList.get(0)) - 1);
        } else if (Integer.parseInt(arrivalTimesArrayList.get(0)) == 0)
        {
            //if the minute field is zero then minus one results in 59 and deducts one from the hour field
            tempInt = 59;
            arrivalTimesArrayList.set(0, tempInt.toString());
            tempInt = Integer.parseInt(arrivalTimesArrayList.get(1));
            if (tempInt > 0)
            {
                //If there is more than 1 hour to go then reduce one from this to account for the minute rllover
                tempInt--;
                arrivalTimesArrayList.set(1, tempInt.toString());
            } else if (tempInt == 0)
            {
//            X days 0 minutes 0 hours
                tempInt = Integer.parseInt(arrivalTimesArrayList.get(2));
                tempInt--;
                arrivalTimesArrayList.set(2, tempInt.toString());
                arrivalTimesArrayList.set(0, "59");
                arrivalTimesArrayList.set(1, "23");
            }

        }

//        Now that we have reduced the departure time by 1, we check if the train is arriving now and that the next update will force an api request
        if (Integer.parseInt(arrivalTimesArrayList.get(2)) == 0 && Integer.parseInt(arrivalTimesArrayList.get(1)) == 0 && Integer.parseInt(arrivalTimesArrayList.get(0)) == 0)
        {
//            return true;
            this.setFields(arrivalTimesArrayList);  //Set the text fields to relevent strings (i.e now if train is coming now)
            return true;
        } else
        {
            //otherwise check that minutes > 0 and add a filler zero if it is, then reurn
            tempInt = (Integer.parseInt(arrivalTimesArrayList.get(0)) - 1);
            if (tempInt < 10 && Integer.parseInt(arrivalTimesArrayList.get(1)) > 0)
            {
                arrivalTimesArrayList.set(0, "0" + tempInt.toString());
            } else
            {
                arrivalTimesArrayList.set(0, tempInt.toString());
            }
            this.setFields(arrivalTimesArrayList);
            return false;
        }
    }

    public String getTransportDirection()
    {
        return transportDirection;
    }


    public String getArrival()
    {
        return arrivalTime;
    }


    public Departure getDepartureItem()
    {
        return departureItem;
    }

    public ArrayList<String> CalculateDepartureTime(Date departureDateTime)
    {
        Integer departHour, departMinute, currentHour, currentMinute, calculatedHour, calculatedMinute;
        ArrayList<String> DepartureArray = new ArrayList();
        Date currentDateTime = new Date();
        String departureTime = departureDateTime.toString().substring(11, 16);
//        Tue Jun 10 23:17:00 AEST 2014
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss d/M/yyyy");
        TimeZone tz = TimeZone.getTimeZone("GMT+10:00");
        dateFormat.setTimeZone(tz);
        //Sets the current time and date based on the device
        dateFormat.format(currentDateTime);
        dateFormat.format(departureDateTime);
        try
        {
            currentDateTime = dateFormat.parse(currentDateTime.toString());
            departureDateTime = dateFormat.parse(departureDateTime.toString());
        }
        catch (Exception e)
        {
            //
        }
//        currentDate = currentDateTime.substring(8, 16);
        currentHour = Integer.parseInt(currentDateTime.toString().substring(11, 13));
        currentMinute = Integer.parseInt(currentDateTime.toString().substring(14, 16));
        //Breaks up the DepartureTime into Hours and Minutes
        departHour = Integer.parseInt(departureTime.substring(0, 2));
        departMinute = Integer.parseInt(departureTime.substring(3, 5));
//                                                                                    JUN 11 2014
//                                                                                    11/Jun/2014
        // If the departure hour is a day in the future, add a day to the departure time to get the accurate time
        if ((currentHour > 12 && currentHour < 24) && departHour <= 12)
        {
            departHour += 24;
        }
        //Calculates how long in hours and minutes until the soonest departure. Normally this will simply be a case of minutes.
        calculatedHour = departHour - currentHour;

        calculatedMinute = departMinute - currentMinute;

        //If the departure is over an hour away, the departureminute may be > currentminute, so we account for this and subtract one from the hour if this is the case
        if (calculatedMinute < 0)
        {
            calculatedMinute = 60 + calculatedMinute;
            calculatedHour--;
        }

        //Add and return the values, if the minutes are less than 10 and there is more than 1 hour until
        // departure then fill the string with a "0" buffer so it displays correctly
        if (calculatedMinute < 10 && calculatedMinute > 0 && calculatedHour != 0)
        {
            DepartureArray.add("0" + calculatedMinute.toString());
        } else
        {
            DepartureArray.add(calculatedMinute.toString());
        }
        DepartureArray.add(calculatedHour.toString());
        //Calculates the days between the departure and current date, primarily useful for nightriders that don't run every day

        DateTime startDate = new DateTime(currentDateTime);
        DateTime endDate = new DateTime(departureDateTime);

        Days d = Days.daysBetween(startDate, endDate);
        int days = d.getDays();
//        days--;
        DepartureArray.add(Integer.toString(days));
        return DepartureArray;
    }

    public String getDate() {return this.date;}
    public String getDepartureTime() {return this.departureTime;}

    public void setTransportDirection(String transportDirection)
    {
        this.transportDirection = transportDirection;
    }

    public String getLineName()
    {
        return lineName;
    }

    public void setLineName(String lineName)
    {
        this.lineName = lineName;
    }

    public String convertTimeToString(ArrayList<String> arrivalTimesList)
    {
        String time = "";
        if (Integer.parseInt(arrivalTimesList.get(2)) >= 1)
        {
            if (Integer.parseInt(arrivalTimesList.get(2)) == 1)
            {
                time = (arrivalTimesList.get(2) + " day"); //set 1 day or
            } else if (Integer.parseInt(arrivalTimesList.get(2)) >= 2)
            {
                time = (arrivalTimesList.get(2) + " days"); //xx days
            }
        }
        //else if less than one day, less than one hour
        else if (Integer.parseInt(arrivalTimesList.get(2)) < 1 &&
                Integer.parseInt(arrivalTimesList.get(1)) == 0 &&
                Integer.parseInt(arrivalTimesList.get(0)) < 60)
        {
            if (Integer.parseInt(arrivalTimesList.get(0)) == 1)
                time = "1 min";
            else
            time = (arrivalTimesList.get(0) + " mins"); //xx minutes
        }
        //else if less than one day, more than one hour
        else if (Integer.parseInt(arrivalTimesList.get(1)) > 0 &&
                Integer.parseInt(arrivalTimesList.get(2)) < 1 &&
                Integer.parseInt(arrivalTimesList.get(0)) == 0)
        {
            //if 1 hour exactly
            if (Integer.parseInt(arrivalTimesList.get(1)) == 1 &&
                    Integer.parseInt(arrivalTimesList.get(0)) == 0 &&
                    Integer.parseInt(arrivalTimesList.get(2)) < 1)
            {
                time = (arrivalTimesList.get(1) + " hour");
            }
            //if more than one hour exactly
            else if (Integer.parseInt(arrivalTimesList.get(1)) > 1 &&
                    Integer.parseInt(arrivalTimesList.get(0)) == 0 &&
                    Integer.parseInt(arrivalTimesList.get(2)) < 1)
            {
                time = (arrivalTimesList.get(1) + " hours");
            }
        } else
        {
            time = (arrivalTimesList.get(1) + ":" + arrivalTimesList.get(0) + " hours");
        }
        if ((Integer.parseInt(arrivalTimesList.get(2)) == 0 && Integer.parseInt(arrivalTimesList.get(1)) == 0 && Integer.parseInt(arrivalTimesList.get(0)) == 0))
        {
            time = "NOW";
        }

        return time;
    }


    public void setDestTime(Date destTime)
    {
        DateTime time = new DateTime(destTime);
        this.destTime = "Arrives: " + getTime12Hr(time.getHourOfDay(),time.getMinuteOfHour());
    }
    public String getTime12Hr(int hour, int minute) //returns the time in the format hh:mm:pm in 12 hour time from 12 hour
    {
        if (hour <= 11)
        {
            if (minute < 10)
                return hour + ":0" + minute + "am";
            return hour + ":" + minute + "am";
        } else
        {
            if (minute < 10)
                if (hour == 12)
                    return  12 + ":0" + minute + "pm";
                else
            return hour - 12 + ":0" + minute + "pm";

            if (hour == 12)
                return 12+":"+minute+"pm";
            return hour - 12 + ":" + minute + "pm";
        }
    }


    public String getDestTime()
    {
        return destTime;
    }

}

