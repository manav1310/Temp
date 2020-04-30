package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class JoinNode extends AppCompatActivity {

    private boolean isSourceNodeSelected = false;
    private boolean isDestinationNodeSelected = false;
    private String sourceNode;
    private String destinationNode;
    private int magnetometerReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_node);
    }

    @Override
    public void onResume(){
        List<String> nodes = new ArrayList<>();
        nodes.add(0,"Select");
        for( Node node: Graph.getInstance().getNodes()){
            if(!node.getBreadcrumb()) {
                nodes.add(node.getNodeName());
            }
        }
        Spinner firstNodeSpinner = findViewById(R.id.StartNodeSpinner);
        final Spinner secondNodeSpinner = findViewById(R.id.DestinationNodeSpinner);

        ArrayAdapter<String> nodeListAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, nodes);
        nodeListAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        firstNodeSpinner.setAdapter(nodeListAdapter);
        secondNodeSpinner.setAdapter(nodeListAdapter);
        firstNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    isSourceNodeSelected = true;
                    sourceNode = parent.getAdapter().getItem(position).toString();
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    secondNodeSpinner.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode, Toast.LENGTH_LONG).show();
                }else{
                    findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
                    secondNodeSpinner.setVisibility(View.INVISIBLE);
                    isSourceNodeSelected = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        secondNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>0){
                    isDestinationNodeSelected = true;
                    destinationNode = parent.getAdapter().getItem(position).toString();
                    Toast.makeText(getApplicationContext(), "Destination Selected : " + destinationNode,Toast.LENGTH_LONG).show();
                }else{
                    isDestinationNodeSelected = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        super.onResume();
    }

    public void onButtonClick(View view){
        if(isSourceNodeSelected&&isDestinationNodeSelected){
            if(!sourceNode.equals(destinationNode)) {
                //TODO get distance
                Intent magnetometerReaderIntent = new Intent(this, MagnetometerReader.class);
                startActivityForResult(magnetometerReaderIntent,Helper.GET_MAGNETOMETER_REQUEST_CODE);
            }else{
                Toast.makeText(getApplicationContext(),"Same nodes selected as Source and Destination ",Toast.LENGTH_LONG).show();
            }
        }else if(isDestinationNodeSelected){
            Toast.makeText(getApplicationContext(),"Source Not Selected",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"Destination Not Selected",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Helper.GET_MAGNETOMETER_REQUEST_CODE) {
            if(resultCode==RESULT_OK) {
                magnetometerReading = data.getIntExtra(Helper.Magnetometer, -1);
                if (magnetometerReading != (-1)) {
                    saveEdge();
                }
            }
        }
    }
    private void saveEdge() {
        Graph.getInstance().addEdges(new Edge(sourceNode, destinationNode, 5,magnetometerReading));
        Graph.getInstance().addEdges(new Edge(destinationNode, sourceNode, 5,magnetometerReading));
        setResult(RESULT_OK);
        finish();
    }
}
