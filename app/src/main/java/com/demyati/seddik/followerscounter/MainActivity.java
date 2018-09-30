package com.demyati.seddik.followerscounter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

 /**
  * You must register in Facebook Developers  website first
  * in order to use this app. Then you can login to the same account
  * you registered with in Facebook Developers yo get followers counter.
  *
  * This app is created using Facebook Graph 3.0
**/

public class MainActivity extends AppCompatActivity {

    List<String> Mins;
    private final String APPLICATION_ID="YOUR_FB_APP_ID";
    private static String ACCESS_TOKEN = "";
    AccessToken accessToken;
    TextView Result;
    static List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
    int ViewsCounter=0;
    String fan_count="",name="",Link="";
    static int timeInterval=5;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button Refresh;
    Spinner TimeInterval;
    LoginButton loginButton;
    ToggleButton Automatic;
    private static String USER_ID="";
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hashkey();

        sharedPreferences=getSharedPreferences("Data",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        //first run info
        if (sharedPreferences.getBoolean("FirstReminder", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs


            final ShowcaseView showcaseView;
            showcaseView = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseWelcomeTitle))
                    .setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseWelcomeInfo))
                    .blockAllTouches()
                    .setStyle(R.style.CustomShowcaseTheme3)
                    .setTarget(Target.NONE)
                    .build();

            ViewsCounter=0;
            showcaseView.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ViewsCounter==0){
                        showcaseView.setTarget(new ViewTarget(findViewById(R.id.login_button)));
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseLoginInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseLoginTitle));
                    }
                    else if(ViewsCounter==1){
                        showcaseView.setTarget(new ViewTarget(findViewById(R.id.Add_fab)));
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseAddInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseAddTitle));
                    }
                    else if(ViewsCounter==2){
                        showcaseView.setTarget(new ViewTarget(findViewById(R.id.Refresh)));
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseManualInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseManualTitle));
                    }
                    else if(ViewsCounter==3){
                        showcaseView.setTarget(new ViewTarget(findViewById(R.id.Automatic)));
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseAutoInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseAutoTitle));
                    }

                    else if(ViewsCounter==4){
                        showcaseView.setTarget(new ViewTarget(findViewById(R.id.TimeInterval)));
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseTimeInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseTimeTitle));
                    }

                    else if(ViewsCounter==5){
                        showcaseView.setTarget(Target.NONE);
                        showcaseView.setContentText(getApplicationContext().getResources().getString(R.string.ShowCaseEndInfo));
                        showcaseView.setContentTitle(getApplicationContext().getResources().getString(R.string.ShowCaseEndTitle));
                        showcaseView.setButtonText("Close");
                    }
                    else if(ViewsCounter==6)
                        showcaseView.hide();
                    ++ViewsCounter;
                }
            });

            sharedPreferences.edit().putBoolean("FirstReminder", false).commit();
        }

        name=sharedPreferences.getString("name","null");
        fan_count=sharedPreferences.getString("fan_count","null");

        if(!name.equals("null")&&!fan_count.equals("null")) {
            Result.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
            Result.setText(MainActivity.this.getResources().getString(R.string.Result)
                    + " \" " + name + " \" " + MainActivity.this.getResources().getString(R.string.is) + fan_count);
        }


        USER_ID=sharedPreferences.getString("USER_ID","null");
        ACCESS_TOKEN=sharedPreferences.getString("ACCESS_TOKEN","null");

        if(!USER_ID.equals("null")&&!ACCESS_TOKEN.equals("null")) {
            accessToken = new AccessToken(ACCESS_TOKEN, APPLICATION_ID,
                    USER_ID, null, null, null, null
                    , null);
            Refresh(findViewById(android.R.id.content));
        }

        if(sharedPreferences.getString("Automatic","False").equals("True")){
            Automatic.setChecked(true);
            TimeInterval.setSelection(sharedPreferences.getInt("Spinner",1));
        }


        Automatic=findViewById(R.id.Automatic);
        TimeInterval=findViewById(R.id.TimeInterval);

        Mins=Arrays.asList(getResources().getStringArray(R.array.Interval));
        ArrayAdapter<String> HoursAdapter=new ArrayAdapter(getApplicationContext(),android.R.layout.simple_spinner_item,Mins);
        HoursAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        TimeInterval.setAdapter(HoursAdapter);
        TimeInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            //when item is pressed
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt("Spinner",i);
                editor.apply();
                if(i==0)
                    timeInterval=1;
                else if(i==1)
                    timeInterval=5;
                else if(i==2)
                    timeInterval=15;
                else if(i==3)
                    timeInterval=30;
                else if(i==4)
                    timeInterval=60;
                System.out.println(String.valueOf(timeInterval));
            }
            //when nothing is selected
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        loginButton = findViewById(R.id.login_button);
        loginButton.setPublishPermissions(Arrays.asList("manage_pages"));
        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                ACCESS_TOKEN =loginResult.getAccessToken().getToken();
                USER_ID=loginResult.getAccessToken().getUserId();
                editor.putString("USER_ID",USER_ID);
                editor.putString("ACCESS_TOKEN", ACCESS_TOKEN);
                editor.apply();
                System.out.println(USER_ID+"\n"+ ACCESS_TOKEN);
                accessToken=loginResult.getAccessToken();
                Refresh(findViewById(android.R.id.content));
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.onCancel),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("Failed " + exception);
                Toast.makeText(MainActivity.this,exception.toString(),Toast.LENGTH_LONG).show();
            }
        });

        if (!isReceiverRegistered(NewItemReceiver)){
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(NewItemReceiver,
                    new IntentFilter("NewAddedItem"));
            receivers.add(NewItemReceiver);
        }

        if (!isReceiverRegistered(UpdateNumber)){
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(UpdateNumber,
                    new IntentFilter("UpdateNumber"));
            receivers.add(UpdateNumber);
        }

        Result =findViewById(R.id.Result);
        Result.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    BroadcastReceiver NewItemReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Link = intent.getStringExtra("AddedItem");

            Result.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorError));
            if (!Link.toLowerCase().contains("facebook.com/"))
                Result.setText(getApplicationContext().getResources().getString(R.string.LinkError));
            else if(!haveNetworkConnection()) {
                Result.setText(getApplicationContext().getResources().getString(R.string.InternetError));

                //if the page has no username then extract id from the link
                if(Link.contains("-")){
                    try {
                        Link = URLDecoder.decode(Link, "utf-8");
                        Link=Link.replaceAll("\\D+","");
                        System.out.println("Link: "+Link);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] id = Link.split("/");
                    Link=id[3];
                }
                editor.putString("Link",Link);
                editor.apply();
            }
            else {
                if(Link.contains("-")){
                    try {
                        Link = URLDecoder.decode(Link, "utf-8");
                        Link=Link.replaceAll("\\D+","");
                        System.out.println("Link: "+Link);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] id = Link.split("/");
                    Link=id[3];
                }
                editor.putString("Link", Link);
                editor.apply();
                if (accessToken != null) {
                    if (!accessToken.isExpired()) {
                        if (accessToken.getCurrentAccessToken().getPermissions().contains("manage_pages"))
                            FacebookCall(Link);
                        else
                            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.PermissionsError), Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.ExpiredToken), Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.onCancel), Toast.LENGTH_LONG).show();
            }
        }
    };

    BroadcastReceiver UpdateNumber =new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Link=sharedPreferences.getString("Link","null");
            if(haveNetworkConnection()){
                if(Link!="null"){
                    if(accessToken!=null) {
                        if(!accessToken.isExpired()) {
                            if (accessToken.getCurrentAccessToken().getPermissions().contains("manage_pages")) {
                                FacebookCall(Link);
                            }
                            else {
                                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.PermissionsError), Toast.LENGTH_LONG).show();
                                SetAlarm("Cancel");
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.ExpiredToken), Toast.LENGTH_LONG).show();
                            SetAlarm("Cancel");
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.onCancel), Toast.LENGTH_LONG).show();
                        SetAlarm("Cancel");
                    }
                } else {
                    Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.LinkError), Toast.LENGTH_LONG).show();
                    SetAlarm("Cancel");
                }
            } else {
                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.InternetError), Toast.LENGTH_LONG).show();
                SetAlarm("Cancel");
            }
        }
    };

    //check if receive is registered in system
    public boolean isReceiverRegistered(BroadcastReceiver receiver){
        boolean registered = receivers.contains(receiver);
        return registered;
    }

    public void AddNewMission(View view) {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        AddItemDialog popUpActivity=new AddItemDialog();
        popUpActivity.setContext(getApplicationContext());
        popUpActivity.show(fragmentTransaction,"Add Item");
    }

    @Override
    protected void onDestroy() {
        //unregister receiver
        if (isReceiverRegistered(NewItemReceiver)) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(NewItemReceiver);
            receivers.remove(NewItemReceiver);
        }
        if (isReceiverRegistered(UpdateNumber)) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(UpdateNumber);
            receivers.remove(UpdateNumber);
        }
        super.onDestroy();
    }

    //excute graph call to fetch data from facebook
    public void FacebookCall(String id){
        fan_count=name="";
        System.out.println("/"+id);
        GraphRequest request= new GraphRequest(
                accessToken,
                "/"+id,
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            fan_count = response.getJSONObject().getString("fan_count");
                            name = response.getJSONObject().getString("name");
                            System.out.println(fan_count + "," + name);
                            if (android.text.TextUtils.isDigitsOnly(fan_count)) {
                                Result.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
                                Result.setText(MainActivity.this.getResources().getString(R.string.Result)
                                        + " \" " + name + " \" " + MainActivity.this.getResources().getString(R.string.is) + fan_count);
                                editor.putString("name", name);
                                editor.putString("fan_count", fan_count);
                                editor.apply();
                                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.Refresh), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            System.out.println("Error  " + e);
                            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.UnknownError),Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "fan_count,name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    //You need 3 hash keys for registering your app in facebook developers
    //first is generated by this function
    //the rest two are called development and release hash keys
    private void hashkey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.demyati.seddik.followerscounter",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                System.out.println("KeyHash: \n"+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    //check if there is an internet connection
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void Refresh(View view) {
        Link=sharedPreferences.getString("Link","null");
        if(haveNetworkConnection()){
            if(Link!="null"){
                if(accessToken!=null) {
                    if(!accessToken.isExpired()) {
                        if (accessToken.getCurrentAccessToken().getPermissions().contains("manage_pages"))
                            FacebookCall(Link);
                        else
                            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.PermissionsError), Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.ExpiredToken), Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.onCancel), Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.LinkError),Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.InternetError),Toast.LENGTH_LONG).show();
    }

    public void Automatic(View view) {
        Link=sharedPreferences.getString("Link","null");
        if(haveNetworkConnection()){
            if(Link!="null"){
                if(accessToken!=null) {
                    if(!accessToken.isExpired()) {
                        if (accessToken.getCurrentAccessToken().getPermissions().contains("manage_pages")) {

                            if(!Automatic.isChecked()) {
                                SetAlarm("Add");
                                editor.putString("Automatic", "True");
                                editor.apply();
                            }
                            else {
                                SetAlarm("Cancel");
                                editor.putString("Automatic", "False");
                                editor.apply();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.PermissionsError), Toast.LENGTH_LONG).show();
                            Automatic.setChecked(true);
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.ExpiredToken), Toast.LENGTH_LONG).show();
                        Automatic.setChecked(true);
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.onCancel), Toast.LENGTH_LONG).show();
                    Automatic.setChecked(true);
                }
            } else {
                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.LinkError), Toast.LENGTH_LONG).show();
                Automatic.setChecked(true);
            }
        } else {
            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.InternetError), Toast.LENGTH_LONG).show();
            Automatic.setChecked(true);
        }
    }

    public void SetAlarm(String state) {
        Intent notificationReceiver = new Intent("Interval", null, getApplicationContext()
                , UpdateClass.class);
        //its  info can be edited in the future
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext()
                , 1, notificationReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)
                getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        if (state.equals("Add"))
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    timeInterval * 60 * 1000, pendingIntent);
        else if (state.equals("Cancel"))
            alarmManager.cancel(pendingIntent);
    }
}
