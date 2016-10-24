package gridstone.happycommute.app.adapter.reminderAdapter;

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
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListItem;

/**
 * Created by CHRIS on 28/09/2014.
 */
public class ReminderAdapter extends BaseAdapter
{
    private final ArrayList<DepartureListItem> listData;

    private LayoutInflater layoutInflater;
    private ArrayList<String> destTime;

    public ReminderAdapter(Context context, ArrayList<DepartureListItem> listData, ArrayList destTime)
    {
        this.destTime = destTime;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return listData.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.reminder_list_row_layout, parent, false);
            holder = new ViewHolder();
            holder.lineNameView = (TextView) convertView.findViewById(R.id.line);
            holder.platformNumberView = (TextView) convertView.findViewById(R.id.direction);
            holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);
            holder.destTimeView = (TextView) convertView.findViewById(R.id.destTime);
            holder.remindTimeView = (TextView) convertView.findViewById(R.id.remindTime);
            holder.progressionArrowsImageView = (ImageView) convertView.findViewById(R.id.progressArrowsImageView);


            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lineNameView.setText(listData.get(position).getLineName());
        holder.platformNumberView.setText(listData.get(position).getTransportDirection());
        holder.arrivalTimeView.setText(listData.get(position).getDate());
        holder.remindTimeView.setText(listData.get(position).getDepartureTime());
        if (destTime != null)
            holder.destTimeView.setText(destTime.get(position).toString());
//4fc5d3


        holder.progressionArrowsImageView.setImageResource(R.drawable.ui_progress_arrows_white);
        holder.lineNameView.setTextColor(Color.parseColor("#FFFFFF"));
        holder.platformNumberView.setTextColor(Color.parseColor("#FFFFFF"));
        holder.arrivalTimeView.setTextColor(Color.parseColor("#FFFFFF"));
        holder.remindTimeView.setTextColor(Color.parseColor("#FFFFFF"));
        if (holder.destTimeView != null)
            holder.destTimeView.setTextColor(Color.parseColor("#FFFFFF"));
        convertView.setBackgroundColor(Color.parseColor("#81CFE0"));


        return convertView;
    }

    private static class ViewHolder
    {
        TextView lineNameView;
        TextView platformNumberView;
        TextView arrivalTimeView;
        TextView destTimeView;
        TextView remindTimeView;
        ImageView progressionArrowsImageView;
    }

}
