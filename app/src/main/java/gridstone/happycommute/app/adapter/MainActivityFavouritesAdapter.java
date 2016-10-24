package gridstone.happycommute.app.adapter;
/**
 * Created by Matt on 28/8/2014.
 * Last edited by Matt on 31/8/2014
 * Last edited by Holly on 21/9/2014
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.activity.ShowTransportNearby;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;

public class MainActivityFavouritesAdapter extends BaseAdapter
{
    private ArrayList<FavouriteListItem> listData;
    private LayoutInflater layoutInflater;
    private Context mContext;

    private boolean loadingTime;

    public MainActivityFavouritesAdapter(Context context, ArrayList<FavouriteListItem> listData, boolean loadingTime)
    {
        this.listData = listData;
        mContext = context;
        this.loadingTime = loadingTime;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public Object getItem(int position)
    {
        if (listData != null)
        {
            return listData.get(position -1);
        } else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getCount()
    {
        if (listData != null)
        {
            return listData.size() + 1;
        } else
        {
            return 2;
        }
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
        {
            return 0;
        } else
        {
            return 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

//        If its the first row then its the row with all of the buttons
        if (position == 0)
        {
//            inflate the layout
            convertView = layoutInflater.inflate(R.layout.main_screen_button_layout, parent, false);
            //create ImageButtons for all of the buttons and set them
            ImageButton tramButton = (ImageButton) convertView.findViewById(R.id.tram_button);
            ImageButton busButton = (ImageButton) convertView.findViewById(R.id.bus_button);
            ImageButton trainButton = (ImageButton) convertView.findViewById(R.id.train_button);
            ImageButton nightriderButton = (ImageButton) convertView.findViewById(R.id.nightrider_button);

            final Intent showTransportNearbyIntent = new Intent(mContext, ShowTransportNearby.class);
            //handle the on click event for all 4 buttons
            tramButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "tram");
                    mContext.startActivity(showTransportNearbyIntent);// jump to another activity(Screen)
                }
            });
            trainButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "train");
                    mContext.startActivity(showTransportNearbyIntent);// jump to another activity(Screen)
                }
            });

            busButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "bus");
                    mContext.startActivity(showTransportNearbyIntent);// jump to another activity(Screen)
                }
            });
            nightriderButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showTransportNearbyIntent.putExtra("TRANSPORT_TYPE", "nightrider");
                    mContext.startActivity(showTransportNearbyIntent);// jump to another activity(Screen)
                }
            });

        } else if (listData != null) //else if there are favourites
        {
            ViewHolder holder;

            if (convertView == null)
            {
                //set the view to follow the favourites list row layout
                if (!loadingTime)
                {
                    convertView = layoutInflater.inflate(R.layout.favourite_list_row_layout, parent, false);
                } else
                {
                    convertView = layoutInflater.inflate(R.layout.list_item_loading_row, parent, false);
                }
                //add the 3 fields to a temporary viewholder
                holder = new ViewHolder();
                holder.departureView = (TextView) convertView.findViewById(R.id.departure);
                holder.arrivalView = (TextView) convertView.findViewById(R.id.arrival);

                if (!loadingTime)
                {

                    holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);
                } else
                {
                    holder.loadingBar = (ProgressBar) convertView.findViewById(R.id.loadingProgressBar);
                }

//;
                convertView.setTag(holder);
                convertView.setBackgroundColor(Color.parseColor("#50000000"));

            } else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            //set the values for departures/arrival/arrivaltime within the view holder
            holder.departureView.setText("Departs at: " + listData.get(position - 1).getDepartureStop());
            holder.arrivalView.setText("Arrives at: " + listData.get(position - 1).getArrivalStop());

            if (!loadingTime)
            {
                holder.arrivalTimeView.setText(listData.get(position - 1).getDepartureTime());
            }
        } else if (position == 1 && listData == null)
        {
            //TODO: set a unique layout for this instead of reusing this one
            convertView = layoutInflater.inflate(R.layout.favourite_list_row_layout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.departureView = (TextView) convertView.findViewById(R.id.departure);
            holder.arrivalView = (TextView) convertView.findViewById(R.id.arrival);
            holder.arrivalTimeView = (TextView) convertView.findViewById(R.id.departureTime);
            holder.departureView.setText("No Favourites!");
            holder.departureView.setGravity(Gravity.CENTER_HORIZONTAL);
            holder.arrivalView.setText("");
            holder.arrivalTimeView.setGravity(Gravity.CENTER);
            holder.arrivalTimeView.setText("Please touch to store favourite trips");
            convertView.setTag(holder);
        }

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
