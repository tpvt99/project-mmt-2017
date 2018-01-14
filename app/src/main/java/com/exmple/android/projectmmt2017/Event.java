package com.exmple.android.projectmmt2017;


public class Event {

    public final Boolean redLed;
    public final Boolean greenLed;
    public final Boolean blueLed;

    /**
     * Constructs a new {@link Event}.
     *
     * @param eventBlueLed is the true/false of blue led
     * @param eventGreenLed is the true/false of green led
     * @param eventRedLed is the true/false of red led
     */
    public Event(Boolean eventRedLed, Boolean eventGreenLed, Boolean eventBlueLed) {
        redLed = eventRedLed;
        greenLed = eventGreenLed;
        blueLed = eventBlueLed;
    }
}
