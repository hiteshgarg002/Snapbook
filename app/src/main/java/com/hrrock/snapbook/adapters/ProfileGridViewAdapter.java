package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.ViewPostActivity;
import com.hrrock.snapbook.models.ProfilePostsGridViewModel;
import com.hrrock.snapbook.utils.GlideApp;
import com.hrrock.snapbook.utils.SquareImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Collections;
import java.util.List;

import spencerstudios.com.bungeelib.Bungee;

public class ProfileGridViewAdapter extends RecyclerView.Adapter<ProfileGridViewAdapter.MyHolder> {
    private List<ProfilePostsGridViewModel> list = Collections.emptyList();
    private LayoutInflater inflater;
    private Context ctx;

    public ProfileGridViewAdapter(Context context, List<ProfilePostsGridViewModel> list) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.ctx = context;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        SquareImageView photo;
        RelativeLayout photoLayout;
        AVLoadingIndicatorView progressBar;

        MyHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.nImageV);
            photoLayout = itemView.findViewById(R.id.photoView);
            progressBar = itemView.findViewById(R.id.gridProgressBar);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_photo_profile_grid, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ProfilePostsGridViewModel gridViewModel = list.get(position);
        if (gridViewModel != null) {
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getResources().getString(R.string.BASE_URL_PHOTOS_PROFILE) + gridViewModel.getPhoto())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.photo.setVisibility(View.VISIBLE);

                            return false;
                        }
                    })
                    .into(holder.photo);

            holder.photoLayout.setOnClickListener(view -> {
                Intent intent = new Intent(ctx, ViewPostActivity.class);
                intent.putExtra("postid", gridViewModel.getPostId());
                ctx.startActivity(intent);
                Bungee.slideLeft(ctx);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
