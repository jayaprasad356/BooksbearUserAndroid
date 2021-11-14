package com.asquare.booksbear.fragment;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.asquare.booksbear.helper.ApiConfig.GetSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.asquare.booksbear.R;
import com.asquare.booksbear.adapter.ProductLoadMoreAdapter;
import com.asquare.booksbear.adapter.XProductLoadMoreAdapter;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.Session;
import com.asquare.booksbear.helper.VolleyCallback;
import com.asquare.booksbear.model.Product;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@SuppressLint("NotifyDataSetChanged")
public class XProductListFragment extends Fragment {
    public static ArrayList<Product> productArrayList;
    @SuppressLint("StaticFieldLeak")
    public static XProductLoadMoreAdapter mAdapter;
    View root;
    Session session;
    int total;
    NestedScrollView nestedScrollView;
    Activity activity;
    int offset = 0;
    String id, filterBy, from;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    int filterIndex;
    TextView tvAlert;
    boolean isSort = false, isLoadMore = false;
    boolean isGrid = false;
    int resource;
    private ShimmerFrameLayout mShimmerViewContainer;
    LinearLayout lytList, lytGrid;

    ListView listView;
    EditText searchView;
    TextView noResult, msg;
    LinearLayout lytSearchView;
    String[] productsName;
    ArrayAdapter<String> arrayAdapter;
    int list_position;
    String query = "";
    String url = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        assert getArguments() != null;
        root = inflater.inflate(R.layout.fragment_x_product_list, container, false);
        setHasOptionsMenu(true);
        offset = 0;
        activity = getActivity();

        session = new Session(activity);

        from = requireArguments().getString(Constant.FROM);
        id = getArguments().getString(Constant.ID);
        list_position = getArguments().getInt(Constant.LIST_POSITION);


        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvAlert = root.findViewById(R.id.tvAlert);
        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        listView = root.findViewById(R.id.listView);
        searchView = root.findViewById(R.id.searchView);
        noResult = root.findViewById(R.id.noResult);
        msg = root.findViewById(R.id.msg);
        lytSearchView = root.findViewById(R.id.lytSearchView);
        lytList = root.findViewById(R.id.lytList);
        lytGrid = root.findViewById(R.id.lytGrid);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        if (session.getBoolean("grid")) {
            resource = R.layout.x_lyt_item_grid;
            isGrid = true;
            lytGrid.setVisibility(View.VISIBLE);
            lytList.setVisibility(View.GONE);
            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));

        } else {
            resource = R.layout.x_lyt_item_list;
            isGrid = false;
            lytGrid.setVisibility(View.GONE);
            lytList.setVisibility(View.VISIBLE);
            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }

        GetSettings(activity);

        filterIndex = -1;

        if (ApiConfig.isConnected(activity)) {
            if (from.equals("regular") || from.equals("sub_cate")) {
                GetData();
                isSort = true;
            } else if (from.equals("similar")) {
                GetSimilarData();
            } else if (from.equals("section")) {
                GetSectionData();
            }
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeLayout.setOnRefreshListener(() -> {
            if (productArrayList != null && productArrayList.size() > 0) {
                offset = 0;
                swipeLayout.setRefreshing(false);

                Log.d("SWIPE OFFER",String.valueOf(productArrayList.size()));
                productArrayList.clear();
                if (from.equals("regular") || from.equals("sub_cate")) {
                    GetData();
                } else if (from.equals("similar")) {
                    GetSimilarData();
                } else if (from.equals("section")) {
                    GetSectionData();
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(searchView.getText().toString().trim());
                if (searchView.getText().toString().trim().length() > 0 && listView.getVisibility() == View.GONE) {
                    listView.setVisibility(View.VISIBLE);
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close, 0);
                } else {
                    listView.setVisibility(View.GONE);
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchView.setOnEditorActionListener((v, actionId, event) -> {
            query = v.getText().toString().trim();
            listView.setVisibility(View.GONE);
            GetData();
            return true;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            searchView.setText(arrayAdapter.getItem(position));
            searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close, 0);
            query = arrayAdapter.getItem(position);
            listView.setVisibility(View.GONE);
            GetData();
        });

        searchView.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (searchView.getText().toString().trim().length() > 0) {
                    if (event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                        searchView.setText("");
                    }
                }
            }
            return false;
        });

        return root;
    }

    void GetData() {
        productArrayList = new ArrayList<>();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        switch (from) {
            case "sub_cate":
                url = Constant.GET_PRODUCTS_URL;
                params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                params.put(Constant.SUB_CATEGORY_ID, id);
                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                    params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                }
                isSort = true;
                break;
            case "similar":
                url = Constant.GET_PRODUCTS_URL;
                params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                params.put(Constant.PRODUCT_ID, id);
                params.put(Constant.CATEGORY_ID, requireArguments().getString("cat_id"));
                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                    params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                }
                break;
            case "section":
                url = Constant.GET_SECTION_URL;
                params.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
                params.put(Constant.SECTION_ID, id);
                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                    params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                }
                break;
            case "search":
                url = Constant.GET_PRODUCTS_URL;
                params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                    params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                }
                params.put(Constant.SEARCH, query);
                break;
        }

        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);
        if (filterIndex != -1) {
            params.put(Constant.SORT, filterBy);
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        if (offset == 0) {
                            productArrayList = new ArrayList<>();
                            tvAlert.setVisibility(View.GONE);
                        }
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                        } catch (Exception e) {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);

                        }
                        if (offset == 0) {
                            mAdapter = new XProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            mAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(mAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);

                            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                    if (productArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
                                                //bottom of list!
                                                productArrayList.add(null);
                                                mAdapter.notifyItemInserted(productArrayList.size() - 1);

                                                offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
                                                Map<String, String> params1 = new HashMap<>();
                                                switch (from) {
                                                    case "sub_cate":
                                                        params1.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                                                        params1.put(Constant.SUB_CATEGORY_ID, id);
                                                        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                            params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                        }
                                                        isSort = true;
                                                        break;
                                                    case "similar":
                                                        params1.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                                                        params1.put(Constant.PRODUCT_ID, id);
                                                        params1.put(Constant.CATEGORY_ID, requireArguments().getString("cat_id"));
                                                        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                            params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                        }
                                                        break;
                                                    case "section":
                                                        params1.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
                                                        params1.put(Constant.SECTION_ID, id);
                                                        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                            params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                        }
                                                        break;
                                                    case "search":
                                                        params1.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
                                                        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                            params1.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                        }
                                                        params1.put(Constant.SEARCH, query);
                                                        break;
                                                }

                                                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                                                    params1.put(Constant.USER_ID, session.getData(Constant.ID));
                                                }
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                if (filterIndex != -1) {
                                                    params1.put(Constant.SORT, filterBy);
                                                }

                                                ApiConfig.RequestToVolley((result1, response1) -> {

                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {

                                                                JSONObject object1 = new JSONObject(response1);
                                                                total = Integer.parseInt(object1.getString(Constant.TOTAL));
                                                                JSONArray jsonArray1 = object1.getJSONArray(Constant.DATA);
                                                                productArrayList.remove(productArrayList.size() - 1);
                                                                mAdapter.notifyItemRemoved(productArrayList.size());
                                                                try {
                                                                    productArrayList.addAll(ApiConfig.GetProductList(jsonArray1));
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                mAdapter.notifyDataSetChanged();
                                                                mAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, url, params1, false);
                                                isLoadMore = true;
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        if (offset == 0) {
                            Log.d("XPRODUCT","3");
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            tvAlert.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("XPRODUCT",String.valueOf(e));
                    mShimmerViewContainer.stopShimmer();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    tvAlert.setVisibility(View.VISIBLE);

                }
            } else {
                Log.d("XPRODUCT","1");
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                tvAlert.setVisibility(View.VISIBLE);
            }
        }, activity, url, params, false);
    }
    void GetSimilarData() {
        productArrayList = new ArrayList<>();
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
        params.put(Constant.PRODUCT_ID, id);
        params.put(Constant.CATEGORY_ID, getArguments().getString("cat_id"));
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            total = Integer.parseInt(objectbject.getString(Constant.TOTAL));
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            try {
                                try {
                                    productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                                } catch (Exception e) {
                                    mShimmerViewContainer.stopShimmer();
                                    mShimmerViewContainer.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                            if (offset == 0) {
                                mAdapter = new XProductLoadMoreAdapter(activity, productArrayList, resource, from);
                                mAdapter.setHasStableIds(true);
                                recyclerView.setAdapter(mAdapter);
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        // if (diff == 0) {
                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                            if (productArrayList.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size() - 1) {
                                                        //bottom of list!
                                                        productArrayList.add(null);
                                                        mAdapter.notifyItemInserted(productArrayList.size() - 1);

                                                        offset += Integer.parseInt("" + Constant.LOAD_ITEM_LIMIT);
                                                        Map<String, String> params = new HashMap<>();
                                                        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
                                                        params.put(Constant.PRODUCT_ID, id);
                                                        params.put(Constant.CATEGORY_ID, getArguments().getString("cat_id"));
                                                        params.put(Constant.USER_ID, session.getData(Constant.ID));
                                                        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                                            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
                                                        }
                                                        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                        params.put(Constant.OFFSET, "" + offset);

                                                        ApiConfig.RequestToVolley(new VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(boolean result, String response) {

                                                                if (result) {
                                                                    try {
                                                                        JSONObject objectbject = new JSONObject(response);
                                                                        if (!objectbject.getBoolean(Constant.ERROR)) {

                                                                            JSONObject object = new JSONObject(response);
                                                                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                                                                            productArrayList.remove(productArrayList.size() - 1);
                                                                            mAdapter.notifyItemRemoved(productArrayList.size());

                                                                            try {
                                                                                productArrayList.addAll(ApiConfig.GetProductList(jsonArray));
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }

                                                                            mAdapter.notifyDataSetChanged();
                                                                            mAdapter.setLoaded();
                                                                            isLoadMore = false;
                                                                        }
                                                                    } catch (JSONException e) {

                                                                    }
                                                                }
                                                            }
                                                        }, activity, Constant.GET_PRODUCTS_URL, params, false);
                                                        isLoadMore = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            if (offset == 0) {
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                tvAlert.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }
    private void GetSectionData() {
        recyclerView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.GET_ALL_SECTIONS, Constant.GetVal);
        params.put(Constant.SECTION_ID, id);
        Log.d("jSON",id);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);

                        if (!objectbject.getBoolean(Constant.ERROR)) {

                            JSONObject object = new JSONObject(response);
                            //Log.d("JSON",String.valueOf(object));
                            productArrayList = ApiConfig.GetProductList(object.getJSONArray(Constant.SECTIONS).getJSONObject(0).getJSONArray(Constant.PRODUCTS));
                            Log.d("PRODUCT SIZE",String.valueOf(productArrayList.size()));
                            //Toast.makeText(activity, String.valueOf(productArrayList.size()), Toast.LENGTH_SHORT).show();
                            for (int p = 0;p < productArrayList.size(); p++){
                                Log.d("JSON OFFER",String.valueOf(productArrayList.get(p).getId()));

                            }

                            mAdapter = new XProductLoadMoreAdapter(activity, productArrayList, resource, from);
                            recyclerView.setAdapter(mAdapter);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, activity, Constant.GET_SECTION_URL, params, false);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.filter_by));
            builder.setSingleChoiceItems(Constant.filterValues, filterIndex, (dialog, item1) -> {
                filterIndex = item1;
                switch (item1) {
                    case 0:
                        filterBy = Constant.NEW;
                        break;
                    case 1:
                        filterBy = Constant.OLD;
                        break;
                    case 2:
                        filterBy = Constant.HIGH;
                        break;
                    case 3:
                        filterBy = Constant.LOW;
                        break;
                }
                if (item1 != -1)
                    GetData();
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (item.getItemId() == R.id.toolbar_layout) {
            if (isGrid) {
                lytGrid.setVisibility(View.GONE);
                lytList.setVisibility(View.VISIBLE);
                isGrid = false;
                recyclerView.setAdapter(null);
                resource = R.layout.x_lyt_item_list;
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            } else {
                lytGrid.setVisibility(View.VISIBLE);
                lytList.setVisibility(View.GONE);
                isGrid = true;
                recyclerView.setAdapter(null);
                resource = R.layout.x_lyt_item_grid;
                recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
            }
            session.setBoolean("grid", isGrid);
            mAdapter = new XProductLoadMoreAdapter(activity, productArrayList, resource, from);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            activity.invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        activity.getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.toolbar_sort).setVisible(isSort);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));
        menu.findItem(R.id.toolbar_layout).setVisible(true);

        Drawable myDrawable;
        if (isGrid) {
            myDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_list_); // The ID of your drawable
        } else {
            myDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_grid_); // The ID of your drawable.
        }
        menu.findItem(R.id.toolbar_layout).setIcon(myDrawable);

        super.onPrepareOptionsMenu(menu);
    }

    public void startShimmer() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
    }

    public void stopShimmer() {
        mShimmerViewContainer.stopShimmer();
        mShimmerViewContainer.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        assert getArguments() != null;
        Constant.TOOLBAR_TITLE = getArguments().getString(Constant.NAME);
        activity.invalidateOptionsMenu();
        if (getArguments().getString(Constant.FROM).equals("search")) {

            searchView.requestFocus();
            showSoftKeyboard(searchView);
        } else {
            hideKeyboard();
        }
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }
}