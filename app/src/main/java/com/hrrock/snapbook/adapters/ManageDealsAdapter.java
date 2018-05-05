package com.hrrock.snapbook.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hrrock.snapbook.R;
import com.hrrock.snapbook.models.DealsModel;

import java.util.List;

public class ManageDealsAdapter extends ArrayAdapter<DealsModel> {
    private Context ctx;
    private int layout;
    private List<DealsModel> deals;
    private View.OnClickListener clickListener;
    private LayoutInflater layoutInflater;

    public ManageDealsAdapter(@NonNull Context context, int resource, @NonNull List<DealsModel> objects, View.OnClickListener clickListener) {
        super(context, resource, objects);
        ctx = context;
        layout = resource;
        this.deals = objects;
        this.clickListener = clickListener;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        private TextView productName, productCost, productCondition, productStatus, productCategory, date;
        private ImageView delete;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ManageDealsAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, null);
            holder = new ManageDealsAdapter.ViewHolder();
            holder.productName = convertView.findViewById(R.id.nameOnMD);
            holder.productCondition = convertView.findViewById(R.id.conditionOnMD);
            holder.productStatus = convertView.findViewById(R.id.statusOnMD);
            holder.productCost = convertView.findViewById(R.id.costOnMD);
            holder.productCategory = convertView.findViewById(R.id.categoryOnMD);
            holder.date = convertView.findViewById(R.id.dateOnMD);
            holder.delete = convertView.findViewById(R.id.deleteOnMD);

            convertView.setTag(holder);
        } else {
            holder = (ManageDealsAdapter.ViewHolder) convertView.getTag();
        }

        DealsModel dealsModel = getItem(position);

        if (dealsModel != null) {
            holder.productName.setText(dealsModel.getProductName());
            holder.productCategory.setText(dealsModel.getProductCategory());
            holder.productCondition.setText(dealsModel.getProductCondition());
            holder.productCost.setText(dealsModel.getProductCost());
            holder.productStatus.setText(dealsModel.getProductStatus());
            holder.date.setText(dealsModel.getDate());
        }

        setClickListeners(holder.delete);
        setTagsToViews(holder.delete, position);

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
