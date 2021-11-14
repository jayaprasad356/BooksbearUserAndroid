package com.asquare.booksbear.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.asquare.booksbear.BuildConfig;
import com.asquare.booksbear.CheckNetwork;
import com.asquare.booksbear.MainAlertActivity;
import com.asquare.booksbear.R;
import com.asquare.booksbear.helper.Constant;
import com.asquare.booksbear.helper.Session;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;


import org.jsoup.Jsoup;

import java.io.IOException;

public class SplashActivity extends Activity {
    Session session;
    Activity activity;
    int SPLASH_TIME_OUT = 500;
    private String sLatestVersion,sCurrentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = SplashActivity.this;
        session = new Session(activity);
        session.setBoolean("update_skip", false);

        new GetLatestVersion().execute();
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
    private class GetLatestVersion extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... strings) {
            try {

                sLatestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id="+getPackageName()).timeout(30000).get().select("div.hAyfc:nth-child(4)>"+
                        "span:nth-child(2) > div:nth-child(1)"+"> span:nth-child(1)").first().ownText();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sLatestVersion;
        }

        @Override
        protected void onPostExecute(String s) {
            if(CheckNetwork.isInternetAvailable(SplashActivity.this)){
                sCurrentVersion = BuildConfig.VERSION_NAME;
                /*Log.e("VERSION",sCurrentVersion + " "+sLatestVersion);
                long cVersion = Long.parseLong(sCurrentVersion);
                long lVersion = Long.parseLong(sLatestVersion);*/
                if (sLatestVersion != null && !sLatestVersion.equals(sCurrentVersion)){
                    updateAlertDialog();
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        GotoActivity();
                    }
                    else {
                        Intent intent = new Intent(SplashActivity.this, MainAlertActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    //new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, WelcomeActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);

                }

            }
            else {
                Toast.makeText(SplashActivity.this, "Please Connect Internet", Toast.LENGTH_SHORT).show();


            }

        }
    }

    private void updateAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage("Update Available");
        builder.setCancelable(false);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }






    private void GotoActivity() {
        Uri data = this.getIntent().getData();
        if (data == null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && !session.getBoolean("is_first_time")) {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            } else {
                //requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_splash);
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        }
        else if (String.valueOf(data).contains("admin.booksbear.in") && data != null && data.isHierarchical()) {
            Log.d("DATAPATH",data.getPath() + String.valueOf(data));
            switch (data.getPath().split("/")[1]) {
                case "itemdetail": // Handle the item detail deep link
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", data.getPath().split("/")[2]);
                    intent.putExtra(Constant.FROM, "share");
                    intent.putExtra("vpos", 0);
                    startActivity(intent);
                    finish();
                    break;
                case "refer": // Handle the refer deep link
                    if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                        Constant.FRND_CODE = data.getPath().split("/")[2];
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", Constant.FRND_CODE);
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(SplashActivity.this, R.string.refer_code_copied, Toast.LENGTH_LONG).show();
                        Intent referIntent = new Intent(this, LoginActivity.class);
                        referIntent.putExtra(Constant.FROM, "refer");
                        startActivity(referIntent);
                        finish();
                    } else {
                        new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
                        Toast.makeText(activity, activity.getString(R.string.msg_refer), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        } else {
            //requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_splash);
            new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);

            /*if (!session.getBoolean("is_first_time")) {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            } else {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }*/
        }
    }
}
