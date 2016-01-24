package com.photoapp.model.upload;

public abstract class UploadStateListener {
    public UploadStateListener(String absoluteLocalPath) {
        this.absoluteLocalPath = absoluteLocalPath;
    }

    public String absoluteLocalPath;

    public abstract void onCompleted(long countBytesTransfered);
    public abstract void onInit(long countBytesTransfered);
    public abstract void onProgress(long countBytesTransfered);
    public abstract void onFail(long countBytesTransfered);
}
