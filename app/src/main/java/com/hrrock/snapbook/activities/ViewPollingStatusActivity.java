package com.hrrock.snapbook.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.adapters.CommentsAdapter;
import com.hrrock.snapbook.models.CommentsModel;
import com.hrrock.snapbook.networks.VolleyConnect;
import com.qhutch.bottomsheetlayout.BottomSheetLayout;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewPollingStatusActivity extends AppCompatActivity {
    private Context ctx;
    private RequestQueue requestQueue;
    private AnimatedPieView animatedPieView;
    private RelativeLayout rel_progress, rel_noVotes, viewComments, rel_commentsProgress, rel_noComments, rel_error;
    private LinearLayout linear_chart;
    private TextView productName, tagViewComment;
    private BottomSheetLayout bottomSheetLayout;
    private ListView commentsList;
    private CommentsAdapter adapter;
    private List<CommentsModel> list;
    private JsonArrayRequest jsonArrayRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_polling_status);
        ctx = this;

        requestQueue = VolleyConnect.getInstance().getRequestQueue();
        rel_noVotes = findViewById(R.id.rel_no_votes);
        rel_progress = findViewById(R.id.rel_progress);
        animatedPieView = findViewById(R.id.animatedPieView);
        linear_chart = findViewById(R.id.linear_votingOnPollingStatus);
        productName = findViewById(R.id.productNameOnProductStatus);
        ImageView back = findViewById(R.id.back);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout_product_status);
        viewComments = findViewById(R.id.rel_viewComments);
        tagViewComment = findViewById(R.id.tag_viewComment_onProductStatus);
        commentsList = findViewById(R.id.commentsListOnProductStatus);
        rel_commentsProgress = findViewById(R.id.rel_progress_commentsOnProductStatus);
        rel_noComments = findViewById(R.id.rel_no_commentsOnProductStatus);
        rel_error = findViewById(R.id.rel_network_errorOnPS);
        list = new ArrayList<>();

        getPollCounts();
        setNavigationStatusBarColor();

        back.setOnClickListener(view -> onBackPressed());
        bottomSheetStuffs();

        getComments();
        adapter = new CommentsAdapter(ctx, R.layout.layout_polling_status_comments_row, list);
        commentsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void setupPieView(float voteUp, float voteDown) {
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.startAngle(-90)// Starting angle offset
                .addData(new SimplePieInfo(voteUp, Color.parseColor("#34AC3C"), "Vote UP"))//Data (bean that implements the IPieInfo interface)
                .addData(new SimplePieInfo(voteDown, Color.parseColor("#FB0005"), "Vote DOWN"))
                .duration(2000)// draw pie animation duration
                .strokeMode(false)
                .drawText(true)
                .textSize(25f)
                .focusAlphaType(AnimatedPieViewConfig.FOCUS_WITH_ALPHA_REV)
                .focusAlpha(50)
                .splitAngle(2f);

        animatedPieView.start(config);
    }

    private void getPollCounts() {
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/getPollingDealbyId?postid=" + getPostId();

        jsonArrayRequest = new JsonArrayRequest(url, response -> {
            if (response.length() > 0) {
                JSONObject jsonObject = response.optJSONObject(0);
                productName.setText(jsonObject.optString("productname"));

                calVotingPercentage(Integer.parseInt(jsonObject.optString("voteup")),
                        Integer.parseInt(jsonObject.optString("votedown")));

                if (Integer.parseInt(jsonObject.optString("voteup")) != 0 ||
                        Integer.parseInt(jsonObject.optString("votedown")) != 0) {

                    rel_progress.setVisibility(View.GONE);
                    rel_noVotes.setVisibility(View.GONE);
                    linear_chart.setVisibility(View.VISIBLE);
                } else {
                    rel_progress.setVisibility(View.GONE);
                    linear_chart.setVisibility(View.GONE);
                    rel_noVotes.setVisibility(View.VISIBLE);
                }
            } else {
                rel_progress.setVisibility(View.GONE);
                linear_chart.setVisibility(View.GONE);
                rel_noVotes.setVisibility(View.VISIBLE);
            }
        }, error -> {

        });

        requestQueue.add(jsonArrayRequest);
    }

    private void setNavigationStatusBarColor() {
        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(ctx, R.color.appBarColor));

        window.setNavigationBarColor(getResources().getColor(R.color.colorGray, null));
    }

    private String getPostId() {
        if (getIntent().hasExtra("postid")) {
            return Objects.requireNonNull(getIntent().getExtras()).getString("postid");
        } else {
            return "";
        }
    }

    private void calVotingPercentage(int voteUp, int voteDown) {
        int totalVotes = voteUp + voteDown;
        if (totalVotes != 0) {
            float up = (voteUp * 100) / totalVotes;
            float down = (voteDown * 100) / totalVotes;

            setupPieView(up, down);
        } else {
            setupPieView(0, 0);
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void bottomSheetStuffs() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
       // int width = displayMetrics.widthPixels;
        int marginTop=(height*30)/100;

        BottomSheetLayout.LayoutParams layoutParams=new BottomSheetLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0,marginTop,0,0);
        bottomSheetLayout.setLayoutParams(layoutParams);

        viewComments.setOnClickListener(view -> bottomSheetLayout.toggle());

        bottomSheetLayout.setOnProgressListener(progress -> {
            if (progress == 0.0) {
                tagViewComment.setText("View Comments");
            } else {
                tagViewComment.setText("Hide Comments");
            }
        });
    }

    private void getComments() {
        list.clear();
        final String url = "http://" + ctx.getString(R.string.ip) + "/Snapbook/index.php/PollingController/getDealCommentsByPostId?postid=" + getPostId();

        jsonArrayRequest = new JsonArrayRequest(url, response -> {
            if (response.length() > 0) {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = response.optJSONObject(i);

                    CommentsModel commentsModel = new CommentsModel(jsonObject.optString("commentid"),
                            jsonObject.optString("postid"),
                            jsonObject.optString("username"),
                            jsonObject.optString("comment"));

                    list.add(commentsModel);
                }
                adapter.notifyDataSetChanged();
                rel_commentsProgress.setVisibility(View.GONE);
                rel_noComments.setVisibility(View.GONE);
                rel_error.setVisibility(View.GONE);
                commentsList.setVisibility(View.VISIBLE);
            } else {
                rel_commentsProgress.setVisibility(View.GONE);
                commentsList.setVisibility(View.GONE);
                rel_error.setVisibility(View.GONE);
                rel_noComments.setVisibility(View.VISIBLE);
            }
        }, error -> {
            rel_commentsProgress.setVisibility(View.GONE);
            commentsList.setVisibility(View.GONE);
            rel_noComments.setVisibility(View.GONE);
            rel_error.setVisibility(View.VISIBLE);
        });

        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetLayout.isExpended()) {
            bottomSheetLayout.collapse();
        } else {
            super.onBackPressed();
        }
    }
}
