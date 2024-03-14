package com.example.chatappfinalprototype.ViewModel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatappfinalprototype.Model.Message;
import java.util.ArrayList;

public class ActivityViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Message>> AtoF_message = new MutableLiveData<>();

    public void setData(ArrayList<Message> message) {
        Log.d("log9999", "setAtoF_message: value set");
        AtoF_message.setValue(message);}
    public LiveData<ArrayList<Message>> getData() {return AtoF_message;}

}
