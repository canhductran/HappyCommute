package gridstone.happycommute.app.model;

/**
 * Created by CHRIS on 19/05/2014.
 */

import java.util.HashMap;
import java.util.Map;

public enum TransportType
{
    TRAIN(0), TRAM(1), BUS(2), NIGHTRIDER(4),;
    private static Map<Integer, TransportType> map = new HashMap<Integer, TransportType>();
    static
    {
        for (TransportType TransportEnum : TransportType.values())
        {
            map.put(TransportEnum.fTransport, TransportEnum);
        }
    }
    private int fTransport;

    private TransportType(final int aTransport) { fTransport = aTransport; }

    public static TransportType valueOf(int aTransport)
    {
        return map.get(aTransport);
    }
}
