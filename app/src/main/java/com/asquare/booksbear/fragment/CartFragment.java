package com.asquare.booksbear.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.asquare.booksbear.R;
import com.asquare.booksbear.activity.LoginActivity;
import com.asquare.booksbear.activity.MainActivity;
import com.asquare.booksbear.adapter.CartAdapter;
import com.asquare.booksbear.adapter.OfflineCartAdapter;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.DatabaseHelper;
import com.asquare.booksbear.helper.Session;
import com.asquare.booksbear.helper.VolleyCallback;
import com.asquare.booksbear.model.Cart;
import com.asquare.booksbear.model.OfflineCart;
import com.asquare.booksbear.model.PinCode;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CartFragment extends Fragment {
    public static LinearLayout lytempty;
    public static RelativeLayout lytTotal;
    public static ArrayList<Cart> carts;
    public static ArrayList<OfflineCart> offlineCarts;
    public static HashMap<String, String> values;
    public static boolean isSoldOut = false;
    public static boolean isDeliverable = false;
    static TextView txttotalamount, txttotalitems, tvConfirmOrder;
    static CartAdapter cartAdapter;
    static OfflineCartAdapter offlineCartAdapter;
    static Activity activity;
    static Session session;
    static JSONObject objectbject;
    View root;
    RecyclerView cartrecycleview;
    NestedScrollView scrollView;
    double total;
    Button btnShowNow;
    DatabaseHelper databaseHelper;
    private ShimmerFrameLayout mShimmerViewContainer;
    public static TextView tvLocation, tvTitleLocation;
    boolean dialog_visible = false;

    @SuppressLint("SetTextI18n")
    public static void SetData() {
        txttotalamount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(String.valueOf(Constant.FLOAT_TOTAL_AMOUNT)));
        txttotalitems.setText(Constant.TOTAL_CART_ITEM + " Items");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cart, container, false);

        values = new HashMap<>();
        activity = getActivity();
        session = new Session(getActivity());
        lytTotal = root.findViewById(R.id.lytTotal);
        lytempty = root.findViewById(R.id.lytempty);
        btnShowNow = root.findViewById(R.id.btnShowNow);
        txttotalamount = root.findViewById(R.id.txttotalamount);
        txttotalitems = root.findViewById(R.id.txttotalitems);
        scrollView = root.findViewById(R.id.scrollView);
        cartrecycleview = root.findViewById(R.id.cartrecycleview);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        tvLocation = root.findViewById(R.id.tvLocation);
        tvTitleLocation = root.findViewById(R.id.tvTitleLocation);

        databaseHelper = new DatabaseHelper(activity);


        setHasOptionsMenu(true);

        Constant.FLOAT_TOTAL_AMOUNT = 0.00;

        tvLocation.setText(session.getData(Constant.GET_SELECTED_PINCODE_NAME));

        carts = new ArrayList<>();
        cartrecycleview.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (ApiConfig.isConnected(getActivity())) {
            GetSettings(activity);
        }

        if (session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0") || session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("")) {
            OpenPinCodeDialog(activity, session);
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

        tvConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApiConfig.isConnected(requireActivity())) {
                    if (!isSoldOut && !isDeliverable) {
                        if (Float.parseFloat(session.getData(Constant.min_order_amount)) <= Constant.FLOAT_TOTAL_AMOUNT) {
                            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                                if (values.size() > 0) {
                                    ApiConfig.AddMultipleProductInCart(session, getActivity(), values);
                                }
                                Constant.selectedAddressId = "";
                                Fragment fragment = new AddressListFragment();
                                final Bundle bundle = new Bundle();
                                bundle.putString(Constant.FROM, "process");
                                bundle.putDouble("total", Constant.FLOAT_TOTAL_AMOUNT);
                                fragment.setArguments(bundle);
                                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                            } else {
                                startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("fromto", "checkout").putExtra("total", Constant.FLOAT_TOTAL_AMOUNT).putExtra(Constant.FROM, "checkout"));
                            }
                        } else {
                            Toast.makeText(activity, getString(R.string.msg_minimum_order_amount) + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(session.getData(Constant.min_order_amount)), Toast.LENGTH_SHORT).show();
                        }
                    } else if (isDeliverable) {
                        Toast.makeText(activity, getString(R.string.msg_non_deliverable), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, getString(R.string.msg_sold_out), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnShowNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.fm.popBackStack();
            }
        });

        return root;
    }

    private void GetOfflineCart() {
        CartFragment.isDeliverable = false;
        offlineCarts = new ArrayList<>();
        cartrecycleview.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        if (databaseHelper.getTotalItemOfCart(activity) >= 1) {
            offlineCarts = new ArrayList<>();
            offlineCartAdapter = null;
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_VARIENTS_OFFLINE, Constant.GetVal);
            params.put(Constant.VARIANT_IDs, databaseHelper.getCartList().toString().replace("[", "").replace("]", "").replace("\"", ""));
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
                                session.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL));

                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                                Gson g = new Gson();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    OfflineCart cart = g.fromJson(jsonObject1.toString(), OfflineCart.class);
                                    offlineCarts.add(cart);
                                }
                                offlineCartAdapter = new OfflineCartAdapter(getContext(), getActivity(), offlineCarts);
                                offlineCartAdapter.setHasStableIds(true);
                                cartrecycleview.setAdapter(offlineCartAdapter);
                                lytTotal.setVisibility(View.VISIBLE);
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                cartrecycleview.setVisibility(View.VISIBLE);
                            } else {
                                cartrecycleview.setVisibility(View.GONE);
                                lytempty.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            cartrecycleview.setVisibility(View.VISIBLE);

                        }
                    }
                }
            }, getActivity(), Constant.GET_PRODUCTS_URL, params, false);
        } else {
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            cartrecycleview.setVisibility(View.VISIBLE);
            cartrecycleview.setVisibility(View.GONE);
            lytempty.setVisibility(View.VISIBLE);
        }
    }

    public void GetSettings(final Activity activity) {
        Session session = new Session(activity);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_TIMEZONE, Constant.GetVal);
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            JSONObject object = objectbject.getJSONObject(Constant.SETTINGS);

                            session.setData(Constant.minimum_version_required, object.getString(Constant.minimum_version_required));
                            session.setData(Constant.is_version_system_on, object.getString(Constant.is_version_system_on));

                            session.setData(Constant.CURRENCY, object.getString(Constant.CURRENCY));

                            session.setData(Constant.min_order_amount, object.getString(Constant.min_order_amount));
                            session.setData(Constant.max_cart_items_count, object.getString(Constant.max_cart_items_count));
                            session.setData(Constant.area_wise_delivery_charge, object.getString(Constant.area_wise_delivery_charge));


                            if (session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
                                OpenPinCodeDialog(activity, session);
                            } else {
                                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                                    getCartData();
                                } else {
                                    GetOfflineCart();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }


    public void OpenPinCodeDialog(final Activity activity, Session session) {
        if (!dialog_visible) {

            ArrayList<PinCode> pinCodes = new ArrayList<>();
            ArrayList<String> pinCodeNames = new ArrayList<>();

            try {
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
                        Constant.FLOAT_TOTAL_AMOUNT = 00.00;
                        session.setBoolean(Constant.GET_SELECTED_PINCODE, true);
                        session.setData(Constant.GET_SELECTED_PINCODE_ID, pinCodes.get(position).getId());
                        session.setData(Constant.GET_SELECTED_PINCODE_NAME, pinCodeNames.get(position));
                        HomeFragment.tvLocation.setText(pinCodeNames.get(position));
                        tvLocation.setText(pinCodeNames.get(position));

                        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                            getCartData();
                        } else {
                            GetOfflineCart();
                        }
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

    private void getCartData() {
        CartFragment.isDeliverable = false;
        carts = new ArrayList<>();
        cartrecycleview.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE_ID, session.getData(Constant.GET_SELECTED_PINCODE_ID));
        }

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            Gson g = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                if (jsonObject1 != null) {
                                    Cart cart = g.fromJson(jsonObject1.toString(), Cart.class);
                                    carts.add(cart);
                                } else {
                                    break;
                                }
                            }
                            cartAdapter = new CartAdapter(getContext(), getActivity(), carts);
                            cartAdapter.setHasStableIds(true);
                            cartrecycleview.setAdapter(cartAdapter);

                            lytTotal.setVisibility(View.VISIBLE);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            cartrecycleview.setVisibility(View.VISIBLE);
                            total = Double.parseDouble(objectbject.getString(Constant.TOTAL));
                            session.setData(Constant.TOTAL, String.valueOf(total));
                            Constant.TOTAL_CART_ITEM = Integer.parseInt(objectbject.getString(Constant.TOTAL));
                            SetData();
                        } else {
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            cartrecycleview.setVisibility(View.VISIBLE);
                            lytempty.setVisibility(View.VISIBLE);
                            lytTotal.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        cartrecycleview.setVisibility(View.VISIBLE);

                    }
                }
            }
        }, getActivity(), Constant.CART_URL, params, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (values.size() > 0) {
                ApiConfig.AddMultipleProductInCart(session, getActivity(), values);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.cart);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}
