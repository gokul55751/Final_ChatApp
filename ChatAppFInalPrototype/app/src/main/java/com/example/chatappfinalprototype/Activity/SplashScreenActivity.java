package com.example.chatappfinalprototype.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappfinalprototype.Network.SocketHandler;
import com.example.chatappfinalprototype.R;

import org.json.JSONObject;

import java.util.UUID;

import io.socket.client.Socket;


public class SplashScreenActivity extends AppCompatActivity {

    Button saveButton;
    TextView id;
    TextView name;
    TextView number;
    TextView image;

    Socket socket;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SocketHandler.setSocket();
        socket = SocketHandler.getSocket();
        socket.connect();

        SharedPreferences preferences = getSharedPreferences("Login", MODE_PRIVATE);
        String isLogin = preferences.getString("auth", null);

        if(isLogin!=null){
            Toast.makeText(this, isLogin, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
        }

        saveButton = findViewById(R.id.btn);
        id = findViewById(R.id.edt_uuid);
        name = findViewById(R.id.edt_name);
        number = findViewById(R.id.edt_number);
        image = findViewById(R.id.edt_image);

        String uuid = UUID.randomUUID().toString();


        saveButton.setOnClickListener(v -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", uuid);
                jsonObject.put("name", name.getText().toString());
                jsonObject.put("number", number.getText().toString());
                jsonObject.put("image", image.getText().toString());
                socket.emit("createProfile", jsonObject);
            }catch (Exception e){}

        });

        socket.on(uuid, args -> runOnUiThread(() -> {
            JSONObject jsonObject = (JSONObject) args[0];
            try{
                String auth = jsonObject.getString("uuid");
                if(auth!=null){
                    SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("auth", auth);
                    editor.apply();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }));
    }
}