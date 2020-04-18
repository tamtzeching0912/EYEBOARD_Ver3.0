package edu.cuhk.fyp.eyeboardver20;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.cuhk.fyp.eyeboardver20.adapter.DeviceAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private Button bt_command, bt_disconnect;
    private TextView msg_box;
    private ListView device_list;
    private boolean deviceFound = false;

    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private BTOperation targetBle;

    private static MainActivity instance;
    private int EOG_input = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        instance = this;

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        TextView txt_name = (TextView) findViewById(R.id.txt_name);
        switch (v.getId()) {
            case R.id.bt_command:
                if (bt_command.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                }
                else if (bt_command.getText().equals(getString(R.string.pair))) {
                    txt_name.performClick();
                }
                else if (bt_command.getText().equals(getString(R.string.calib))) {
                    byte[] data = new byte[]{'c', '\r', '\n'};
                    targetBle.write(data);
                }
                break;
            case R.id.bt_disconnect:
                txt_name.performClick();
                break;
            default:
                break;
        }
    }

    private void initView() {
        msg_box = (TextView) findViewById(R.id.msg_box);
        msg_box.setText(R.string.tap_scan);

        bt_command = (Button) findViewById(R.id.bt_command);
        bt_command.setOnClickListener(this);

        bt_disconnect = (Button) findViewById(R.id.bt_disconnect);
        bt_disconnect.setOnClickListener(this);

        device_list = (ListView) findViewById(R.id.device_list);

        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        });
        ListView listView_device = (ListView) findViewById(R.id.device_list);
        listView_device.setAdapter(mDeviceAdapter);
    }

    public static MainActivity getInstance(){
        return instance;
    }

    public void updateEOGinput(int i){
        EOG_input = i;
        EOGKeyboard kb = EOGKeyboard.getInstance();
        if(kb!=null && EOG_input<=4){
            kb.getInstance().dirToQuart(EOG_input);
        }
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    private void setScanRule() {
        UUID[] serviceUuids = null;
        String[] names = {"EYEBOARD"};
        String mac = null;
        boolean isAutoConnect = true;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)
                .setDeviceName(true, names)
                .setDeviceMac(mac)
                .setAutoConnect(isAutoConnect)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                bt_command.setText("Scanning Device");
                bt_command.setClickable(false);
                device_list.setVisibility(View.GONE);
                deviceFound = false;
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                device_list.setVisibility(View.VISIBLE);
                deviceFound = true;
                BleManager.getInstance().cancelScan();
                msg_box.setText(R.string.tap_pair);
                bt_command.setText(R.string.pair);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                bt_command.setClickable(true);
                if (!deviceFound) {
                    msg_box.setText(R.string.tap_scan);
                    bt_command.setText(R.string.start_scan);
                    Toast.makeText(getApplicationContext(), "Device Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                bt_command.setText(getString(R.string.start_scan));
                msg_box.setText(getString(R.string.tap_scan));
                device_list.setVisibility(View.GONE);

                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bt_command.setText(getString(R.string.calib));
                bt_disconnect.setVisibility(View.VISIBLE);
                msg_box.setText(getString(R.string.tap_calib));

                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                targetBle = new BTOperation(getApplicationContext(), bleDevice,
                                "0000ff00-0000-1000-8000-00805f9b34fb",
                                 "0000ff02-0000-1000-8000-00805f9b34fb",
                                "0000ff01-0000-1000-8000-00805f9b34fb");
                targetBle.BT_notify();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                msg_box.setText(R.string.tap_scan);
                device_list.setVisibility(View.GONE);
                bt_command.setText(getString(R.string.start_scan));
                bt_disconnect.setVisibility(View.GONE);

                progressDialog.dismiss();
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                }
                targetBle.BT_stop_notify();
            }
        });
    }




    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }
}
