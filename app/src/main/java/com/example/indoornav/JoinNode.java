package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
    private String destinationdNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_node);
    }

    @Override
    public void onResume(){
        List<String> nodes = new ArrayList<>();
        nodes.add(0,"");
        for( Node node: Graph.getInstance().getNodes()){
            nodes.add(node.getNodeName());
        }
        Spinner firstNodeSpinner = (Spinner) findViewById(R.id.StartNodeSpinner);
        Spinner secondNodeSpinner = (Spinner) findViewById(R.id.DestinationNodeSpinner);

        ArrayAdapter<String> nodeListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nodes);
        nodeListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstNodeSpinner.setAdapter(nodeListAdapter);
        secondNodeSpinner.setAdapter(nodeListAdapter);
        firstNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isSourceNodeSelected = true;
                sourceNode = parent.getAdapter().getItem(position).toString();
                Toast.makeText(getApplicationContext(), "selected : " + sourceNode,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        secondNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isDestinationNodeSelected = true;
                destinationdNode = parent.getAdapter().getItem(position).toString();
                Toast.makeText(getApplicationContext(), "selected : " + destinationdNode,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        super.onResume();
    }

    public void onButtonClick(View view){
        if(isSourceNodeSelected&&isDestinationNodeSelected){
            if(!sourceNode.equals(destinationdNode)) {
                //TODO add distance and magnetometer reading
                //TODO create add edge to graph
                Intent intent = new Intent();
                setResult(RESULT_OK);
                finish();
            }else{
                Toast.makeText(getApplicationContext(),"Same nodes selected as Source and Destination ",Toast.LENGTH_LONG).show();
            }
        }
    }

}
