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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.marcoscg.dialogsheet.DialogSheet;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

public class NewsFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context ctx;
    // private int layout;
    private LayoutInflater layoutInflater;
    private static final int PHOTO_LAYOUT = R.layout.layout_news_feed_post_photo_row;
    private static final int DEAL_LAYOUT = R.layout.layout_news_feed_post_deal_polling_row;
    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    private JsonArrayRequest jsonArrayRequest;
    private static final String POLL_UP = "up";
    private static final String POLL_DOWN = "down";
    private static final String USER_PREFERENCES = "userinfo";
    private SharedPreferences preferences;
    private List<NewsFeedModel> list;
    private View.OnClickListener clickListener;
    private Intent intent;
    private PowerMenu powerMenu;
    private DialogSheet deleteDialog;

    public NewsFeedAdapter(@NonNull Context context, @NonNull List<NewsFeedModel> objects, View.OnClickListener onClickListener) {
        this.ctx = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.requestQueue = VolleyConnect.getInstance().getRequestQueue();
        this.preferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        this.list = objects;
        this.clickListener = onClickListener;
    }

    class DealHolder extends RecyclerView.ViewHolder {
        private SquareImageView photo;
        private CircleImageView dp;
        private TextView caption, username, dName, dCategory, dCondition, dStatus, dCost, dDesc, date, viewDeal, voteUp, voteDown;
        private AVLoadingIndicatorView progressBar, dpProgress;
        private ImageView thumbsUp, thumbsDown, comment, savePost, options;
        private LinearLayout votingLinear, captionLinear;

        DealHolder(View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.newsFeedPostDP);
            viewDeal = itemView.findViewById(R.id.newsFeedViewDeal);
            dCategory = itemView.findViewById(R.id.productCategoryOnNewsFeed);
            dCondition = itemView.findViewById(R.id.productConditionOnNewsFeed);
            dName = itemView.findViewById(R.id.productNameOnNewsFeed);
            dCost = itemView.findViewById(R.id.productCostOnNewsFeed);
            dStatus = itemView.findViewById(R.id.productStatusOnNewsFeed);
            username = itemView.findViewById(R.id.newsFeedPostUserName);
            dDesc = itemView.findViewById(R.id.productDescriptionOnNewsFeed);
            thumbsUp = itemView.findViewById(R.id.thumb_up);
            thumbsDown = itemView.findViewById(R.id.thumb_down);
            comment = itemView.findViewById(R.id.comments_OnNewsFeed);
            votingLinear = itemView.findViewById(R.id.linearVotes);
            date = itemView.findViewById(R.id.dateOnNewsFeedPost);
            voteUp = itemView.findViewById(R.id.upVotesOnNewsFeedPost);
            voteDown = itemView.findViewById(R.id.downVotesOnNewsFeedPost);
            dpProgress = itemView.findViewById(R.id.newsFeedPostDPProgress);
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder {
        private SquareImageView photo;
        private CircleImageView dp;
        private TextView caption, username, dName, dCategory, dCondition, dStatus, dCost, dDesc, date, viewDeal, voteUp, voteDown;
        private AVLoadingIndicatorView progressBar, dpProgress;
        private ImageView thumbsUp, thumbsDown, comment, savePost, options;
        private LinearLayout votingLinear, captionLinear;

        PhotoHolder(View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.newsFeedPostDP);
            photo = itemView.findViewById(R.id.newsFeedPost);
            username = itemView.findViewById(R.id.newsFeedPostUserName);
            progressBar = itemView.findViewById(R.id.newsFeedPostProgressBar);
            thumbsUp = itemView.findViewById(R.id.thumb_up);
            thumbsDown = itemView.findViewById(R.id.thumb_down);
            comment = itemView.findViewById(R.id.comments_OnNewsFeed);
            votingLinear = itemView.findViewById(R.id.linearVotes);
            captionLinear = itemView.findViewById(R.id.linearCaptionOnNewsFeed);
            date = itemView.findViewById(R.id.dateOnNewsFeedPost);
            caption = itemView.findViewById(R.id.captionOnNewsFeedPost);
            voteUp = itemView.findViewById(R.id.upVotesOnNewsFeedPost);
            voteDown = itemView.findViewById(R.id.downVotesOnNewsFeedPost);
            savePost = itemView.findViewById(R.id.savePostOnNewsFeed);
            options = itemView.findViewById(R.id.newsFeedPostOption);
            dpProgress = itemView.findViewById(R.id.newsFeedPostDPProgress);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final RecyclerView.ViewHolder holder;
        View view;

        switch (viewType) {
            case PHOTO_LAYOUT:
                view = layoutInflater.inflate(PHOTO_LAYOUT, parent, false);
                holder = new PhotoHolder(view);
                break;

            case DEAL_LAYOUT:
                view = layoutInflater.inflate(DEAL_LAYOUT, parent, false);
                holder = new DealHolder(view);
                break;

            default:
                view = layoutInflater.inflate(PHOTO_LAYOUT, parent, false);
                holder = new PhotoHolder(view);
                break;
        }
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final NewsFeedModel newsFeedModel = list.get(position);

        if (holder instanceof PhotoHolder) {
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getString(R.string.BASE_URL_PROFILE_PIC) + newsFeedModel.getUserName() + ".jpg")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PhotoHolder) holder).dp.setImageResource(R.drawable.no_dp_big);
                            ((PhotoHolder) holder).dpProgress.setVisibility(View.INVISIBLE);
                            ((PhotoHolder) holder).dp.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PhotoHolder) holder).dpProgress.setVisibility(View.INVISIBLE);
                            ((PhotoHolder) holder).dp.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.no_dp_big)
                    .into(((PhotoHolder) holder).dp);

            ((PhotoHolder) holder).date.setText(newsFeedModel.getDate());

            if (!Objects.equals(newsFeedModel.getVoteUp(), "0")
                    || !Objects.equals(newsFeedModel.getVoteDown(), "0")
                    || !Objects.equals(newsFeedModel.getVoteUp(), "0")
                    && !Objects.equals(newsFeedModel.getVoteDown(), "0")) {
                ((PhotoHolder) holder).voteUp.setText(newsFeedModel.getVoteUp() + " " + ctx.getString(R.string.vote_up));
                ((PhotoHolder) holder).voteDown.setText(newsFeedModel.getVoteDown() + " " + ctx.getString(R.string.vote_down));

                ((PhotoHolder) holder).votingLinear.setVisibility(View.VISIBLE);
            } else {
                ((PhotoHolder) holder).votingLinear.setVisibility(View.GONE);
            }

            //    Toast.makeText(ctx, newsFeedModel.getPollStatus()+" - "+newsFeedModel.getUserName(),Toast.LENGTH_SHORT).show();

            switch (newsFeedModel.getPollStatus()) {
                case "up":
                    ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
                case "down":
                    ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
                default:
                    ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
            }

            Glide.with(ctx.getApplicationContext()).load(ctx.getString(R.string.BASE_URL_PHOTOS_NEWS_FEED)
                    + newsFeedModel.getPhoto())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PhotoHolder) holder).progressBar.setVisibility(View.INVISIBLE);
                            ((PhotoHolder) holder).photo.setVisibility(View.VISIBLE);

                            return false;
                        }
                    })
                    .into(((PhotoHolder) holder).photo);

            ((PhotoHolder) holder).username.setText(newsFeedModel.getUserName());

            if (!Objects.equals(newsFeedModel.getCaption(), "")) {
                ((PhotoHolder) holder).caption.setText(newsFeedModel.getCaption());
                ((PhotoHolder) holder).captionLinear.setVisibility(View.VISIBLE);
            } else {
                ((PhotoHolder) holder).captionLinear.setVisibility(View.GONE);
            }

            if (Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                ((PhotoHolder) holder).options.setVisibility(View.VISIBLE);
            } else {
                ((PhotoHolder) holder).options.setVisibility(View.INVISIBLE);
            }

            ((PhotoHolder) holder).options.setOnClickListener(view -> {
                if (Objects.equals(newsFeedModel.getUserName(), preferences.getString("username", ""))) {
                    powerMenu = new PowerMenu.Builder(ctx)
                            .addItem(new PowerMenuItem("Delete", false))
                            .setAnimation(MenuAnimation.DROP_DOWN)
                            .setMenuRadius(10f)
                            .setMenuShadow(10f)
                            .setTextColor(ctx.getResources().getColor(R.color.black, null))
                            .setSelectedTextColor(Color.WHITE)
                            .setMenuColor(Color.WHITE)
                            .setSelectedMenuColor(ctx.getResources().getColor(R.color.colorPrimary, null))
                            .setOnMenuItemClickListener((position1, item) -> {
                                powerMenu.dismiss();
                                deleteDialog(newsFeedModel.getPostId(), holder.getAdapterPosition());
                            })
                            .build();

                    powerMenu.showAsDropDown(view);
                }
            });

            ((PhotoHolder) holder).thumbsUp.setOnClickListener(view -> {

                switch (newsFeedModel.getPollStatus()) {
                    case "up":
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    case "down":
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    default:
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                }
                setPollOnAction(newsFeedModel, POLL_UP, ((PhotoHolder) holder).thumbsUp, ((PhotoHolder) holder).thumbsDown, ((PhotoHolder) holder).voteUp, ((PhotoHolder) holder).voteDown, ((PhotoHolder) holder).votingLinear);
            });

            ((PhotoHolder) holder).thumbsDown.setOnClickListener(view -> {

                switch (newsFeedModel.getPollStatus()) {
                    case "up":
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        break;
                    case "down":
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    default:
                        ((PhotoHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((PhotoHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        break;
                }
                setPollOnAction(newsFeedModel, POLL_DOWN, ((PhotoHolder) holder).thumbsDown, ((PhotoHolder) holder).thumbsUp, ((PhotoHolder) holder).voteUp, ((PhotoHolder) holder).voteDown, ((PhotoHolder) holder).votingLinear);
            });

            ((PhotoHolder) holder).username.setOnClickListener(view -> {
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

            ((PhotoHolder) holder).dp.setOnClickListener(view -> {
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

            setClickListeners(((PhotoHolder) holder).comment);
            setTagsToViews(((PhotoHolder) holder).comment, position);

        } else if (holder instanceof DealHolder) {
            GlideApp.with(ctx.getApplicationContext())
                    .load(ctx.getString(R.string.BASE_URL_PROFILE_PIC) + newsFeedModel.getUserName() + ".jpg")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((DealHolder) holder).dp.setImageResource(R.drawable.no_dp_big);
                            ((DealHolder) holder).dpProgress.setVisibility(View.INVISIBLE);
                            ((DealHolder) holder).dp.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((DealHolder) holder).dpProgress.setVisibility(View.INVISIBLE);
                            ((DealHolder) holder).dp.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.no_dp_big)
                    .into(((DealHolder) holder).dp);

            ((DealHolder) holder).date.setText(newsFeedModel.getDate());

            if (!Objects.equals(newsFeedModel.getVoteUp(), "0")
                    || !Objects.equals(newsFeedModel.getVoteDown(), "0")
                    || !Objects.equals(newsFeedModel.getVoteUp(), "0")
                    && !Objects.equals(newsFeedModel.getVoteDown(), "0")) {
                ((DealHolder) holder).voteUp.setText(newsFeedModel.getVoteUp() + " " + ctx.getString(R.string.vote_up));
                ((DealHolder) holder).voteDown.setText(newsFeedModel.getVoteDown() + " " + ctx.getString(R.string.vote_down));

                ((DealHolder) holder).votingLinear.setVisibility(View.VISIBLE);
            } else {
                ((DealHolder) holder).votingLinear.setVisibility(View.GONE);
            }

            //    Toast.makeText(ctx, newsFeedModel.getPollStatus()+" - "+newsFeedModel.getUserName(),Toast.LENGTH_SHORT).show();

            switch (newsFeedModel.getPollStatus()) {
                case "up":
                    ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
                case "down":
                    ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                    ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
                default:
                    ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                    break;
            }

            ((DealHolder) holder).username.setText(newsFeedModel.getUserName());
            ((DealHolder) holder).dName.setText(newsFeedModel.getDName());
            ((DealHolder) holder).dCategory.setText(newsFeedModel.getDCategory());
            ((DealHolder) holder).dCondition.setText(newsFeedModel.getDCondition());
            ((DealHolder) holder).dCost.setText(newsFeedModel.getDCost());
            ((DealHolder) holder).dStatus.setText(newsFeedModel.getDStatus());
            ((DealHolder) holder).dDesc.setText(newsFeedModel.getDDesc());

            ((DealHolder) holder).viewDeal.setOnClickListener(view -> {
                Intent intent = new Intent(ctx, ViewProductActivity.class);
                intent.putExtra("dealid", newsFeedModel.getDealId() + "");
                ctx.startActivity(intent);
            });

            ((DealHolder) holder).thumbsUp.setOnClickListener(view -> {

                switch (newsFeedModel.getPollStatus()) {
                    case "up":
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    case "down":
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    default:
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                }
                setPollOnAction(newsFeedModel, POLL_UP, ((DealHolder) holder).thumbsUp, ((DealHolder) holder).thumbsDown, ((DealHolder) holder).voteUp, ((DealHolder) holder).voteDown, ((DealHolder) holder).votingLinear);
            });

            ((DealHolder) holder).thumbsDown.setOnClickListener(view -> {

                switch (newsFeedModel.getPollStatus()) {
                    case "up":
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        break;
                    case "down":
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        break;
                    default:
                        ((DealHolder) holder).thumbsUp.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.white, null)));
                        ((DealHolder) holder).thumbsDown.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.link_blue, null)));
                        break;
                }
                setPollOnAction(newsFeedModel, POLL_DOWN, ((DealHolder) holder).thumbsDown, ((DealHolder) holder).thumbsUp, ((DealHolder) holder).voteUp, ((DealHolder) holder).voteDown, ((DealHolder) holder).votingLinear);
            });

            ((DealHolder) holder).username.setOnClickListener(view -> {
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

            ((DealHolder) holder).dp.setOnClickListener(view -> {
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

            setClickListeners(((DealHolder) holder).comment);
            setTagsToViews(((DealHolder) holder).comment, position);
        }
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

    private void deletePost(String postId, int position) {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PostsController/deletePost?postid=" + postId;

        stringRequest = new StringRequest(url, response -> {
            if (Objects.equals(response, "success")) {
                list.remove(position);
                notifyItemRemoved(position);
                //    notifyDataSetChanged();
            } else {
                Toast.makeText(ctx, "Error deleting!", Toast.LENGTH_LONG).show();
            }
        }, error -> {
        });

        requestQueue.add(stringRequest);
    }

    private void deleteDialog(String postId, int position) {
        deleteDialog = new DialogSheet(ctx)
                .setTitle(R.string.app_name)
                .setMessage("Delete post?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, v -> {
                    deletePost(postId, position);
                    deleteDialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, view -> deleteDialog.dismiss())
                .setBackgroundColor(ctx.getResources().getColor(R.color.colorGray, null)) // Your custom background color
                .setButtonsColorRes(R.color.colorPrimary);

        deleteDialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        NewsFeedModel model = list.get(position);

        if (Objects.equals(model != null ? model.getType() : null, "photo")) {
            return PHOTO_LAYOUT;
        } else {
            return DEAL_LAYOUT;
        }
    }

    private void setClickListeners(View view) {
        view.setOnClickListener(clickListener);
    }

    private void setTagsToViews(View view, int position) {
        view.setTag(R.id.key_position, position);
    }
}
