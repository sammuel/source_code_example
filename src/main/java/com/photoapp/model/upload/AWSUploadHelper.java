package com.photoapp.model.upload;

import android.content.Context;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.photoapp.controller.Constants;

import java.io.File;

public class AWSUploadHelper {
    private static final String LOG_TAG = AWSUploadHelper.class.getCanonicalName();
//    private TransferManager transferManager;

    public TransferUtility transferUtility;

    public AWSUploadHelper(Context context) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
                Constants.AWS_S3_ACCESS_KEY,
                Constants.AWS_S3_SECRET_KEY
        );

        AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        transferUtility = new TransferUtility(s3, context);

    }

    public  void upload (File localFile, String remoteName, final UploadStateListener listener) {


        final TransferObserver upload = transferUtility.upload(Constants.AWS_S3_BUCKET_NAME, remoteName, localFile);
        upload.setTransferListener(new TransferListener() {
            long bytesTransferred = -1;
            @Override
            public void onStateChanged(int id, TransferState state) {
                bytesTransferred = upload.getBytesTransferred();
                switch (state) {
                    case WAITING:
                        break;
                    case IN_PROGRESS:
                        listener.onProgress(bytesTransferred);

                        break;
                    case PAUSED:
                        break;
                    case RESUMED_WAITING:
                        break;
                    case COMPLETED:
                        removeListener(upload , this);
                        listener.onCompleted(bytesTransferred);
                        break;
                    case WAITING_FOR_NETWORK:
                        break;
                    case PENDING_NETWORK_DISCONNECT:
                    case CANCELED:
                    case FAILED:
                        removeListener(upload , this);
                        listener.onFail(bytesTransferred);
                        break;

                    case PART_COMPLETED:
                        break;
                    case PENDING_CANCEL:
                        break;
                    case PENDING_PAUSE:
                        break;

                    case UNKNOWN:
                        break;
                }
            }

            private void removeListener(TransferObserver upload, TransferListener transferListener) {
                upload.cleanTransferListener();
                int id = upload.getId();
                transferUtility.cancel(id);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

                     //   listener.onFail(bytesTransferred);
            }
        });
                long bytesTransferred = 0;
        listener.onInit(bytesTransferred);
    }
}
