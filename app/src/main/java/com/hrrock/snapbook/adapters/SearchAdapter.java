package com.hrrock.snapbook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.models.SearchModel;
import com.hrrock.snapbook.utils.GlideApp;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hp-u on 3/24/2018.
 */

public class SearchAdapter extends ArrayAdapter<SearchModel> {
    private Context ctx;
    private List<SearchModel> people;
    private View.OnClickListener clickListener;
    private LayoutInflater layoutInflater;
    private SharedPreferences preferences;
    private static final String USER_PREFERENCE = "userinfo";

    public SearchAdapter(@NonNull Context context, int resource, @NonNull List<SearchModel> objects, View.OnClickListener clickListener) {
        super(context, resource, objects);
        this.ctx = context;
        this.people = objects;
        this.clickListener = clickListener;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = ctx.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
    }

    private static class ViewHolder {
        private TextView name, userName;
        private CircleImageView dp;
        private ImageView followStatus;
        private LottieAnimationView animationView;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_search_row, null);
            holder = new ViewHolder();

            holder.name = convertView.findViewById(R.id.nameOnSearch);
            holder.userName = convertView.findViewById(R.id.userNameOnSearch);
            holder.dp = convertView.findViewById(R.id.dpOnSearch);
            holder.followStatus = convertView.findViewById(R.id.followStatusOnSearch);
            holder.animationView = convertView.findViewById(R.id.me_anim);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SearchModel searchModel = getItem(position);

        if (searchModel != null) {
            GlideApp.with(ctx.getApplicationContext())
                    .load("http://" + ctx.getString(R.string.ip) + "/Snapbook/images/profile/" + searchModel.getPhoto())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.no_dp_big)
                    .into(holder.dp);
        }
        holder.userName.setText(searchModel != null ? searchModel.getUserName() : null);
        holder.name.setText(searchModel != null ? searchModel.getName() : null);

        if (searchModel != null) {
            if (Objects.equals(searchModel.getUserName(), preferences.getString("username", ""))) {
                // holder.followStatus.setImageResource(R.drawable.happy_emoji);
                //holder.followStatus.setClickable(false);
                //holder.followStatus.setTag("me");
                holder.followStatus.setVisibility(View.INVISIBLE);
                holder.animationView.setVisibility(View.VISIBLE);
            } else {
                holder.followStatus.setVisibility(View.VISIBLE);
                holder.animationView.setVisibility(View.INVISIBLE);
                if (Objects.equals(searchModel.getFollowStatus(), "null")) {
                    holder.followStatus.setImageResource(R.drawable.follow);
                    holder.followStatus.setTag("follow");
                } else {
                    holder.followStatus.setImageResource(R.drawable.following);
                    holder.followStatus.setTag("following");
                }
            }
        }

        setClickListeners(holder.followStatus);
        setTagsToViews(holder.followStatus, position);

        return convertView;
    }

    @Override
    public int getCount() {
        if (people != null) {
            return people.size();
        }
        return 0;
    }

    @Nullable
    @Override
    public SearchModel getItem(int position) {
        return people.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private void setClickListeners(View view) {
        view.setOnClickListener(clickListener);
    }

    private void setTagsToViews(View view, int position) {
        view.setTag(R.id.key_position, position);
    }
}
