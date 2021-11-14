package com.asquare.booksbear.pincode;


import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.asquare.booksbear.R;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PincodeActivity extends AppCompatActivity {
    Button button;
    EditText editText;
    ListView listView;
    private ArrayList<PostOffice> goodModelArrayList;
    private List<PostOffice> heroList;
    private ArrayList<String> playerNames = new ArrayList<String>();
    private ArrayList<String> cat = new ArrayList<String>();
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pincode);
        button=findViewById(R.id.bt);
        editText=findViewById(R.id.pin);
        listView=findViewById(R.id.list);
        spinner = findViewById(R.id.areaSpinner);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retrofit retrofit=new Retrofit.Builder()
                        .baseUrl("http://postalpincode.in/api/pincode/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Register register=retrofit.create(Register.class);

                Call<ResponseData> responseDataCall=register.CreateUser(editText.getText().toString());

                responseDataCall.enqueue(new Callback<ResponseData>() {
                    @Override
                    public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                        if (response.isSuccessful()){
                            cat.clear();



                            for (int i = 0; i < response.body().getPostOffice().size(); i++){
                                PostOffice jsonObject = response.body().getPostOffice().get(i);
                                String dropDownDisplayCategoryName = jsonObject.getName();
                                cat.add(dropDownDisplayCategoryName);

                            }
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(PincodeActivity.this, android.R.layout.simple_spinner_item, cat);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            spinner.setAdapter(spinnerArrayAdapter);


                            /*String jsonresponse = response.body().toString();
                            spinJSON(jsonresponse);*/
                        }
                        else {
                            Toast.makeText(PincodeActivity.this, "response failed", Toast.LENGTH_SHORT).show();
                        }



                    }

                    @Override
                    public void onFailure(Call<ResponseData> call, Throwable t) {
                        Log.i("ggg",t.toString());

                    }
                });


            }
        });
    }

    private void spinJSON(String response){

        try {

            JSONObject obj = new JSONObject(response);
            goodModelArrayList = new ArrayList<>();
            JSONArray dataArray  = obj.getJSONArray("PostOffice");

            for (int i = 0; i < dataArray.length(); i++) {

                PostOffice spinnerModel = new PostOffice();
                JSONObject dataobj = dataArray.getJSONObject(i);

                spinnerModel.setName(dataobj.getString("Name"));


                goodModelArrayList.add(spinnerModel);

            }

            for (int i = 0; i < goodModelArrayList.size(); i++){
                playerNames.add(goodModelArrayList.get(i).getName().toString());
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(PincodeActivity.this, android.R.layout.simple_spinner_item, playerNames);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            spinner.setAdapter(spinnerArrayAdapter);

        } catch (JSONException e) {
            Toast.makeText(PincodeActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
}