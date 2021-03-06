package com.asquare.booksbear.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.asquare.booksbear.R;
import com.asquare.booksbear.activity.MainActivity;
import com.asquare.booksbear.fragment.AllProductsFragment;
import com.asquare.booksbear.fragment.ProductListFragment;
import com.asquare.booksbear.fragment.SellerProductsFragment;
import com.asquare.booksbear.fragment.SubCategoryFragment;
import com.asquare.booksbear.fragment.XProductListFragment;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.model.Category;


public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionHolder> {

    public final ArrayList<Category> sectionList;
    public final Activity activity;
    final Context context;

    public SectionAdapter(Context context, Activity activity, ArrayList<Category> sectionList) {
        this.context = context;
        this.activity = activity;
        this.sectionList = sectionList;
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    @Override
    public void onBindViewHolder(SectionHolder holder1, final int position) {
        final Category section;
        section = sectionList.get(position);
        holder1.tvTitle.setText(section.getName());
        holder1.tvSubTitle.setText(section.getSubtitle());

        switch (section.getStyle()) {
            case "style_1":
                holder1.recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                AdapterStyle1 adapter = new AdapterStyle1(context, activity, section.getProductList(),section.getName(),section.getId(), R.layout.offer_layout);
                holder1.recyclerView.setAdapter(adapter);
                break;
            case "style_2":
                holder1.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                AdapterStyle2 adapterStyle2 = new AdapterStyle2(context, activity, section.getProductList());
                holder1.recyclerView.setAdapter(adapterStyle2);
                break;
            case "style_3":
                holder1.recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
                AdapterStyle1 adapter3 = new AdapterStyle1(context, activity, section.getProductList(),section.getName(),section.getId(), R.layout.offer_layout);
                holder1.recyclerView.setAdapter(adapter3);
                break;
        }

        holder1.tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (section.getName().equals("All Product")){
                    Fragment fragment = new AllProductsFragment();
                    Bundle bundle = new Bundle();
                    fragment.setArguments(bundle);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                }
                else if (section.getName().equals("Super Saver Lots")){
                    Fragment fragment = new XProductListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.ID, "57");
                    bundle.putString(Constant.NAME, "Super Saver Pack");
                    bundle.putString(Constant.FROM, "sub_cate");
                    fragment.setArguments(bundle);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                }
                else if (section.getName().equals("Mystery BearBox")){
                    Fragment fragment = new XProductListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.FROM, "section");
                    bundle.putString(Constant.NAME, section.getName());
                    bundle.putString(Constant.ID, section.getId());

                    fragment.setArguments(bundle);

                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                }
                else if (section.getName().equals("Explore Educational Books")){
                    Fragment fragment = new SubCategoryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.ID, "2");
                    bundle.putString(Constant.NAME, "Books");
                    bundle.putString(Constant.FROM, "category");
                    fragment.setArguments(bundle);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                }
                else if (section.getName().equals("Job & Comepetetive Exams")){
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
                    bundle.putString(Constant.ID, section.getProductList().get(0).getCategory_id());
                    bundle.putString(Constant.NAME, section.getName());
                    bundle.putString(Constant.FROM, "category");
                    fragment.setArguments(bundle);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
//                    Fragment fragment = new XProductListFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(Constant.FROM, "section");
//                    bundle.putString(Constant.NAME, section.getName());
//                    bundle.putString(Constant.ID, section.getId());
//
//                    fragment.setArguments(bundle);
//
//                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();

                }


            }
        });
    }

    @Override
    public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_layout, parent, false);
        return new SectionHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class SectionHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSubTitle;
        final TextView tvMore;
        final RecyclerView recyclerView;
        final RelativeLayout relativeLayout;

        public SectionHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvMore = itemView.findViewById(R.id.tvMore);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);

        }
    }


}
