package com.slobodsky.nick.bluetoothscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_ACCESS_COURSE_LOCATION = 1;

    static final int REQUEST_ENABLE_BLUETOOTH = 11;

    ListView deviceListView;

    Button scanButton;

    BluetoothAdapter bluetoothAdapter;

    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.button);

        deviceListView = findViewById(R.id.list_view);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        deviceListView.setAdapter(listAdapter);

        checkBluetoothState();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
                {
                    if (checkCoarseLocationPermission())
                    {
                        listAdapter.clear();

                        bluetoothAdapter.startDiscovery();
                    }
                }
                else
                {
                    checkBluetoothState();
                }
            }
        });

        checkCoarseLocationPermission();
    }

    @Override
    protected void onResume() {

        super.onResume();

        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothAdapter
                .ACTION_DISCOVERY_STARTED));

        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothAdapter
                .ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onPause() {

        super.onPause();

        unregisterReceiver(deviceFoundReceiver);
    }

    boolean checkCoarseLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COURSE_LOCATION);

            return false;
        }
        else
        {
            return true;
        }
    }

    void checkBluetoothState()
    {
        if (bluetoothAdapter != null)
        {
            Toast.makeText(this, "Bluetooth isn't supported on your device !",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (bluetoothAdapter.isEnabled())
            {
                if (bluetoothAdapter.isDiscovering())
                {
                    Toast.makeText(this, "Device is discovering process...",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "Bluetooth is enabled.", Toast.LENGTH_SHORT)
                            .show();

                    scanButton.setEnabled(true);
                }
            }
            else
            {
                Toast.makeText(this, "You need to enable Bluetooth.",
                        Toast.LENGTH_SHORT)
                        .show();

                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableIntent, REQUEST_ACCESS_COURSE_LOCATION);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH)
        {
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_ACCESS_COURSE_LOCATION :

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Access course location allowed. " +
                                    "You can scan Bluetooth devices."
                            , Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "Access course location forbidden. " +
                                    "You can't scan Bluetooth devices.",
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                listAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                scanButton.setText("Scanning Bluetooth devices...");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                scanButton.setText("Scanning in progress...");
            }
        }
    };
}
