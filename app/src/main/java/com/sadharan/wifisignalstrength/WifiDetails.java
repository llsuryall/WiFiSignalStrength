package com.sadharan.wifisignalstrength;

import android.net.wifi.ScanResult;

import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;

public class WifiDetails extends ViewModel {
    List<ScanResult> scanResults=null;
    Date lastScanned =null;
}
