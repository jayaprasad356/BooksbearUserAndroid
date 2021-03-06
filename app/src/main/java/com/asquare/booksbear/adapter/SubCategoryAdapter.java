package com.asquare.booksbear.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.asquare.booksbear.fragment.XProductListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.asquare.booksbear.R;
import com.asquare.booksbear.fragment.ProductListFragment;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.model.Category;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {
    public final ArrayList<Category> categorylist;
    final int layout;
    final Activity activity;
    final Context context;
    String from = "";


    public SubCategoryAdapter(Context context, Activity activity, ArrayList<Category> categorylist, int layout, String from) {
        this.context = context;
        this.categorylist = categorylist;
        this.layout = layout;
        this.activity = activity;
        this.from = from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Category model = categorylist.get(position);
        holder.txttitle.setText(model.getName());

        Picasso.get()
                .load(model.getImage())
                .fit()
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .into(holder.imgcategory);

        holder.lytMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity1 = (AppCompatActivity) context;
                Fragment fragment = new XProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.ID, model.getId());
                bundle.putString(Constant.NAME, model.getName());
                bundle.putString(Constant.FROM, from);
                fragment.setArguments(bundle);
                activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                Log.d("SUBCATLOG",model.getId() + " - "+model.getName() + " - " + from);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView txttitle;
        final ImageView imgcategory;
        final LinearLayout lytMain;

        public ViewHolder(View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);
            imgcategory = itemView.findViewById(R.id.imgcategory);
            txttitle = itemView.findViewById(R.id.txttitle);
        }

    }
}
