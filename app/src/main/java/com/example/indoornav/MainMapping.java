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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MainMapping extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int selected=0;
    private List<String> maps;
    private String mapName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mapping);
    }

    @Override
    public void onResume() {
        maps = new ArrayList<>();
        File mapFile = new File(getApplicationContext().getFilesDir(),Helper.MapNamesFileList);
        try {
            mapFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            }
        }
        Collections.sort(maps);
        maps.add(0,"Select");
        Spinner spinner = findViewById(R.id.LoadMapSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, maps);
        dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
        super.onResume();
    }

    public void onLoadMapButtonClick(View view){
        if(selected!=0) {
            Intent intent = new Intent(this, Mapping.class);
            intent.putExtra(Helper.EdgesMapName, mapName+Helper.Edges);
            intent.putExtra(Helper.NodesMapName, mapName+Helper.Nodes);
            startActivity(intent);
        }else{

            Toast.makeText(getApplicationContext(), "Select a Map ",Toast.LENGTH_SHORT).show();
        }
    }

    public void onCreateMapButtonClick(View view){
        Intent intent = new Intent(this, CreateMap.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selected = position;
        mapName = maps.get(selected);
        if (selected != 0) {
            Toast.makeText(getApplicationContext(), "Selected : " + mapName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
