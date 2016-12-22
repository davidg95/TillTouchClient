/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class ClockThread extends Thread {

    private static final ClockThread CLOCK_THREAD;

    protected boolean isRunning;

    protected JLabel dateTimeLabel;

    protected SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    protected Time time;

    public ClockThread() {
        this.isRunning = true;
    }

    static {
        CLOCK_THREAD = new ClockThread();
        CLOCK_THREAD.start();
    }

    public static void setClockLabel(JLabel label) {
        CLOCK_THREAD.setLabel(label);
    }
    
    public static Time getTime(){
        return CLOCK_THREAD.getCurrentTime();
    }

    @Override
    public void run() {
        while (isRunning) {
            Calendar currentCalendar = Calendar.getInstance();
            Date currentTime = currentCalendar.getTime();
            time = new Time(currentCalendar.getTimeInMillis());
            if (dateTimeLabel != null) {
                SwingUtilities.invokeLater(() -> {
                    dateTimeLabel.setText(timeFormat.format(currentTime));
                });
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
            }
        }
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setLabel(JLabel label) {
        this.dateTimeLabel = label;
    }
    
    public Time getCurrentTime(){
        return time;
    }

}
