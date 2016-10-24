package gridstone.happycommute.app.adapter.transportRunAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import gridstone.happycommute.app.R;

/**
 * Created by Matt on 10/9/2014.
 */
public class TransportRunAdapter extends BaseAdapter
{
    private final ArrayList<TransportRunListItem> runItems;
    Integer currentStationIndex;
    private LayoutInflater layoutInflater;
    private Context mContext;

    public TransportRunAdapter(Context context, ArrayList<TransportRunListItem> runItems, Integer currentStationIndex)
    {
        this.mContext = context;
        this.runItems = runItems;
        layoutInflater = LayoutInflater.from(context);
        this.currentStationIndex = currentStationIndex;
    }

    @Override
    public int getCount()
    {
        return runItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return runItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = layoutInflater.inflate(R.layout.run_list_row_layout, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.stationName = (TextView) convertView.findViewById(R.id.runStationNameTextView);
        holder.runIcon = (ImageView) convertView.findViewById(R.id.runIconImage);
        holder.departureTime = (TextView) convertView.findViewById(R.id.runStationDepartureTimeTextView);
        holder.stationName.setText(runItems.get(position).getStationName());
        holder.departureTime.setText(runItems.get(position).getStationDepartureTime());


        if (position == currentStationIndex) //if the row we're populating is the current station
        {
            holder.runIcon.setImageResource(R.drawable.run_stop_icon_current_station);  //show the green icon
        } else if (position == 0)
        {
            holder.runIcon.setImageResource(R.drawable.run_stop_icon_start);  //if it is earlier than the current station show the red icon
        } else if (position == runItems.size() - 1)
            holder.runIcon.setImageResource(R.drawable.run_stop_icon_finish);
        else
            holder.runIcon.setImageResource(R.drawable.run_stop_icon);




        convertView.setTag(holder);
        convertView.setBackgroundColor(Color.parseColor("#81CFE0"));
        holder.departureTime.setTextColor(Color.parseColor("#FFFFFF"));
        holder.stationName.setTextColor(Color.parseColor("#FFFFFF"));


        if (position == currentStationIndex)
        {
            holder.departureTime.setTextColor(Color.parseColor("#666666"));
            holder.stationName.setTextColor(Color.parseColor("#666666"));
            convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        }

        return convertView;
    }

    private static class ViewHolder
    {
        TextView departureTime;
        TextView stationName;
        ImageView runIcon;
    }

}

