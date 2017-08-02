package com.example.emily.beaconside;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.powenko.ifroglab_bt_lib.*;

import java.util.ArrayList;
public class SearchDevice extends AppCompatActivity implements ifrog.ifrogCallBack{
    //	private EditText editText1;
    private ListView listView1;

    /* 運用library */
    private ifrog mifrog;
    ArrayList<String> Names = new ArrayList<String>();
    ArrayList<String> Address = new ArrayList<String>();




    /* 若沒有開啟藍芽，預設畫面 */
    String[] testValues= new String[]{	"Beacon1","Beacon2","Beacon3","Beacon4"};
    String[] testValues2= new String[]{	"12","34","56","78"};

    private rowdata adapter;
    /* 藍芽 */
    final int REQUEST_ENABLE_BT = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_device);

        /* DeviceList */
        listView1=(ListView) findViewById(R.id.beaconList);   //取得listView1

        /* bluetooth */

        BTinit();

    }//end onCreate


//    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//        mystatus = isChecked;
//        if (isChecked) {// The toggle is enabled
//            checkBTopen();
//        }else{
//            mifrog.scanLeDevice(isChecked,3600000);
//        }
//
//    }



    public void BTinit(){//藍芽初始化動作
        mifrog=new ifrog();
        mifrog.setTheListener(this);//設定監聽->CallBack(當有什麼反應會有callback的動作)->新增SearchFindDevicestatus, onDestroy

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {//要求開啟藍芽的視窗
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            mifrog.scanLeDevice(true,3600000);
        }

        //取得藍牙service，並把這個service交給此有藍芽的設備(BLE)。有些人有藍芽的設備不見得有藍芽的軟體。// Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mifrog.InitCheckBT(bluetoothManager) == null) {
            Toast.makeText(this,"this Device doesn't support Bluetooth BLE", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void SetupList() {
        adapter = new rowdata(this, testValues, testValues2);//顯示的方式
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() { //選項按下反應
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = testValues[position];      //哪一個列表
                Toast.makeText(SearchDevice.this, item + " selected", Toast.LENGTH_LONG).show(); //顯示訊號
            }
        });
    }

    @Override
    public void BTSearchFindDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String t_address= device.getAddress();//有找到裝置的話先抓Address

        int index=0;
        boolean t_NewDevice=true;
        for(int i=0;i<Address.size();i++){
            String t_Address2=Address.get(i);
            if(t_Address2.compareTo(t_address)==0){//如果address和列表中的address一模一樣
                t_NewDevice=false;//登記說他不是新的device
                index=i;//把index記起來
                break;
            }
        }
        if(device.getName() != null){

            if(t_NewDevice==true){//如果是新的device
                Address.add(t_address);
                Names.add(device.getName());//+" RSSI="+Integer.toString(rssi)+" d="+calculateDistance(rssi)+"cm"+" myD ="+Float.toString(turntoTarget));//抓名字然後放進列表

                testValues = Names.toArray(new String[Names.size()]);
                testValues2 =Address.toArray(new String[Address.size()]);
            }else{//如果不是新的device
                Names.set(index,device.getName());//+" RSSI="+Integer.toString(rssi)+" d="+calculateDistance(rssi)+"cm"+" myD ="+Float.toString(turntoTarget));//更改device名字，RSSI:藍芽4.0裡面可以知道訊號強度
                testValues = Names.toArray(new String[Names.size()]);//放進array
            }
        }
        SetupList();//更新畫面
    }

    @Override
    public void BTSearchFindDevicestatus(boolean arg0) {//arg0:true/false，代表有沒有在找
        if(arg0==false){
            Toast.makeText(getBaseContext(),"Stop Search", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(),"Start Search",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {//當程式離開了就把service關掉，不然service一直跑會浪費電。
        super.onDestroy();
        mifrog.BTSearchStop();
    }
}