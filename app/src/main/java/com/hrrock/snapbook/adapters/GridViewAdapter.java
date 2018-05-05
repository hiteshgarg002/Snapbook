package com.hrrock.snapbook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.utils.SquareImageView;

import java.io.File;
import java.util.List;

/**
 * Created by hp-u on 10/4/2017.
 */

public class GridViewAdapter extends ArrayAdapter<String> {
    private Context ctx;
    private List<String> list;
    private LayoutInflater layoutInflater;
    private ViewHolder holder;

    public GridViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        ctx = context;
        list = objects;
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        private SquareImageView squareImageView;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_photo_moment_grid, null);
            holder = new ViewHolder();
            holder.squareImageView = convertView.findViewById(R.id.nImageV);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Uri uri = Uri.fromFile(new File(list.get(position)));

        Glide.with(ctx.getApplicationContext()).load(uri).into(holder.squareImageView);

        return convertView;
    }
}
