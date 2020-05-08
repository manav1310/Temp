package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainNavigation extends AppCompatActivity {

    private ArrayList<String> maps;
    private Boolean isMapSet = false;
    private Graph graph = null;
    private String mapname;
    private String height;
    private String sourceNodeName;
    private String destinationNodeName;
    private Boolean isSourceNameSelected = false;
    private Boolean isDestinationNameSelected = false;
    private Spinner sourceNodeSpinner;
    private Spinner destinationNodeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
    }

    @Override
    protected void onResume(){
        maps = new ArrayList<>();
        maps.add("Select");

        FileInputStream fis = null;
        try {
            fis = getApplication().openFileInput(Helper.MapNamesFileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fis != null;
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                maps.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ignored) {
                ;
            }
        }
        sourceNodeSpinner = findViewById(R.id.NavSourceNodeSpinner);
        destinationNodeSpinner = findViewById(R.id.NavDestNodeSpinner);

        Spinner spinner = findViewById(R.id.NavigationLoadMapSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, maps);
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) {
                    String mapName = maps.get(position);
                    mapname = mapName;
                    String EdgesFileContent = getFileContents(mapName + Helper.Edges);
                    String NodesFileContent = getFileContents(mapName + Helper.Nodes);
                    graph = Graph.getInstance(mapName, NodesFileContent, EdgesFileContent);
                    isMapSet = true;
                    updateNodesSpinner();
                    findViewById(R.id.NavChSrcTextView).setVisibility(View.VISIBLE);
                    sourceNodeSpinner.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Selected : " + mapName, Toast.LENGTH_SHORT).show();
                }else{
                    sourceNodeSpinner.setVisibility(View.INVISIBLE);
                    findViewById(R.id.NavChSrcTextView).setVisibility(View.INVISIBLE);
                    destinationNodeSpinner.setVisibility(View.INVISIBLE);
                    findViewById(R.id.NavChDestTextView).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        super.onResume();
    }

    private void updateNodesSpinner(){
        final ArrayList<String> nodesNames = new ArrayList<>();
        nodesNames.add("Select");
        for(Node node : graph.getNodes()){
            if(!node.getBreadcrumb()) {
                nodesNames.add(node.getNodeName());
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice,nodesNames);
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sourceNodeSpinner.setAdapter(dataAdapter);
        sourceNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) {
                    if (isMapSet) {
                        sourceNodeName = nodesNames.get(position);
                        isSourceNameSelected = true;
                        findViewById(R.id.NavChDestTextView).setVisibility(View.VISIBLE);
                        destinationNodeSpinner.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Selected : " + sourceNodeName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Select Map First", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    destinationNodeSpinner.setVisibility(View.INVISIBLE);
                    findViewById(R.id.NavChDestTextView).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        destinationNodeSpinner.setAdapter(dataAdapter);
        destinationNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) {
                    if (isMapSet) {
                        destinationNodeName = nodesNames.get(position);
                        isDestinationNameSelected = true;
                        Toast.makeText(getApplicationContext(), "Selected : " + destinationNodeName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Select Map First", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onSendButtonClick(View view){
        if(isSourceNameSelected && isDestinationNameSelected && !sourceNodeName.equals(destinationNodeName)) {
            //TextView heightip = findViewById(R.id.heightip);
            //height = heightip.getText().toString();
            height = "170";

            Intent intent = new Intent(this, Navigation.class);
            intent.putExtra(Helper.SourceNodeName, sourceNodeName);
            intent.putExtra(Helper.DestinationNodeName, destinationNodeName);
            intent.putExtra(Helper.mapname, mapname);
            intent.putExtra(Helper.Height, height);
            startActivity(intent);
        }else if(!isSourceNameSelected){
            Toast.makeText(getApplicationContext(), "Select Source Node",Toast.LENGTH_SHORT).show();
        }else if(!isDestinationNameSelected){
            Toast.makeText(getApplicationContext(),"Select Destination Node", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Source and Destination are same", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileContents(String fileName){
        FileInputStream fis = null;
        try {
            fis = getApplication().openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fis != null;
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ignored) {
                ;
            }
        }
        return stringBuilder.toString();
    }
}
