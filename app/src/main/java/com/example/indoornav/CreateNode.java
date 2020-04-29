package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class CreateNode extends AppCompatActivity implements ProceedFragment.NoticeDialogListener {

    private String nodeName;
    private String nodeDescription;
    private Map<String, Integer> RSSIDataMap = null;
    private Boolean isRSSIDataSet = false;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_node);
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
            saveNode();
        }
        else{
            ProceedFragment dialog = new ProceedFragment("RSSI data not available.\nProceed without RSSI data.");
            dialog.show(getSupportFragmentManager(), "Proceed without RSSI");
        }
    }

    private void saveNode() {
        Node node = new Node(nodeName, nodeDescription, RSSIDataMap);
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
        if((System.currentTimeMillis() - extras.getLong(Helper.TimeStamp))/1000 < 10) {
            RSSIDataMap = new HashMap<>();
            int[] RSSI = extras.getIntArray("RSSI");
            String[] BSSID = extras.getStringArray("BSSID");
            for (int i = 0; i < RSSI.length; i++) {
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

}
