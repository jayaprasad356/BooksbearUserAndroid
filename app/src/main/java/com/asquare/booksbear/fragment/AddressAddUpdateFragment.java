package com.asquare.booksbear.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.asquare.booksbear.R;
import com.asquare.booksbear.activity.MainActivity;
import com.asquare.booksbear.adapter.AddressAdapter;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.Session;
import com.asquare.booksbear.helper.VolleyCallback;
import com.asquare.booksbear.model.Address;
import com.asquare.booksbear.model.Area;
import com.asquare.booksbear.model.City;
import com.asquare.booksbear.pincode.PostOffice;
import com.asquare.booksbear.pincode.Register;
import com.asquare.booksbear.pincode.ResponseData;

import static com.asquare.booksbear.fragment.AddressListFragment.addressAdapter;
import static com.asquare.booksbear.fragment.AddressListFragment.addresses;
import static com.asquare.booksbear.fragment.AddressListFragment.recyclerView;

public class AddressAddUpdateFragment extends Fragment implements OnMapReadyCallback {
    public static TextView tvCurrent;
    public static double latitude = 0.00, longitude = 0.00;
    public static Address address1;
    public static SupportMapFragment mapFragment;
    public static OnMapReadyCallback mapReadyCallback;
    View root;
    public static String pincodeId = "0", areaId = "0", cityId = "0";
    ArrayList<City> cityArrayList;
    ArrayList<Area> areaList;
    private ArrayList<String> newcityaaray = new ArrayList<String>();
    private ArrayList<String> newareaaaray = new ArrayList<String>();
    Spinner areaSpinner, citySpinner;
    Button btnsubmit;
    ProgressBar progressBar;
    CheckBox chIsDefault;
    RadioButton rdHome, rdOffice, rdOther;
    Session session;
    String isDefault = "0";
    TextView tvUpdate, edtName, edtMobile, edtAlternateMobile, edtAddress, edtLanmark, edtState, edtCounty;
    public static TextView edtPinCode;
    ScrollView scrollView;
    String name, mobile, alternateMobile, address2, landmark, pincode, state, country, isdefault, addressType,city,area;
    int position;
    Activity activity;
    int offset = 0;
    String For;
    Button checkbtn;
    boolean PincodeCheck = false;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_address_add_update, container, false);
        activity = getActivity();
        setHasOptionsMenu(true);

        checkbtn = root.findViewById(R.id.checkbtn);
        edtPinCode = root.findViewById(R.id.edtPinCode);
        areaSpinner = root.findViewById(R.id.areaSpinner);
        citySpinner = root.findViewById(R.id.citySpinner);
        edtName = root.findViewById(R.id.edtName);
        edtMobile = root.findViewById(R.id.edtMobile);
        edtAlternateMobile = root.findViewById(R.id.edtAlternateMobile);
        edtLanmark = root.findViewById(R.id.edtLanmark);
        edtAddress = root.findViewById(R.id.edtAddress);
        edtState = root.findViewById(R.id.edtState);
        edtCounty = root.findViewById(R.id.edtCountry);
        btnsubmit = root.findViewById(R.id.btnsubmit);
        scrollView = root.findViewById(R.id.scrollView);
        progressBar = root.findViewById(R.id.progressBar);
        chIsDefault = root.findViewById(R.id.chIsDefault);
        rdHome = root.findViewById(R.id.rdHome);
        rdOffice = root.findViewById(R.id.rdOffice);
        rdOther = root.findViewById(R.id.rdOther);
        tvCurrent = root.findViewById(R.id.tvCurrent);
        tvUpdate = root.findViewById(R.id.tvUpdate);

        session = new Session(activity);

        edtName.setText(session.getData(Constant.NAME));
        edtAddress.setText(session.getData(Constant.ADDRESS));
        edtMobile.setText(session.getData(Constant.MOBILE));
        pincodeId = session.getData(Constant.CITY_ID);
        areaId = session.getData(Constant.AREA_ID);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Bundle bundle = getArguments();
        assert bundle != null;
        For = bundle.getString("for");
        position = bundle.getInt("position");

        //SetCitySpinnerData();

        checkbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edtPinCode.getText().toString().trim().equals("") && edtPinCode.getText().length() == 6){

                    Updatepostaldata();
                }

            }
        });


        address1 = new Address();
        if (For.equals("update")) {
            btnsubmit.setText(getString(R.string.update));
            address1 = (Address) bundle.getSerializable("model");
            pincodeId = address1.getPincode_id();
            areaId = address1.getArea_id();
            cityId = address1.getCity_id();
            latitude = Double.parseDouble(address1.getLatitude());
            longitude = Double.parseDouble(address1.getLongitude());
            tvCurrent.setText(getString(R.string.location_1) + ApiConfig.getAddress(latitude, longitude, getActivity()));
            mapFragment.getMapAsync(this);
            SetData();
            Updatepostaldata();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.VISIBLE);
            btnsubmit.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            showKeyboard();
            edtAlternateMobile.requestFocus();
        }

        mapReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NotNull GoogleMap googleMap) {
                googleMap.clear();
                LatLng latLng = new LatLng(latitude, longitude);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .title(getString(R.string.current_location)));

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            }
        };

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PincodeCheck){
                    AddUpdateAddress();

                }else {
                    Toast.makeText(activity, "Provide Area,City,State", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 110);
                } else {
                    Fragment fragment = new MapFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.FROM, "address");
                    bundle.putDouble("latitude", latitude);
                    bundle.putDouble("longitude", longitude);
                    fragment.setArguments(bundle);
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                }

            }
        });

        chIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDefault.equalsIgnoreCase("0")) {
                    isDefault = "1";
                } else {
                    isDefault = "0";
                }
            }
        });

        return root;
    }

    private void Updatepostaldata() {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://postalpincode.in/api/pincode/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Register register=retrofit.create(Register.class);

        Call<ResponseData> responseDataCall=register.CreateUser(edtPinCode.getText().toString().trim());

        responseDataCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && !response.body().getStatus().equals("Error")){
                    PincodeCheck = true;
                    progressDialog.dismiss();
                    newareaaaray.clear();
                    newcityaaray.clear();
                    for (int i = 0; i < response.body().getPostOffice().size(); i++){
                        PostOffice jsonObject = response.body().getPostOffice().get(i);
                        String AreaName = jsonObject.getName();
                        String CityName = jsonObject.getDivision();
                        String newValue;
                        if (!newareaaaray.contains(AreaName)) {
                            newareaaaray.add(AreaName);
                        }
                        if (!newcityaaray.contains(CityName)) {
                            newcityaaray.add(CityName);
                        }


                    }
                    ArrayAdapter<String> areaArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newareaaaray);
                    areaArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    areaSpinner.setAdapter(areaArrayAdapter);
                    ArrayAdapter<String> cityArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newcityaaray);
                    cityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    citySpinner.setAdapter(cityArrayAdapter);
                    int cityPosition = cityArrayAdapter.getPosition(city);
                    citySpinner.setSelection(cityPosition);
                    int areaPosition = areaArrayAdapter.getPosition(area);
                    areaSpinner.setSelection(areaPosition);
                    edtState.setText(response.body().getPostOffice().get(0).getState());
                    edtCounty.setText(response.body().getPostOffice().get(0).getCountry());



                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "invalid pincode", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.i("ggg",t.toString());

            }
        });
    }

    void SetData() {

        name = address1.getName();
        mobile = address1.getMobile();
        address2 = address1.getAddress();
        alternateMobile = address1.getAlternate_mobile();
        landmark = address1.getLandmark();
        pincode = address1.getPincode();
        state = address1.getState();
        country = address1.getCountry();
        isdefault = address1.getIs_default();
        addressType = address1.getType();
        city = address1.getCity();
        area = address1.getArea();

        progressBar.setVisibility(View.VISIBLE);
        edtName.setText(name);
        edtMobile.setText(mobile);
        edtAlternateMobile.setText(alternateMobile);
        edtAddress.setText(address2);
        edtLanmark.setText(landmark);
        edtState.setText(state);
        edtCounty.setText(country);
        edtPinCode.setText(address1.getPincode());
        chIsDefault.setChecked(isdefault.equalsIgnoreCase("1"));

        if (addressType.equalsIgnoreCase("home")) {
            rdHome.setChecked(true);
        } else if (addressType.equalsIgnoreCase("office")) {
            rdOffice.setChecked(true);
        } else {
            rdOther.setChecked(true);
        }

        progressBar.setVisibility(View.GONE);

        btnsubmit.setVisibility(View.VISIBLE);
        btnsubmit.setVisibility(View.VISIBLE);

        showKeyboard();
        edtName.requestFocus();
    }

    void AddUpdateAddress() {


        String isDefault = chIsDefault.isChecked() ? "1" : "0";
        String type = rdHome.isChecked() ? "Home" : rdOffice.isChecked() ? "Office" : "Other";
        if (edtName.getText().toString().trim().isEmpty()) {
            edtName.requestFocus();
            edtName.setError("Please enter name!");
        } else if (edtMobile.getText().toString().trim().isEmpty()) {
            edtMobile.requestFocus();
            edtMobile.setError("Please enter mobile!");

        } else if (edtAddress.getText().toString().trim().isEmpty()) {
            edtAddress.requestFocus();
            edtAddress.setError("Please enter address!");
        } else if (edtLanmark.getText().toString().trim().isEmpty()) {
            edtLanmark.requestFocus();
            edtLanmark.setError("Please enter landmark!");
        } else if (edtState.getText().toString().trim().isEmpty()) {
            edtState.requestFocus();
            edtState.setError("Please enter state!");

        } else if (edtCounty.getText().toString().trim().isEmpty()) {
            edtCounty.requestFocus();
            edtCounty.setError("Please enter country");
        } else {
            Map<String, String> params = new HashMap<>();
            if (For.equalsIgnoreCase("add")) {
                params.put(Constant.ADD_ADDRESS, Constant.GetVal);
            } else if (For.equalsIgnoreCase("update")) {
                params.put(Constant.UPDATE_ADDRESS, Constant.GetVal);
                params.put(Constant.ID, address1.getId());
            }

            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.TYPE, type);
            params.put(Constant.NAME, edtName.getText().toString().trim());
            params.put(Constant.MOBILE, edtMobile.getText().toString().trim());
            params.put(Constant.ADDRESS, edtAddress.getText().toString().trim());
            params.put(Constant.LANDMARK, edtLanmark.getText().toString().trim());
            params.put(Constant.AREA_ID, "1");
            params.put(Constant.CITY_ID, "1");
            params.put(Constant.PINCODE_ID, "1");
            params.put(Constant.PINCODE, edtPinCode.getText().toString().trim());
            params.put(Constant.AREA, areaSpinner.getSelectedItem().toString().trim());
            params.put(Constant.CITY, citySpinner.getSelectedItem().toString().trim());
            params.put(Constant.STATE, edtState.getText().toString().trim());
            params.put(Constant.COUNTRY, edtCounty.getText().toString().trim());
            params.put(Constant.ALTERNATE_MOBILE, edtAlternateMobile.getText().toString().trim());
            params.put(Constant.COUNTRY_CODE, session.getData(Constant.COUNTRY_CODE));
            if (address1 != null && (address1.getLongitude() != null && address1.getLatitude() != null)) {
                params.put(Constant.LONGITUDE, address1.getLongitude());
                params.put(Constant.LATITUDE, address1.getLatitude());
            }
            params.put(Constant.IS_DEFAULT, isDefault);

            ApiConfig.RequestToVolley(new VolleyCallback() {
                @Override
                public void onSuccess(boolean result, String response) {
                    Log.d("ADDRES",response);
                    if (result) {

                        try {

                            String msg;
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean(Constant.ERROR)) {

                                offset = 0;
                                Gson g = new Gson();
                                Address address = g.fromJson(jsonObject.toString(), Address.class);

                                if (address.getIs_default().equals("1")) {
                                    for (int i = 0; i < addresses.size(); i++) {
                                        addresses.get(i).setIs_default("0");
                                    }
                                }

                                if (For.equalsIgnoreCase("add")) {
                                    msg = "Address added.";
                                    if (addressAdapter != null) {
                                        addresses.add(address);
                                    } else {
                                        addresses = new ArrayList<>();
                                        addresses.add(address);
                                        addressAdapter = new AddressAdapter(getContext(), getActivity(), addresses);
                                        recyclerView.setAdapter(addressAdapter);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    addresses.set(position, address);
                                    msg = "Address updated.";
                                }

                                AddressListFragment.tvAlert.setVisibility(View.GONE);

                                if (addressAdapter != null) {
                                    addressAdapter.notifyDataSetChanged();
                                }

                                if (address.getIs_default().equals("1")) {
                                    Constant.selectedAddressId = address.getId();
                                } else {
                                    if (Constant.selectedAddressId.equals(address.getId()))
                                        Constant.selectedAddressId = "";
                                }
                                MainActivity.fm.popBackStack();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }, getActivity(), Constant.GET_ADDRESS_URL, params, true);
        }
    }

    void SetCitySpinnerData() {
        cityArrayList = new ArrayList<>();
        try {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_CITIES, Constant.GetVal);
            Activity activity = getActivity();
            if (activity != null) {
                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
                            JSONObject objectbject = new JSONObject(response);
                            if (!objectbject.getBoolean(Constant.ERROR)) {

                                if (getContext() != null) {
                                    JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        City pinCode = new Gson().fromJson(jsonObject.toString(), City.class);
                                        cityArrayList.add(pinCode);
                                    }
                                    AddressAddUpdateFragment.CityAdapter cityAdapter = new AddressAddUpdateFragment.CityAdapter(activity, cityArrayList, citySpinner);
                                    citySpinner.setAdapter(cityAdapter);
                                    cityAdapter.setItem(cityId, areaId);
                                }
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.blank_city_message), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, activity, Constant.GET_LOCATIONS_URL, params, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getActivity().getString(R.string.address);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        double saveLatitude, saveLongitude;
        if (For.equals("update")) {
            btnsubmit.setText(getString(R.string.update));
            assert getArguments() != null;
            address1 = (Address) getArguments().getSerializable("model");
            pincodeId = address1.getPincode_id();
            areaId = address1.getArea_id();
            latitude = Double.parseDouble(address1.getLatitude());
            longitude = Double.parseDouble(address1.getLongitude());
        }
        if (latitude <= 0 || longitude <= 0) {
            saveLatitude = Double.parseDouble(session.getCoordinates(Constant.LATITUDE));
            saveLongitude = Double.parseDouble(session.getCoordinates(Constant.LONGITUDE));
        } else {
            saveLatitude = latitude;
            saveLongitude = longitude;
        }
        googleMap.clear();

        LatLng latLng = new LatLng(saveLatitude, saveLongitude);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.current_location)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }


    public class CityAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<City> cities;
        final LayoutInflater inflter;
        Spinner pinCodeSpinner;


        public CityAdapter(Context applicationContext, ArrayList<City> cities, Spinner pinCodeSpinner) {
            this.context = applicationContext;
            this.cities = cities;
            this.pinCodeSpinner = pinCodeSpinner;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return cities.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String pinCodeId, String areaId) {
            for (int i = 0; i < cities.size(); i++) {
                if (cities.get(i).getId().equals(pinCodeId)) {
                    citySpinner.setSelection(i);
                    areaList = cities.get(i).getAreas();
                    AddressAddUpdateFragment.AreaAdapter areaAdapter;
                    try {
                        /*areaAdapter = new AddressAddUpdateFragment.AreaAdapter(activity, areaList, areaSpinner);
                        areaSpinner.setAdapter(areaAdapter);
                        areaAdapter.setItem(areaId);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, activity.getString(R.string.blank_area_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.txtmeasurement);

            City city = cities.get(position);
            measurement.setText(city.getCity_name());

            pinCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    cityId = city.getId();
                    AddressAddUpdateFragment.AreaAdapter areaAdapter;
                    try {
                        areaAdapter = new AddressAddUpdateFragment.AreaAdapter(activity, city.getAreas(), areaSpinner);
                        areaSpinner.setAdapter(areaAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, activity.getString(R.string.blank_area_message), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

    public static class AreaAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Area> areas;
        final LayoutInflater inflter;
        Spinner areaSpinner;

        public AreaAdapter(Context applicationContext, ArrayList<Area> areas, Spinner areaSpinner) {
            this.context = applicationContext;
            this.areas = areas;
            this.areaSpinner = areaSpinner;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return areas.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String areaId) {
            try {
                Thread.sleep(1000);
                if (areaId.equals("0")) {
                    areaSpinner.setSelection(0);
                } else {
                    for (int i = 0; i < areas.size(); i++) {
                        if (areas.get(i).getId().equals(areaId)) {
                            areaSpinner.setSelection(i);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.txtmeasurement);

            Area area = areas.get(position);
            measurement.setText(area.getArea_name());

            areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    areaId = area.getId();
                    pincodeId = area.getPincode_id();
                    //edtPinCode.setText(area.getPincode());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }
}