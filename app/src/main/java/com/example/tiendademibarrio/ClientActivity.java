package com.example.tiendademibarrio;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClientActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener{

    Button  crear, ver, modificar, eliminar, foto;
    ImageButton escaner;
    EditText isbn, nombre, descripcion, precio;

    AdminSQLiteOpenHelper admin;
    SQLiteDatabase bd;
    ContentValues registro;

    ProgressDialog progreso;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    StringRequest stringRequest;
    int cont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        escaner=findViewById(R.id.btnEscaner);

        isbn=findViewById(R.id.etIsbn);
        nombre=(EditText)findViewById(R.id.etNombre);
        descripcion=(EditText)findViewById(R.id.etDescripcion);
        precio=(EditText)findViewById(R.id.etPrecio);

        ver=(Button)findViewById(R.id.btnVer);

        escaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escanear();
            }
        });
    }

    //METODOS DEL ESCANER
    ///////////////////////////////////

    public void escanear(){
        IntentIntegrator inten = new IntentIntegrator(this);
        inten.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);

        inten.setPrompt("ESCANEAR CODIGO DE BARRA DEL PRODUCTO");
        inten.setCameraId(0);
        inten.setBeepEnabled(false);
        inten.setOrientationLocked(false);
        inten.setCaptureActivity(CaptureActivityPortrait.class);
        inten.setBarcodeImageEnabled(false);
        inten.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result !=null){
            if(result.getContents()==null){
                Toast.makeText(this, "Cancelaste el escaneo", Toast.LENGTH_LONG).show();
            } else{
                isbn.setText(result.getContents().toString());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //METODOS DE SQLite
    ////////////////////////////////////////

    public void verProductoLocal(View v){

        if(isbn.getText().toString().trim().equalsIgnoreCase(""))

            Toast.makeText(ClientActivity.this, "Escanea el producto que quieras ver", Toast.LENGTH_LONG).show();

        else{


            admin = new AdminSQLiteOpenHelper(this, "administracion", null,1);
            bd = admin.getWritableDatabase();
            String num = isbn.getText().toString();
            Cursor registro = bd.rawQuery("select nombre, descripcion, precio from productos where isbn=" + num,null);
            if (registro.moveToFirst()){
                nombre.setText(registro.getString(0));
                descripcion.setText(registro.getString(1));
                precio.setText(registro.getString(2));
            } else
                Toast.makeText(this, "No existe un producto con ese numero", Toast.LENGTH_SHORT).show();
            bd.close();
        }
    }

    //VER MAPA
    ///////////////////////////////////////////////////////

    public void mapa(View view){
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }


    // BASE DE DATOS HTTP
    ////////////////////////////////////////////////////////

    public void verProducto(View view){
        cont=1;
        progreso = new ProgressDialog(this);
        progreso.setMessage("Buscando...");
        progreso.show();

        String url = "https://servidormoviles.000webhostapp.com/ver.php?isbn=" + isbn.getText().toString();


        url = url.replace(" ", "%20");
        request = Volley.newRequestQueue(this);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
    }




    @Override
    public void onErrorResponse(VolleyError error) {

            progreso.hide();
            Toast.makeText(this, "no se ha encontrado producto" + error.toString(), Toast.LENGTH_SHORT).show();
            Log.i("Error", error.toString());

    }

    @Override
    public void onResponse(JSONObject response) {

            progreso.hide();
            Toast.makeText(this, "Datos de "+ nombre.getText().toString() + " encontrados", Toast.LENGTH_SHORT).show();
            Personas persona = new Personas();

            JSONArray json = response.optJSONArray("datos");
            JSONObject jsonObject=null;


            try {
                jsonObject = json.getJSONObject(0);
                persona.setNombre(jsonObject.optString("nombre"));
                persona.setTelefono(jsonObject.optString("descripcion"));
                persona.setEmail(jsonObject.optString("precio"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            nombre.setText(persona.getNombre());
            descripcion.setText(persona.getTelefono());
            precio.setText(persona.getEmail());

    }
}
