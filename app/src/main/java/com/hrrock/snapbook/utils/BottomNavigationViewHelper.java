package com.hrrock.snapbook.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.HomeActivity;
import com.hrrock.snapbook.activities.MomentActivity;
import com.hrrock.snapbook.activities.NotificationActivity;
import com.hrrock.snapbook.activities.ProfileActivity;
import com.hrrock.snapbook.activities.SearchActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by hp-u on 2/18/2018.
 */

public class BottomNavigationViewHelper {

    public static void setUpBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableBottomNavigationView(Context ctx, Activity activity, BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_home:
                    if (!(activity instanceof HomeActivity)) {
                        ctx.startActivity(new Intent(ctx, HomeActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        activity.overridePendingTransition(0, 0);
                        //  Bungee.fade(ctx);
                        activity.finish();
                    }
                    break;

                case R.id.item_search:
                    if (!(activity instanceof SearchActivity)) {
                        ctx.startActivity(new Intent(ctx, SearchActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        activity.overridePendingTransition(0, 0);
                        // Bungee.fade(ctx);
                        activity.finish();
                    }
                    break;

                case R.id.item_moment:
                    if (!(activity instanceof MomentActivity)) {
                        ctx.startActivity(new Intent(ctx, MomentActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        activity.overridePendingTransition(0, 0);
                        //   Bungee.fade(ctx);
                        activity.finish();
                    }
                    break;

                case R.id.item_notification:
                    if (!(activity instanceof NotificationActivity)) {
                        ctx.startActivity(new Intent(ctx, NotificationActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        activity.overridePendingTransition(0, 0);
                        // Bungee.fade(ctx);
                        activity.finish();
                    }
                    break;

                case R.id.item_profile:
                    if (!(activity instanceof ProfileActivity)) {
                        ctx.startActivity(new Intent(ctx, ProfileActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        activity.overridePendingTransition(0, 0);
                        // Bungee.fade(ctx);
                        activity.finish();
                    }
                    break;
            }

            return false;
        });
    }
}
