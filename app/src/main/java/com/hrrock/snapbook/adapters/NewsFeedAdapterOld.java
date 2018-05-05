package com.hrrock.snapbook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.ProfileActivity;
import com.hrrock.snapbook.activities.UserProfileActivity;
import com.hrrock.snapbook.activities.ViewProductActivity;
import com.hrrock.snapbook.models.NewsFeedModel;
import com.hrrock.snapbook.networks.VolleyConnect;
import com.hrrock.snapbook.utils.GlideApp;
import com.hrrock.snapbook.utils.SquareImageView;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

/**
 * Created by hp-u on 2/20/2018.
 */

public class NewsFeedAdapterOld extends ArrayAdapter<NewsFeedModel> implements OnMenuItemClickListener<PowerMenuItem> {
    private Context ctx;
    // private int layout;
    private LayoutInflater layoutInflater;
    private static final int PHOTO_LAYOUT = 0;
    private static final int DEAL_LAYOUT = 1;
    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    private JsonArrayRequest jsonArrayRequest;
    private static final String POLL_UP = "up";
    private static final String POLL_DOWN = "down";
    private static final String USER_PREFERENCES = "userinfo";
    private SharedPreferences preferences;
    private List<NewsFeedModel> newsFeedModel;
    private View.OnClickListener clickListener;
    private Intent intent;

    public NewsFeedAdapterOld(@NonNull Context context, int resource, @NonNull List<NewsFeedModel> objects, View.OnClickListener onClickListener) {
        super(context, resource, objects);
        this.ctx = context;
        // layout = resource;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.requestQueue = VolleyConnect.getInstance().getRequestQueue();
        this.preferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        this.newsFeedModel = objects;
        this.clickListener = onClickListener;
    }

    private static class ViewHolder {
        private SquareImageView photo;
        private CircleImageView dp;
        private TextView caption, username, dName, dCategory, dCondition, dStatus, dCost, dDesc, date, viewDeal, voteUp, voteDown;
        private AVLoadingIndicatorView progressBar, dpProgress;
        private ImageView thumbsUp, thumbsDown, comment, savePost, options;
        private LinearLayout votingLinear, captionLinear;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ViewHolder holder = null;

        switch (getItemViewType(position)) {
            case PHOTO_LAYOUT:
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.layout_news_feed_post_photo_row, null);
                    holder = new ViewHolder();

                    holder.dp = convertView.findViewById(R.id.newsFeedPostDP);
                    holder.photo = convertView.findViewById(R.id.newsFeedPost);
                    holder.username = convertView.findViewById(R.id.newsFeedPostUserName);
                    holder.progressBar = convertView.findViewById(R.id.newsFeedPostProgressBar);
                    holder.thumbsUp = convertView.findViewById(R.id.thumb_up);
                    holder.thumbsDown = convertView.findViewById(R.id.thumb_down);
                    holder.comment = convertView.findViewById(R.id.comments_OnNewsFeed);
                    holder.votingLinear = convertView.findViewById(R.id.linearVotes);
                    holder.captionLinear = convertView.findViewById(R.id.linearCaptionOnNewsFeed);
                    holder.date = convertView.findViewById(R.id.dateOnNewsFeedPost);
                    holder.caption = convertView.findViewById(R.id.captionOnNewsFeedPost);
                    holder.voteUp = convertView.findViewById(R.id.upVotesOnNewsFeedPost);
                    holder.voteDown = convertView.findViewById(R.id.downVotesOnNewsFeedPost);
                    holder.savePost = convertView.findViewById(R.id.savePostOnNewsFeed);
                    holder.options = convertView.findViewById(R.id.newsFeedPostOption);
                    holder.dpProgress = convertView.findViewById(R.id.newsFeedPostDPProgress);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                break;

            case DEAL_LAYOUT:
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.layout_news_feed_post_deal_polling_row, null);
                    holder = new ViewHolder();

                    holder.dp = convertView.findViewById(R.id.newsFeedPostDP);
                    holder.viewDeal = convertView.findViewById(R.id.newsFeedViewDeal);
                    holder.dCategory = convertView.findViewById(R.id.productCategoryOnNewsFeed);
                    holder.dCondition = convertView.findViewById(R.id.productConditionOnNewsFeed);
                    holder.dName = convertView.findViewById(R.id.productNameOnNewsFeed);
                    holder.dCost = convertView.findViewById(R.id.productCostOnNewsFeed);
                    holder.dStatus = convertView.findViewById(R.id.productStatusOnNewsFeed);
                    holder.username = convertView.findViewById(R.id.newsFeedPostUserName);
                    holder.dDesc = convertView.findViewById(R.id.productDescriptionOnNewsFeed);
                    holder.thumbsUp = convertView.findViewById(R.id.thumb_up);
                    holder.thumbsDown = convertView.findViewById(R.id.thumb_down);
                    holder.comment = convertView.findViewById(R.id.comments_OnNewsFeed);
                    holder.votingLinear = convertView.findViewById(R.id.linearVotes);
                    holder.date = convertView.findViewById(R.id.dateOnNewsFeedPost);
                    holder.voteUp = convertView.findViewById(R.id.upVotesOnNewsFeedPost);
                    holder.voteDown = convertView.findViewById(R.id.downVotesOnNewsFeedPost);
                    holder.dpProgress = convertView.findViewById(R.id.newsFeedPostDPProgress);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                break;

            default:
                break;
        }

        NewsFeedModel newsFeedModel = getItem(position);
        //Toast.makeText(ctx, (newsFeedModel != null ? newsFeedModel.getType() : null) +"",Toast.LENGTH_SHORT).show();

        if (holder != null && newsFeedModel != null) {

            ViewHolder finalHolder1 = holder;
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getString(R.string.BASE_URL_PROFILE_PIC) + newsFeedModel.getUserName() + ".jpg")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            finalHolder1.dp.setImageResource(R.drawable.no_dp_big);
                            finalHolder1.dpProgress.setVisibility(View.INVISIBLE);
                            finalHolder1.dp.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            finalHolder1.dpProgress.setVisibility(View.INVISIBLE);
                            finalHolder1.dp.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.no_dp_big)
                    .into(holder.dp);

            holder.date.setText(newsFeedModel.getDate());

            if (!Objects.equals(newsFeedModel.getVoteUp(), "0")
                    || !Objects.equals(newsFeedModel.getVoteDown(), "0")
                    || !Objects.equals(newsFeedModel.getVoteUp(), "0")
                    && !Objects.equals(newsFeedModel.getVoteDown(), "0")) {
                holder.voteUp.setText(newsFeedModel.getVoteUp() + " " + ctx.getString(R.string.vote_up));
                holder.voteDown.setText(newsFeedModel.getVoteDown() + " " + ctx.getString(R.string.vote_down));

                holder.votingLinear.setVisibility(View.VISIBLE);
            } else {
                holder.votingLinear.setVisibility(View.GONE);
            }

            //    Toast.makeText(ctx, newsFeedModel.getPollStatus()+" - "+newsFeedModel.getUserName(),Toast.LENGTH_SHORT).show();

            if (Objects.equals(newsFeedModel.getPollStatus(), "up")) {
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
            } else if (Objects.equals(newsFeedModel.getPollStatus(), "down")) {
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
            } else {
                holder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                holder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
            }

            if (getItemViewType(position) == PHOTO_LAYOUT) {
                ViewHolder finalHolder = holder;
                Glide.with(ctx.getApplicationContext()).load(ctx.getString(R.string.BASE_URL_PHOTOS_NEWS_FEED)
                        + newsFeedModel.getPhoto())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                finalHolder.progressBar.setVisibility(View.INVISIBLE);
                                finalHolder.photo.setVisibility(View.VISIBLE);

                                return false;
                            }
                        })
                        .into(holder.photo);

                holder.username.setText(newsFeedModel.getUserName());

                if (!Objects.equals(newsFeedModel.getCaption(), "")) {
                    holder.caption.setText(newsFeedModel.getCaption());
                    holder.captionLinear.setVisibility(View.VISIBLE);
                } else {
                    holder.captionLinear.setVisibility(View.GONE);
                }

                holder.options.setOnClickListener(view -> {
                    if (Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                        PowerMenu powerMenu = new PowerMenu.Builder(ctx)
                                .addItem(new PowerMenuItem("Delete", false))
                                .setAnimation(MenuAnimation.DROP_DOWN)
                                .setMenuRadius(10f)
                                .setMenuShadow(10f)
                                .setTextColor(ctx.getResources().getColor(R.color.black, null))
                                .setSelectedTextColor(Color.WHITE)
                                .setMenuColor(Color.WHITE)
                                .setSelectedMenuColor(ctx.getResources().getColor(R.color.colorPrimary, null))
                                .setOnMenuItemClickListener(this)
                                .build();

                        powerMenu.showAsDropDown(view);
                    }
                });
            }

            if (getItemViewType(position) == DEAL_LAYOUT) {
                holder.username.setText(newsFeedModel.getUserName());
                holder.dName.setText(newsFeedModel.getDName());
                holder.dCategory.setText(newsFeedModel.getDCategory());
                holder.dCondition.setText(newsFeedModel.getDCondition());
                holder.dCost.setText(newsFeedModel.getDCost());
                holder.dStatus.setText(newsFeedModel.getDStatus());
                holder.dDesc.setText(newsFeedModel.getDDesc());

                holder.viewDeal.setOnClickListener(view -> {
                    Intent intent = new Intent(ctx, ViewProductActivity.class);
                    intent.putExtra("dealid", newsFeedModel.getDealId() + "");
                    ctx.startActivity(intent);
                });
            }

            ViewHolder finalHolder = holder;
            holder.thumbsUp.setOnClickListener(view -> {

                if (Objects.equals(newsFeedModel.getPollStatus(), "up")) {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else if (Objects.equals(newsFeedModel.getPollStatus(), "down")) {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                }
                setPollOnAction(newsFeedModel, POLL_UP, finalHolder.thumbsUp, finalHolder.thumbsDown, finalHolder.voteUp, finalHolder.voteDown, finalHolder.votingLinear);
            });

            holder.thumbsDown.setOnClickListener(view -> {

                if (Objects.equals(newsFeedModel.getPollStatus(), "up")) {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                } else if (Objects.equals(newsFeedModel.getPollStatus(), "down")) {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                } else {
                    finalHolder.thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    finalHolder.thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                }
                setPollOnAction(newsFeedModel, POLL_DOWN, finalHolder.thumbsDown, finalHolder.thumbsUp, finalHolder.voteUp, finalHolder.voteDown, finalHolder.votingLinear);
            });

            holder.username.setOnClickListener(view -> {
                if (Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                    intent = new Intent(ctx, ProfileActivity.class);
                    ctx.startActivity(intent);

                    Bungee.slideLeft(ctx);
                } else {
                    intent = new Intent(ctx, UserProfileActivity.class);
                    intent.putExtra("username", newsFeedModel.getUserName());
                    ctx.startActivity(intent);

                    Bungee.inAndOut(ctx);
                }
            });

            holder.dp.setOnClickListener(view -> {
                if (Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                    intent = new Intent(ctx, ProfileActivity.class);
                    ctx.startActivity(intent);

                    Bungee.slideLeft(ctx);
                } else {
                    intent = new Intent(ctx, UserProfileActivity.class);
                    intent.putExtra("username", newsFeedModel.getUserName());
                    ctx.startActivity(intent);

                    Bungee.inAndOut(ctx);
                }
            });

            setClickListeners(holder.comment);
            setTagsToViews(holder.comment, position);
        }

        return convertView;
    }

    private void setPollOnAction(NewsFeedModel newsFeedModel, String polltype, ImageView poll, ImageView otherPoll, TextView voteUp, TextView voteDown, LinearLayout votingLinear) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/setPoll?" +
                "postid=" + newsFeedModel.getPostId() + "&username=" + preferences.getString("username", "") + "&polltype=" + polltype;

        stringRequest = new StringRequest(url, response -> {
            //Toast.makeText(ctx,response+"",Toast.LENGTH_SHORT).show();

            if (Objects.equals(response, "up") || Objects.equals(response, "down")) {
                poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                otherPoll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));

                if (!Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                    sendVoteNotification(newsFeedModel.getToken());
                    makeNotification(newsFeedModel.getUserName(), newsFeedModel.getPostId(), newsFeedModel.getType());
                }
            } else {
                poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                otherPoll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));

                if (!Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                    removeNotification(newsFeedModel.getUserName(), newsFeedModel.getPostId());
                }
            }

            updateVoteCount(newsFeedModel, voteUp, voteDown, votingLinear);
        }, error -> poll.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null))));

        requestQueue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
    private void updateVoteCount(NewsFeedModel newsFeedModel, TextView voteUp, TextView voteDown, LinearLayout votingLinear) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/updateVoteCount?" +
                "postid=" + newsFeedModel.getPostId();
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

    private void makeNotification(String username, String postId, String type) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/NotificationController/makeYouNotification?" +
                "uname=" + preferences.getString("username", "") + "&username=" + username + "&notification=" + preferences.getString("username", "") + " voted on your post!&postid=" + postId + "&type=" + type;

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
    public void onItemClick(int position, PowerMenuItem item) {

    }


    @Override
    public int getCount() {
        return newsFeedModel.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        NewsFeedModel newsFeedModel = getItem(position);
        // Toast.makeText(ctx, (newsFeedModel != null ? newsFeedModel.getType() : null) +"",Toast.LENGTH_SHORT).show();
        if (Objects.equals(newsFeedModel != null ? newsFeedModel.getType() : null, "photo")) {
            return PHOTO_LAYOUT;
        } else {
            return DEAL_LAYOUT;
        }
    }

    @Nullable
    @Override
    public NewsFeedModel getItem(int position) {
        return newsFeedModel.get(position);
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