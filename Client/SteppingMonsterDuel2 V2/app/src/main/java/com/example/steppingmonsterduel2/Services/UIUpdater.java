package com.example.steppingmonsterduel2.Services;

import android.widget.TextView;

import com.example.steppingmonsterduel2.ButtonViews.GPSButton;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton that Serves as a communicator between the location service and the different activites
 * if any UI element needs to be updated following the modification of the number of steps this
 * class must handle it
 */
public class UIUpdater {
    private int steps = 0;

    private Set<TextView> watchers; // used to any TextView element that displays the number of steps
    private GPSButton gpsButton; // updates home activity's GPSbutton when the activity is in use
    private static UIUpdater singleton = null;

    public UIUpdater(){
        watchers = new HashSet<TextView>();
        gpsButton = null;
    }

    public static UIUpdater getInstance(){
        if (singleton == null){
            singleton = new UIUpdater();
        }
        return  singleton;
    }
    /*
       handles changes to the gps button and redraws it
       0 -> blue // 1 -> red // 2 -> yellow
     */
    public void updateButtonColor(int color){
        GPSButton.IS_GPS = color;
        if (gpsButton != null){
            gpsButton.invalidate();
        }
    }

    public void setGpsButtonEnabled(boolean bo){
        if (gpsButton != null)
            gpsButton.setEnabled(bo);
    }

    public void setGpsButton(GPSButton button){
        gpsButton = button;
    }

    public void removeGpsButton(){
        gpsButton = null;
    }
    public void addWatcher(TextView view){
        watchers.add(view);
        view.setText("" + steps);
    }

    public void removeWatcher(TextView view){
        if (watchers.contains(view)){
            watchers.remove(view);
        }
    }

    public void updateWatchers(){
        for (TextView view : watchers){
            if (view == null)
                continue;
            view.setText("" + steps);
        }
    }

    public void setSteps(int s){
        steps = s;
        updateWatchers();
    }

    public int getSteps(){
        return this.steps;
    }
}
