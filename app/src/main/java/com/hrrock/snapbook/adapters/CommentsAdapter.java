package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.models.CommentsModel;
import com.hrrock.snapbook.utils.GlideApp;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends ArrayAdapter<CommentsModel> {
    private Context ctx;
    private int layout;
    private LayoutInflater layoutInflater;
    private List<CommentsModel> list;

    public CommentsAdapter(@NonNull Context context, int resource, @NonNull List<CommentsModel> objects) {
        super(context, resource, objects);
        this.ctx = context;
        this.layout = resource;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = objects;
    }

    private static class ViewHolder {
        private TextView username, comment;
        private CircleImageView dp;
        private AVLoadingIndicatorView dpProgress;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, null);

            holder = new ViewHolder();
            holder.dp = convertView.findViewById(R.id.dpOnComments);
            holder.username = convertView.findViewById(R.id.usernameOnComments);
            holder.comment = convertView.findViewById(R.id.commentOnComments);
            holder.dpProgress=convertView.findViewById(R.id.dpProgressOnComments);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.size() > 0) {
            CommentsModel commentsModel = getItem(position);
            if (commentsModel != null) {
                GlideApp.with(ctx.getApplicationContext())
                        .load(ctx.getString(R.string.BASE_URL_PROFILE_PIC) + commentsModel.getUserName() + ".jpg")
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.dp.setImageResource(R.drawable.no_dp_big);
                                holder.dpProgress.setVisibility(View.GONE);
                                holder.dp.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if(holder.dpProgress!=null) {
                                    holder.dpProgress.setVisibility(View.GONE);
                                }
                                if(holder.dp!=null) {
                                    holder.dp.setVisibility(View.VISIBLE);
                                }
                                return false;
                            }
                        })
                        .placeholder(R.drawable.no_dp_big)
                        .into(holder.dp);

                holder.username.setText(commentsModel.getUserName());
                holder.comment.setText(commentsModel.getComment());
            }
        }
        return convertView;
    }
}
