package com.qianhai.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> blueList = new ArrayList<>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                blueList.add(device);
                Log.e("MainActivity", "搜索到的设备名称" + device.getName());
//                System.out.println(device.getName());
            }
            //搜索完成
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                Log.e("MainActivity", "搜索完成");
                bluetoothAdapter.cancelDiscovery();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
// 注册广播接收器，接收并处理搜索结果
        registerReceiver(receiver, intentFilter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "设备不支持蓝牙", Toast.LENGTH_SHORT);
            // 设备不支持蓝牙
        }
        // 打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置蓝牙可见性，最多300秒
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);
        }

        findViewById(R.id.btn_scan_bluetooth).setOnClickListener(new View.OnClickListener() {//搜索蓝牙
            @Override
            public void onClick(View view) {
                blueList.clear();
// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
                bluetoothAdapter.startDiscovery();
            }
        });

        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {//连接蓝牙
            @Override
            public void onClick(View view) {
                // 获取蓝牙设备的连接状态
                if (blueList.size() == 0) {
                    Toast.makeText(MainActivity.this, "没有搜索到蓝牙设备", Toast.LENGTH_SHORT);
                    return;
                }
                BluetoothDevice device = blueList.get(0);
                Toast.makeText(MainActivity.this, "与" + device.getName() + "设备配对", Toast.LENGTH_SHORT);
                int connectState = device.getBondState();
                switch (connectState) {
                    // 未配对
                    case BluetoothDevice.BOND_NONE:
                        // 配对
                        try {
                            Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                            createBondMethod.invoke(device);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, "配对", Toast.LENGTH_SHORT);
                        break;
                    // 已配对
                    case BluetoothDevice.BOND_BONDED:
                        break;
                }
                try {
                    // 连接
                    connect(device);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "连接", Toast.LENGTH_SHORT);
            }
        });
    }

    private void connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
        Log.e("MainActivity", "蓝牙是否连接" + socket.isConnected());
    }
}
