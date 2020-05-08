package com.example.indoornav;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Map;

public class SelectRSSIFragment extends DialogFragment{

    private ArrayList<String> BSSIDs;
    private Map<String, String> RSSISSIDs;
    private String message;
    public interface NoticeDialogListener {
        void onRSSIDialogPositiveClick(DialogFragment dialog);
        void onRSSIDialogNegativeClick(DialogFragment dialog);
    }

    SelectRSSIFragment.NoticeDialogListener listener;

    SelectRSSIFragment(Map<String,String> RSSISSIDs, String message){
        this.message = message;
        this.RSSISSIDs = RSSISSIDs;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (SelectRSSIFragment.NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException( " Calling classs must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> SSIDs = new ArrayList<>();
        final ArrayList<String> BSSIDs = new ArrayList<>(RSSISSIDs.keySet());
        final ArrayList<Boolean> selected = new ArrayList<>();
        for(String BSSID:BSSIDs){
            SSIDs.add(RSSISSIDs.get(BSSID));
            selected.add(false);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Proceed)
                .setMultiChoiceItems(SSIDs.toArray(new CharSequence[BSSIDs.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            selected.set(which, true);
                        }else{
                            selected.set(which, false);
                        }
                    }
                })
                .setPositiveButton(R.string.Proceed, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setBSSIDs(BSSIDs, selected);
                        listener.onRSSIDialogPositiveClick(SelectRSSIFragment.this);
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onRSSIDialogNegativeClick(SelectRSSIFragment.this);
                    }
                });
        return builder.create();
    }

    private void setBSSIDs(ArrayList<String> BSSIDs,ArrayList<Boolean> selected) {
        this.BSSIDs = new ArrayList<>();
        for(int i=0;i<selected.size();i++) {
            if(selected.get(i)){
                this.BSSIDs.add(BSSIDs.get(i));
            }
        }
    }

    public ArrayList<String> getBSSIDs() {
        return BSSIDs;
    }
}
