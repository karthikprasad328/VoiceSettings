package com.example.kprasad.speechrecognizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    protected static final int REQUEST_OK = 1;
    static Boolean isFlashlightDisabled = true;
    static Boolean isSoundEnabled = true;
    private Camera camera;
    Camera.Parameters params;
    private boolean isFlashOn;
    private boolean hasFlash;
    private Camera mCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);

        TextView textView=(TextView)findViewById(R.id.commandText);
        textView.setText("Commands:-\n---------------\nLess Brightness\nMore Brightness\nScreen Rotation\nFlashlight\nWifi\nLocation\nBluetooth\nSound");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            voiceRecognize(thingsYouSaid);
        }
    }

    public void voiceRecognize(ArrayList<String> thingsYouSaid){
        ((TextView) findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
        if (thingsYouSaid.get(0).contains("launch")) {
            Toast.makeText(getApplicationContext(), "Trying to launch an application", Toast.LENGTH_SHORT).show();
            System.out.println(thingsYouSaid.get(0));
        } else if (thingsYouSaid.get(0).contains("less brightness")) {
            changeBrightness(thingsYouSaid.get(0), false);
        } else if (thingsYouSaid.get(0).contains("more brightness")) {
            changeBrightness(thingsYouSaid.get(0), true);
        } else if (thingsYouSaid.get(0).contains("screen rotation")) {
            setAutoOrientationEnabled(getApplicationContext());
        } else if (thingsYouSaid.get(0).contains("flashlight") || thingsYouSaid.get(0).contains("flash light")) {
            flashLightToggle();
        } else if (thingsYouSaid.get(0).contains("wifi")) {
            wifiToggle();
        } else if (thingsYouSaid.get(0).contains("location")) {
            locationServicesToggle();
        } else if (thingsYouSaid.get(0).contains("bluetooth") || thingsYouSaid.get(0).contains("blue tooth")) {
            bluetoothToggle();
        } else if (thingsYouSaid.get(0).contains("sound")) {
            soundToggle();
        } else if (thingsYouSaid.get(0).contains("lock")) {
            toggleLockScreen();
        }
    }

    public void changeBrightness(String command, boolean isIncrease) {
        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, isIncrease ? 255 : 0);
        Toast.makeText(getApplicationContext(), "changing brightness", Toast.LENGTH_SHORT).show();
        System.out.println(command);

    }

    public void setAutoOrientationEnabled(Context context) {
        if (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            Toast.makeText(MainActivity.this, "Screen Rotation OFF", Toast.LENGTH_SHORT).show();
        } else {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
            Toast.makeText(MainActivity.this, "Screen Rotation ON", Toast.LENGTH_SHORT).show();
        }

    }

    public void flashLightToggle() {
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
        }

        // get the camera
        getCamera();

        if (isFlashOn) {
            // turn off flash
            turnOffFlash();
        } else {
            // turn on flash
            turnOnFlash();
        }
    }


    // getting camera parameters
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Failed to Open: ", e.getMessage());
            }
        }
    }

    /*
 * Turning On flash
 */
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }
    }

    /*
 * Turning Off flash
 */
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        }
    }

    public void wifiToggle() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(false);
            Toast.makeText(MainActivity.this, "WIFI OFF", Toast.LENGTH_SHORT).show();
        } else {
            wifi.setWifiEnabled(true);
            Toast.makeText(MainActivity.this, "WIFI ON", Toast.LENGTH_SHORT).show();
        }
    }

    public void locationServicesToggle() {
        //you can't really enable/disable location. google doesnt allow this. this opens an intent where user will have to manually have to enable/disable location
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void bluetoothToggle() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(MainActivity.this, "Bluetooth OFF", Toast.LENGTH_SHORT).show();
        } else {
            mBluetoothAdapter.enable();
            Toast.makeText(MainActivity.this, "Bluetooth ON", Toast.LENGTH_SHORT).show();
        }
    }

    public void soundToggle() {
        AudioManager amanager;
        amanager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (isSoundEnabled) {
            //turn ringer silent
            amanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.i("RINGER_MODE_SILENT", "Set to true");

            //turn off sound, disable notifications
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            Log.i("STREAM_SYSTEM", "Set to true");
            //notifications
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            Log.i("STREAM_NOTIFICATION", "Set to true");
            //alarm
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            Log.i("STREAM_ALARM", "Set to true");
            //ringer
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            Log.i("STREAM_RING", "Set to true");
            //media
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            Log.i("STREAM_MUSIC", "Set to true");
            isSoundEnabled = false;
            Toast.makeText(MainActivity.this, "System sounds OFF", Toast.LENGTH_SHORT).show();
        } else {
            //turn ringer silent
            amanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.i(".RINGER_MODE_NORMAL", "Set to true");

            // turn on sound, enable notifications
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            Log.i("STREAM_SYSTEM", "Set to False");
            //notifications
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            Log.i("STREAM_NOTIFICATION", "Set to False");
            //alarm
            amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
            Log.i("STREAM_ALARM", "Set to False");
            //ringer
            amanager.setStreamMute(AudioManager.STREAM_RING, false);
            Log.i("STREAM_RING", "Set to False");
            //media
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            Log.i("STREAM_MUSIC", "Set to False");
            isSoundEnabled = true;
            Toast.makeText(MainActivity.this, "System sounds ON", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleLockScreen(){
        //Get the window from the context

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);



//       //Unlock
//
//        //http://developer.android.com/reference/android/app/Activity.html#getWindow()
//        Window window = getWindow();
//
//        window.addFlags(wm.LayoutParams.FLAG_DISMISS_KEYGUARD);



        //Lock device

        DevicePolicyManager mDPM;

        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        mDPM.lockNow();
    }
}
