package gridstone.happycommute.app.adapter.NavigationAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import gridstone.happycommute.app.R;


/**
 * Created by Matt on 23/9/2014.
 */
public class NavigationDrawerAdapter extends BaseAdapter {
    private ArrayList<NavigationDrawerItem> navOptions; //Arraylist of items to appear in the navigation drawer
    private LayoutInflater layoutInflater;

    public NavigationDrawerAdapter(Context context, ArrayList<NavigationDrawerItem> navOptions)
    {
        this.navOptions = navOptions;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return navOptions.size();
    }

    @Override
    public Object getItem(int position)
    {
        return navOptions.get(position);
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
            convertView = layoutInflater.inflate(R.layout.navigation_drawer_item, parent, false);
            holder = new ViewHolder();
            holder.optionName = (TextView) convertView.findViewById(R.id.navigationItemName);
            holder.navigationIcon = (ImageView) convertView.findViewById(R.id.navigationItemIcon);
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.optionName.setText(navOptions.get(position).getOptionName()); //set the textview to the option name of the nav item
        holder.navigationIcon.setImageDrawable(convertView.getResources().getDrawable(navOptions.get(position).getOptionIconResourceID())); //set the nav icon from the arraylist

        return convertView;
    }

    private static class ViewHolder
    {
        ImageView navigationIcon;
        TextView optionName;
    }

}

