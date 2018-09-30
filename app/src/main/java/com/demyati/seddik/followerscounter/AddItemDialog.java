package com.demyati.seddik.followerscounter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddItemDialog extends android.support.v4.app.DialogFragment implements  View.OnClickListener{

    View view;
    EditText ItemText;
    Context context;
    Intent AddItem;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //these two lines important when rotating and recreating
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        getDialog().setTitle(context.getResources().getString(R.string.Title));

        //getDialog().getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
        AddItem=new Intent("NewAddedItem");
        view=inflater.inflate(R.layout.add_item_dialog,container,false);
        ItemText=view.findViewById(R.id.ItemText);
        Button Done=view.findViewById(R.id.Done);
        Button Cancel=view.findViewById(R.id.Cancel);
        Done.setOnClickListener(this);
        Cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        //cast view to button and check which button is clicked
        Button button=(Button)v;
        if(button.getText().toString().equals("Cancel")) {
            this.getDialog().dismiss();
        }
        else if (context.getResources().getResourceEntryName(button.getId()).equals("Done")) {
            String Text=ItemText.getText().toString();
            AddItem.putExtra("AddedItem",Text);
            LocalBroadcastManager.getInstance(context).sendBroadcast(AddItem);
            this.getDialog().dismiss();
        }
    }

    //keeping dialog after rotation
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}