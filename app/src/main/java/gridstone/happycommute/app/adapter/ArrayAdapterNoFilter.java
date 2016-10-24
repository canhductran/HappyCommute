package gridstone.happycommute.app.adapter;

/**
 * Created by CHRIS on 27/09/2014.
 */

/**
 * Class which does not perform any filtering. Filtering is already done by
 * the web service when asking for the list, so there is no need to do any
 * more as well. This way, ArrayAdapter.mOriginalValues is not used when
 * calling e.g. ArrayAdapter.add(), but instead ArrayAdapter.mObjects is
 * updated directly and methods like getCount() return the expected result.
 */

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

public class ArrayAdapterNoFilter extends ArrayAdapter<String>
{

    public ArrayAdapterNoFilter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    private static final NoFilter NO_FILTER = new NoFilter();

    @Override
    public Filter getFilter() {
        return NO_FILTER;
    }


    private static class NoFilter extends Filter
    {
        protected FilterResults performFiltering(CharSequence prefix) {
            return new FilterResults();
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Do nothing
        }
    }
}