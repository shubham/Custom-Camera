package com.example.shubham.mycamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MAINACTIVITY" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCameraPermission();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.e(TAG, "widthPixels: " + metrics.widthPixels + " -- metrics: " + metrics.heightPixels);
        //adding camera fragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new CameraFragment())
                .commit();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//if u denny permission and open next time this will show
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Camera permission necessary");
                alertBuilder.setMessage("You need to give permission to take cooler picture");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Checking the request code of our request
        if (requestCode == 1) {
            // //If permission is granted
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Displaying a toast
                    Toast.makeText(this, "camera permission granted now you can take picture", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "you can not take picture", Toast.LENGTH_LONG).show();
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "storage permission granted now you can save picture", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "you can not save picture", Toast.LENGTH_LONG).show();
                }
            } else {
                //Displaying another toast if permission is not granted
                Log.e("TAG", "back to home screen");
            }
        }
    }
}