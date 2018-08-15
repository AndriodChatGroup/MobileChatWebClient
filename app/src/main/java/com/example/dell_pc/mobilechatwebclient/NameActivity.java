package com.example.dell_pc.mobilechatwebclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NameActivity extends Activity
{
    private Button btnJoin;
    private Button btnLocation;
    private Button btnMap;
    private EditText txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        btnJoin = findViewById(R.id.btnJoin);
        btnLocation = findViewById(R.id.btnLocation);
        btnMap = findViewById(R.id.btnMap);
        txtName = findViewById(R.id.name);


        // Hiding the action bar
        getActionBar().hide();

        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String name;
                if ((name = txtName.getText().toString().trim()).length() > 0)
                {
                    Intent intent = new Intent(NameActivity.this, MainActivity.class);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_LONG).show();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(NameActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(NameActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
