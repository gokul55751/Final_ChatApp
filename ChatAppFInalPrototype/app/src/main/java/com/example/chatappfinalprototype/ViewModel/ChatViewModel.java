package com.example.chatappfinalprototype.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class ChatViewModel extends ViewModel {
    private final MutableLiveData<ViewModelMessage> FtoA_message = new MutableLiveData<>();

    public void setFtoA_message(ViewModelMessage message) {FtoA_message.setValue(message);}
    public LiveData<ViewModelMessage> getFtoA_message() {return FtoA_message;}

}
