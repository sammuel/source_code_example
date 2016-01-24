package com.photoapp.controller.list;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.photoapp.model.database.DAO.PictureFileDao;
import com.photoapp.model.database.PictureFile;
import com.photoapp.R;
import com.photoapp.model.database.DAOHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is custom cursor adapter for upload list. Contains class ViewHolder .
 */

public class UploadListAdapter extends CursorAdapter {

    private static final int RES_ID_IMAGE_UPLOADING = R.drawable.ic_file_upload_black_24dp;
    private static final int RES_ID_IMAGE_UPLOADED = R.drawable.ic_check_black_24dp;
    private static final int RES_ID_IMAGE_IN_QUEUE = R.drawable.ic_history_black_24dp;


    LayoutInflater lInflater;

    static class ViewHolder {
        TextView fileName;
        ImageView imageStatus;
    }

    public UploadListAdapter(Context context, Cursor c) {
        super(context, c, false);
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = lInflater.inflate(R.layout.list_item_file_upload, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.fileName = (TextView) view.findViewById(R.id.fileName);
        holder.imageStatus = (ImageView) view.findViewById(R.id.imageStatus);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String filename = cursor.getString(cursor.getColumnIndex(PictureFileDao.Properties.Filename.columnName));
        Integer status = cursor.getInt(cursor.getColumnIndex(PictureFileDao.Properties.Status.columnName));
        holder.fileName.setText(filename);
        int resId = 0;
        switch (status) {
            case 0:
                resId = RES_ID_IMAGE_IN_QUEUE;
                break;
            case 1:
                resId = RES_ID_IMAGE_UPLOADING;
                break;
            case 2:
                resId = RES_ID_IMAGE_UPLOADED;
                break;
            default:
                resId = RES_ID_IMAGE_IN_QUEUE;
                break;

        }
        holder.imageStatus.setImageResource(resId);

    }
}
