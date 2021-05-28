package com.example.scannerdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ScrollView;

import com.example.scannerdemo.databinding.ScannerMainBinding;
import com.smart.sdk.OnScannerEventListener;
import com.smart.sdk.Scanner;


public class CallBackActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ScannerMainBinding binding;
    private final StringBuffer stringBuffer = new StringBuffer();
    private Scanner mScanner;
    private int receiveCount;
    private boolean bContinuousScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.scanner_main);
        binding.btScan.setOnClickListener(this);
        binding.btClear.setOnClickListener(this);
        binding.swContinueScan.setOnCheckedChangeListener(this);
        binding.scanResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        mScanner = Scanner.getInstance();
        mScanner.init(getApplicationContext(), listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanner.deInit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bContinuousScan)
            mScanner.continuousScan(false);
        else
            mScanner.scan(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanner.open();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_scan) {
            mScanner.scan(true);
        } else if (view.getId() == R.id.bt_clear) {
            stringBuffer.delete(0, stringBuffer.length());
            binding.scanResult.setText("");
            receiveCount = 0;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mScanner.continuousScan(b);
        bContinuousScan = b;
    }

    private final OnScannerEventListener listener = new OnScannerEventListener() {
        @Override
        public void onScannerOpen() {
            mScanner.enableSound(true);
            mScanner.enableVibrator(true);
            binding.swContinueScan.setEnabled(true);
            binding.btScan.setEnabled(true);
            if(bContinuousScan)
                mScanner.continuousScan(true);
        }

        @Override
        public void onScannerClose() {
            binding.swContinueScan.setEnabled(false);
            binding.btScan.setEnabled(false);
        }

        @Override
        public void onScannerError(int i) {
            binding.swContinueScan.setEnabled(false);
            binding.btScan.setEnabled(false);
        }

        @Override
        public void onBarcodeEvent(String s) {
            stringBuffer.append(s);
            stringBuffer.append("\n");
            binding.scanResult.setText(stringBuffer.toString());
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            receiveCount++;
            if (receiveCount == 200) {
                stringBuffer.delete(0, stringBuffer.length());
                receiveCount = 0;
            }
        }

        @Override
        public void onDecodeTimeout() {

        }
    };
}