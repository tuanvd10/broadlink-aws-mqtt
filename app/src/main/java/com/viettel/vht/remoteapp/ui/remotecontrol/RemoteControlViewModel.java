package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RemoteControlViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public RemoteControlViewModel() {
        Log.i(this.getClass().getName(), "Start remote control view model");
        mText = new MutableLiveData<>();
        mText.setValue("Remote control");


    }

    public LiveData<String> getText() {
        return mText;
    }
}
