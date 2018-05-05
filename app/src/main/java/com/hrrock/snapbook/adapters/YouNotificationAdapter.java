package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.UserProfileActivity;
import com.hrrock.snapbook.activities.ViewPollingStatusActivity;
import com.hrrock.snapbook.activities.ViewPostActivity;
import com.hrrock.snapbook.models.YouNotificationModel;
import com.hrrock.snapbook.utils.GlideApp;
import com.hrrock.snapbook.utils.SquareImageView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

public class YouNotificationAdapter extends RecyclerView.Adapter<YouNotificationAdapter.MyHolder> {
    private Context ctx;
    private List<YouNotificationModel> list = Collections.emptyList();
    private LayoutInflater inflater;

    public YouNotificationAdapter(Context ctx, List<YouNotificationModel> list) {
        this.ctx = ctx;
        this.list = list;
        this.inflater = LayoutInflater.from(ctx);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        SquareImageView photo;
        CircleImageView dp;
        TextView notification;
        RelativeLayout root, relViewStatus;

        MyHolder(View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dpOnYouNotification);
            photo = itemView.findViewById(R.id.photoOnYouNotification);
            notification = itemView.findViewById(R.id.notificationOnYouNotification);
            root = itemView.findViewById(R.id.you_notification_row_root);
            relViewStatus = itemView.findViewById(R.id.relViewDealStatusOnYouNotification);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_you_notification_row, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        YouNotificationModel notificationModel = list.get(position);

        if (notificationModel != null) {
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getResources().getString(R.string.BASE_URL_PROFILE_PIC) + notificationModel.getUname() + ".jpg")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .placeholder(R.drawable.no_dp_big)
                    .into(holder.dp);

            switch (notificationModel.getType()) {
                case "photo": {
                    holder.relViewStatus.setVisibility(View.GONE);
                    holder.photo.setVisibility(View.VISIBLE);
                    break;
                }
                case "deal": {
                    holder.photo.setVisibility(View.GONE);
                    holder.relViewStatus.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    break;
            }

            if (!Objects.equals(notificationModel.getPhoto(), "")) {
                Glide.with(ctx.getApplicationContext())
                        .load(ctx.getResources().getString(R.string.BASE_URL_PHOTOS_NEWS_FEED) + notificationModel.getPhoto())
                        .into(holder.photo);
                holder.photo.setVisibility(View.VISIBLE);
            } else {
                holder.photo.setVisibility(View.INVISIBLE);
            }

            holder.notification.setText(notificationModel.getNotification());

            holder.root.setOnClickListener(view -> {
                switch (notificationModel.getType()) {
                    case "photo": {
                        Intent intent = new Intent(ctx, ViewPostActivity.class);
                        intent.putExtra("postid", notificationModel.getPostId());
                        ctx.startActivity(intent);

                        Bungee.slideLeft(ctx);
                        break;
                    }
                    case "deal": {
                        Intent intent = new Intent(ctx, ViewPollingStatusActivity.class);
                        intent.putExtra("postid", notificationModel.getPostId());
                        ctx.startActivity(intent);

                        Bungee.slideLeft(ctx);
                        break;
                    }
                    default:
                        break;
                }
            });

            holder.dp.setOnClickListener(view -> {
                Intent intent = new Intent(ctx, UserProfileActivity.class);
                intent.putExtra("username", notificationModel.getUname());
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
