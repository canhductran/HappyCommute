package gridstone.happycommute.app.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.adapter.departuresAdapter.DepartureListItem;
import gridstone.happycommute.app.adapter.reminderAdapter.ReminderAdapter;
import gridstone.happycommute.app.adapter.reminderAdapter.ReminderListPopulator;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.Departure;
import gridstone.happycommute.app.reminder.ReminderReceiver;

/**
 * Created by CHRIS on 26/09/2014.
 */
public class ShowReminders extends ActionBarActivity
{
    private ActionBar actionBar;
    private SharedPreference sharedPreference;
    private ArrayList<Departure> departureArrayList;
    private Context context;
    private ListView reminderListView;
    private ReminderListPopulator reminderListPopulator;
    private ArrayList<DepartureListItem> departuresListItems;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        this.context = this;
        this.actionBar = getSupportActionBar();
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        this.actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + "Reminders" + "</font></b>"));
        this.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));

        this.sharedPreference = new SharedPreference();
        this.departureArrayList = this.sharedPreference.getReminders(this.context);

        reminderListPopulator = new ReminderListPopulator(departureArrayList);

        departuresListItems = reminderListPopulator.getDeparturesList();
        if(departuresListItems.size() == 0)
        {
            setContentView(R.layout.activity_reminder_no_reminder);
        }
        else
        {
            reminderListView = (ListView) findViewById(R.id.reminder_list);
            reminderListView.setAdapter(new ReminderAdapter(context, departuresListItems, null));
            registerForContextMenu(reminderListView);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.reminder_list)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.reminder_options, menu);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DepartureListItem reminderListItem = (DepartureListItem) reminderListView.getItemAtPosition(info.position);

        switch (item.getItemId())
        {
            case R.id.delete:
                deleteReminder(reminderListItem.getDepartureItem().getTime_timetable_utc());
                finish();
                startActivity(getIntent());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void deleteReminder(Date time)
    {
        Intent intent = new Intent(context, ReminderReceiver.class);
        Uri data = Uri.withAppendedPath(
                Uri.parse("myapp://myapp/Id/#"),
                String.valueOf(time.toString()));
        intent.setData(data);

        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.removeReminder(context, time);
        PendingIntent sender = PendingIntent.getBroadcast(this, (int) time.getTime(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Toast.makeText(this, "Cancelled alarm", Toast.LENGTH_SHORT).show();
    }

    public void deleteAllReminders()
    {
        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.clearAllReminders(context);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Toast.makeText(this, "Cancelled alarm", Toast.LENGTH_SHORT).show();
    }
}
