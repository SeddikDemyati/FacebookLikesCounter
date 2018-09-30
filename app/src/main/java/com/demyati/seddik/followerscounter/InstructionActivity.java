package com.demyati.seddik.followerscounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InstructionActivity extends AppCompatActivity {

    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        textView2=findViewById(R.id.textView2);
        textView2.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
