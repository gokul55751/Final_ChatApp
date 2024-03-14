package com.example.chatappfinalprototype.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatappfinalprototype.Adapter.MainUserAdapter;
import com.example.chatappfinalprototype.Listener.OnUserClickedListener;
import com.example.chatappfinalprototype.Model.User;
import com.example.chatappfinalprototype.R;
import com.example.chatappfinalprototype.ViewModel.UserViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnUserClickedListener {

    private View view;
    private String uuid;
    Context context;
    private RecyclerView recycleView;
    private ArrayList<User> userList;
    private MainUserAdapter adapter;
    private UserViewModel userViewModel;

    public HomeFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences preferences = context.getSharedPreferences("Login", MODE_PRIVATE);
        uuid = preferences.getString("auth", null);

        Log.d("log9999", "onCreateView: called");

        init();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void init() {
//viewModel
        recycleView = view.findViewById(R.id.main_recycleView);
        userList = new ArrayList<>();
        recycleView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MainUserAdapter(context, userList, this);
        recycleView.setAdapter(adapter);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getData().observe(getActivity(), item -> {
            Log.d("log9999", "init: observe");
            userList.addAll(item);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onUserClicked(String uuid, String name) {
        Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString("uuid", uuid);
        bundle.putString("name", name);
        ChatFragment chatFragment = new ChatFragment(context);
        chatFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, chatFragment).addToBackStack(null).commit();
    }
}