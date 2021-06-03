package com.example.bluetoothcontrolledcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_DISCOVER_BLUETOOTH = 1;
    private Button onoffButton, discoverableButton, pairedButton;
    private ListView pairedDevicesList;
    private ImageView bluetoothImage;
    private final ArrayList<BluetoothDevice> arrayListDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    public static final String TAG = "[BluetoothCar]";
    public static final String MY_UUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    public static final String NAME = "BluetoothCarController";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                bluetoothImage.setImageResource(R.drawable.ic_action_on);
                Toast.makeText(this, "Bluetooth is ON.", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(this, "Can't open bluetooth.", Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_DISCOVER_BLUETOOTH) {
            if (resultCode == RESULT_OK) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);
        onoffButton = findViewById(R.id.buttonOnOff);
        discoverableButton = findViewById(R.id.buttonDiscoverable);
        pairedButton = findViewById(R.id.buttonPairedDev);

        pairedDevicesList = findViewById(R.id.pairedDevList);
        bluetoothImage = findViewById(R.id.bluetoothImage);

        // adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.isEnabled()) {
            bluetoothImage.setImageResource(R.drawable.ic_action_on);
            onoffButton.setText(R.string.bluetoothOFF);
        } else {
            bluetoothImage.setImageResource(R.drawable.ic_action_off);
            onoffButton.setText(R.string.bluetoothON);
        }
    }

    public void openController(View view) {
        if (bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(this, ControllerActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Turn on bluetooth first!", Toast.LENGTH_LONG).show();
        }
    }

    public void powerOnOffBluetooth(View view) {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothImage.setImageResource(R.drawable.ic_action_off);
            Toast.makeText(this, "Turning off bluetooth...", Toast.LENGTH_LONG).show();
            onoffButton.setText(R.string.bluetoothON);

            bluetoothAdapter.disable();
        } else {
            bluetoothImage.setImageResource(R.drawable.ic_action_on);
            Toast.makeText(this, "Turning on bluetooth...", Toast.LENGTH_LONG).show();
            onoffButton.setText(R.string.bluetoothOFF);

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public void findDiscoverableDevs(View view) {
        if (!bluetoothAdapter.isDiscovering()) {
            Toast.makeText(this, "Your device is discoverable.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(intent, REQUEST_DISCOVER_BLUETOOTH);
        }
    }

    public void exitApp(View view) {
        MyAlertDialog myAlertDialog = new MyAlertDialog(this);
        myAlertDialog.show(getSupportFragmentManager(), "[EXIT]");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getPairedDevices(View view) {
        if (bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            final ArrayList<BluetoothDevice> arrayListDevices = new ArrayList<>(pairedDevices);
            final ArrayAdapter<BluetoothDevice> devices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListDevices);

            if (!pairedDevices.isEmpty()) {
                pairedDevicesList.setAdapter(devices);
            } else
                Toast.makeText(this, "No paired devices...", Toast.LENGTH_LONG).show();

            for (BluetoothDevice b : pairedDevices)
                Log.e("DEVICE-BLTH", "Device: " + b.getName() + " | Address: " + b.getAddress());

            pairedDevicesList.setOnItemClickListener((adapterView, view1, i, l) -> {
                BluetoothDevice device = arrayListDevices.get(i);
                String address = device.getAddress();
                Toast.makeText(MainActivity.this,"Device address: " + address, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, ControllerActivity.class);
                intent.putExtra("MAC-Address", address);
                startActivity(intent);
            });
        } else
            Toast.makeText(this, "Turn on bluetooth first!", Toast.LENGTH_LONG).show();
    }
}