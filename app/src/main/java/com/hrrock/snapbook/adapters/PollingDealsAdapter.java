package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.activities.UserProfileActivity;
import com.hrrock.snapbook.activities.ViewPollingStatusActivity;
import com.hrrock.snapbook.activities.ViewProductActivity;
import com.hrrock.snapbook.models.FollowModel;
import com.hrrock.snapbook.models.PollingDealsModel;
import com.hrrock.snapbook.networks.VolleyConnect;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

public class PollingDealsAdapter extends RecyclerView.Adapter<PollingDealsAdapter.MyHolder> {
    private List<PollingDealsModel> list = Collections.emptyList();
    private LayoutInflater inflater;
    private Context ctx;

    public PollingDealsAdapter(Context context, List<PollingDealsModel> list) {
        this.list = list;
        this.ctx = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView dealName, date;
        RelativeLayout viewProduct,viewStatus;

        MyHolder(View itemView) {
            super(itemView);
            dealName = itemView.findViewById(R.id.dealNameOnPD);
            date = itemView.findViewById(R.id.dateOnPD);
            viewProduct=itemView.findViewById(R.id.viewProductOnPD);
            viewStatus=itemView.findViewById(R.id.viewProductStatusOnPD);
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.layout_polling_deals_row, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        PollingDealsModel dealsModel = list.get(position);
        if (dealsModel != null) {
            holder.date.setText(dealsModel.getDate());
            holder.dealName.setText(dealsModel.getDealName());

            holder.viewProduct.setOnClickListener(view -> {
                Intent intent=new Intent(ctx, ViewProductActivity.class);
                intent.putExtra("dealid",dealsModel.getDealId());
                ctx.startActivity(intent);
                Bungee.inAndOut(ctx);
            });

            holder.viewStatus.setOnClickListener(view -> {
                Intent intent=new Intent(ctx, ViewPollingStatusActivity.class);
                intent.putExtra("postid",dealsModel.getPostId());
                ctx.startActivity(intent);
                Bungee.inAndOut(ctx);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
