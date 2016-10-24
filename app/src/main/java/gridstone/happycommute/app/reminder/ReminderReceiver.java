package gridstone.happycommute.app.reminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import gridstone.happycommute.app.R;
import gridstone.happycommute.app.activity.ShowReminders;
import gridstone.happycommute.app.favourites.SharedPreference;
import gridstone.happycommute.app.model.Departure;

/**
 * Created by CHRIS on 26/09/2014.
 */
public class ReminderReceiver extends BroadcastReceiver
{

    private SharedPreference sharedPreference;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Departure departure = (Departure) intent.getSerializableExtra("departure");
        this.sharedPreference = new SharedPreference();
        this.context = context;
        Calendar now = GregorianCalendar.getInstance();
        deleteReminder(departure.getTime_timetable_utc());

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Notification")
                        .setContentText("Your Train Is Coming.");

        Intent resultIntent = new Intent(context, ShowReminders.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ShowReminders.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(alert == null){
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        final Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), alert);
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolumeAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int maxVolumeRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolumeAlarm, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        r.play();
        Toast.makeText(context.getApplicationContext(), "Alarm started", Toast.LENGTH_LONG).show();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        AlertDialog dialog = new AlertDialog.Builder(context).
                                            setTitle("Reminder")
                                            .setMessage("Remind for: \nLine: " + departure.getPlatform().getDirection().getLine().getLine_name() + "\nStop: " + departure.getPlatform().getStop().getLocation_name() + "\nDepartures at: " + df.format(departure.getTime_timetable_utc()))
                                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener()
                                            {
                                                public void onClick(DialogInterface dialog, int whichButton)
                                                {
                                                    r.stop();
                                                    dialog.dismiss();
                                                }
                                            }).create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

    }

    public void deleteReminder(Date time)
    {
        sharedPreference.removeReminder(context, time);
    }
}
