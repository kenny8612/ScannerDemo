package com.example.scannerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ScrollView;


import com.example.scannerdemo.databinding.ScannerMainBinding;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class BroadCastActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ScannerMainBinding binding;
    private ScanReader mScanner;
    private final StringBuffer stringBuffer = new StringBuffer();
    private int receiveCount;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.scanner_main);
        binding.btScan.setOnClickListener(this);
        binding.btScan.setEnabled(true);
        binding.btClear.setOnClickListener(this);
        binding.swContinueScan.setOnCheckedChangeListener(this);
        binding.swContinueScan.setEnabled(true);
        binding.scanResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ScanReader.ACTION_SCAN_RESULT);
        registerReceiver(resultReceiver, filter);

        mScanner = new ScanReader(getApplicationContext());
        mScanner.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mScanner.stopScan();
        mScanner.closeScan();
        unregisterReceiver(resultReceiver);
    }

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] barcodeArray = intent.getByteArrayExtra(ScanReader.SCAN_RESULT);
            String barcode = new String(barcodeArray);
            Log.d("BroadCastActivity", "barcode-->"+barcode);
            stringBuffer.append(barcode);
            stringBuffer.append("\n");
            binding.scanResult.setText(stringBuffer.toString());
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            receiveCount++;
            if (receiveCount == 200) {
                stringBuffer.delete(0, stringBuffer.length());
                receiveCount = 0;
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_scan) {
            mScanner.startScan();
        } else if (view.getId() == R.id.bt_clear) {
            stringBuffer.delete(0, stringBuffer.length());
            binding.scanResult.setText("");
            receiveCount = 0;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mScanner.startScan();
                    }
                }, 10, 200);
            }
        } else {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }


    public static class ScanReader {

        public static final String ACTION_SCAN_RESULT = "com.action.SCAN_RESULT";

        public static final String SCAN_RESULT = "scanContext";
        public static final String ACTION_START_SCAN = "com.action.START_SCAN";
        public static final String ACTION_STOP_SCAN = "com.action.STOP_SCAN";
        public static final String ACTION_INIT = "com.action.INIT_SCAN";
        public static final String ACTION_KILL = "com.action.KILL_SCAN";

        private final Context context;

        public ScanReader(Context context) {
            this.context = context;
        }

        public void init() {
            Intent intent = new Intent();
            intent.setAction(ACTION_INIT);
            context.sendBroadcast(intent);
        }

        public void closeScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_KILL);
            context.sendBroadcast(intent);
        }

        public void startScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_START_SCAN);
            context.sendBroadcast(intent);
        }

        public void stopScan() {
            Intent intent = new Intent();
            intent.setAction(ACTION_STOP_SCAN);
            context.sendBroadcast(intent);
        }
    }
}
