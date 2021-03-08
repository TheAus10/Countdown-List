package aus10.firstApp.countdownList;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class listAdapter extends ArrayAdapter<eventInfo> {
    private static final String TAG = "listAdapter";                        // activity tag

    private Context mContext;                                               // activity context
    int mResource;                                                          // activity resource

    public listAdapter(@NonNull Context context, int resource, @NonNull ArrayList<eventInfo> objects) {
        super(context, resource, objects);

        // setting context and resource
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // getting all attributes of the selected item
        int    id       = getItem(position).getID();
        long   endTime  = getItem(position).getEndTime();
        long   timeLeft = getItem(position).getTimeLeft();
        String title    = getItem(position).getTitle();
        CountDownTimer countDownTimer = getItem(position).getCountDownTimer();

        // new object
        eventInfo event = new eventInfo(id, title, endTime, countDownTimer, timeLeft);

        // converting the view to use the list adapter
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvTitle = convertView.findViewById(R.id.titleTextView);
        TextView tvTime  = convertView.findViewById(R.id.timeTextView);


        // converting the time left to  days, hours and minutes
        int day     = (int) (event.getTimeLeft() / 86400000);
        int hour    = (int) (event.getTimeLeft() % 86400000) / 3600000;
        int minute  = (int) ((event.getTimeLeft() % 86400000) % 3600000) / 60000;
        int seconds = (int) (((event.getTimeLeft() % 86400000) % 3600000) % 60000) / 1000;

        // formatting the output time to only show the two most relevant fields for each event
        String timeLeftText = "";

        // timeLeftText = X day(s) X hour(s)
        if (day > 1) {
            timeLeftText = day + " days ";
            if (hour > 1) timeLeftText += hour + " hours ";
            else if (hour == 1) timeLeftText += hour + " hour ";
        }
        else if (day == 1) {
            timeLeftText = day + " day ";
            if (hour > 1) timeLeftText += hour + " hours ";
            else if (hour == 1) timeLeftText += hour + " hour ";
        }
        // timeLeftText = X hour(s) X minute(s)
        else if (hour > 1) {
            timeLeftText = hour + " hours ";
            if (minute > 1) timeLeftText += minute + " minutes ";
            else if (minute == 1) timeLeftText += minute + " minute ";
        }
        else if (hour == 1) {
            timeLeftText = hour + " hour ";
            if (minute > 1) timeLeftText += minute + " minutes ";
            else if (minute == 1) timeLeftText += minute + " minute ";
        }
        // timeLeftText = X minutes(s) X seconds(s)
        else if (minute > 1) {
            timeLeftText = minute + " minutes ";
            if (seconds > 1) timeLeftText += seconds + " seconds ";
            else if (seconds == 1) timeLeftText += seconds + " second ";
        }
        else if (minute == 1) {
            timeLeftText = minute + " minute ";
            if (seconds > 1) timeLeftText += seconds + " seconds ";
            else if (seconds == 1) timeLeftText += seconds + " second ";
        }
        else {
            if (seconds > 1 || seconds == 0) timeLeftText = seconds + " seconds ";
            else if (seconds == 1) timeLeftText = seconds + " second ";
        }

        // showing title and the time left
        tvTitle.setText(title);
        tvTime.setText(timeLeftText);

        return convertView;
    }

}
