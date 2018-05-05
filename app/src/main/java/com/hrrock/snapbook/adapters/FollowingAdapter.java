package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.UserProfileActivity;
import com.hrrock.snapbook.models.FollowModel;
import com.hrrock.snapbook.networks.VolleyConnect;
import com.hrrock.snapbook.utils.GlideApp;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.MyHolder> {
    private List<FollowModel> list = Collections.emptyList();
    private LayoutInflater inflater;
    private Context ctx;
    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    private SharedPreferences preferences;
    private final String USER_PREFERENCES="userinfo";

    public FollowingAdapter(Context context, List<FollowModel> list) {
        this.list = list;
        this.ctx = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.requestQueue = VolleyConnect.getInstance().getRequestQueue();
        this.preferences=ctx.getSharedPreferences(USER_PREFERENCES,Context.MODE_PRIVATE);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView dp;
        TextView userName, name;
        ImageView followStatus;
        LinearLayout nameUserNameLinear;
        AVLoadingIndicatorView dpProgress;

        MyHolder(View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dpOnFollowing);
            followStatus = itemView.findViewById(R.id.followStatusOnFollowing);
            userName = itemView.findViewById(R.id.usernameOnFollowing);
            name = itemView.findViewById(R.id.nameOnFollowing);
            nameUserNameLinear=itemView.findViewById(R.id.nameUsernameLinearOnFollowing);
            dpProgress=itemView.findViewById(R.id.dpProgressOnFollowing);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_following_row, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        FollowModel followModel = list.get(position);
        if (followModel != null) {
            if(!Objects.equals(followModel.getProfileHolderUserName(), preferences.getString("username", ""))){
                holder.followStatus.setVisibility(View.INVISIBLE);
            }

            if(!Objects.equals(followModel.getPhoto(), "")) {
                GlideApp.with(ctx.getApplicationContext())
                        .load(ctx.getResources().getString(R.string.BASE_URL_PROFILE_PIC) + followModel.getPhoto())
                        .skipMemoryCache(true)
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
                                holder.dpProgress.setVisibility(View.GONE);
                                holder.dp.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .placeholder(R.drawable.no_dp_big)
                        .into(holder.dp);
            }else {
                holder.dp.setImageResource(R.drawable.no_dp_big);
                holder.dpProgress.setVisibility(View.GONE);
                holder.dp.setVisibility(View.VISIBLE);
            }

            holder.userName.setText(followModel.getUserName());
            holder.name.setText(followModel.getName());
            if (Objects.equals(followModel.getFollowingStatus(), "")) {
                holder.followStatus.setImageResource(R.drawable.follow);
                holder.followStatus.setTag("follow");
            } else {
                holder.followStatus.setImageResource(R.drawable.following);
                holder.followStatus.setTag("following");
            }

            holder.followStatus.setOnClickListener(view -> {
                if(holder.followStatus.getTag()=="follow"){
                    doFollow(followModel.getUserName(),holder.followStatus);
                }else if(holder.followStatus.getTag()=="following"){
                    doUnFollow(followModel.getUserName(),holder.followStatus);
                }
            });

            holder.nameUserNameLinear.setOnClickListener(view -> {
                Intent intent=new Intent(ctx, UserProfileActivity.class);
                intent.putExtra("username",followModel.getUserName());
                ctx.startActivity(intent);
                Bungee.slideLeft(ctx);
            });

            holder.dp.setOnClickListener(view -> {
                Intent intent=new Intent(ctx, UserProfileActivity.class);
                intent.putExtra("username",followModel.getUserName());
                ctx.startActivity(intent);
                Bungee.slideLeft(ctx);
            });
        }
    }

    private void doFollow(String username,ImageView followStatus){
        String url="http://"+ctx.getString(R.string.ip)+"/Snapbook/index.php/FollowController/doFollow?" +
                "follower="+preferences.getString("username", "")+"&following="+username;

        stringRequest=new StringRequest(url, response -> {
            if(Objects.equals(response, "success")){
                followStatus.setImageResource(R.drawable.following);
                followStatus.setTag("following");
            }else{
                followStatus.setImageResource(R.drawable.follow);
                followStatus.setTag("follow");
            }
        }, error -> {
            followStatus.setImageResource(R.drawable.follow);
            followStatus.setTag("follow");
        });

        requestQueue.add(stringRequest);
    }

    private void doUnFollow(String username,ImageView followStatus){
        String url="http://"+ctx.getString(R.string.ip)+"/Snapbook/index.php/FollowController/doUnFollow?" +
                "follower="+preferences.getString("username", "")+"&following="+username;

        stringRequest=new StringRequest(url, response -> {
            if(Objects.equals(response, "success")){
                followStatus.setImageResource(R.drawable.follow);
                followStatus.setTag("follow");
            }else{
                followStatus.setImageResource(R.drawable.following);
                followStatus.setTag("following");
            }
        }, error -> {
            followStatus.setImageResource(R.drawable.following);
            followStatus.setTag("following");
        });

        requestQueue.add(stringRequest);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
