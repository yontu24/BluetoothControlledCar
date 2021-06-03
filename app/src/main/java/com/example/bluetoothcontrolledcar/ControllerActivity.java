package com.example.bluetoothcontrolledcar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

public class ControllerActivity extends AppCompatActivity {

    private Button mainActivityButton, motorEngine, direction;
    private SeekBar motorSpeedSeekBar;
    private Switch aSwitch;
    private TextView textViewSpeed;
    private ProgressDialog progress;
    private ImageView right, left;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private String bluetoothMacAddress;

    private boolean isConnected = false;
    private boolean isEngineOn = false;
    private int directionValue = 90;  // [0, 180]
    public static final String TAG = "[ControllerActivity]";
    public static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final int OFFSET = 10;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothMacAddress = getIntent().getStringExtra("MAC-Address");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_controller);

        mainActivityButton = findViewById(R.id.buttonController);
        motorEngine = findViewById(R.id.startEngine);
        direction = findViewById(R.id.changeDirection);
        motorSpeedSeekBar = findViewById(R.id.seekBarSpeed);
        textViewSpeed = findViewById(R.id.textViewSpeed);
        right = findViewById(R.id.imageViewRight);
        left = findViewById(R.id.imageViewLeft);
        aSwitch = findViewById(R.id.switchOpticalSensors);

        right.setImageResource(R.drawable.ic_action_rigth);
        left.setImageResource(R.drawable.ic_action_left);
        motorSpeedSeekBar.getProgressDrawable().setColorFilter(Color.rgb(76, 175, 80), PorterDuff.Mode.SRC_IN);
        motorSpeedSeekBar.getThumb().setColorFilter(Color.rgb(76, 175, 80), PorterDuff.Mode.SRC_IN);

        new BluetoothConnection().execute();

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            try {
                sendCommand("S");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        right.setOnClickListener(l -> {
            directionValue = Math.min(directionValue + OFFSET, 180);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> right.setImageResource(R.drawable.ic_action_rigth_clicked), 100);
            handler.postDelayed(() -> right.setImageResource(R.drawable.ic_action_rigth), 150);

            try {
                sendCommand("Lr" + directionValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "directionValue = " + directionValue);
        });

        left.setOnClickListener(l -> {
            directionValue = Math.max(directionValue - OFFSET, 0);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> left.setImageResource(R.drawable.ic_action_left_clicked), 100);
            handler.postDelayed(() -> left.setImageResource(R.drawable.ic_action_left), 150);

            try {
                sendCommand("Lr" + directionValue);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e(TAG, "directionValue = " + directionValue);
        });

        motorEngine.setOnClickListener(l -> {
            isEngineOn = !isEngineOn;
            try {
                sendCommand("A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Engine power " + (isEngineOn ? "up." : "down."), Toast.LENGTH_LONG).show();
        });

        direction.setOnClickListener(l -> {
            try {
                sendCommand("B");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Direction changed successfully.", Toast.LENGTH_SHORT).show();
        });

        motorSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSpeed.setText("Current motor speed: " + progress + "/255");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    sendCommand("Ms" + seekBar.getProgress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void mainActivity(View view) throws IOException {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        disconnect();
    }

    private void sendCommand(String cmd) throws IOException {
        if (bluetoothSocket != null)
            bluetoothSocket.getOutputStream().write(cmd.getBytes());
    }

    private void disconnect() throws IOException {
        if (bluetoothSocket != null) {
            bluetoothSocket.close();
            bluetoothSocket = null;
            isConnected = false;
        }
        finish();
    }

    private class BluetoothConnection extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ControllerActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        // the connection is done in background
        protected Void doInBackground(Void... devices) {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(bluetoothMacAddress);
                    // create a RFCOMM (SPP) connection
                    bluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Connection Failed. Try again!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected.", Toast.LENGTH_LONG).show();
                isConnected = true;
            }
            progress.dismiss();
        }
    }
}