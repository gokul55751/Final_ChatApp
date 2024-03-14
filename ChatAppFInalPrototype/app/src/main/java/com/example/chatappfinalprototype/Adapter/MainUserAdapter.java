package com.example.chatappfinalprototype.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappfinalprototype.Listener.OnUserClickedListener;
import com.example.chatappfinalprototype.Model.User;
import com.example.chatappfinalprototype.R;

import java.util.ArrayList;

public class MainUserAdapter extends RecyclerView.Adapter<MainUserAdapter.ViewHolder> {

    Context context;
    ArrayList<User> usersList;
    OnUserClickedListener listener;

    public MainUserAdapter(Context context, ArrayList<User> users, OnUserClickedListener listener) {
        this.context = context;
        this.usersList = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User currentUser = usersList.get(position);
        holder.userTitle.setText(currentUser.getName());
        holder.userLastMessage.setText("last message");
        holder.userLastOnline.setText("Today");
        char s = currentUser.getName().charAt(0);
        holder.initLetter.setText(s+"");
        holder.itemView.setOnClickListener(v->{
            listener.onUserClicked(currentUser.getUuid(), currentUser.getName());
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userTitle, userLastMessage, userLastOnline, initLetter;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userTitle = itemView.findViewById(R.id.main_userName);
            userLastMessage = itemView.findViewById(R.id.main_userLastMessages);
            userLastOnline = itemView.findViewById(R.id.main_userLastOnline);
            initLetter = itemView.findViewById(R.id.main_initLetter);
        }

    }
}
