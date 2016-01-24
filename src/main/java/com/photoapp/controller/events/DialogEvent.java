package com.photoapp.controller.events;

public class DialogEvent {

    public String filename;
    public enum DialogType {
        NO_LOCATION_SERVICES,
        FILE_WAS_DELETED,
        UPLOAD_ERROR,
        INTERNET_DISCONNECTED,
        NO_SD_CARD,
        NO_ENOUGH_SPACE, DECODER_NOT_AVAILABLE, NETWORK_NOT_AVAILABLE,

    }
    public  DialogType dialogType;

    public DialogEvent() {
    }

    public DialogEvent(DialogType dialogType, String filename) {
        this.filename = filename;
        this.dialogType = dialogType;
    }

    public DialogEvent(DialogType dialogType) {

        this.dialogType = dialogType;
    }

    @Override
    public String toString() {
        return "DialogEvent{" +
                "filename='" + filename + '\'' +
                ", dialogType=" + dialogType +
                '}';
    }
}
