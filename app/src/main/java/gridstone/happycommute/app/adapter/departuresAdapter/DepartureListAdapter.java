package gridstone.happycommute.app.adapter.departuresAdapter;
/**
 * Created by Matt on 6/5/2014.
 * Last modified 21/10 by Matt
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import gridstone.happycommute.app.R;

public class DepartureListAdapter extends BaseAdapter
{

    private final ArrayList<DepartureListItem> listData;

    private LayoutInflater layoutInflater;
    private ArrayList<String> destTime;
    private boolean loading;
    private String headerString = "asd";

    public DepartureListAdapter(Context context, ArrayList<DepartureListItem> listData, ArrayList destTime, String headerString, boolean loading)
    {
        this.destTime = destTime;
        this.listData = listData;
        this.loading = loading;
        this.headerString = headerString;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return listData.size() + 1;
    }

    @Override
    public Object getItem(int position)
    {
        return listData.get(position -1);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent)
    {

        if (position == 0)
        {
            ViewHolder holder;
            convertView = layoutInflater.inflate(R.layout.departures_listview_header, parent, false);
            holder = new ViewHolder();
            holder.lineNameView = (TextView) convertView.findViewById(R.id.departuresHeaderString);
            convertView.setTag(holder);
            holder.lineNameView.setText(headerString);

////            headerViewHolder holder = new headerViewHolder();
//            convertView = layoutInflater.inflate(R.layout.departures_listview_header, parent, false);
//            holder.headerString.setText(headerString);
//            holder.headerString = (TextView) convertView.findViewById(R.id.departuresHeaderString);
            return convertView;
        } else if (position > 0)
        {
            ViewHolder holder;

            if (loading)
            {
                convertView = layoutInflater.inflate(R.layout.list_item_loading_row, parent, false);
                holder = new ViewHolder();
                holder.lineNameView = (TextView) convertView.findViewById(R.id.departure);
                convertView.setTag(holder);
                holder.lineNameView.setText("Loading...");
                convertView.setBackgroundColor(Color.parseColor("#81CFE0"));
                return convertView;

            } else
            {
                convertView = layoutInflater.inflate(R.layout.departure_list_row_layout, parent, false);
                holder = new ViewHolder();
                holder.lineNameView = (TextView) convertView.findViewById(R.id.line);
                holder.platformNumberView = (TextView) convertView.findViewById(R.id.direction);
                holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);
                holder.destTimeView = (TextView) convertView.findViewById(R.id.destTime);
                holder.progressionArrowsImageView = (ImageView) convertView.findViewById(R.id.progressArrowsImageView);


                convertView.setTag(holder);
            }
//            holder = (ViewHolder) convertView.getTag();

            holder.lineNameView.setText(listData.get(position -1).getLineName());
            holder.platformNumberView.setText(listData.get(position -1).getTransportDirection());
            holder.arrivalTimeView.setText(listData.get(position -1).getArrival());
            if (destTime != null)
                holder.destTimeView.setText(destTime.get(position -1).toString());
            else
                holder.destTimeView.setText(listData.get(position -1).getDestTime());

            //set white colour for imminent departures
            if (holder.arrivalTimeView.getText().equals("NOW") || holder.arrivalTimeView.getText().equals("1 min"))
            {
                holder.progressionArrowsImageView.setImageResource(R.drawable.ui_progress_arrows_grey);
                holder.lineNameView.setTextColor(Color.parseColor("#666666"));
                holder.platformNumberView.setTextColor(Color.parseColor("#666666"));
                holder.arrivalTimeView.setTextColor(Color.parseColor("#666666"));
                if (holder.destTimeView != null)
                    holder.destTimeView.setTextColor(Color.parseColor("#666666"));
                convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else
            //set colours
            {
                holder.progressionArrowsImageView.setImageResource(R.drawable.ui_progress_arrows_white);
                holder.lineNameView.setTextColor(Color.parseColor("#FFFFFF"));
                holder.platformNumberView.setTextColor(Color.parseColor("#FFFFFF"));
                holder.arrivalTimeView.setTextColor(Color.parseColor("#FFFFFF"));
                if (holder.destTimeView != null)
                    holder.destTimeView.setTextColor(Color.parseColor("#FFFFFF"));
                convertView.setBackgroundColor(Color.parseColor("#81CFE0"));
            }
        }
        return convertView;

    }

    private static class ViewHolder
    {
        TextView lineNameView;
        TextView platformNumberView;
        TextView arrivalTimeView;
        TextView destTimeView;
        ImageView progressionArrowsImageView;
        ProgressBar loadingBar;
    }
    private static class headerViewHolder
    {
        TextView headerString;
    }

}
