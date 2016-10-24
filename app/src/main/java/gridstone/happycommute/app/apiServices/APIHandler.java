package gridstone.happycommute.app.apiServices;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import gridstone.happycommute.app.algo.HaversineAlgorithm;
import gridstone.happycommute.app.gson.JSONHandler;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.model.Stop;
import gridstone.happycommute.app.model.StoppingPattern;
import gridstone.happycommute.app.model.TransportType;

/**
 * Created by CHRIS on 18/05/2014.
 * Last edited by Matt on 9/9/2014
 */
public class
        APIHandler
{
    private final String ptvDevId = "1000125"; // PTV Developer ID
    private final String ptvDevKey = "ad932e38-bac7-11e3-8bed-0263a9d0b8a0"; // PTV Developer Secret Key
    private final String ptvApiBase = "http://timetableapi.ptv.vic.gov.au"; // PTV API Base URL
    private final JSONHandler jsonHandler;

    public APIHandler()
    {
        this.jsonHandler = new JSONHandler();
    }


    public ArrayList<Stop> searchStop(String searchString, int aType)
    {
        ArrayList<Stop> searchStops = new ArrayList<Stop>();
        JSONArray departuresJsonArray;
        try
        {
            String newString = searchString.replace("/", "%20");
            newString = newString.replace(" ", "%20");
            URI uri = new URI((generateCompleteURLWithSignature("/v2/search/" + newString)));
            departuresJsonArray = new JSONArray(requestAPI(uri));

            if (aType == 0)
            {
                for (int i = 0; i < departuresJsonArray.length(); i++)
                {
                    if (departuresJsonArray.getJSONObject(i).getString("type").toUpperCase().equals("STOP"))
                    {
                        Stop stop = jsonHandler.parseJSONtoStop(departuresJsonArray.
                                getJSONObject(i).
                                getJSONObject("result").
                                toString());
                        if (stop.getTransport_type().toUpperCase().equals(TransportType.valueOf(aType).toString()) && stop.getLocation_name().toUpperCase().contains(searchString.toUpperCase()))
                        {
                            searchStops.add(stop);
                        }
                    }
                }
            } else
            {
                for (int i = 0; i < departuresJsonArray.length(); i++)
                {
                    if (departuresJsonArray.getJSONObject(i).getString("type").toUpperCase().equals("STOP"))
                    {
                        Stop stop = jsonHandler.parseJSONtoStop(departuresJsonArray.
                                getJSONObject(i).
                                getJSONObject("result").
                                toString());
                        Log.d("trans type, string value, location name, type", TransportType.valueOf(aType) + " " + searchString + stop.getTransport_type() + stop.getLocation_name());

                        if (stop.getTransport_type().
                                toUpperCase().
                                equals(TransportType.valueOf(aType).toString()) && stop.getLocation_name().
                                toUpperCase().
                                contains(searchString.toUpperCase()))
                        {
                            searchStops.add(stop);
                        }
                    }
                }
            }
        }
        catch (JSONException e)
        {
            Log.d("JSONException at method searchStop (class APIHandler): ", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at method searchStop (class APIHandler): ", e.toString());
        }
        return searchStops;
    }

    public ArrayList<StoppingPattern> getStoppingPattern(int mode, int run_id, int stop_id, Date time)
    {
        ArrayList<StoppingPattern> stoppingPatterns = new ArrayList<StoppingPattern>();
        JSONArray departuresJsonArray;
        JSONObject departuresJsonObject;
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            TimeZone tz = TimeZone.getTimeZone("Australia/Melbourne");
            dateFormat.setTimeZone(tz);
            String output = dateFormat.format(time);

            URI uri = new URI((generateCompleteURLWithSignature("/v2/mode/" + mode + "/run/" + run_id + "/stop/" + stop_id + "/stopping-pattern?for_utc=" + output)));
            departuresJsonObject = new JSONObject(requestAPI(uri));
            departuresJsonArray = (departuresJsonObject.getJSONArray("values"));

            for (int i = 0; i < departuresJsonArray.length(); i++)
            {
                StoppingPattern sp = this.jsonHandler.parseJSONtoStoppingPattern(departuresJsonArray.getJSONObject(i).toString());
                stoppingPatterns.add(sp);
            }

        }
        catch (JSONException e)
        {
            Log.d("JSONException at getStoppingPattern (class APIHandler): : ", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at getStoppingPattern (class APIHandler): : ", e.toString());
        }

        return stoppingPatterns;
    }

    public ArrayList<Stop> getTransportPOI(double long1, double lat1, double long2, double lat2, int aType, int gridDepth, int aLimit)
    {
        ArrayList<Stop> stopsNearBy = new ArrayList<Stop>();
        JSONArray departuresJsonArray;
        JSONObject departuresJsonObject;
        try
        {
            URI uri = new URI((generateCompleteURLWithSignature("/v2/poi/" + aType + "/lat1/" + lat1 + "/long1/" + long1 + "/lat2/" + lat2 + "/long2/" + long2 + "/griddepth/" + gridDepth + "/limit/" + aLimit)));
            departuresJsonObject = new JSONObject(requestAPI(uri));
            departuresJsonArray = departuresJsonObject.getJSONArray("locations");

            if (departuresJsonArray.length() > 0)
            {
                for (int i = 0; i < departuresJsonArray.length(); i++)
                {
                    Stop stop = jsonHandler.parseJSONtoStop(departuresJsonArray.getJSONObject(i).toString());

                    stopsNearBy.add(stop);
                }
            }
        }
        catch (JSONException e)
        {
            Log.d("JSONException at getTransportPOI (class APIHandler)", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at getTransportPOI (class APIHandler)", e.toString());
        }

        return stopsNearBy;
    }

    public ArrayList<Departure> getNextSpecificDepartures(Integer transportStopID, int lineId, int directionId, int limit, int mode)
    {

        ArrayList<Departure> lNextDepartures = new ArrayList<Departure>();
        try
        {
            JSONArray departuresJsonArray;
            JSONObject departuresJsonObject;
            URI uri = new URI(generateCompleteURLWithSignature("/v2/mode/" + mode + "/line/" + lineId + "/stop/" + transportStopID + "/directionid/" + directionId + "/departures/all/limit/" + limit));
            departuresJsonObject = new JSONObject(requestAPI(uri));//request to PTV API, receive the next 2 departures


            departuresJsonArray = (departuresJsonObject.getJSONArray("values"));
            for (int i = 0; i < departuresJsonArray.length(); i++)
            {
                Departure lDeparture = this.jsonHandler.parseJSONtoDeparture(departuresJsonArray.getJSONObject(i).toString());
                lNextDepartures.add(lDeparture);
            }

        }
        catch (JSONException e)
        {
            Log.d("JSONException at getNextSpecificDepartures (class APIHandler)", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at getNextSpecificDepartures (class APIHandler)", e.toString());
        }

        return lNextDepartures;
    }

    public ArrayList<Departure> getBroadNextDepartures(Integer transportStopID, int aLimit, int aType, boolean removeDuplicates)
    {
        ArrayList<Departure> nextDepartures = new ArrayList<Departure>();
        try
        {
            JSONArray departuresJsonArray;
            JSONObject departuresJsonObject;
            URI uri = new URI(generateCompleteURLWithSignature("/v2/mode/" + aType + "/stop/" + transportStopID + "/departures/by-destination/limit/" + aLimit));
            departuresJsonObject = new JSONObject(requestAPI(uri));//request to PTV API, receive the next 2 departures


            departuresJsonArray = (departuresJsonObject.getJSONArray("values"));
            for (int i = 0; i < departuresJsonArray.length(); i++)
            {
                Departure departure = this.jsonHandler.parseJSONtoDeparture(departuresJsonArray.getJSONObject(i).toString());
                nextDepartures.add(departure);
            }

        }
        catch (JSONException e)
        {
            Log.d("JSONException at getBroadNextDepartures (class APIHandler)", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at getBroadNextDepartures (class APIHandler)", e.toString());
        }

        if (aLimit != 0)
            if (nextDepartures.size() > aLimit * 2)
            {
                nextDepartures = new ArrayList(nextDepartures.subList(0, aLimit * 2)); //remove elements exceeding 2x the requested limit from the list to speed up processing
            }

        if (removeDuplicates)
        {
            if (aLimit != 0)
                if (nextDepartures.size() > aLimit * 2)
                {
                    nextDepartures = new ArrayList(nextDepartures.subList(0, aLimit * 2)); //remove elements exceeding 2x the requested limit from the list to speed up processing
                }
            nextDepartures = removeDuplicateDepartures(nextDepartures, aType, transportStopID);
        }
        return nextDepartures;
    }

    //this function will return the specified number of departures after the provided time
    public ArrayList<Departure> getBroadNextDeparturesAtTime(Integer transportStopID, int aLimit, int aType, boolean removeDuplicates, Date queryTime)
    {
        //TODO: convert aest time to utc time if we want to pass this function aest time instead
        ArrayList<Departure> nextDepartures = new ArrayList<Departure>();
        try
        {
            nextDepartures = getBroadNextDepartures(transportStopID, 0, aType, false); //get all departures for the day
        }
        catch (Exception e)
        {
            Log.w(getClass().getSimpleName(), "Exception: " + e);
        }
        nextDepartures = removeEarlierTimes(nextDepartures, queryTime); //get rid of the times in the list that are earlier than the provided time
        //if (nextDepartures.size() > aLimit * 2)
        //nextDepartures = new ArrayList(nextDepartures.subList(0, aLimit * 2)); //remove elements exceeding 2x the requested limit from the list to speed up processing

        //ArrayList<Departure> trimmedList = new ArrayList(nextDepartures.subList(0, aLimit)); //trim the list to the size requested
        return nextDepartures;
    }

    //this function will remove all elements from the provied departure array that occur at a time earlier than the limiting time
    private ArrayList<Departure> removeEarlierTimes(ArrayList<Departure> departures, Date limitingDate)
    {
        DateTime removeTime = DateTime.now();
        Log.d("Starting remove earlier at ", removeTime.toString());
        //assume that the results are already ordered by time
        Collections.reverse(departures); //sort the aray so that the last departure becomes the first
        int i = 0;
        for (Departure d : departures)
        {
            if (d.getTime_timetable_utc().getTime() < limitingDate.getTime()) //find the first occurence of an earlier time
            {
                break; //break the loop, i will now be set to the value of the first earliest time index
            }
            i++;
        }
        Collections.reverse(departures); //reverse the list again, it is now in ascending arrival time again
        i = departures.size() - i; //get the new index value for what correlates to the last value that is before the time we want to limit
        i--;
        //iterate through the array and remove all values that are before the threshhold, starting at the border")
        for (int x = i; x >= 0; x--)
        {
            departures.remove(x);
        }
        DateTime removeTime2 = DateTime.now();
        Log.d("finish remove earlier at ", removeTime2.toString());
        Log.d("This took: ", String.valueOf(String.valueOf(removeTime.getMillis() - removeTime2.getMillis())));
        return departures;

    }

    public Stop getStopNearBy(double aLatitude, double aLongitude, int aType)
    {
        if (getStopsNearbyByLocation(aLatitude, aLongitude, aType).size() != 0)
        {
            return getStopsNearbyByLocation(aLatitude, aLongitude, aType).get(0);
        } else
        {
            double initialTopLeftLat = aLatitude;
            double topLeftLatitude = aLatitude;
            double topLeftLongitude = aLongitude;

            double bottomRightLatitude = aLatitude;
            double bottomRightLongitude = aLongitude;
            //.01deg is roughly 1.5km If no stops are returned within 15km break this loop to prevent overloading the servers
            ArrayList<Stop> stopsNearBy;
            for (; ; )
            {
                topLeftLatitude = topLeftLatitude + 0.01;
                topLeftLongitude = topLeftLongitude - 0.01;

                bottomRightLatitude = bottomRightLatitude - 0.01;
                bottomRightLongitude = bottomRightLongitude + 0.01;

                stopsNearBy = getTransportPOI(topLeftLongitude, topLeftLatitude, bottomRightLongitude, bottomRightLatitude, aType, 0, 20);
                if (stopsNearBy.size() > 0)
                {
                    break;
                } else if (topLeftLatitude == initialTopLeftLat + .1)
                {
                    return null;
                }


            }

            Stop closestStop = stopsNearBy.get(0);
            int closestDistance = HaversineAlgorithm.HaversineInM(closestStop.getLat(), closestStop.getLon(), aLatitude, aLongitude);
            for (Stop s : stopsNearBy)
            {
                if (HaversineAlgorithm.HaversineInM(s.getLat(), s.getLon(), aLatitude, aLongitude) < closestDistance)
                {
                    closestStop = s;
                    closestDistance = HaversineAlgorithm.HaversineInM(s.getLat(), s.getLon(), aLatitude, aLongitude);
                }
            }

            return closestStop;
        }
    }

    public ArrayList<Stop> getSpecifiedNearestStops(final double aLatitude, final double aLongitude, int aType, int numberOfStops)
    {
        double topLeftLatitude = aLatitude;
        double topLeftLongitude = aLongitude;

        double bottomRightLatitude = aLatitude;
        double bottomRightLongitude = aLongitude;

        ArrayList<Stop> stopsNearby;
        for (; ; )
        {
            //loop requesting stops within an area, increasing the area each time until 5 stops are returned
            topLeftLatitude = topLeftLatitude + 0.01;
            topLeftLongitude = topLeftLongitude - 0.01;

            bottomRightLatitude = bottomRightLatitude - 0.01;
            bottomRightLongitude = bottomRightLongitude + 0.01;

            stopsNearby = getTransportPOI(topLeftLongitude, topLeftLatitude, bottomRightLongitude, bottomRightLatitude, aType, 0, 20);
            if (stopsNearby.size() > numberOfStops)
            {
                break;
            }
        }
        //sort the array based on distance
        Collections.sort(stopsNearby, new Comparator<Stop>()
        {
            @Override
            public int compare(Stop stop1, Stop stop2)
            {
                return HaversineAlgorithm.HaversineInM(stop1.getLat(), stop1.getLon(), aLatitude, aLongitude) - HaversineAlgorithm.HaversineInM(stop2.getLat(), stop2.getLon(), aLatitude, aLongitude); // Ascending
            }

        });

        for (int i = stopsNearby.size() - 1; i >= numberOfStops; i--)
        {
            //remove unneeded values from the array in case the ptv request gets too many
            stopsNearby.remove(i);
        }

        return stopsNearby;
    }

    public ArrayList<Stop> getStopsNearbyByLocation(double aLatitude, double aLongitude, int aType)
    {
        ArrayList<Stop> stopsNearby = new ArrayList<Stop>();
        try
        {
            JSONArray departuresJsonArray;
            URI uri = new URI(generateCompleteURLWithSignature("/v2/nearme/latitude/" + aLatitude + "/longitude/" + aLongitude));
            departuresJsonArray = new JSONArray(requestAPI(uri));

            /*
            Type collectionType = new TypeToken<ArrayList<Stop>>() {}.getType();
            ArrayList<Stop> lStops = gson.fromJson(requestAPI(lNearbySignature), collectionType);
            */

            String mode = "";
            switch (aType)
            {
                case 0:
                    mode = "TRAIN";
                    break;
                case 1:
                    mode = "TRAM";
                    break;
                case 2:
                    mode = "BUS";
                    break;
                case 4:
                    mode = "NIGHTRIDER";
                    break;
            }

            for (int i = 0; i < departuresJsonArray.length(); i++)
            {
                Stop stop = this.jsonHandler.parseJSONtoStop(departuresJsonArray.getJSONObject(i).getJSONObject("result").toString());
                if (stop.getTransport_type().toUpperCase().equals(mode))
                {
                    stopsNearby.add(stop);
                }
            }
        }
        catch (JSONException e)
        {
            Log.d("JSONException at getStopsNearbyByLocation (class APIHandler)", e.toString());
        }
        catch (URISyntaxException e)
        {
            Log.d("URISyntaxException at getStopsNearbyByLocation (class APIHandler)", e.toString());
        }

        return stopsNearby;
    }


    public String requestAPI(URI aRequestSignature)
    {
        String json = "";
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(aRequestSignature);
            HttpResponse response = httpclient.execute(request);
            json = EntityUtils.toString(response.getEntity());

        }
        catch (HttpResponseException e)
        {
            Log.d(getClass().getSimpleName(), "Exception: " + e);
        }
        catch (IOException e)
        {
            Log.d(getClass().getSimpleName(), "Exception: " + e);
        }
        return json;
    }

    public String generateSignature(final String uri)
    {
        String encoding = "UTF-8";
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String signature;
        StringBuffer uriWithDeveloperID = new StringBuffer();
        uriWithDeveloperID.append(uri).append(uri.contains("?") ? "&" : "?").append("devid=" + ptvDevId);
        try
        {
            byte[] keyBytes = ptvDevKey.getBytes(encoding);
            byte[] uriBytes = uriWithDeveloperID.toString().getBytes(encoding);
            Key signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] signatureBytes = mac.doFinal(uriBytes);
            StringBuffer buf = new StringBuffer(signatureBytes.length * 2);
            for (byte signatureByte : signatureBytes)
            {
                int intVal = signatureByte & 0xff;
                if (intVal < 0x10)
                {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(intVal));
            }
            signature = buf.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e)
        {
            throw new RuntimeException(e);
        }
        return signature.toUpperCase();
    }

    public String generateCompleteURLWithSignature(final String uri)
    {
        StringBuffer url = new StringBuffer(ptvApiBase).append(uri).append(uri.contains("?") ? "&" : "?").append("devid=" + ptvDevId).append("&signature=" + generateSignature(uri));
        Log.d("COMPLETE PTV REQUEST URL:", url.toString());
        return url.toString();
    }

    //currently deletes any departures that are a subdeparture of another departure
    private ArrayList<Departure> removeDuplicateDepartures(ArrayList<Departure> departures, int aType, int stopID)
    {
        DateTime removeTime = DateTime.now();
        Log.d("Starting remove duplicates at ", removeTime.toString());

        Date currentDepartureTime = null;
        ArrayList<Departure> departuresList = new ArrayList<Departure>();
        ArrayList<Departure> tempRemovalList = new ArrayList<Departure>();
        Set<Integer> stopsToRemove = new TreeSet<Integer>();
        ArrayList<Integer> stopsToRemoveArrayList = new ArrayList<Integer>();
        int z = 0; //incrementor for outer loop
        int x = 0;//incrementor for inner loop
        for (int i = 0; i < departures.size(); i++)
        {
            if (currentDepartureTime == null)
            {
                currentDepartureTime = departures.get(0).getTime_timetable_utc(); //if it is the first item in the list this will be true
                tempRemovalList.add(departures.get(0));
            }
            else if (departures.get(i).getTime_timetable_utc().compareTo(currentDepartureTime) == 0) //if the departures leave at the same time
                tempRemovalList.add(departures.get(i));

            else if (departures.get(i).getTime_timetable_utc().compareTo(currentDepartureTime) > 0) //if it is a later departure
            {
                if (tempRemovalList.size() > 1)
                {
                    for (Departure d : tempRemovalList) //get the stopping pattern for the items to remove
                    {
                        if (d.getStoppingPatternArrayList() == null)
                            d.setStoppingPatternArrayList(this.getStoppingPattern(aType, d.getRun().getRun_id(), stopID, d.getTime_timetable_utc()));
                    }

                    z=0;
                    for (Departure d1 : tempRemovalList)
                    {
                        for (Departure d2 : tempRemovalList)
                        {
                            if (d2.getRun().getRun_id() !=(d1.getRun().getRun_id())) //don't compare an item to itself
                            {
                                for (StoppingPattern s : d2.getStoppingPatternArrayList()) //for the stopping patterns within the stopping pattern array specific to the current departure
                                {
                                    if (s.getPlatform().getStop().getStop_id() == d1.getRun().getDestination_id() //if the stop is the destination of the second departure it is a subset
                                            && compareDateHours(s.getTime_timetable_utc(),d1.getTime_timetable_utc()) > 0)
                                    {
                                        stopsToRemove.add((Integer) z); //therefore add it to an arraylist that we will iterate through and delete via later
                                        break;
                                    }
                                }
                            }
                        }
                        z++;
                    }
                    if (stopsToRemove.size() > 0)
                    {
                        stopsToRemoveArrayList.addAll(stopsToRemove);
                        Collections.reverse(stopsToRemoveArrayList);
                        for (int removalIndex : stopsToRemoveArrayList) //remove the values from the arraylist that we declared as subsets earlier
                        {
                            tempRemovalList.remove(removalIndex);
                        }
                    }
                    departuresList.addAll(tempRemovalList); //add the now pruned list to the final departures list
                    stopsToRemove.clear();
                    stopsToRemoveArrayList.clear();
                    tempRemovalList.clear(); //reset the removal list for the next iteration
                }
                if (tempRemovalList.size() == 1)
                {
                    departuresList.add(tempRemovalList.get(0));
                    tempRemovalList.clear(); //reset the removal list for the next iteration

                }
                tempRemovalList.add(departures.get(i)); //add the current departure as the first item in the removal list

                currentDepartureTime = departures.get(i).getTime_timetable_utc(); //set the departure time of the first item in the temp removal lsit

            }
        }
        DateTime removeTime2 = DateTime.now();
        Log.d("Finishing removing duplicates at : ", removeTime2.toString());
        Log.d("This took: ", String.valueOf(removeTime.getMillis() -removeTime2.getMillis()));
        return departuresList;
    }

    //        DateTime removeTime = DateTime.now();
//        Log.d("Starting remove duplicates at ", removeTime.toString());
//
//        //todo: filter occurences where destination id is the same for two runs that occur at the same time;
//        Set<Integer> stopsToRemove = new TreeSet<Integer>();
//        Set<Integer> reverseSet = null;
//
//        ArrayList<StoppingPattern> mstoppingPattern;
//        List<List<StoppingPattern>> allDepartureStoppingPatterns = new ArrayList<List<StoppingPattern>>(); //arraylist that contains an arraylist of all stopping patterns for each departure
//        for (Departure d:departures)
//        {
//            mstoppingPattern = this.getStoppingPattern(aType, d.getRun().getRun_id(), stopID, d.getTime_timetable_utc()); //get the stopping pattern
//            allDepartureStoppingPatterns.add(mstoppingPattern); //add the arraylist of stopping patterns (departure specific) to the superlist
//            d.setDestinationArrivalDate(mstoppingPattern.get(mstoppingPattern.size()-1).getTime_timetable_utc()); //set arrival time to the last entry in the stopping pattern
//        }
//
//        for (Departure d1:departures)
//        {
//            for (Departure d2 : departures)
//            {
//                if (d1.getRun().getRun_id() != d2.getRun().getRun_id()) //if its not the current departure
//                {
//                    if (d1.getTime_timetable_utc().equals(d2.getTime_timetable_utc())) //if the times are the same
//                    {
//                        //if they leave at the same time, first check to see if departure 2 is a subset of departure 1
////                        mstoppingPattern = this.getStoppingPattern(aType, d2.getRun().getRun_id(), stopID, d2.getTime_timetable_utc());
//                        if (d1.getDestinationArrivalDate() == null)
//                            //set the destination arrival date to the last stop in the stopping pattern array belonging to this departure
//                            d1.setDestinationArrivalDate(allDepartureStoppingPatterns.get(x).get(allDepartureStoppingPatterns.get(x).size() -1).getTime_timetable_utc());
//                        for (StoppingPattern s : allDepartureStoppingPatterns.get(x)) //for the stopping patterns within the stopping pattern array specific to the current departure
//                        {
//                            if (s.getPlatform().getStop().getStop_id() == d1.getRun().getDestination_id() //if the stop is the destination of the second departure it is a subset
//                                    && s.getTime_timetable_utc().compareTo(d1.getTime_timetable_utc()) > 0)
//                            {
//                                  stopsToRemove.add((Integer)i); //therefore add it to an arraylist that we will iterate through and delete via later
//                                break;
//                            }
//                        }
//                    }
//                }
//                x++;
//            }
//            x=0;
//            z++;
//        }
//
//        ArrayList<Integer> stopsToRemoveArrayList = new ArrayList<Integer>();
//        stopsToRemoveArrayList.addAll(stopsToRemove);
//        Collections.reverse(stopsToRemoveArrayList);
//        for (int removalIndex :stopsToRemoveArrayList) //remove the values from the arraylist that we declared as subsets earlier
//        {
//            departures.remove(removalIndex);
//        }
//        DateTime removeTime2 = DateTime.now();
//        Log.d("FInishing removing duplicates at : ", removeTime2.toString());
//        Log.d("This took: ", String.valueOf(removeTime.getMillis() -removeTime2.getMillis()));
//        return departures;
//    }
//}
    public int compareDateHours(Date date1,Date date2) //Note: this function is necessary because sometimes the api likes to think dates that are supposedly a day in the past.
// As trains run daily, we simply ignore the date all together and check whether the dates are different in terms of hours and minutes.
    {
        DateTime date1Time = new DateTime(date1);
        DateTime date2Time = new DateTime(date2);

        int Comparison =
                (((date1Time.getHourOfDay() * 60) + date1Time.getMinuteOfHour())  -
                        ((date2Time.getHourOfDay() * 60) + date2Time.getMinuteOfHour())); //if return is > 0 date1 is in the future of date2, otherwise vice versa.

        return Comparison;
    }
}