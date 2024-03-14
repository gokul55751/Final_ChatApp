package com.example.chatappfinalprototype.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatappfinalprototype.Model.User;

import java.util.ArrayList;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<User>> userList = new MutableLiveData<>();

    public void setData(ArrayList<User> userArrayList) {userList.setValue(userArrayList);}
    public LiveData<ArrayList<User>> getData() {return userList;}
}
