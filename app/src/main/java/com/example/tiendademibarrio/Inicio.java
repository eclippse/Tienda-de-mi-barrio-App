package com.example.tiendademibarrio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;


public class Inicio extends AppCompatActivity {

    ImageButton tendero, cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incio);

        tendero = (ImageButton)findViewById(R.id.btnTendero);
        cliente = (ImageButton)findViewById(R.id.btnCliente);

        tendero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Inicio.this, MainActivity.class);
                startActivity(i);
            }
        });

        cliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Inicio.this, ClientActivity.class);
                startActivity(i);
            }
        });
}
}
