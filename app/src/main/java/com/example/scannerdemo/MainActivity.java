package com.example.scannerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.example.scannerdemo.databinding.ActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACTION_SCAN_RESULT = "com.action.SCAN_RESULT";
    public static final String ACTION_SCAN_STATE = "com.action.SCAN_STATE";

    private ActivityMainBinding binding;
    private ScanDevice mScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btScanOpen.setOnClickListener(this);
        binding.btScanClose.setOnClickListener(this);
        binding.btStartDecode.setOnClickListener(this);
        binding.btStopDecode.setOnClickListener(this);
        binding.btStartContinueDecode.setOnClickListener(this);
        binding.btStopContinueDecode.setOnClickListener(this);

        binding.scanOutMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mScanner.setOutScanMode(b ? 1 : 0);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_RESULT);
        filter.addAction(ACTION_SCAN_STATE);
        registerReceiver(resultReceiver, filter);

        mScanner = new ScanDevice();


        //getOutScanMode接口旧版本系统会调用失败，若运行demo出现崩溃，请屏蔽此接口
        if (mScanner.getOutScanMode() != 0)
            mScanner.setOutScanMode(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(resultReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanner.stopScan();
        mScanner.stopContinuousScan();
    }

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SCAN_RESULT.equals(action)) {
                byte[] barcodeArray = intent.getByteArrayExtra("scanContext");
                String barcode = new String(barcodeArray, 0, barcodeArray.length);
                binding.scanResult.append(getString(R.string.broadcast_output));
                binding.scanResult.append(barcode);
                binding.scanResult.append("\n");
            }
            //此广播旧版本的系统会接收不到，请手动使能相关button
            else if (ACTION_SCAN_STATE.equals(action)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 0) {//close
                    binding.btStartDecode.setEnabled(false);
                    binding.btStopDecode.setEnabled(false);
                    binding.btStartContinueDecode.setEnabled(false);
                    binding.btStopContinueDecode.setEnabled(false);
                } else if (state == 1) {//open
                    binding.btStartDecode.setEnabled(true);
                    binding.btStopDecode.setEnabled(true);
                    binding.btStartContinueDecode.setEnabled(true);
                    binding.btStopContinueDecode.setEnabled(true);
                } else if (state == 2) {//decode timeout

                } else if (state == -1) {//error
                    binding.btStartDecode.setEnabled(false);
                    binding.btStopDecode.setEnabled(false);
                    binding.btStartContinueDecode.setEnabled(false);
                    binding.btStopContinueDecode.setEnabled(false);
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.bt_scan_open) {
            mScanner.openScan();
        } else if (id == R.id.bt_scan_close) {
            mScanner.closeScan();
        } else if (id == R.id.bt_start_decode) {
            mScanner.startScan();
        } else if (id == R.id.bt_stop_decode) {
            mScanner.stopScan();
        } else if (id == R.id.bt_start_continue_decode) {
            mScanner.startContinuousScan();
        } else if (id == R.id.bt_stop_continue_decode) {
            mScanner.stopContinuousScan();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.scanner_settings) {
            Intent intent = new Intent("android.settings.SCANNER_SETTINGS");
            startActivity(intent);
        } else if (item.getItemId() == R.id.result_clear) {
            binding.scanResult.setText("");
        }
        return super.onOptionsItemSelected(item);
    }
}
