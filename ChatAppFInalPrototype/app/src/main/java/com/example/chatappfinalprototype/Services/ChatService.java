package com.example.chatappfinalprototype.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappfinalprototype.Activity.MainActivity;
import com.example.chatappfinalprototype.Adapter.MessageAdapter;
import com.example.chatappfinalprototype.Model.Message;
import com.example.chatappfinalprototype.Network.SocketHandler;
import com.example.chatappfinalprototype.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatService extends Service {

    private ViewGroup floatView;
    private WindowManager.LayoutParams floatWindowLayoutParams;
    private int LAYOUT_TYPE;
    private WindowManager windowManager;

    private ImageView chat_backBtn;
    private EditText msgBox;
    private CircleImageView sendBtn;

    private Socket socket;
    private String auth;
    private String TAG = "log9999";
    private MessageAdapter adapter;
    private ArrayList<Message> messageArrayList;
    private RecyclerView recyclerView;
    private Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.service_chat, null);

        init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }
        floatWindowLayoutParams = new WindowManager.LayoutParams(
                (int) Math.round(width * 0.75f),
                (int) Math.round(height * 0.75f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        floatWindowLayoutParams.gravity = Gravity.CENTER;
        floatWindowLayoutParams.x = 0;
        floatWindowLayoutParams.y = 0;
        windowManager.addView(floatView, floatWindowLayoutParams);

        //stop (close)
        chat_backBtn.setOnClickListener(v -> {
            stopSelf();
            windowManager.removeView(floatView);
//            only if go back to application from service
            Intent intent = new Intent(ChatService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        sendBtn.setOnClickListener(v -> {
            String msg = msgBox.getText().toString();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", auth);
                jsonObject.put("receiverId", auth);
                jsonObject.put("data", msg);
                jsonObject.put("senderId", auth);
                jsonObject.put("time", "11");
                jsonObject.put("type", "text");
                socket.emit("uploadMessage", jsonObject);
                messageArrayList.add(new Message(msg, "g1", "11", "text"));

                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        socketReceiver();

//        if you want to move floating window

//        stop moving the floating window
        msgBox.setOnTouchListener((view, motionEvent) -> {
            msgBox.setCursorVisible(true);
            WindowManager.LayoutParams updatedFloatParamsFlag = floatWindowLayoutParams;
            updatedFloatParamsFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowManager.updateViewLayout(floatView, updatedFloatParamsFlag);
            return false;
        });
    }

    private void init() {
        SocketHandler.setSocket();
        socket = SocketHandler.getSocket();
        socket.connect();

        chat_backBtn = floatView.findViewById(R.id.chat_backBtn);
        msgBox = floatView.findViewById(R.id.edt_message);
        sendBtn = floatView.findViewById(R.id.chat_sendBtn);
        SharedPreferences preferences = getSharedPreferences("Login", MODE_PRIVATE);
        auth = preferences.getString("auth", null);

        messageArrayList = new ArrayList<>();
        messageArrayList.add(new Message("helo", "g1", "11", "text"));
        recyclerView = floatView.findViewById(R.id.chat_recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(getApplicationContext(), messageArrayList, auth);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        windowManager.removeView(floatView);
    }

    private void socketReceiver() {
        Log.d(TAG, "socketReceiver: 1 " + Thread.currentThread().getName() );
        handler = new Handler();

        socket.on(auth, args -> {
            Log.d(TAG, "socketReceiver: called");
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                String type = jsonObject.getString("type");
                if (type.equals("message")) {
                    fetchMessages(jsonObject);
                    Log.d(TAG, "socketReceiver: 2 " + Thread.currentThread().getName() );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            adapter.notifyDataSetChanged();
        });
    }

    private void fetchMessages(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d(TAG, "fetchMessages: rrr");
                JSONObject obj = (JSONObject) jsonArray.get(i);
//                name changed
                Message message = new Message(obj.getString("data"), obj.getString("senderId"), obj.getString("time"), obj.getString("type"));

                messageArrayList.add(message);
                Log.d(TAG, "fetchMessages: " + message.toString() + " - " + messageArrayList.size());
                handler.post(() -> adapter.notifyDataSetChanged());
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Have error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}