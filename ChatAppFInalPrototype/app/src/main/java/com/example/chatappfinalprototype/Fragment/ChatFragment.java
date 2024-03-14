package com.example.chatappfinalprototype.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatappfinalprototype.Activity.MainActivity;
import com.example.chatappfinalprototype.Adapter.MessageAdapter;
import com.example.chatappfinalprototype.Model.Message;
import com.example.chatappfinalprototype.Network.SocketHandler;
import com.example.chatappfinalprototype.R;
import com.example.chatappfinalprototype.ViewModel.ActivityToChatViewModel;
import com.example.chatappfinalprototype.ViewModel.ActivityViewModel;
import com.example.chatappfinalprototype.ViewModel.ChatViewModel;
import com.example.chatappfinalprototype.ViewModel.UserViewModel;
import com.example.chatappfinalprototype.ViewModel.ViewModelMessage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;

public class ChatFragment extends Fragment {

    String auth;
    String senderRoom;
    String receiverRoom;

    String SEND_TYPE = "text";

    TextView userName;
    ImageView chat_backBtn;

    ArrayList<Message> messageArrayList;

    RecyclerView recyclerView;
    MessageAdapter adapter;
    ImageView attach;
    CircleImageView sendButton;
    EditText edt_message;
    LinearLayoutCompat sendingImageLayout;
    ImageView sendingImage;
    ImageView videoCall;
    ImageView audioCall;
    TextView imageDescription;
    Context context;

    Socket socket;
    ChatViewModel chatViewModel;
    ActivityViewModel activityViewModel;


    String TAG = "log9999";
    String uuid = "";
    private ActivityToChatViewModel activityToChatViewModel;

    public ChatFragment(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        SharedPreferences preferences = context.getSharedPreferences("Login", MODE_PRIVATE);
        auth = preferences.getString("auth", null);

        uuid = getArguments().getString("uuid");
        String name = getArguments().getString("name");

        init(view);

        activityToChatViewModel = new ViewModelProvider(requireActivity()).get(ActivityToChatViewModel.class);
        activityToChatViewModel.getData().observe(getActivity(), item -> {
            messageArrayList.addAll(item);
            adapter.notifyDataSetChanged();
        });

        userName.setText(name);
        chat_backBtn.setOnClickListener(v->getActivity().onBackPressed());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(context, messageArrayList, auth);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        activityViewModel.getData().observe(getActivity(), item -> {
            Log.d(TAG, "onCreateView: r2222");
            messageArrayList.addAll(item);
            adapter.notifyDataSetChanged();
        });

        sendButton.setOnClickListener(v->{
            String msg = edt_message.getText().toString();
            edt_message.setText("");
            Message message = new Message(msg, auth, "11", SEND_TYPE);
            messageArrayList.add(message);
            adapter.notifyDataSetChanged();
            chatViewModel.setFtoA_message(new ViewModelMessage(message, uuid));
        });

        videoCall.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("uuid", uuid);
            CallingFragment callingFragment = new CallingFragment(context);
            callingFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, callingFragment).addToBackStack(null).commit();
        });
        return view;
    }

    private void init(View view) {
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        activityViewModel = new ViewModelProvider(requireActivity()).get(ActivityViewModel.class);
        messageArrayList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.chat_recyclerView);
        attach = view.findViewById(R.id.chat_attach);
        edt_message = view.findViewById(R.id.edt_message);
        sendButton = view.findViewById(R.id.chat_sendBtn);
        userName = view.findViewById(R.id.chat_userName);
        chat_backBtn = view.findViewById(R.id.chat_backBtn);
        sendingImageLayout = view.findViewById(R.id.sendingImageLayout);
        sendingImage = view.findViewById(R.id.sendingImage);
        imageDescription = view.findViewById(R.id.imageDescription);
        videoCall = view.findViewById(R.id.videoCall);
        audioCall = view.findViewById(R.id.audioCall);
    }
}