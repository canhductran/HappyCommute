package gridstone.happycommute.app.adapter.favouriteAdapter;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import gridstone.happycommute.app.R;

/**
 * Created by CHRIS on 27/08/2014.
 * Last edited by Holly on 21/9/2014
 */
public class FavouriteListAdapter extends BaseAdapter
{
    private ArrayList<FavouriteListItem> listData;
    private LayoutInflater layoutInflater;
    private boolean loadingTime;
    private Context mContext;
    private SparseBooleanArray mSelectedItemsIds;
    public FavouriteListAdapter(Context context, ArrayList<FavouriteListItem> listData, boolean loadingTime)
    {
        this.listData = listData;
        this.layoutInflater = LayoutInflater.from(context);
        this.loadingTime = loadingTime;
        this.mContext = context;
        mSelectedItemsIds = new SparseBooleanArray();
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

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            if (!loadingTime)
            {
                convertView = layoutInflater.inflate(R.layout.favourite_list_row_layout, parent, false);
            } else
            {
                convertView = layoutInflater.inflate(R.layout.list_item_loading_row, parent, false);
            }
            holder = new ViewHolder();
            holder.departureView = (TextView) convertView.findViewById(R.id.departure);
            holder.arrivalView = (TextView) convertView.findViewById(R.id.arrival);
            holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);

            if (!loadingTime)
            {
                holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);
            } else
            {
                holder.loadingBar = (ProgressBar) convertView.findViewById(R.id.loadingProgressBar);
            }


            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.departureView.setText("Departs at: " + listData.get(position).getDepartureStop());
        holder.arrivalView.setText("Arrives at: " + listData.get(position).getArrivalStop());

        if (mSelectedItemsIds.get(position)) //if selected
        {
            if (!loadingTime)
            {
                holder.arrivalTimeView.setText(listData.get(position).getDepartureTime());
                holder.arrivalTimeView.setTextColor(Color.parseColor("#666666"));
            }
            holder.departureView.setTextColor(Color.parseColor("#666666"));
            holder.arrivalView.setTextColor(Color.parseColor("#666666"));
            if (!loadingTime)
                holder.arrivalTimeView.setTextColor(Color.parseColor("#666666"));
            convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else
        {
            if (!loadingTime)
            {
                holder.arrivalTimeView.setText(listData.get(position).getDepartureTime());
                holder.arrivalTimeView.setTextColor(Color.parseColor("#666666"));
            }

            holder.departureView.setTextColor(Color.parseColor("#FFFFFF"));
            holder.arrivalView.setTextColor(Color.parseColor("#FFFFFF"));
            if (!loadingTime)
                holder.arrivalTimeView.setTextColor(Color.parseColor("#FFFFFF"));

            convertView.setBackgroundColor(Color.parseColor("#81CFE0"));

        }
//        }
        return convertView;
    }
    private static class ViewHolder

    {
        TextView departureView;
        TextView arrivalView;
        TextView arrivalTimeView;
        ProgressBar loadingBar;
    }


}
