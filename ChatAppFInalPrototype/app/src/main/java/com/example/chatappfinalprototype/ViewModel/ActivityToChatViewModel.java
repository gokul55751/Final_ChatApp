package com.example.chatappfinalprototype.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatappfinalprototype.Model.Message;

import java.util.ArrayList;

public class ActivityToChatViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Message>> messagesList = new MutableLiveData<>();

    public void setData(ArrayList<Message> messageArrayList) {messagesList.setValue(messageArrayList);}
    public LiveData<ArrayList<Message>> getData() {return messagesList;}

}