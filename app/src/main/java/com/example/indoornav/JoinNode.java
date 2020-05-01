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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class JoinNode extends AppCompatActivity {

    private boolean isSourceNodeSelected = false;
    private boolean isDestinationNodeSelected = false;
    private String sourceNode;
    private String destinationNode;
    private int magnetometerReading;
    private int distreading;
    private int flag1, flag2;
    private int possrc=0,posdest=0;
    public Bundle out = new Bundle();
    private Spinner firstNodeSpinner;
    private Spinner secondNodeSpinner;
    private TextView srcdesttext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_node);
        flag1=0;flag2=0;



    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Saves the data  on saved Instance as a key value pair Example As below key value pair
        outState.putString("sourcenode", sourceNode);
        outState.putString("destnode", destinationNode);
        outState.putInt("magnetometer", magnetometerReading);
        outState.putInt("dist", distreading);
        outState.putInt("src", possrc);
        outState.putInt("dest",posdest);

        // super.onSaveInstanceState(savedInstanceState);

    }
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        sourceNode = savedState.getString("sourcenode");
        destinationNode = savedState.getString("destnode");
        magnetometerReading = savedState.getInt("magnetometer");
        distreading = savedState.getInt("dist");
        possrc = savedState.getInt("src");
        posdest = savedState.getInt("dest");

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
        firstNodeSpinner = findViewById(R.id.StartNodeSpinner);
        secondNodeSpinner = findViewById(R.id.DestinationNodeSpinner);
        srcdesttext = (TextView) findViewById(R.id.textview5);

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
                    possrc =1;
                    srcdesttext.setText("Source node: "+sourceNode+"\nDestination Node:");
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    secondNodeSpinner.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode, Toast.LENGTH_LONG).show();
                }else{
                    if(posdest==0) {
                        findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
                        secondNodeSpinner.setVisibility(View.INVISIBLE);
                    }
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
                    posdest=1;
                    srcdesttext.setText("Source node: "+sourceNode+"\nDestination Node: "+destinationNode);
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
//                Intent magnetometerReaderIntent = new Intent(this, MagnetometerReader.class);
//                startActivityForResult(magnetometerReaderIntent,Helper.GET_MAGNETOMETER_REQUEST_CODE);
                //go back to a prev screen
                // if(flag1==1 && flag2==1)
                saveEdge();
            }else{
                Toast.makeText(getApplicationContext(),"Same nodes selected as Source and Destination ",Toast.LENGTH_LONG).show();
            }
        }else if(isDestinationNodeSelected){
            Toast.makeText(getApplicationContext(),"Source Not Selected",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"Destination Not Selected",Toast.LENGTH_SHORT).show();
        }
    }
    public void onButtonClick1(View view)///for direction(magnetometer reading)
    {

        onSaveInstanceState(out);
        Intent magnetometerReaderIntent = new Intent(this, MagnetometerReader.class);
        startActivityForResult(magnetometerReaderIntent,Helper.GET_MAGNETOMETER_REQUEST_CODE);

    }
    public void  onButtonClick2(View view)///for Distance(step count + distance)
    {
        onSaveInstanceState(out);
        Intent distReaderIntent = new Intent(this, DistanceCalculator.class);
        startActivityForResult(distReaderIntent,Helper.GET_DIST_REQUEST_CODE);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {


        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Helper.GET_MAGNETOMETER_REQUEST_CODE) {
            if(resultCode==RESULT_OK) {
                magnetometerReading = data.getIntExtra(Helper.Magnetometer, -1);
                onRestoreInstanceState(out);
                Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode+" and Destination Selected : "+destinationNode , Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "possrc : " + possrc+" and posdest : "+posdest , Toast.LENGTH_LONG).show();
                if (magnetometerReading != (-1)) {
                    flag1=1;
                }
            }
        }
        if(requestCode == Helper.GET_DIST_REQUEST_CODE) {
            if(resultCode==RESULT_OK) {
                distreading = data.getIntExtra(Helper.Dist, -1);
                onRestoreInstanceState(out);
                Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode+" and Destination Selected : "+destinationNode , Toast.LENGTH_LONG).show();
                if (distreading != (-1)) {
                    flag2=1;
                }
            }
        }


    }
    private void saveEdge() {
        Graph.getInstance().addEdges(new Edge(sourceNode, destinationNode, distreading,magnetometerReading));
        Graph.getInstance().addEdges(new Edge(destinationNode, sourceNode, distreading,magnetometerReading));
        setResult(RESULT_OK);
        finish();
    }
}
