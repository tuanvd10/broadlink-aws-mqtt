package com.viettel.vht.remoteapp.ui.remotecontrol;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.viettel.vht.remoteapp.R;

public class RemoteControlViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public RemoteControlViewModel() {
        Log.i(this.getClass().getName(), "Start remote control view model");
        mText = new MutableLiveData<>();
        mText.setValue("Remote app");

    }

    public LiveData<String> getText() {
        return mText;
    }

}
