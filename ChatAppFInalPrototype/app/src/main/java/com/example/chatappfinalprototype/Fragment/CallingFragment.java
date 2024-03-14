package com.example.chatappfinalprototype.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappfinalprototype.Network.SocketHandler;
import com.example.chatappfinalprototype.R;

import org.json.JSONObject;

import io.socket.client.Socket;

public class CallingFragment extends Fragment {

    private View view;
    private Socket socket;
    private Context context;

    public CallingFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calling, container, false);

        String uuid = getArguments().getString("uuid");

        TextView textView = view.findViewById(R.id.textView);
        Button button = view.findViewById(R.id.call_button);

        SharedPreferences preferences = context.getSharedPreferences("Login", MODE_PRIVATE);
        String auth = preferences.getString("auth", null);

        socket = SocketHandler.getSocket();

        button.setOnClickListener(v -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", auth);
                jsonObject.put("callerId", uuid);
                socket.emit("calling", jsonObject);
            } catch (Exception e) {
                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            }
        });

        socket.on(auth, args -> getActivity().runOnUiThread(() -> {
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                String type = jsonObject.getString("type");
                if (type.equals("callingReceiver")) {
                    String data = jsonObject.getString("data");
                    textView.setText(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        return view;
    }
}