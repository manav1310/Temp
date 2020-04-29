package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class Mapping extends AppCompatActivity {

    private Graph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
    }

    @Override
    protected void onResume() {
        String NodesFileName = getIntent().getStringExtra(Helper.NodesMapName);
        String EdgesFileName = getIntent().getStringExtra(Helper.EdgesMapName);

        String EdgesFileContent = getFileContents(EdgesFileName);
        String NodesFileContent = getFileContents(NodesFileName);

        graph = Graph.getInstance(NodesFileName, NodesFileContent, EdgesFileContent);
        Toast.makeText(getApplicationContext(), "Total Nodes : " + graph.getNodes().size() + "\nTotal Edges : " + graph.getEdges().size(), Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        Graph.destroyGraph();
        super.onDestroy();
    }

    public void onCreateNodeButtonClick(View view){
        Intent intent = new Intent(this, CreateNode.class);
        startActivityForResult(intent, Helper.CREATE_NODE_REQUEST_CODE);
    }

    public void onJoinNodesButtonClick(View view){
        Intent intent = new Intent(this, JoinNode.class);
        startActivityForResult(intent,Helper.JOIN_NODE_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void OnSaveButtonClick(View view){

        String NodesFileName = getIntent().getStringExtra(Helper.NodesMapName);
        String EdgesFileName = getIntent().getStringExtra(Helper.EdgesMapName);

        String EdgesFileContent = graph.edgesToJson().toString();
        String NodesFileContent = graph.nodesToJson().toString();

        writeFileContents(NodesFileName,NodesFileContent);
        writeFileContents(EdgesFileName,EdgesFileContent);

        Toast.makeText(getApplicationContext(), "Map saved Successfully" + "" +
                "\nTotal nodes : " + graph.getNodes().size() +
                "\nTotal edges : " + graph.getEdges().size(), Toast.LENGTH_LONG).show();
    }

    private void writeFileContents(String FileName, String FileContents){
        FileOutputStream fos = null;
        try {
            fos = getApplication().openFileOutput(FileName,MODE_PRIVATE);
        } catch (FileNotFoundException ignored) {
            ;
        }
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        try {
            outputStreamWriter.write(FileContents);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStreamWriter.close();
            } catch (IOException ignored) { }
            try {
                fos.close();
            } catch (IOException ignored) { }
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
