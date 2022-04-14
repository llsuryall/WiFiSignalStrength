package com.sadharan.wifi_signal_strength;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout apList;
    private WifiDetails wifiDetails;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    private final ActivityResultLauncher<String> requestPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted->{
        if(!isGranted){
            System.exit(-1);
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Handle Permissions
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)){
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //Get managers
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        //Get handles to views
        Button scanButton=findViewById(R.id.scanButton);
        this.apList=findViewById(R.id.apList);
        //Set eventListeners
        scanButton.setOnClickListener(this);
        //Update initial results
        this.wifiDetails= new ViewModelProvider(this).get(WifiDetails.class);
        this.updateAPList();
        this.updateLastScanTime();
    }
    public void updateAPList(){
        List<ScanResult> scanResults = this.wifiDetails.getScanResults();
        if(scanResults==null){
            return;
        }
        LayoutInflater layoutInflater = getLayoutInflater();
        this.apList.removeAllViews();
        if(scanResults.size()==0){
            layoutInflater.inflate(R.layout.no_ap_available,apList,true);
        }else{
            HorizontalScrollView cur_hsv;
            TextView cur_tv;
            for(ScanResult scanResult:scanResults){
                cur_hsv=((HorizontalScrollView) layoutInflater.inflate(R.layout.ap_detail,apList,false));
                cur_tv=(TextView)cur_hsv.getChildAt(0);
                cur_tv.setText(getString(R.string.ap_detail,scanResult.BSSID,scanResult.level,scanResult.SSID));
                apList.addView(cur_hsv);
            }
        }
    }
    public void updateLastScanTime(){
        Date lastScanned=this.wifiDetails.getLastScanned();
        if(lastScanned!=null) {
            ((TextView) findViewById(R.id.lastUpdatedTime)).setText(
                    getString(
                            R.string.last_updated,
                            SimpleDateFormat.getTimeInstance().format(lastScanned)
                    )
            );
        }
    }
    public void onClick(View view) {
        //Check if wifi is on!
        if(!this.wifiManager.isWifiEnabled()){
            Toast.makeText(getApplicationContext(),getString(R.string.turn_on_wifi), Toast.LENGTH_SHORT).show();
            return;
        }
        //Check if location is on!
        if(!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(),getString(R.string.turn_on_location), Toast.LENGTH_SHORT).show();
            return;
        }
        //Start scanning
        if(wifiDetails.scanWifi(wifiManager)) {
            this.updateLastScanTime();
            this.updateAPList();
        }
    }
}