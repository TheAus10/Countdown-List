package aus10.firstApp.countdownList;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";                                                              // activity tag

    public static listAdapter adapter;                                                                             // list adapter instance

    // variables for all XML elements
    private ListView lvEventList;                                                                                   // list of events
    private TextView tvSelectDate;                                                                                  // text for date
    private TextView tvSelectTime;                                                                                  // text for time
    private EditText etSelectTitle;                                                                                 // edit text field for title
    private Button   btnAddEvent;                                                                                   // add event button
    private Button   btnSave;                                                                                       // save button
    private Button   btnCancel;                                                                                     // cancel button

    // variables for array handling
    public static    ArrayList<eventInfo> eventList = new ArrayList<>();                                            // array list for events
    boolean          addEvent = true;                                                                               // tells selectEvents() if it is ok to add event to array list

    // SQLite Database variables
    public static     SQLiteHandler  dbHandler;                                                                     // handler for SQLite db
    public            SQLiteDatabase sqLiteDatabase;                                                                // SQLite db var
    public static int deletedEventID = 0;                                                                           // rowID of a selected event to be deleted
    public static String deletedEventName = "";                                                                     // title of a selected event to be deleted


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creating SQLite database
        dbHandler = new SQLiteHandler(MainActivity.this, "dbEventList", null, 1);
        sqLiteDatabase = dbHandler.getWritableDatabase();
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Events (eventID INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR, endTime BIGINT);");

        // matching each variable to their XML element
        btnAddEvent   = findViewById(R.id.btnAddEvent);
        btnSave       = findViewById(R.id.btnSave);
        btnCancel     = findViewById(R.id.btnCancel);
        tvSelectDate  = findViewById(R.id.tvSelectDate);
        tvSelectTime  = findViewById(R.id.tvSelectTime);
        etSelectTitle = findViewById(R.id.etSelectTitle);
        lvEventList   = findViewById(R.id.eventListView);

        // displaying any saved events from db
        adapter = new listAdapter(getApplicationContext(), R.layout.list_adapter, eventList);
        selectEvents(sqLiteDatabase, "Events");
        lvEventList.setAdapter(adapter);
        setTimers();

        // sorting array so the timers with least endTime are at the top
        Collections.sort(eventList);

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // changing visibilities
                tvSelectDate.setVisibility(View.VISIBLE);
                tvSelectTime.setVisibility(View.VISIBLE);
                etSelectTitle.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnAddEvent.setVisibility(View.INVISIBLE);

                tvSelectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDatePickerDialog();
                    }
                });

                tvSelectTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTimePickerDialog();
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(confirmValidInput()) addEvent();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // restores to original layout
                        layoutReset();
                    }
                });
            }
        });

        // checking for a click on an existing item
        lvEventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                // creating popup menu
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);

                // inflating menu
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // setting what happens on each click
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        // checking which menu option was clicked
                       switch (menuItem.getItemId()){
                           case R.id.edit:                                                  // editing info of event

                               // changing visibilities
                               tvSelectDate.setVisibility(View.VISIBLE);
                               tvSelectTime.setVisibility(View.VISIBLE);
                               etSelectTitle.setVisibility(View.VISIBLE);
                               btnSave.setVisibility(View.VISIBLE);
                               btnCancel.setVisibility(View.VISIBLE);
                               btnAddEvent.setVisibility(View.INVISIBLE);

                               // getting date and time from endTime
                               long eventEndTime = eventList.get(i).getEndTime();
                               String eventDateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm aa").format(new Date(eventEndTime));
                               String[] parsedDateTime = eventDateTime.split(" ", 2);

                               // showing the title, date and time of the selected event
                               etSelectTitle.setText(eventList.get(i).getTitle());
                               tvSelectDate.setText(parsedDateTime[0]);
                               tvSelectTime.setText(parsedDateTime[1]);

                               tvSelectDate.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       showDatePickerDialog();
                                   }
                               });

                               tvSelectTime.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       showTimePickerDialog();
                                   }
                               });

                               // editing event
                               btnSave.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       if(confirmValidInput()) editEvent(eventList.get(i).getID());
                                   }
                               });

                               btnCancel.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       // restores to original layout
                                       layoutReset();
                                   }
                               });

                               return true;
                           case R.id.delete:                                                // deleting event

                               // saving the id of the selected event
                               deletedEventID = eventList.get(i).getID();
                               deletedEventName = eventList.get(i).getTitle();

                               // opening up delete dialog box for confirmation
                               showDeleteDialog();
                               return true;
                           default:
                               return false;
                       }
                    }
                });

                // sets to menu to appear on the right side of screen
                popupMenu.setGravity(Gravity.END);

                // showing menu
                popupMenu.show();
            }
        });
    }

    // confirms all input data from user to be correct
    public boolean confirmValidInput() {
        // setting datetime string
        String datetime = tvSelectDate.getText().toString() + " " + tvSelectTime.getText().toString();

        // checking if any fields are null before allowing a save
        if(tvSelectTime.getText().toString().equals("") || tvSelectDate.getText().toString().equals("") || etSelectTitle.getText().toString().equals(""))
        {
            // sending toast to notify user of error
            Toast.makeText(MainActivity.this, "Please fill empty fields", Toast.LENGTH_SHORT).show();

            // setting all empty fields to red color
            if(tvSelectTime.getText().toString().equals(""))
            {
                tvSelectTime.setHintTextColor(getResources().getColor(R.color.red));
            }
            if(tvSelectDate.getText().toString().equals(""))
            {
                tvSelectDate.setHintTextColor(getResources().getColor(R.color.red));
            }
            if(etSelectTitle.getText().toString().equals(""))
            {
                etSelectTitle.setHintTextColor(getResources().getColor(R.color.red));
            }
            return false;
        }
        // checking that the entered date and time is not in the past and showing error details
        else if(!compareDates(datetime))
        {
            // changing text to red and sending error toast
            tvSelectDate.setTextColor(getResources().getColor(R.color.red));
            tvSelectTime.setTextColor(getResources().getColor(R.color.red));
            Toast.makeText(MainActivity.this, "Date/time cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }
        // all data is good and being saved
        else
        {
            return true;
        }
    }

    // formats data and adds to SQLite DB and ArrayList
    public void addEvent() {
        // setting datetime string
        String datetime = tvSelectDate.getText().toString() + " " + tvSelectTime.getText().toString();

        // converting the input date from string to Date
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        Date selectedDatetime = new Date();
        try {
            selectedDatetime = format.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // converting Date to long
        long endTime = selectedDatetime.getTime();

        // adding to SQLite database
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", etSelectTitle.getText().toString());
        contentValues.put("endTime", endTime);
        sqLiteDatabase.insert("Events", null, contentValues);

        // adding to ArrayList
        eventInfo eventInfoInst = new eventInfo(-1, null, -1, null, -1);      // instance of eventInfo class
        eventInfoInst.setTitle(etSelectTitle.getText().toString());
        eventInfoInst.setEndTime(endTime);
        eventList.add(eventInfoInst);

        // setting the new timer
        setTimers();

        // sorting array so the timers with least endTime are at the top
        Collections.sort(eventList);

        // updating adapter
        adapter.notifyDataSetChanged();

        // resetting layout
        layoutReset();
    }

    // formats data and updates SQLite DB and ArrayList to match
    public void editEvent(int id) {

        String datetime = tvSelectDate.getText().toString() + " " + tvSelectTime.getText().toString();

        // converting the input date from string to Date
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        Date selectedDatetime = new Date();
        try {
            selectedDatetime = format.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // converting Date to long
        long endTime = selectedDatetime.getTime();

        // adding to SQLite database
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", etSelectTitle.getText().toString());
        contentValues.put("endTime", endTime);
        sqLiteDatabase.update("Events", contentValues, "eventID = ?", new String[]{String.valueOf(id)});

        // updating the array list
        for (int i = 0; i < eventList.size(); i++)
        {
            if(eventList.get(i).getID() == id)
            {
                eventList.get(i).setTitle(etSelectTitle.getText().toString());
                eventList.get(i).setEndTime(endTime);
                eventList.get(i).setCountDownTimer(null);
                eventList.get(i).setTimeLeft(-1);
            }
        }

        // resetting timers so the new one can be updated
        cancelTimers();
        setTimers();

        // sorting array so the timers with least endTime are at the top
        Collections.sort(eventList);

        // updating adapter
        adapter.notifyDataSetChanged();

        // resetting layout
        layoutReset();
    }

    // shows the date picker dialog box
    public void showDatePickerDialog() {

        // creating the date picker dialog with current date

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                        // gets date from calendar and sets the date textView
                        month += 1;
                        String date = month + "/" + dayOfMonth +"/" + year;
                        tvSelectDate.setText(date);
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // shows time picker dialog and sets the TextView field
    public void showTimePickerDialog() {

        // creating time picker dialog and setting the textView from the selected time
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

                        // creating calendar instance to save the time with
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0,0,0,hourOfDay,minute);
                        tvSelectTime.setText(DateFormat.format("hh:mm aa",calendar));
                    }
                }, 0, 0, false
        );
        timePickerDialog.show();
    }

    // resets visibilities, texts, and colors for the add event section
    public void layoutReset() {
        // resetting visibilities
        tvSelectDate.setVisibility(View.INVISIBLE);
        tvSelectTime.setVisibility(View.INVISIBLE);
        etSelectTitle.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnAddEvent.setVisibility(View.VISIBLE);

        // clearing any text in the textViews
        tvSelectDate.setText(null);
        tvSelectTime.setText(null);
        etSelectTitle.setText(null);

        // resetting hint color
        etSelectTitle.setHintTextColor(getResources().getColor(R.color.colorAccent));
        tvSelectTime.setHintTextColor(getResources().getColor(R.color.colorAccent));
        tvSelectDate.setHintTextColor(getResources().getColor(R.color.colorAccent));

        // resetting text color
        etSelectTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tvSelectTime.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tvSelectDate.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    // retrieves all rows from db and stores them into the eventList array list to be displayed
    public void selectEvents(SQLiteDatabase db, String tableName) {

        ArrayList<String> finishedEvents = new ArrayList<>();       // list for any events that finished

        // clearing anything currently in the array list since it will be reset below to avoid any duplicate entries
        eventList.clear();

        // creating query
        Cursor allRows = db.rawQuery("SELECT rowid, title, endTime FROM " + tableName, null);

        // checking if the query returned anything
        if (allRows.moveToFirst()) {
            String[] columnNames = allRows.getColumnNames();                                                                    // holds all column names

            // looping through all rows of db
            do {
                int colCount = 0;                                                                                               // count for column number the cursor is on
                eventInfo eventInfoInst = new eventInfo(-1, null, -1, null, -1);      // instance of eventInfo class

                // looping through all columns within a row
                for (String name : columnNames) {
                    // event can be added to array list
                    addEvent = true;

                    // checks if it is column 1, 2, or 3 from db and sets the id, tile and date
                    if(colCount == 0) eventInfoInst.setID(allRows.getInt(allRows.getColumnIndex(name)));
                    else if(colCount == 1) eventInfoInst.setTitle(allRows.getString(allRows.getColumnIndex(name)));
                    else {
                        long storedEndTime = allRows.getLong(allRows.getColumnIndex(name));
                        long currentTime = new Date().getTime();
                        final long timeDiff =  storedEndTime - currentTime;

                        // checking that the stored time is still after the current time
                        if(timeDiff > 0)
                        {
                            eventInfoInst.setEndTime(storedEndTime);
                        }
                        else
                        {
                            // telling functions to not update list adapter and deleting event
                            addEvent = false;

                            // adding title to list of finished events
                            finishedEvents.add(eventInfoInst.getTitle());

                            // deleting event
                            deletedEventID = eventInfoInst.getID();
                            deleteEvents();
                        }
                    }

                    // incrementing column count
                    colCount++;
                }

                // adding event to list if allowed
                if(addEvent) eventList.add(eventInfoInst);

            } while (allRows.moveToNext());
        }

        // sorting array so the timers with least endTime are at the top
        Collections.sort(eventList);

        // showing users any events that finished, if any
        if(finishedEvents.size() > 0) showEndedEventsDialog(finishedEvents);
    }

    // removes the selected event from the database and updates the array list
    public void deleteEvents() {
        // canceling the CountDownTimers
        cancelTimers();

        // deleting the selected event from the db
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        db.delete("Events", "eventID = " + deletedEventID, null);

        // updating the orig database
        sqLiteDatabase = db;

        // resetting array list
        selectEvents(sqLiteDatabase, "Events");

        // showing updated list without deleted event if allowed
        adapter.notifyDataSetChanged();

        // resetting the CountDownTimers
        setTimers();
    }

    // creates the delete confirmation dialog box
    public void showDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);    // new builder for dialog

        // setting title
        builder.setTitle("Delete event?");

        // setting message
        String message = "The event \"" + deletedEventName + "\" will be deleted." ;
        builder.setMessage(message);

        // showing a button to delete event
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteEvents();
            }
        });

        // showing a button to cancel (do nothing)
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        // showing that box
        builder.show();
    }

    // creates a dialog box to show the users any events that finished while the app was closed
    public void showEndedEventsDialog(ArrayList<String> finishedEvents) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);    // new builder for dialog

        // setting title
        builder.setTitle("These timers have finished:");

        // setting message
        String message = "";
        for(int i = 0; i < finishedEvents.size(); i++)
        {
            message += finishedEvents.get(i) + "\n";
        }
        builder.setMessage(message);

        // showing a button to get rid of the dialog box
        builder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        // showing that box
        builder.show();
    }

    // takes in a string for datetime, converts to Date var and returns an int representing future(0), present(1), or past(2)
    public boolean compareDates(String inputDatetime) {

        // getting the current date
        Date currentDate = Calendar.getInstance().getTime();

        // converting the input date from string to Date
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        Date selectedDatetime = new Date();
        try {
            selectedDatetime = format.parse(inputDatetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // checks if the input datetime is in the future, present, or past
        if(selectedDatetime.compareTo(currentDate) > 0)
            return true;
        else if(selectedDatetime.compareTo(currentDate) == 0)
            return true;
        else
            return false;
    }

    // loops through array list and starts a CountDownTimer for entries that do not have one yet
    public void setTimers(){
        for(int i = 0; i < eventList.size(); i++)
        {
            // checks if the entry has a timer yet
            if (eventList.get(i).getCountDownTimer() == null) {

                long storedEndTime = eventList.get(i).getEndTime();     // time of event
                long currentTime = new Date().getTime();                // current time
                final long timeDiff = storedEndTime - currentTime;      // difference between current time and stored time
                final int finalI = i;                                   // index variable

                // setting timeLeft
                eventList.get(finalI).setTimeLeft(timeDiff);

                // setting timer
                eventList.get(i).setCountDownTimer(new CountDownTimer(timeDiff, 1000) {
                    @Override
                    public void onTick(long l) {

                        // subtracting a second from the time left and updating adapter
                        eventList.get(finalI).setTimeLeft(eventList.get(finalI).getTimeLeft() - 1000);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFinish() {

                        // deleting events when they finish
                        deletedEventID = eventList.get(finalI).getID();
                        deleteEvents();

                    }
                }.start());
            }
        }
    }

    // loops through array list and cancels every timer
    public void cancelTimers(){
        for(int i = 0; i < eventList.size(); i++)
        {
            // checks if the entry has a timer
            if (eventList.get(i).getCountDownTimer() != null) {

                // canceling the timers
                eventList.get(i).getCountDownTimer().cancel();
            }
        }
    }
}
