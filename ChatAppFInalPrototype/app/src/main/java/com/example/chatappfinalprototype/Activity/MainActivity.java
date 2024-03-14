package com.example.chatappfinalprototype.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.chatappfinalprototype.Database.DatabaseHelper;
import com.example.chatappfinalprototype.Fragment.HomeFragment;
import com.example.chatappfinalprototype.Model.Message;
import com.example.chatappfinalprototype.Model.User;
import com.example.chatappfinalprototype.Network.SocketHandler;
import com.example.chatappfinalprototype.R;
import com.example.chatappfinalprototype.Services.ChatService;
import com.example.chatappfinalprototype.ViewModel.ActivityToChatViewModel;
import com.example.chatappfinalprototype.ViewModel.ActivityViewModel;
import com.example.chatappfinalprototype.ViewModel.ChatViewModel;
import com.example.chatappfinalprototype.ViewModel.UserViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    private String TAG = "log9999";
    private AlertDialog dialog;
    private Socket socket;
    private UserViewModel userViewModel;
    private ChatViewModel chatViewModel;
    private ActivityViewModel activityViewModel;
    private ActivityToChatViewModel activityToChatViewModel;
    private String auth;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: called");

        init();

//        databaseHelper.addUser("1", "gokul", "9199", "image");
//        databaseHelper.fetchALLUser();
//        databaseHelper.writeChat(new Message("hi", "1", "11", "text"), "1");
//        databaseHelper.readChat("1");

        setupFragment();

//        setUpService();

    }

    private void setUpService() {
        if(isServiceRunning()){
            stopService(new Intent(MainActivity.this, ChatService.class));
        }
        if(checkOverlayPermission()){
            new Handler().postDelayed(() -> {
                runOnUiThread(() -> {
                    startService(new Intent(MainActivity.this, ChatService.class));
                    finish();
                });
            }, 5000);
        }else{
            requestFloatingWindowPermission();
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(ChatService.class.getName() == service.service.getClassName()){
                return true;
            }
        }
        return false;
    }
    private void requestFloatingWindowPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Screen Overlay Permission Needed");
        builder.setMessage("Enable 'Display over the App' From setting");
        builder.setPositiveButton("Open Settings", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RESULT_OK);
        });
        dialog = builder.create();
        dialog.show();
    }
    private boolean checkOverlayPermission(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            return Settings.canDrawOverlays(this);
        }
        else return true;
    }

    private void init(){
        SharedPreferences preferences = getSharedPreferences("Login", MODE_PRIVATE);
        auth = preferences.getString("auth", null);

        databaseHelper = new DatabaseHelper(this);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);
        activityToChatViewModel = new ViewModelProvider(this).get(ActivityToChatViewModel.class);

        socket = SocketHandler.getSocket();
        
        if(socket.connected()) Log.d(TAG, "init: socket is connected");
        else Log.d(TAG, "init: socket is not connected");
        
        socketReceiver();

        sendAllPendingMessages();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", auth);
            socket.emit("refresher", jsonObject);
            Log.d(TAG, "init: worked");
        }catch (Exception e){
            Log.d(TAG, "init: faied to refresh");
            e.printStackTrace();
        }
        // setup database

        chatViewModel.getFtoA_message().observe(MainActivity.this, item -> {

            Message message = item.getMessage();
            try {
                if(!isConnected(this) && !socket.connected()) {
                    databaseHelper.addMessage(item.getSenderId(), message.getData(), message.getTimeStamp(), message.getType());
                    Log.d(TAG, "init: added to pending message");
                    return;
                }
                sendAllPendingMessages();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", auth);
                jsonObject.put("receiverId", item.getSenderId());
                jsonObject.put("data", message.getData());
                jsonObject.put("senderId", auth);
                jsonObject.put("time", message.getTimeStamp());
                jsonObject.put("type", message.getType());
                socket.emit("uploadMessage", jsonObject);
//                databaseHelper.addMessage(item.getSenderId(), message.getData(), message.getTimeStamp(), message.getType());
                //save to database
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    private void sendAllPendingMessages() {
        ArrayList<Message> messageArrayList = databaseHelper.fetchALLMessage();
        if(!isConnected(this) && !socket.connected()) return;
        
        for(int i=0; i<messageArrayList.size(); i++){
            Message message = messageArrayList.get(i);
            Log.d(TAG, "init: " + message.toString());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", auth);
                jsonObject.put("receiverId", message.getSenderId());
                jsonObject.put("data", message.getData());
                jsonObject.put("senderId", auth);
                jsonObject.put("time", message.getTimeStamp());
                jsonObject.put("type", message.getType());
                socket.emit("uploadMessage", jsonObject);
            }catch (Exception e){
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
        }
        databaseHelper.deleteAllMessages();
    }

    private void setupFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, new HomeFragment(MainActivity.this));
        fragmentTransaction.commit();
    }

    private void socketReceiver(){
        socket.on(auth, args -> runOnUiThread(() -> {
            JSONObject jsonObject = (JSONObject) args[0];
            Log.d(TAG, "socketReceiver: socket incoming");
            try{
                String type = jsonObject.getString("type");
                if(type.equals("users")){
                    fetchUsers(jsonObject);
                }else if(type.equals("message")){
                    fetchMessages(jsonObject);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }));
    }

    private void fetchUsers(JSONObject jsonObject) {
        ArrayList<User> userArrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d(TAG, "socketReceiver: user received");
                JSONObject obj = (JSONObject) jsonArray.get(i);
                User user = new User(obj.getString("_id"), obj.getString("name"), obj.getString("number"), obj.getString("image"));
                //save The user in the database (user table) if not exist
                if(!user.getUuid().equals(auth)) {
                    Log.d(TAG, "fetchUsers: " + user.toString());
                    if (databaseHelper.getUser(user.getNumber()) == null) {
                        Log.d(TAG, "fetchUsers: inside get user");
                        databaseHelper.addUser(user.getUuid(), user.getName(), user.getNumber(), user.getImage());
                    }
                    userArrayList.add(user);
                }
            }
            userViewModel.setData(userArrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void fetchMessages(JSONObject jsonObject) {
        ArrayList<Message> messagesList = new ArrayList<>();

        Toast.makeText(this, "someting come", Toast.LENGTH_SHORT).show();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d(TAG, "fetchMessages: rrr");
                JSONObject obj = (JSONObject) jsonArray.get(i);
//                name changed
                Message message = new Message(obj.getString("data"), obj.getString("senderId"), obj.getString("time"), obj.getString("type"));
                Log.d(TAG, "fetchMessages: " + message.toString());
                messagesList.add(message);

                Log.d(TAG, "fetchMessages: rrr " + messagesList.size());
                //store in local database
                //send to chat fragment
//                activityViewModel.setData(messagesList);
            }
            activityToChatViewModel.setData(messagesList);
        } catch (JSONException e) {
            Toast.makeText(this, "Have error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifiConn!=null) Log.d("log9999", "is wifi Connected: " + wifiConn.isConnected());
        if(mobileConn!=null) Log.d("log9999", "is mobile Connected: " + mobileConn.isConnected());

        return (wifiConn != null && wifiConn.isConnected()) || (mobileConn!=null && mobileConn.isConnected());
    }
}