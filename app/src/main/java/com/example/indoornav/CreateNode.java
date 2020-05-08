package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateNode extends AppCompatActivity implements ProceedFragment.NoticeDialogListener, SelectRSSIFragment.NoticeDialogListener {

    private Boolean isBreadCrumb = false;
    private String nodeName;
    private String nodeDescription;
    private Map<String, Integer> RSSIDataMap = null;
    private Map<String, String> RSSISSIDs = null;
    private Boolean isRSSIDataSet = false;
    boolean flag = false;
    private SelectRSSIFragment dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_node);
        ArrayList<String> isBreadCrumbChoices = new ArrayList<>();
        isBreadCrumbChoices.add("No");
        isBreadCrumbChoices.add("Yes");
        Spinner spinner = findViewById(R.id.isBreadCrumbSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, isBreadCrumbChoices);
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isBreadCrumb = position == 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        startService(new Intent(this, RSSICalculator.class));
        registerReceiver(RSSIBroadcastReceiver, new IntentFilter(Helper.RSSICalculator_Broadcast));
    }

    @Override
    protected void onResume(){
        startService(new Intent(this, RSSICalculator.class));
        registerReceiver(RSSIBroadcastReceiver, new IntentFilter(Helper.RSSICalculator_Broadcast));
        super.onResume();
    }

    @Override
    protected void onPause(){
        stopService(new Intent(this, RSSICalculator.class));
        unregisterReceiver(RSSIBroadcastReceiver);
        super.onPause();
    }

    public void onButtonClick(View view) {
        flag = false;
        nodeName = ((EditText) findViewById(R.id.NodeNameEditText)).getText().toString();
        nodeDescription = ((EditText) findViewById(R.id.NodeDescriptionEditText)).getText().toString();

        Graph graph = Graph.getInstance();
        for (Node node : graph.getNodes()) {
            if (nodeName.equals(node.getNodeName())) {
                flag = true;
                break;
            }
        }
        if(flag)
            Toast.makeText(getApplicationContext(), "Node Already Exists", Toast.LENGTH_SHORT).show();
        else if(isRSSIDataSet){
            Toast.makeText(getApplicationContext(), "Wifi found : " + RSSISSIDs.size()+" "+RSSIDataMap.size(),Toast.LENGTH_SHORT).show();
            dialog = new SelectRSSIFragment(RSSISSIDs,"Choose SSIDs for storing their RSSI values");
            dialog.show(getSupportFragmentManager(), "Choose SSIDs for storing their RSSI");
        }
        else{
            ProceedFragment dialog = new ProceedFragment("RSSI data not available.\nProceed without RSSI data.");
            dialog.show(getSupportFragmentManager(), "Proceed without RSSI");
        }
    }

    private void saveNode() {
        Node node = new Node(nodeName, nodeDescription, RSSIDataMap, isBreadCrumb);
        Graph.getInstance().addNodes(node);
        Toast.makeText(getApplicationContext(), "Node Created Successfully", Toast.LENGTH_SHORT).show();
        exitActivity();
    }

    private void exitActivity(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private BroadcastReceiver RSSIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getRSSIData(intent);
        }
    };

    private void getRSSIData(Intent intent){
        Bundle extras = intent.getExtras();
        assert extras != null;
        if((System.currentTimeMillis() - extras.getLong(Helper.TimeStamp))/1000 < 10) {
            RSSISSIDs = new HashMap<>();
            RSSIDataMap = new HashMap<>();
            int[] RSSI = extras.getIntArray(Helper.RSSI);
            String[] BSSID = extras.getStringArray(Helper.BSSID);
            String[] SSID = extras.getStringArray(Helper.SSID);
            assert RSSI != null;
            for (int i = 0; i < RSSI.length; i++) {
                assert BSSID != null;
                assert SSID != null;
                RSSISSIDs.put(BSSID[i], SSID[i]);
                RSSIDataMap.put(BSSID[i], RSSI[i]);
            }
            isRSSIDataSet = true;
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        saveNode();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        exitActivity();
    }

    @Override
    public void onRSSIDialogPositiveClick(DialogFragment dialog) {
        ArrayList<String> BSSIDs = this.dialog.getBSSIDs();
        Map<String, Integer> newRSSIData = new HashMap<>();
        for (String BSSID : BSSIDs) {
            newRSSIData.put(BSSID, RSSIDataMap.get(BSSID));
        }
        RSSIDataMap = newRSSIData;
        if (RSSIDataMap.isEmpty()) {
            ProceedFragment proceedFragment = new ProceedFragment("Proceed without RSSI data.");
            proceedFragment.show(getSupportFragmentManager(), "Proceed without RSSI");
        } else {
            saveNode();
        }
    }

    @Override
    public void onRSSIDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}
