package com.photoapp.model.events;

import com.photoapp.model.debug.Log;

import de.greenrobot.event.EventBus;

public class Bus {
    private static final String LOG_TAG = Bus.class.getCanonicalName();

    public Bus() {

    }

    public static EventBus getDefault() {
        return EventBus.getDefault();
    }

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(Object event) {
        Log.w(LOG_TAG, "post EVENT " + event);
        EventBus.getDefault().post(event);
    }
}
