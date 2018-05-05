package com.hrrock.snapbook.networks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.multipartutility.MultipartUtility;
import com.hrrock.snapbook.utils.GlideApp;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hp-u on 11/28/2017.
 */

public class CallHttpRequest extends AsyncTask<String, Void, String> {
    private File destination;
    @SuppressLint("StaticFieldLeak")
    private Context ctx;
    @SuppressLint("StaticFieldLeak")
    private LoadingButton loginButton;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBar;
    private String requestFrom;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    @SuppressLint("StaticFieldLeak")
    private Activity activityRef;
    // private static final String LOGIN_ACTIVITY = "LoginActivity";
    private static final String SIGNUP_ACTIVITY = "SignupActivity";
    private static final String PROFILE_ACTIVITY = "ProfileActivity";
    private static final String EDIT_PROFILE_ACTIVITY = "EditProfileActivity";
    private static final String MOMENT_ACTIVITY = "MomentActivity";
    private static final String CREATE_DEAL_ACTIVITY = "CreateDealActivity";
    private static final String SPLASH_SCREEN_ACTIVITY = "SplashScreenActivity";
    private static final String USER_PREFERENCE = "userinfo";
    @SuppressLint("StaticFieldLeak")
    private AVLoadingIndicatorView dpUpdateProgress;
    @SuppressLint("StaticFieldLeak")
    private CircleImageView dp;
    @SuppressLint("StaticFieldLeak")
    private FloatingActionButton dpFab;
    private BottomNavigationViewEx bottomNavigationViewEx;

    public CallHttpRequest(Context ctx, LoadingButton loginButton, String requestFrom, Activity activityRef) {
        this.ctx = ctx;
        this.loginButton = loginButton;
        this.requestFrom = requestFrom;
        this.activityRef = activityRef;
    }

    public CallHttpRequest(Context ctx, LoadingButton loginButton, String requestFrom) {
        this.ctx = ctx;
        this.loginButton = loginButton;
        this.requestFrom = requestFrom;
    }

    public CallHttpRequest(Context ctx, String requestFrom, Activity activityRef, ProgressBar progressBar) {
        this.ctx = ctx;
        this.requestFrom = requestFrom;
        this.activityRef = activityRef;
        this.progressBar = progressBar;
    }

    public CallHttpRequest(Context ctx, File destination, String requestFrom) {
        this.ctx = ctx;
        this.destination = destination;
        this.requestFrom = requestFrom;
    }

    public CallHttpRequest(Context ctx, File destination, String requestFrom, AVLoadingIndicatorView dpUpdateProgress, CircleImageView dp, FloatingActionButton dpFab, BottomNavigationViewEx bottomNavigationViewEx) {
        this.ctx = ctx;
        this.destination = destination;
        this.requestFrom = requestFrom;
        this.dpUpdateProgress = dpUpdateProgress;
        this.dp = dp;
        this.dpFab = dpFab;
        this.bottomNavigationViewEx = bottomNavigationViewEx;
        preferences = ctx.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
    }

    public CallHttpRequest(Context ctx, File destination, String requestFrom, AVLoadingIndicatorView dpUpdateProgress, CircleImageView dp, FloatingActionButton dpFab) {
        this.ctx = ctx;
        this.destination = destination;
        this.requestFrom = requestFrom;
        this.dpUpdateProgress = dpUpdateProgress;
        this.dp = dp;
        this.dpFab = dpFab;
        preferences = ctx.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
    }

    @Override
    protected String doInBackground(String... url) {
        String res;
        if ((Objects.equals(requestFrom, SIGNUP_ACTIVITY)) || (Objects.equals(requestFrom, PROFILE_ACTIVITY)) ||
                (Objects.equals(requestFrom, MOMENT_ACTIVITY)) || (Objects.equals(requestFrom, CREATE_DEAL_ACTIVITY)) ||
                (Objects.equals(requestFrom, EDIT_PROFILE_ACTIVITY))) {
            res = fileUpload(url[0], destination);
        } else {
            res = callRequest(url[0]);
        }
        return res;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if ((Objects.equals(requestFrom, PROFILE_ACTIVITY))) {
            if ((Objects.equals(s, "success"))) {
                bottomNavigationViewEx.setVisibility(View.VISIBLE);
                dp.setImageDrawable(null);
                String dpURL = "http://" + ctx.getString(R.string.ip) + "/Snapbook/images/profile/" + preferences.getString("profile", "");
                GlideApp.with(ctx)
                        .load(dpURL)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.no_dp_big)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                dpUpdateProgress.setVisibility(View.INVISIBLE);
                                dp.setVisibility(View.VISIBLE);
                                dpFab.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(dp);
            } else {
                dpUpdateProgress.setVisibility(View.INVISIBLE);
                dp.setVisibility(View.VISIBLE);
                dpFab.setVisibility(View.VISIBLE);
            }
        }

        if ((Objects.equals(requestFrom, EDIT_PROFILE_ACTIVITY))) {
            if ((Objects.equals(s, "success"))) {
                dp.setImageDrawable(null);
                String dpURL = "http://" + ctx.getString(R.string.ip) + "/Snapbook/images/profile/" + preferences.getString("profile", "");
                Glide.with(ctx)
                        .load(dpURL)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                dpUpdateProgress.setVisibility(View.INVISIBLE);
                                dp.setVisibility(View.VISIBLE);
                                dpFab.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(dp);
            } else {
                dpUpdateProgress.setVisibility(View.INVISIBLE);
                dp.setVisibility(View.VISIBLE);
                dpFab.setVisibility(View.VISIBLE);
            }
        }

        /*if ((Objects.equals(requestFrom, LOGIN_ACTIVITY))) {
            if (Objects.equals(s, "invalid")) {
                loginButton.loadingFailed();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.optJSONObject(i);
                        preferences = ctx.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
                        editor = preferences.edit();

                        editor.putString("name", jsonObject.optString("name"));
                        editor.putString("password", jsonObject.optString("password"));
                        editor.putString("mobileno", jsonObject.optString("mobileno"));
                        editor.putBoolean("login", true);
                        editor.apply();

                        loginButton.loadingSuccessful();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if ((Objects.equals(requestFrom, SPLASH_SCREEN_ACTIVITY))) {
            if (Objects.equals(s, "invalid")) {
                progressBar.setVisibility(View.INVISIBLE);
                Bungee.slideRight(ctx);
                activityRef.finish();
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.optJSONObject(i);

                        preferences = ctx.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
                        editor = preferences.edit();

                        editor.putString("name", jsonObject.optString("name"));
                        editor.putString("password", jsonObject.optString("password"));
                        editor.putString("mobileno", jsonObject.optString("mobileno"));
                        editor.putBoolean("login", true);
                        editor.apply();

                        Bungee.card(ctx);
                        activityRef.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    private String callRequest(String Url) {
        try {
            URL url = new URL(Url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            DataInputStream in = new DataInputStream(con.getInputStream());

            StringBuilder output = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                output.append(str);
            }
            in.close();
            return (output.toString());
        } catch (Exception e) {
        }
        return (null);
    }

    private String fileUpload(String requestURL, File uploadFile) {
        String charset = "UTF-8";
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            multipart.addFilePart("image", uploadFile);
            List<String> response = multipart.finish();
            System.out.println("SERVER REPLIED:");
            for (String line : response) {
                System.out.println(line);
            }
            return response.get(0);
        } catch (IOException ex) {
            return ("Fail");
        }
    }
}




