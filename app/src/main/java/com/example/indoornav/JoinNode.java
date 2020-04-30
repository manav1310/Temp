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
    private float magnetomerReading;

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
        Spinner firstNodeSpinner = (Spinner) findViewById(R.id.StartNodeSpinner);
        Spinner secondNodeSpinner = (Spinner) findViewById(R.id.DestinationNodeSpinner);

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
                    Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode, Toast.LENGTH_LONG).show();
                }else{
                    isSourceNodeSelected = false;
                    Toast.makeText(getApplicationContext(),"Select Source", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Select Destination", Toast.LENGTH_SHORT).show();
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
                Intent magnetometerReaderIntent = new Intent(this, MagnetometerReader.class);
                startActivityForResult(magnetometerReaderIntent,Helper.GET_MAGNETOMETER_REQUEST_CODE);
                //TODO get distance
                //TODO create and add edge
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
            magnetomerReading = data.getFloatExtra(Helper.Magnetometer,-1);
            saveEdge();
        }
    }
    private void saveEdge() {

        Intent intent = new Intent();
        //Temporary statements for testing, Remove after implementing method for add edge
        Graph.getInstance().addEdges(new Edge(sourceNode, destinationNode, 5));
        Graph.getInstance().addEdges(new Edge(destinationNode, sourceNode, 5));
        setResult(RESULT_OK);
        finish();
    }
}
