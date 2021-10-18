package com.example.steppingmonsterduel2.Services;

public class FakeServer {

    //does the server's supposed stuff for steps calculation // TO REMOVE LATER
    // this is obsolete, should be removed

    private static int userSteps = 0;
    private final static float stepPerMeter = 1.31f;
    private final static float maxDistancePerUpdate = 30f;

    public static int updateSteps(float distance){
        //converts distance (meters) to steps and sends back total steps
        //basic conversion 1meter => 1,31 steps  / 1 km => 1312,34 steps
        if (distance > maxDistancePerUpdate)
            return userSteps;

        int extraSteps = (int)Math.floor(distance*stepPerMeter);
        userSteps += extraSteps;
        return userSteps;
    }
}
