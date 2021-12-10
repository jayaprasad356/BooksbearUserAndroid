package com.asquare.booksbear.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.asquare.booksbear.activity.MainActivity;
import com.asquare.booksbear.fragment.SubCategoryFragment;
import com.asquare.booksbear.fragment.XProductListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.asquare.booksbear.R;
import com.asquare.booksbear.fragment.ProductDetailFragment;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.Session;
import com.asquare.booksbear.model.Product;

/**
 * Created by shree1 on 3/16/2017.
 */

public class AdapterStyle1 extends RecyclerView.Adapter<AdapterStyle1.VideoHolder> {

    public ArrayList<Product> productList;
    public Activity activity;
    public String secname;
    public String secid;
    public int itemResource;
    Context context;

    public AdapterStyle1(Context context, Activity activity, ArrayList<Product> productList,String secname, String secid, int itemResource) {
        this.context = context;
        this.activity = activity;
        this.productList = productList;
        this.itemResource = itemResource;
        this.secname = secname;
        this.secid = secid;


    }


    @Override
    public int getItemCount() {
        int product;
        if (productList.size() > 13) {
            product = 13;
        } else {
            product = productList.size();
        }
        return product;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(VideoHolder holder, final int position) {
        final Product product = productList.get(position);

        if (position == 12){
            Picasso.get()
                    .load(R.drawable.allproductsimg)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.allproductsimg)
                    .error(R.drawable.allproductsimg)
                    .into(holder.thumbnail);
            holder.tvTitle.setText("View All Products");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (secname.equals("Super Saver Lots")){
                        Fragment fragment = new XProductListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.ID, "57");
                        bundle.putString(Constant.NAME, "Super Saver Pack");
                        bundle.putString(Constant.FROM, "sub_cate");
                        fragment.setArguments(bundle);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();


                    }else if (secname.equals("Explore Educational Books")){
                        Fragment fragment = new SubCategoryFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.ID, "2");
                        bundle.putString(Constant.NAME, "Books");
                        bundle.putString(Constant.FROM, "category");
                        fragment.setArguments(bundle);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();


                    }
                    else if (secname.equals("Job & Comepetetive Exams")){
                        AppCompatActivity activity1 = (AppCompatActivity) context;
                        Fragment fragment = new XProductListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.ID, "8");
                        bundle.putString(Constant.NAME, "Job & Govt. exams");
                        bundle.putString(Constant.FROM, "sub_cate");
                        fragment.setArguments(bundle);
                        activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                    }
                    else {
                        Fragment fragment = new SubCategoryFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.ID, productList.get(0).getCategory_id());
                        bundle.putString(Constant.NAME, secname);
                        bundle.putString(Constant.FROM, "category");
                        fragment.setArguments(bundle);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                    }
//                    Fragment fragment = new XProductListFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(Constant.FROM, "section");
//                    bundle.putString(Constant.NAME, secname);
//                    bundle.putString(Constant.ID, secid);
//
//                    fragment.setArguments(bundle);
//
//                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                }
            });

        }
        else {
            Picasso.get()
                    .load(product.getImage())
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.thumbnail);


            holder.tvTitle.setText(product.getName());


            double price = 0;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (product.getPriceVariations().get(0).getDiscounted_price().equals("0") || product.getPriceVariations().get(0).getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(product.getPriceVariations().get(0).getPrice()) + ((Float.parseFloat(product.getPriceVariations().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(product.getPriceVariations().get(0).getDiscounted_price()) + ((Float.parseFloat(product.getPriceVariations().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            }
            holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AppCompatActivity activity1 = (AppCompatActivity) context;
                    Fragment fragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.ID, product.getId());
                    bundle.putString(Constant.FROM, "section");
                    bundle.putInt("vpos", 0);
                    fragment.setArguments(bundle);
                    activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();


                }
            });

        }



    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemResource, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class VideoHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView tvTitle, tvPrice;
        public RelativeLayout relativeLayout;

        public VideoHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            relativeLayout = itemView.findViewById(R.id.play_layout);

        }


    }
}