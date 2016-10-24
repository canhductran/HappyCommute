package gridstone.happycommute.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.ArrayAdapterNoFilter;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListPopulator;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rio on 25/08/2014.
 * Last update by Rio on 25/09/2014
 * Edited by Chris on 26/09/2014
 */
public class SearchLocation extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener, TextWatcher, AdapterView.OnItemSelectedListener
{

    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private Context context;
    private LatLng latLng;
    private final LatLng MELBOURNE = new LatLng(-37.8145, 144.9654);
    private static final int THRESHOLD = 3;
    private List<Address> autoCompleteSuggestionAddresses;
    private ArrayAdapter<String> autoCompleteAdapter;
    private AutoCompleteTextView etLocation;
    private String location;
    private Address selectedAddress;
    private final Locale locale = new Locale("en","AU");
    private ActionBar actionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmap_layout);


        this.actionBar = getActionBar();
        this.actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + "Search Location" + "</font></b>"));
        this.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));
        context = this;

        autoCompleteAdapter = new ArrayAdapterNoFilter(this, R.layout.autocompletelist_item);

        this.etLocation = (AutoCompleteTextView) findViewById(R.id.locationSearch);
        etLocation.addTextChangedListener(this);
        etLocation.setOnItemSelectedListener(this);
        etLocation.setThreshold(THRESHOLD);
        etLocation.setAdapter(autoCompleteAdapter);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        supportMapFragment.getView().setFocusableInTouchMode(true);

        googleMap = supportMapFragment.getMap();

        // Assign Melbourne LatLng to MELBOURNE constant
        // Move Map camera to focus on Melbourne area when map opened initially
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 10));

        // Enabling current location button in the Map
        googleMap.setMyLocationEnabled(true);

        // Enabling on click listener in the map
        googleMap.setOnMapClickListener(this);

        // Enabling on info window click within a marker listener
        googleMap.setOnInfoWindowClickListener(this);

        // Attach button find on the map
        Button btn_find = (Button) findViewById(R.id.btnFind);

        // When clicking the search button this procedure processed
        View.OnClickListener findClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                location = etLocation.getText().toString();

                if(location!=null && !location.equals(""))
                {
                    geoCoderPopulate();
                }
            }
        };

        btn_find.setOnClickListener(findClickListener);

    }


    // Allows add marker in the Map on click
    public void onMapClick(final LatLng point)
    {
        // Assign the selected point to latLng
        latLng = point;
        // Clear any existing marker on the Map
        googleMap.clear();

        // Zoom in to the specified location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

        // Add blue marker into Map to pick desired destination
        Marker setMarker = googleMap.addMarker(new MarkerOptions().position(point).title("Confirm departure location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        // Automatically shows the info window to confirm the location
        setMarker.showInfoWindow();
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface timeSelectionDialog, final int id)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("location", point);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface timeSelectionDialog, final int id) {
                timeSelectionDialog.dismiss();
            }
        });
        builder.show();
*/
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("location", latLng);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 < autoCompleteSuggestionAddresses.size()) {
            Address selected = autoCompleteSuggestionAddresses.get(arg2);
            double latitude = selected.getLatitude();
            double longitude = selected.getLongitude();
            latLng = new LatLng(latitude, longitude);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent resultIntent = new Intent(context, ShowTransportNearby.class);
                String transportTypeString = getIntent().getExtras().getString("TRANSPORT_TYPE");
                resultIntent.putExtra("TRANSPORT_TYPE", transportTypeString);
                context.startActivity(resultIntent);
                return true;

        }
        return(super.onOptionsItemSelected(item));
    }


    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        final String value = arg0.toString();

        if (!"".equals(value) && value.length() >= THRESHOLD) {
            populateSuggestions(value);
        } else {
            autoCompleteAdapter.clear();
        }
    }

    @Override
    public void afterTextChanged(Editable arg0) {
    }

    public void populateSuggestions(String value)
    {
        Subscriber<String> subscriber = new Subscriber<String>()
        {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {

            }

            @Override
            public void onNext(String s)
            {
                autoCompleteAdapter.clear();
                for (Address a : autoCompleteSuggestionAddresses)
                {
                    String addressString = a.getAddressLine(0);
                    String cityString = a.getAddressLine(1);
                    String countryString = a.getAddressLine(2);


                    // Populates the address name to a String
                    //String addressText = String.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", address.getAdminArea(), address.getSubAdminArea(), address.getCountryName());

                    String addressText = addressString + " " + cityString + " " + countryString;

                    String suggestionText = addressText.replace("null", "");
                    autoCompleteAdapter.add(suggestionText);
                }
                autoCompleteAdapter.notifyDataSetChanged();
            }
        };

        findSuggestions(value).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }
    public Observable<String> findSuggestions(String value) {
        final String finalValue = value;

        return Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> sub)
            {
                try
                {
                    autoCompleteSuggestionAddresses = new Geocoder(getBaseContext()).getFromLocationName(finalValue + "Victoria" + "Australia", 10);
                }
                catch (IOException ex)
                {
                    Log.e(this.getClass().getName(), "Failed to get autocomplete suggestions", ex);
                }

                sub.onNext("proceed");
                sub.onCompleted();
            }
        });


    }


    public void geoCoderPopulate()
    {
        Subscriber<List<Address>> subscriber = new Subscriber<List<Address>>()
        {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {

            }

            @Override
            public void onNext(List<Address> addresses)
            {
                if (selectedAddress == null) //means the user enter the location manually
                {
                    // If the address in the search bar can not be find
                    if (addresses == null || addresses.size() == 0)
                    {
                        // Shows text box in the map
                        Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
                    }

                    // Clear the existing markers
                    googleMap.clear();

                    for (int i = 0; i < addresses.size(); i++)
                    {
                        Address address = (Address) addresses.get(i);

                        latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        String addressString = address.getAddressLine(0);
                        String cityString = address.getAddressLine(1);
                        String countryString = address.getAddressLine(2);

                        // Populates the address name to a String
                        //String addressText = String.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", address.getAdminArea(), address.getSubAdminArea(), address.getCountryName());

                        String addressText = addressString + " " + cityString + " " + countryString;
                        // Create new marker and populates it with the searched address
                        markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(addressText);

                        // Add marker to Map
                        googleMap.addMarker(markerOptions);

                        // Zoom in to the specified location
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }
                else // means the user enters location by selecting suggestions
                {
                    latLng = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());

                    // Populates the address name to a String
                    //String addressText = String.format("%s, %s", selectedAddress.getMaxAddressLineIndex() > 0 ? selectedAddress.getAddressLine(0) : "", selectedAddress.getCountryName());

                    String addressString = selectedAddress.getAddressLine(0);
                    String cityString = selectedAddress.getAddressLine(1);
                    String countryString = selectedAddress.getAddressLine(2);
                    String addressText = addressString + " " + cityString + " " + countryString;

                    // Create new marker and populates it with the searched address
                    markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(addressText);

                    // Add marker to Map
                    googleMap.addMarker(markerOptions);

                    // Zoom in to the specified location
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    selectedAddress = null;
                }
            }
        };

        geoCoderSearchLocation().subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
    }
    public Observable<List<Address>> geoCoderSearchLocation()
    {
        return Observable.create(new Observable.OnSubscribe<List<Address>>()
        {
            @Override
            public void call(Subscriber<? super List<Address>> sub)
            {
                Geocoder geocoder = new Geocoder(getBaseContext(), locale);
                List<Address> addresses = null;

                try
                {
                    addresses = geocoder.getFromLocationName(location + "Victoria Australia", 3);
                    location = null;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                sub.onNext(addresses);
            }
        });

    }
}
