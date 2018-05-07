package com.hrrock.snapbook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.models.ProfilePostsListViewModel;
import com.hrrock.snapbook.networks.VolleyConnect;
import com.hrrock.snapbook.utils.GlideApp;
import com.hrrock.snapbook.utils.SquareImageView;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileListViewAdapter extends RecyclerView.Adapter<ProfileListViewAdapter.MyHolder> {
    private List<ProfilePostsListViewModel> list = Collections.emptyList();
    private LayoutInflater inflater;
    private Context ctx;
    private StringRequest stringRequest;
    private JsonArrayRequest jsonArrayRequest;
    private RequestQueue requestQueue;
    private static final String POLL_UP = "up";
    private static final String POLL_DOWN = "down";
    private static final String USER_PREFERENCES = "userinfo";
    private SharedPreferences preferences;
    private View.OnClickListener clickListener;

    public ProfileListViewAdapter(Context context, List<ProfilePostsListViewModel> list, View.OnClickListener onClickListener) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.ctx = context;
        this.requestQueue = VolleyConnect.getInstance().getRequestQueue();
        this.preferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        this.clickListener = onClickListener;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        SquareImageView photo;
        CircleImageView dp;
        TextView caption, username, date, voteUp, voteDown;
        ImageView thumbsUp, thumbsDown, comment;
        LinearLayout votingLinear, captionLinear;

        MyHolder(View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dpOnProfilePostsListView);
            photo = itemView.findViewById(R.id.postOnProfilePostsListView);
            username = itemView.findViewById(R.id.userNameOnProfilePostsListView);
            thumbsUp = itemView.findViewById(R.id.thumb_upOnProfilePostsListView);
            thumbsDown = itemView.findViewById(R.id.thumb_downOnProfilePostsListView);
            comment = itemView.findViewById(R.id.comments_OnProfilePostsListView);
            votingLinear = itemView.findViewById(R.id.linearVotes);
            captionLinear = itemView.findViewById(R.id.linearCaptionOnProfilePostsListView);
            date = itemView.findViewById(R.id.dateOnProfilePostsListView);
            caption = itemView.findViewById(R.id.captionOnProfilePostsListView);
            voteUp = itemView.findViewById(R.id.upVotesOnProfilePostsListView);
            voteDown = itemView.findViewById(R.id.downVotesOnProfilePostsListView);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_profile_posts_list_view_row, parent, false);
        return new MyHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ProfilePostsListViewModel listViewModel = list.get(position);
        if (listViewModel != null) {
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getResources().getString(R.string.BASE_URL_PROFILE_PIC) + listViewModel.getUserName() + ".jpg")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.dp);

            Glide.with(ctx.getApplicationContext()).load(ctx.getString(R.string.BASE_URL_PHOTOS_PROFILE)
                    + listViewModel.getPhoto()).into(holder.photo);
            holder.username.setText(listViewModel.getUserName());

            holder.date.setText(listViewModel.getDate());

            if (!Objects.equals(listViewModel.getVoteUp(), "0")
                    || !Objects.equals(listViewModel.getVoteDown(), "0")
                    || !Objects.equals(listViewModel.getVoteUp(), "0")
                    && !Objects.equals(listViewModel.getVoteDown(), "0")) {
                holder.voteUp.setText(listViewModel.getVoteUp() + " " + ctx.getString(R.string.vote_up));
                holder.voteDown.setText(listViewModel.getVoteDown() + " " + ctx.getString(R.string.vote_down));

                holder.votingLinear.setVisibility(View.VISIBLE);
            } else {
                holder.votingLinear.setVisibility(View.GONE);
            }

            if (Objects.equals(listViewModel.getPollStatus(), "up")) {
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
            } else if (Objects.equals(listViewModel.getPollStatus(), "down")) {
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
            } else {
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
            }

            if (!Objects.equals(listViewModel.getCaption(), "")) {
                holder.caption.setText(listViewModel.getCaption());
                holder.captionLinear.setVisibility(View.VISIBLE);
            } else {
                holder.captionLinear.setVisibility(View.GONE);
            }

            holder.thumbsUp.setOnClickListener(view -> {
                if (Objects.equals(listViewModel.getPollStatus(), "up")) {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else if (Objects.equals(listViewModel.getPollStatus(), "down")) {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                }
                setPollOnAction(listViewModel, POLL_UP, holder.thumbsUp, holder.thumbsDown, holder.voteUp, holder.voteDown, holder.votingLinear);
            });

            holder.thumbsDown.setOnClickListener(view -> {
                if (Objects.equals(listViewModel.getPollStatus(), "up")) {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                } else if (Objects.equals(listViewModel.getPollStatus(), "down")) {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else {
                    holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                }
                setPollOnAction(listViewModel, POLL_DOWN, holder.thumbsDown, holder.thumbsUp, holder.voteUp, holder.voteDown, holder.votingLinear);
            });

            setClickListeners(holder.comment);
            setTagsToViews(holder.comment, position);
        }
    }

    private void setPollOnAction(ProfilePostsListViewModel listViewModel, String polltype, ImageView poll, ImageView otherPoll, TextView voteUp, TextView voteDown, LinearLayout votingLinear) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/setPoll?" +
                "postid=" + listViewModel.getPostId() + "&username=" + preferences.getString("username", "") + "&polltype=" + polltype;

        stringRequest = new StringRequest(url, response -> {
            updateVoteCount(listViewModel, voteUp, voteDown, votingLinear);

            if (Objects.equals(response, "up") || Objects.equals(response, "down")) {
                poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                otherPoll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));

                if (!Objects.equals(listViewModel.getUserName(), preferences.getString("username", ""))) {
                    sendVoteNotification(listViewModel.getToken());
                    makeNotification(listViewModel.getUserName(), listViewModel.getPostId());
                }
            } else {
                poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                otherPoll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));

                if (!Objects.equals(listViewModel.getUserName(), preferences.getString("username", ""))) {
                    removeNotification(listViewModel.getUserName(), listViewModel.getPostId());
                }
            }
        }, error -> poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null))));

        requestQueue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
    private void updateVoteCount(ProfilePostsListViewModel listViewModel, TextView voteUp, TextView voteDown, LinearLayout votingLinear) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/updateVoteCount?" +
                "postid=" + listViewModel.getPostId();
        //Toast.makeText(ctx,newsFeedModel.getPostId()+"",Toast.LENGTH_SHORT).show();

        jsonArrayRequest = new JsonArrayRequest(url, response -> {
            JSONObject jsonObject = response.optJSONObject(0);

            if (!Objects.equals(jsonObject.optString("voteup"), "0") || !Objects.equals(jsonObject.optString("votedown"), "0")
                    || !Objects.equals(jsonObject.optString("voteup"), "0") && !Objects.equals(jsonObject.optString("votedown"), "0")) {

                voteUp.setText(jsonObject.optString("voteup") + " " + ctx.getString(R.string.vote_up));
                voteDown.setText(jsonObject.optString("votedown") + " " + ctx.getString(R.string.vote_down));

                votingLinear.setVisibility(View.VISIBLE);
            } else {
                votingLinear.setVisibility(View.GONE);
            }
            // Toast.makeText(ctx,jsonObject.optString("voteup")+"-"+jsonObject.optString("votedown"),Toast.LENGTH_SHORT).show();
        }, error -> {

        });

        requestQueue.add(jsonArrayRequest);
    }

    private void sendVoteNotification(String token) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Firebase/notification.php?" +
                "regId=" + token + "&title=Snapbook&message=" + preferences.getString("username", "") + " voted on your post!&push_type=individual";

        stringRequest = new StringRequest(url, response -> {
        }, error -> {
        });
        requestQueue.add(stringRequest);
    }

    private void makeNotification(String username, String postId) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/NotificationController/makeYouNotification?" +
                "uname=" + preferences.getString("username", "") + "&username=" + username + "&notification=" + preferences.getString("username", "") + " voted on your post!&postid=" + postId + "&type=vote";

        stringRequest = new StringRequest(url, response -> {
        }, error -> {
        });
        requestQueue.add(stringRequest);
    }

    private void removeNotification(String username, String postId) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/NotificationController/removeYouNotification?" +
                "uname=" + preferences.getString("username", "") + "&username=" + username + "&notification=" + preferences.getString("username", "") + " voted on your post!&postid=" + postId + "&type=vote";

        stringRequest = new StringRequest(url, response -> {
        }, error -> {
        });
        requestQueue.add(stringRequest);
    }

    @Override
    public int getItemCount() {
        return list.size();
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
