package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CreateMap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_map);
    }

    public void onCreateMapButtonClick(View view){
        Intent intent = new Intent(this, Mapping.class);
        EditText editText= findViewById(R.id.MapNameEditText);
        String mapName = editText.getText().toString();
        ArrayList<String> mapList = mapsList();

        if(!mapList.contains(mapName)) {
            File nodeFile = new File(getApplicationContext().getFilesDir(), mapName + Helper.Nodes);
            try {
                if (!nodeFile.createNewFile()) {
                    Toast.makeText(getApplicationContext(), "Can't create File", Toast.LENGTH_SHORT).show();
                }
                intent.putExtra(Helper.EdgesMapName, mapName + Helper.Edges);
                File edgesFile = new File(getApplicationContext().getFilesDir(), mapName + Helper.Edges);
                try {
                    if (!edgesFile.createNewFile()) {
                        Toast.makeText(getApplicationContext(), "Can't create File", Toast.LENGTH_SHORT).show();
                    }
                    intent.putExtra(Helper.NodesMapName, mapName + Helper.Nodes);
                    try {
                        File mapFile = new File(getApplicationContext().getFilesDir(), Helper.MapNamesFileList);
                        FileWriter fileWriter = new FileWriter(mapFile, true);
                        BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
                        if(mapList.isEmpty())
                            fileWriter.append(mapName);
                        else
                            fileWriter.append("\n").append(mapName);
                        bufferFileWriter.close();
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Map couldn't be added", Toast.LENGTH_LONG).show();
                    }
                    startActivity(intent);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    Toast.makeText(getApplicationContext(), "File couldn't be created", Toast.LENGTH_LONG).show();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                Toast.makeText(getApplicationContext(), "File couldn't be created", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Map Already Exists",Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> mapsList() {
        ArrayList<String> maps = new ArrayList<>();
        File mapFile = new File(getApplicationContext().getFilesDir(),Helper.MapNamesFileList);
        FileInputStream fis = null;
        try {
            fis = getApplication().openFileInput(Helper.MapNamesFileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fis == null){
            return maps;
        }
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
        return maps;
    }
}
