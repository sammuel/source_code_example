package com.photoapp.controller.events;

/**
 * Class-event when user is signed up
 */
public class SignUpEvent {
    private final String userName;

    public SignUpEvent(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
