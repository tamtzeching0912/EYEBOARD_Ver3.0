package edu.cuhk.fyp.eyeboardver20;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import static android.content.ContentValues.TAG;

public class BTOperation {
    private Context context;
    private final BleDevice bleDevice;
    private final String uuid_service, uuid_write, uuid_notify;

    public BTOperation(Context context, final BleDevice bleDevice, final String uuid_service, final String uuid_write, final String uuid_notify) {
        this.context = context;
        this.bleDevice = bleDevice;
        this.uuid_service = uuid_service;
        this.uuid_write = uuid_write;
        this.uuid_notify = uuid_notify;
    }

    public void BT_notify() {
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_write,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Toast.makeText( context, "Notify FAIL", Toast.LENGTH_SHORT ).show();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        Intent intent = new Intent("com.example.broadcast.LOCAL_BROADCAST");
                        intent.putExtra("data",data);
                        MainActivity.getInstance().updateEOGinput(Character.getNumericValue(data[0]));
                        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                    }
                }
        );
    }

    public void BT_stop_notify(){
        BleManager.getInstance().stopNotify(bleDevice,uuid_service, uuid_notify);
        BleManager.getInstance().clearCharacterCallback( bleDevice);
    }

    public void write(byte[] data){
        BleManager.getInstance().write(
            bleDevice,
            uuid_service,
            uuid_write,
            data,
            new BleWriteCallback() {
                @Override
                public void onWriteSuccess ( int current, int total, byte[] justWrite){
                    Log.d(TAG, "onWriteSuccess: start calibration");
                }

                @Override
                public void onWriteFailure (BleException exception){

                }
            });
    }
}
