package gridstone.happycommute.app.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListAdapter;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListItem;
import gridstone.happycommute.app.adapter.favouriteAdapter.FavouriteListPopulator;
import gridstone.happycommute.app.database.DatabaseHelper;
import gridstone.happycommute.app.favourites.FavoriteManipulater;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.FavoriteJourney;
import gridstone.happycommute.app.model.StoppingPattern;
import gridstone.happycommute.app.model.TransportType;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Matt  on 1/04/2014.
 * Edited by Chris 13/09/2014
 * Last edited by Matt on /9/2014
 */

public class FavouriteJourneys extends ActionBarActivity
{
    private final Context context = this;
    private ProgressDialog progressDialog;
    private ActionBar actionBar;
    private SharedPreference sharedPreference;
    private FavoriteManipulater favoriteManipulater;
    private DatabaseHelper db;
    private AutoCompleteTextView departureTextView;
    private AutoCompleteTextView arrivalTextView;
    private FavouriteListPopulator favourites;
    private ArrayList<ToggleButton> toggleButtons = new ArrayList<ToggleButton>();
    private ArrayList<FavoriteJourney> favoriteJourneyArrayList;
    private ArrayAdapter<String> autoCompleteAdapter;
    private ListView favouritesListView;
    private ArrayList<FavouriteListItem> favouriteListItems = new ArrayList<FavouriteListItem>();
    private boolean connectingLineFound = false;
    private FavouriteListAdapter favouritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_journeys);
        progressDialog = new ProgressDialog(context);
        this.actionBar = getSupportActionBar();
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + "Favourite Journeys" + "</font></b>"));
        this.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));

        this.db = new DatabaseHelper(this);
        this.favouritesListView = (ListView) findViewById(R.id.fav_list);
        //set a listener to handle the long click on an item, and to check it when this occurs

        setListSelection();
        showFavourite();
    }
    //Sets the selection mode to multichoice and handles the onclick events to allow selecting and deleting favourites
    private void setListSelection()
    {
        favouritesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        favouritesListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
        {


            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
            {
                // Set the CAB title according to total checked items
                mode.setTitle(favouritesListView.getCheckedItemCount() + " Selected");
                favouritesAdapter.toggleSelection(position); //toggle the selection
                Log.d("position changed",String.valueOf(position));
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId())
                {
                    case R.id.delete_favourite:
                        showDeleteConfirmation(mode); //if delete show confirmation
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.favourite_journeys_delete_contextual, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                favouritesAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
    }

    public void showDeleteConfirmation(final ActionMode mode)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int buttonPressed)
            {
                if (buttonPressed == DialogInterface.BUTTON_POSITIVE)
                {
                    deleteSelectedItems();
                    finish();
                    startActivity(getIntent());
                    mode.finish(); // Action picked, so close the CAB
                }
                mode.finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete favourites?").setMessage("Are you sure you want to delete the selected favourites?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteSelectedItems()
    {
        //TODO: confirmation dialog
        SparseBooleanArray checkedItemIds = favouritesAdapter.getSelectedIds();

        for (int i = 0; i < checkedItemIds.size(); i++)
        {
            if (checkedItemIds.valueAt(i)) //if true then the item was selected for removal
            {
                Log.d("Deleting ", String.valueOf(i));
                deleteFavourite(((FavouriteListItem) favouritesListView.getItemAtPosition(checkedItemIds.keyAt(i))).getDepartureStop(), ((FavouriteListItem) favouritesListView.getItemAtPosition(checkedItemIds.keyAt(i))).getArrivalStop());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favourite_journeys, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId())
        {
            case (R.id.add_fav):
                showAddFavouritesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //this class will show a dialogue with the required fields needed to add a favourite journey to the shared preferences
    public void showAddFavouritesDialog()
    {
        //declare all of the views, buttons, and autocompletetextviews needed
        final View addView = getLayoutInflater().inflate(R.layout.favourites_transport_selection_dialog, null);

        this.departureTextView = (AutoCompleteTextView) addView.findViewById(R.id.autoCompleteDeparture);
        this.arrivalTextView = (AutoCompleteTextView) addView.findViewById(R.id.autoCompleteArrival);

        final ToggleButton trainToggle = (ToggleButton) addView.findViewById(R.id.toggleButtonTrain);
        final ToggleButton tramToggle = (ToggleButton) addView.findViewById(R.id.toggleButtonTram);
        final ToggleButton busToggle = (ToggleButton) addView.findViewById(R.id.toggleButtonBus);
        final ToggleButton nightriderToggle = (ToggleButton) addView.findViewById(R.id.toggleButtonNightrider);
        //this.getSuggestionsAsync.execute();    //set the autocomplete adapter to the default of train
        getSuggestion();

        //when the selection is changed to a different transport mode :
        final CompoundButton.OnCheckedChangeListener changeChecker = new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    if (buttonView == trainToggle)
                    {
                        tramToggle.setChecked(false);
                        busToggle.setChecked(false);
                        nightriderToggle.setChecked(false);
                    }
                    if (buttonView == tramToggle)
                    {
                        trainToggle.setChecked(false);
                        busToggle.setChecked(false);
                        nightriderToggle.setChecked(false);
                    }
                    if (buttonView == busToggle)
                    {
                        tramToggle.setChecked(false);
                        trainToggle.setChecked(false);
                        nightriderToggle.setChecked(false);
                    }
                    if (buttonView == nightriderToggle)
                    {
                        tramToggle.setChecked(false);
                        busToggle.setChecked(false);
                        trainToggle.setChecked(false);
                    }

                    departureTextView.setText("");
                    arrivalTextView.setText("");
                    //getSuggestionsAsync = new getSuggestionsAsyncAdapter(); //Reget and attatch a new autocorrect aapter based on the clicked button
                    //getSuggestionsAsync.execute();
                    getSuggestion();
                }
            }
        };
        //set the listener for each of the toggle buttons
        trainToggle.setOnCheckedChangeListener(changeChecker);
        tramToggle.setOnCheckedChangeListener(changeChecker);
        busToggle.setOnCheckedChangeListener(changeChecker);
        nightriderToggle.setOnCheckedChangeListener(changeChecker);


        //add the togglebuttons to an arraylist for us to iterate through and see which one is checked
        this.toggleButtons.add(trainToggle);
        this.toggleButtons.add(nightriderToggle);
        this.toggleButtons.add(busToggle);
        this.toggleButtons.add(tramToggle);


        //set the adapter here for the default state before a selection has been made
        this.departureTextView.setAdapter(this.autoCompleteAdapter);
        this.arrivalTextView.setAdapter(this.autoCompleteAdapter);
        this.departureTextView.requestFocus();   //set focus on the departureTextView field
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);  //show the keyboard
        this.departureTextView.setThreshold(0);  //show the autocorrect suggestions when >0 characters are entered
        this.arrivalTextView.setThreshold(0);

        arrivalTextView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                Log.d("option chosen: ", (String.valueOf(position)));
            }


        });
        //when switching between text fields
        this.departureTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    autoCompleteAdapter.notifyDataSetChanged();
                    departureTextView.showDropDown();
                }
            }
        });

        arrivalTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    autoCompleteAdapter.notifyDataSetChanged();
                    arrivalTextView.showDropDown();
                }
            }
        });

        arrivalTextView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    savePressed();
                }
                return false;
            }
        });

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Save Favourite Journey").setView(addView);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                savePressed();
            }
        }).setNegativeButton("Cancel", null).show();
    }

    private void savePressed()
    {
        if(this.departureTextView.getText().toString().contains("(") && this.arrivalTextView.getText().toString().contains("("))
        {
            if (!db.checkStationExists(getSelectedTransportType(toggleButtons), departureTextView.getText().toString().substring(0, departureTextView.getText().toString().indexOf("("))) || (!db.checkStationExists(getSelectedTransportType(toggleButtons), arrivalTextView.getText().toString().substring(0, arrivalTextView.getText().toString().indexOf("(")))))
            {
                //build a new timeSelectionDialog to notify the user
                AlertDialog.Builder notifyFailureToSave = new AlertDialog.Builder(FavouriteJourneys.this);
                notifyFailureToSave.setTitle("Station not found");
                TextView failedNotification = new TextView(FavouriteJourneys.this);
                failedNotification.setText("Please try using the auto correct suggestion");
                notifyFailureToSave.setView(failedNotification);
                notifyFailureToSave.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //hide the keyboard when we dismiss the timeSelectionDialog
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                });
                notifyFailureToSave.show();
            } else //otherwise the stations are invalid
            {
                //TODO: Insert validation to make sure that the two stations intercept
                addFavourite();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);

            }
        }
        else
        {
            //build a new timeSelectionDialog to notify the user
            AlertDialog.Builder notifyFailureToSave = new AlertDialog.Builder(FavouriteJourneys.this);
            notifyFailureToSave.setTitle("Station not found");
            TextView failedNotification = new TextView(FavouriteJourneys.this);
            failedNotification.setText("Please try using the auto correct suggestion");
            notifyFailureToSave.setView(failedNotification);
            notifyFailureToSave.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //hide the keyboard when we dismiss the timeSelectionDialog
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(arrivalTextView.getWindowToken(), 0);
                    dialog.dismiss();
                }
            });
            notifyFailureToSave.show();
        }
    }

    //return the transport type as an integer corresponding to the ptv api requirements based on the togglebuttons
    private Integer getSelectedTransportType(ArrayList<ToggleButton> toggleButtons)
    {
        if (toggleButtons.get(0).isChecked())
        {
            return 0;
        } else if (toggleButtons.get(1).isChecked())
        {
            return 4;
        } else if (toggleButtons.get(2).isChecked())
        {
            return 2;
        } else
        {
            return 1;
        }
    }

    public void deleteFavourite(String departureStop, String arrivalStop)
    {
        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.removeFavorite(context, departureStop, arrivalStop);
    }

    public void deleteAllFavourites()
    {
        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.clearAllFavourites(context);
    }

    public void saveFavourite()
    {
        SharedPreference sharedPreference = new SharedPreference();

        ArrayList<FavoriteJourney> savedFavourites = sharedPreference.getFavorite(context);

        boolean duplicated = false;
        //Characters in the text field before the first opening parenthisis constitute the location name
        String startLocation = departureTextView.getText().toString().substring(0, departureTextView.getText().toString().indexOf("("));
        String arrivalLocation = arrivalTextView.getText().toString().substring(0, arrivalTextView.getText().toString().indexOf("("));
        //Characters within the text fields constitute the suburb
        String startSuburb = departureTextView.getText().toString().substring(departureTextView.getText().toString().indexOf("(") + 1, departureTextView.getText().toString().indexOf(")"));
        String arrivalSuburb = arrivalTextView.getText().toString().substring(arrivalTextView.getText().toString().indexOf("(") + 1, arrivalTextView.getText().toString().indexOf(")"));

        if (savedFavourites.size() > 0) //check no duplicated save
        {
            for (FavoriteJourney favoriteJourney : savedFavourites)
            {
                if (favoriteJourney.getArrival().getLocation_name().toUpperCase().equals(arrivalLocation.toUpperCase()) && favoriteJourney.getStart().getLocation_name().toUpperCase().equals(startLocation.toUpperCase()))
                {
                    //alertMessageDuplicatedTrips();
                    duplicated = true;
                    break;
                }
            }
        }

        if (!duplicated)
        {
            int startLocationStopId = 0;
            startLocationStopId = this.db.getStopIDFromStationName(startLocation, startSuburb, getSelectedTransportType(toggleButtons));
            int arrivalLocationStopId = 0;
            arrivalLocationStopId = this.db.getStopIDFromStationName(arrivalLocation, arrivalSuburb, getSelectedTransportType(toggleButtons));

            ArrayList<StoppingPattern> sp;

            if (startLocation.equals("") || arrivalLocation.equals(""))
            {
                Toast.makeText(context, "Please enter all the stops names", Toast.LENGTH_SHORT).show();
            } else if (startLocationStopId == 0 || arrivalLocationStopId == 0)
            {
                Toast.makeText(context, "Cannot find the input stops", Toast.LENGTH_SHORT).show();
            } else
            {
                this.favoriteManipulater = new FavoriteManipulater(startLocationStopId, arrivalLocationStopId, null, getSelectedTransportType(toggleButtons));
                sp = this.favoriteManipulater.findFavorite(true);


                if (sp.size() >= 2)
                {
                    this.connectingLineFound = true;
                    FavoriteJourney favoriteJourney = new FavoriteJourney();
                    favoriteJourney.setStart(sp.get(0).getPlatform().getStop());
                    favoriteJourney.setArrival(sp.get(1).getPlatform().getStop());
                    sharedPreference.addFavorite(this, favoriteJourney);
                } else
                {
                    this.connectingLineFound = false;
                }
            }
        } else
        {
            Toast.makeText(context, "Duplicated favourite, please try again", Toast.LENGTH_SHORT).show();
        }
    }


    public void showFavourite()
    {
        this.sharedPreference = new SharedPreference();
        this.favoriteJourneyArrayList = this.sharedPreference.getFavorite(this.context);
        this.favourites = new FavouriteListPopulator(this.context, 0, this.favoriteJourneyArrayList, true);
        this.favouriteListItems = favourites.getFavouriteList();
        favouritesAdapter = new FavouriteListAdapter(context, favouriteListItems, true);
        this.favouritesListView.setAdapter(favouritesAdapter);

        //check the item that is pressed when we enter CAB mode
        favouritesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id)
            {
                favouritesAdapter.toggleSelection(position);
                return true;
            }
        });


        Subscriber<ArrayList<FavouriteListItem>> subscriber = new Subscriber<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void onNext(ArrayList<FavouriteListItem> favouriteListItems)
            {
                if(favouriteListItems.size() == 0)
                {
                    setContentView(R.layout.activity_favourite_journeys_no_favourite);
                }
                else
                {
                    favouritesAdapter = new FavouriteListAdapter(context, favouriteListItems, false);
                    favouritesListView.setAdapter(favouritesAdapter);
                }

            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}

        };

        //observable object will be executed in background thread.
        //afterwards, when the observable object has finished executed its task,
        // it will transfer the result of calculation to its subscriber in main thread and the subscriber will update the UI in onNext()
        getFavouriteListItems().subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }

    public Observable<ArrayList<FavouriteListItem>> getFavouriteListItems()
    {
        return Observable.create(new Observable.OnSubscribe<ArrayList<FavouriteListItem>>()
        {
            @Override
            public void call(Subscriber<? super ArrayList<FavouriteListItem>> sub)
            {
                favourites = new FavouriteListPopulator(context, 0, favoriteJourneyArrayList, false);
                favouriteListItems = favourites.getFavouriteList();
                try
                {
                    String lineString;
                    for (FavouriteListItem favouriteListItem : favouriteListItems)
                    {
                        //Gets the substring from index 0 to the index of the second hyphen
                        lineString = favouriteListItem.getLineName();
                        favouriteListItem.setLineName(lineString);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                sub.onNext(favouriteListItems);
                sub.onCompleted();
            }
        });
    }

    public void getSuggestion()
    {
        Subscriber<String> subscriber = new Subscriber<String>()
        {
            @Override
            public void onNext(String result)
            {
                departureTextView.setAdapter(autoCompleteAdapter);  //set the autocomplete adapter to both departure and arrival
                arrivalTextView.setAdapter(autoCompleteAdapter);
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}
        };

        Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> sub)
            {
                autoCompleteAdapter = db.getAutocompleteAdapter((TransportType) TransportType.values()[getSelectedTransportType(toggleButtons)]); //get the right adapter for the transportmode in question
                sub.onNext("operation finished");
                sub.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }

    public void addFavourite()
    {
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMessage("Adding Favourite...");
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setProgress(0);
        this.progressDialog.show();
        Subscriber<String> subscriber = new Subscriber<String>()
        {
            @Override
            public void onNext(String result)
            {
                if (connectingLineFound)
                {
                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(FavouriteJourneys.this, FavouriteJourneys.class));
                } else
                {
                    Toast.makeText(FavouriteJourneys.this, "Cannot find the connecting lines, please try again later.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(FavouriteJourneys.this, FavouriteJourneys.class));
                }
            }

            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable error) {}
        };

        Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> sub)
            {
                saveFavourite();
                sub.onNext("operation finished");
                sub.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(subscriber);
    }
}