package com.asquare.booksbear.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asquare.booksbear.R;
import com.asquare.booksbear.helper.ApiConfig;
import com.asquare.booksbear.helper.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReferDetailActivity extends AppCompatActivity {
    EditText Referet;
    Button GetDetails;
    TextView Nos,Purchase;
    ImageView imageMenu;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_detail);

        Referet = findViewById(R.id.referet);
        GetDetails = findViewById(R.id.getbtn);
        Nos = findViewById(R.id.nos);
        Purchase = findViewById(R.id.puramt);
        imageMenu = findViewById(R.id.imageMenu);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, getTheme()));
        toolbarTitle.setText("Refer Details");
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
                /*Intent intent = new Intent(LoadUrlActivity.this,MainActivity.class);
                startActivity(intent);
                finish();*/
            }
        });
        GetDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Referet.getText().toString().equals("")){
                    Referet.setError("Enter Code");
                    Referet.requestFocus();
                }
                else {
                    GetData();
                }
            }
        });
    }

    private void GetData()
    {
        Map<String, String> params = new HashMap<>();
        params.put("refercode", Referet.getText().toString().trim());
        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);
                        JSONObject jsonobj = object.getJSONObject(Constant.DATA);
                        Nos.setText("No. of Sales = "+String.valueOf(jsonobj.getInt("totalsale")));
                        Purchase.setText("Total Purchase Amount = "+String.valueOf(jsonobj.getInt("totalamount")));


                    }
                    else {
                        Toast.makeText(this, ""+String.valueOf(jsonObject.getString("message")), Toast.LENGTH_SHORT).show();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
            else {
                Toast.makeText(this, String.valueOf(response) +String.valueOf(result), Toast.LENGTH_SHORT).show();

            }
        }, ReferDetailActivity.this, Constant.REFERDETAILS_URL, params, true);


    }
}