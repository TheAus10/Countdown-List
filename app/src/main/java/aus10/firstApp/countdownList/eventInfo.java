package aus10.firstApp.countdownList;

import android.os.CountDownTimer;

public class eventInfo implements Comparable<eventInfo> {

    private int    id;                                                          // event id
    private String title;                                                       // title of event
    private long   endTime;                                                     // the time (in millis) that the event ends
    private CountDownTimer countDownTimer;                                      // the timer for each event
    private long   timeLeft;                                                    // millis left til endTime

    // constructor
    public eventInfo(int id, String title, long endTime, CountDownTimer countDownTimer, long timeLeft) {
        this.id = id;
        this.title = title;
        this.endTime = endTime;
        this.countDownTimer = countDownTimer;
        this.timeLeft = timeLeft;
    }

    // setters and getters for each attribute
    public int getID() {
        return id;
    }
    public void setID(int id) { this.id = id; }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) { this.title = title; }

    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public CountDownTimer getCountDownTimer() { return countDownTimer; }
    public void setCountDownTimer(CountDownTimer countDownTimerInput) { this.countDownTimer = countDownTimerInput; }

    public long getTimeLeft() {
        return timeLeft;
    }
    public void setTimeLeft(long timeLeft) { this.timeLeft = timeLeft; }

    // comparable for Collections.sort()
    @Override
    public int compareTo(eventInfo x) {
        // checks if the value is lt, gt, or equal to current value for sort
        return this.endTime < x.endTime ? -1 : (this.endTime > x.endTime ? 1 : 0);
    }


}
