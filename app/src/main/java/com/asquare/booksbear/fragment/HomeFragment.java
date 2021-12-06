package com.asquare.booksbear.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.asquare.booksbear.activity.TrialMainActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.asquare.booksbear.R;
import com.asquare.booksbear.activity.MainActivity;
import com.asquare.booksbear.adapter.CategoryAdapter;
import com.asquare.booksbear.adapter.OfferAdapter;
import com.asquare.booksbear.adapter.SectionAdapter;
import com.asquare.booksbear.adapter.SellerAdapter;
import com.asquare.booksbear.adapter.SliderAdapter;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.Session;
import com.asquare.booksbear.helper.VolleyCallback;
import com.asquare.booksbear.model.Category;
import com.asquare.booksbear.model.PinCode;
import com.asquare.booksbear.model.Seller;
import com.asquare.booksbear.model.Slider;


public class HomeFragment extends Fragment {

    public Session session;
    public static ArrayList<Category> categoryArrayList, sectionList;
    public static ArrayList<Seller> sellerArrayList;
    ArrayList<Slider> sliderArrayList;
    Activity activity;
    NestedScrollView nestedScrollView;
    SwipeRefreshLayout swipeLayout;
    View root;
    int timerDelay = 0, timerWaiting = 0;
    EditText searchview;
    RecyclerView categoryRecyclerView, sectionView, offerView, sellerRecyclerView;
    ViewPager mPager;
    LinearLayout mMarkersLayout;
    int size;
    Timer swipeTimer;
    Handler handler;
    Runnable Update;
    int currentPage = 0;
    LinearLayout lytCategory, lytSearchview, lytSeller;
    Menu menu;
    TextView tvMore, tvMoreSeller;
    boolean searchVisible = false;
    private ArrayList<String> offerList;
    private ShimmerFrameLayout mShimmerViewContainer;
    public static TextView tvLocation;
    public TextView tvTitleLocation;
    boolean dialog_visible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);
        session = new Session(getContext());
        activity = getActivity();

        timerDelay = 3000;
        timerWaiting = 3000;
        setHasOptionsMenu(true);

        swipeLayout = root.findViewById(R.id.swipeLayout);
        categoryRecyclerView = root.findViewById(R.id.categoryrecycleview);

        sectionView = root.findViewById(R.id.sectionView);
        sectionView.setLayoutManager(new LinearLayoutManager(getContext()));
        sectionView.setNestedScrollingEnabled(false);

        offerView = root.findViewById(R.id.offerView);
        offerView.setLayoutManager(new LinearLayoutManager(getContext()));
        offerView.setNestedScrollingEnabled(false);

        nestedScrollView = root.findViewById(R.id.nestedScrollView);
        mMarkersLayout = root.findViewById(R.id.layout_markers);
        lytCategory = root.findViewById(R.id.lytCategory);
        lytSeller = root.findViewById(R.id.lytSeller);
        lytSearchview = root.findViewById(R.id.lytSearchview);
        sellerRecyclerView = root.findViewById(R.id.sellerRecyclerView);
        tvMore = root.findViewById(R.id.tvMore);
        tvMoreSeller = root.findViewById(R.id.tvMoreSeller);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        tvTitleLocation = root.findViewById(R.id.tvTitleLocation);
        tvLocation = root.findViewById(R.id.tvLocation);

        searchview = root.findViewById(R.id.searchview);
        ApiConfig.GetPinCode(activity);

        if (!session.getData(Constant.GET_SELECTED_PINCODE_NAME).equals("")) {
            tvLocation.setText(session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }

        tvTitleLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPinCodeDialog(activity, session);
            }
        });

        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPinCodeDialog(activity, session);
            }
        });

        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    Rect scrollBounds = new Rect();
                    nestedScrollView.getHitRect(scrollBounds);
                    if (!lytSearchview.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < lytSearchview.getHeight()) {
                        searchVisible = true;
                        menu.findItem(R.id.toolbar_search).setVisible(true);
                    } else {
                        searchVisible = false;
                        menu.findItem(R.id.toolbar_search).setVisible(false);
                    }
                    activity.invalidateOptionsMenu();
                }
            });
        }

        tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.categoryClicked) {
                    MainActivity.fm.beginTransaction().add(R.id.container, MainActivity.categoryFragment).show(MainActivity.categoryFragment).hide(MainActivity.active).commit();
                    MainActivity.categoryClicked = true;
                } else {
                    MainActivity.fm.beginTransaction().show(MainActivity.categoryFragment).hide(MainActivity.active).commit();
                }
                //MainActivity.bottomNavigationView.setItemActiveIndex(1);
                MainActivity.active = MainActivity.categoryFragment;
            }
        });

        tvMoreSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.fm.beginTransaction().add(R.id.container, new SellerListFragment()).addToBackStack(null).commit();
            }
        });

        searchview.setOnTouchListener((View v, MotionEvent event) -> {
            MainActivity.fm.beginTransaction().add(R.id.container, new SearchFragment()).addToBackStack(null).commit();
            return false;
        });

        lytSearchview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.fm.beginTransaction().add(R.id.container, new SearchFragment()).addToBackStack(null).commit();
            }
        });

        mPager = root.findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                ApiConfig.addMarkers(position, sliderArrayList, mMarkersLayout, getContext());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        categoryArrayList = new ArrayList<>();

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (swipeTimer != null) {
                    swipeTimer.cancel();
                }
                timerDelay = 3000;
                timerWaiting = 3000;
                if (ApiConfig.isConnected(getActivity())) {
                    ApiConfig.getWalletBalance(activity, session);
                    if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                        ApiConfig.getWalletBalance(activity, new Session(activity));
                    }
                    GetHomeData();
                }
                swipeLayout.setRefreshing(false);
            }
        });

        if (ApiConfig.isConnected(getActivity())) {
            GetHomeData();
            if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
            }
        } else {
            nestedScrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();
        }

        return root;
    }

    public void GetHomeData() {
        if (swipeTimer != null) {
            swipeTimer.cancel();
        }
        timerDelay = 3000;
        timerWaiting = 3000;
        nestedScrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            offerList = new ArrayList<>();
                            sliderArrayList = new ArrayList<>();
                            categoryArrayList = new ArrayList<>();
                            sectionList = new ArrayList<>();

                            offerList.clear();
                            sliderArrayList.clear();
                            categoryArrayList.clear();
                            sectionList.clear();

                            GetOfferImage(jsonObject.getJSONArray(Constant.OFFER_IMAGES));
                            GetCategory(jsonObject);
                            SectionProductRequest(jsonObject.getJSONArray(Constant.SECTIONS));
                            GetSlider(jsonObject.getJSONArray(Constant.SLIDER_IMAGES));
                            GetSeller(jsonObject.getJSONArray(Constant.SELLER));
                            Log.d("SECTION",String.valueOf(jsonObject.getJSONArray(Constant.SECTIONS)));
                        } else {
                            nestedScrollView.setVisibility(View.VISIBLE);
                            mShimmerViewContainer.setVisibility(View.GONE);
                            mShimmerViewContainer.stopShimmer();
                        }
                    } catch (JSONException e) {
                        nestedScrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();

                    }
                }
            }
        }, getActivity(), Constant.GET_ALL_DATA_URL, params, false);
    }

    public void GetOfferImage(JSONArray jsonArray) {
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    offerList.add(object.getString(Constant.IMAGE));
                }
                offerView.setAdapter(new OfferAdapter(offerList, R.layout.offer_lyt));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void GetCategory(JSONObject object) {
        try {
            int visible_count;
            int column_count;

            JSONArray jsonArray = object.getJSONArray(Constant.CATEGORIES);

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Category category = new Gson().fromJson(jsonObject.toString(), Category.class);
                    categoryArrayList.add(category);
                }

                if (!object.getString("style").equals("")) {
                    if (object.getString("style").equals("style_1")) {
                        visible_count = Integer.parseInt(object.getString("visible_count"));
                        column_count = Integer.parseInt(object.getString("column_count"));
                        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), column_count));
                        categoryRecyclerView.setAdapter(new CategoryAdapter(getContext(), getActivity(), categoryArrayList, R.layout.lyt_category_grid, "home", visible_count));
                    } else if (object.getString("style").equals("style_2")) {
                        visible_count = Integer.parseInt(object.getString("visible_count"));
                        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        categoryRecyclerView.setAdapter(new CategoryAdapter(getContext(), getActivity(), categoryArrayList, R.layout.lyt_category_list, "home", visible_count));
                    }
                } else {
                    categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    categoryRecyclerView.setAdapter(new CategoryAdapter(getContext(), getActivity(), categoryArrayList, R.layout.lyt_category_list, "home", 6));
                }
            } else {
                lytCategory.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void SectionProductRequest(JSONArray jsonArray) {  //json request for product search
        try {
            for (int j = 0; j < jsonArray.length(); j++) {
                Category section = new Category();
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                section.setName(jsonObject.getString(Constant.TITLE));
                section.setId(jsonObject.getString(Constant.ID));
                section.setStyle(jsonObject.getString(Constant.SECTION_STYLE));
                section.setSubtitle(jsonObject.getString(Constant.SHORT_DESC));
                JSONArray productArray = jsonObject.getJSONArray(Constant.PRODUCTS);
                Log.d("SECTIONHOME",jsonObject.getString(Constant.TITLE) + jsonObject.getString(Constant.ID) + productArray.length());

                section.setProductList(ApiConfig.GetProductList(productArray));
                sectionList.add(section);
            }
            sectionView.setVisibility(View.VISIBLE);
            SectionAdapter sectionAdapter = new SectionAdapter(getContext(), getActivity(), sectionList);
            sectionView.setAdapter(sectionAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void GetSlider(JSONArray jsonArray) {
        try {
            size = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sliderArrayList.add(new Slider(jsonObject.getString(Constant.TYPE), jsonObject.getString(Constant.TYPE_ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.IMAGE)));
            }
            mPager.setAdapter(new SliderAdapter(sliderArrayList, getActivity(), R.layout.lyt_slider, "home"));
            ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, getContext());
            handler = new Handler();
            Update = () -> {
                if (currentPage == size) {
                    currentPage = 0;
                }
                try {
                    mPager.setCurrentItem(currentPage++, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            swipeTimer = new Timer();
            swipeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(Update);
                }
            }, timerDelay, timerWaiting);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        nestedScrollView.setVisibility(View.VISIBLE);
        mShimmerViewContainer.setVisibility(View.GONE);
        mShimmerViewContainer.stopShimmer();
    }

    void GetSeller(JSONArray jsonArray) {
        try {
            sellerArrayList = new ArrayList<>();
            if (jsonArray.length() > 0) {
                lytSeller.setVisibility(View.VISIBLE);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Seller seller = new Gson().fromJson(jsonObject.toString(), Seller.class);
                    sellerArrayList.add(seller);
                }

                sellerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), Constant.GRIDCOLUMN));
                sellerRecyclerView.setAdapter(new SellerAdapter(getContext(), getActivity(), sellerArrayList, R.layout.lyt_seller, "home", 3));
            } else {
                lytSeller.setVisibility(View.GONE);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    public void OpenPinCodeDialog(final Activity activity, Session session) {
        if (!dialog_visible) {

            ArrayList<PinCode> pinCodes = new ArrayList<>();
            ArrayList<String> pinCodeNames = new ArrayList<>();

            try {
                PinCode pinCode = new PinCode("0", activity.getString(R.string.select_all));
                pinCodes.add(pinCode);
                pinCodeNames.add(activity.getString(R.string.select_all));
                JSONArray jsonArray = new JSONArray(session.getData(Constant.GET_PINCODES_RESPONSE));
                for (int i = 0; i < jsonArray.length(); i++) {
                    pinCodes.add(new PinCode(jsonArray.getJSONObject(i).getString(Constant.ID), ""));
                    pinCodeNames.add(jsonArray.getJSONObject(i).getString(Constant.PINCODE));
                }
                if (pinCodeNames.size() > 0) {
                    View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_searchable_dropdown, null);
                    ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
                    if (parentViewGroup != null) {
                        parentViewGroup.removeAllViews();
                    }

                    final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetTheme);
                    mBottomSheetDialog.setContentView(sheetView);
                    mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    ImageView imgclose = sheetView.findViewById(R.id.imgClose);
                    EditText edtTextView = sheetView.findViewById(R.id.edtTextView);
                    ListView listView = sheetView.findViewById(R.id.listView);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, pinCodeNames);
                    listView.setAdapter(arrayAdapter);

                    imgclose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBottomSheetDialog.dismiss();
                        }
                    });

                    edtTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            arrayAdapter.getFilter().filter(charSequence);
                        }

                        @Override
                        public void afterTextChanged(Editable charSequence) {

                        }
                    });

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        session.setBoolean(Constant.GET_SELECTED_PINCODE, true);
                        session.setData(Constant.GET_SELECTED_PINCODE_ID, pinCodes.get(position).getId());
                        if (position == 0) {
                            session.setData(Constant.GET_SELECTED_PINCODE_NAME, activity.getString(R.string.all));
                            HomeFragment.tvLocation.setText(activity.getString(R.string.all));
                        } else {
                            session.setData(Constant.GET_SELECTED_PINCODE_NAME, pinCodeNames.get(position));
                            HomeFragment.tvLocation.setText(pinCodeNames.get(position));
                        }
                        GetHomeData();
                        mBottomSheetDialog.cancel();
                    });

                    mBottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            dialog_visible = true;
                        }
                    });

                    mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog_visible = false;
                        }
                    });

                    mBottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog_visible = false;
                        }
                    });

                    mBottomSheetDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ApiConfig.GetPinCode(activity);
        activity.invalidateOptionsMenu();
        ApiConfig.GetSettings(activity);
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        this.menu = menu;
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(searchVisible);
    }

}