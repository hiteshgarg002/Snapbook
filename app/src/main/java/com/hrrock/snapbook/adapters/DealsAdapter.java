package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hrrock.snapbook.R;
import com.hrrock.snapbook.models.DealsModel;
import com.hrrock.snapbook.utils.SquareImageView;

import java.util.List;
import java.util.Objects;

public class DealsAdapter extends ArrayAdapter<DealsModel> {
    private Context ctx;
    private int layout;
    private List<DealsModel> deals;
    private View.OnClickListener clickListener;
    private LayoutInflater layoutInflater;

    public DealsAdapter(@NonNull Context context, int resource, @NonNull List<DealsModel> objects, View.OnClickListener clickListener) {
        super(context, resource, objects);
        this.ctx = context;
        this.deals = objects;
        this.clickListener = clickListener;
        this.layout = resource;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        private TextView productName, productCost, productCondition, productStatus;
        private ImageView productConditionStar, productShare;
        private SquareImageView productPhoto;
        private RelativeLayout relProductPhoto, relProductPhotoProgress;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        DealsAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, null);

            holder = new DealsAdapter.ViewHolder();
            holder.productName = convertView.findViewById(R.id.productNameOnDeals);
            holder.productCondition = convertView.findViewById(R.id.productConditionOnDeals);
            holder.productPhoto = convertView.findViewById(R.id.productImageOnDeals);
            holder.productConditionStar = convertView.findViewById(R.id.productConditionStarOnDeals);
            holder.productStatus = convertView.findViewById(R.id.productStatusOnDeals);
            holder.productShare = convertView.findViewById(R.id.productShareOnDeals);
            holder.productCost = convertView.findViewById(R.id.productCostOnDeals);
            holder.productShare = convertView.findViewById(R.id.productShareOnDeals);
            holder.relProductPhoto = convertView.findViewById(R.id.relProductImageOnDeals);
            holder.relProductPhotoProgress = convertView.findViewById(R.id.relProductImageLoadingOnDeals);

            convertView.setTag(holder);
        } else {
            holder = (DealsAdapter.ViewHolder) convertView.getTag();
        }

        DealsModel dealsModel = getItem(position);

        assert dealsModel != null;
        holder.productName.setText(dealsModel.getProductName());
        holder.productCondition.setText(dealsModel.getProductCondition());

        if (Objects.equals(dealsModel.getProductCondition(), "New")) {
            holder.productConditionStar.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.green, null)));
        }

        if (Objects.equals(dealsModel.getProductCondition(), "Old")) {
            holder.productConditionStar.setImageTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.red, null)));
        }

        holder.productStatus.setText(dealsModel.getProductStatus());
        holder.productCost.setText(dealsModel.getProductCost());

        if (!Objects.equals(dealsModel.getProductPhoto(), "")) {
            Glide.with(ctx.getApplicationContext())
                    .load(ctx.getString(R.string.BASE_URL_PHOTOS_PRODUCT) + dealsModel.getProductPhoto())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.relProductPhotoProgress.setVisibility(View.INVISIBLE);
                            holder.relProductPhoto.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.productPhoto);
        }

        setClickListeners(holder.productShare);
        setTagsToViews(holder.productShare, position);

        return convertView;
    }

    @Override
    public int getCount() {
        if (deals != null) {
            return deals.size();
        }
        return 0;

    }

    @Nullable
    @Override
    public DealsModel getItem(int position) {
        return deals.get(position);
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
