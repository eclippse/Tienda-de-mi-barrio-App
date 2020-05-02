package com.example.tiendademibarrio;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener{

    Button  crear, ver, modificar, eliminar;
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
        setContentView(R.layout.activity_main);

        escaner=findViewById(R.id.btnEscaner);

        isbn=findViewById(R.id.etIsbn);
        nombre=(EditText)findViewById(R.id.etNombre);
        descripcion=(EditText)findViewById(R.id.etDescripcion);
        precio=(EditText)findViewById(R.id.etPrecio);

        crear=(Button)findViewById(R.id.btnCrear);
        ver=(Button)findViewById(R.id.btnVer);
        modificar=(Button)findViewById(R.id.btnModificar);
        eliminar=(Button)findViewById(R.id.btnEliminar);

        escaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escanear();
            }
        });
    }


    //METODOS DEL ESCANER
    //////////////////////////////////

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

    public void crearProductoLocal(View v){

        if(isbn.getText().toString().trim().equalsIgnoreCase("")||
                nombre.getText().toString().trim().equalsIgnoreCase("")||
                descripcion.getText().toString().trim().equalsIgnoreCase("")||
                precio.getText().toString().trim().equalsIgnoreCase(""))

            Toast.makeText(MainActivity.this, "Hay información por rellenar", Toast.LENGTH_LONG).show();

        else {

            admin = new AdminSQLiteOpenHelper(MainActivity.this, "administracion", null, 1);
            bd = admin.getWritableDatabase();
            registro = new ContentValues();

            registro.put("isbn", isbn.getText().toString());
            registro.put("nombre", nombre.getText().toString());
            registro.put("descripcion", descripcion.getText().toString());
            registro.put("precio", precio.getText().toString());

            bd.insert("productos", null, registro);
            bd.close();

            isbn.setText("");
            nombre.setText("");
            descripcion.setText("");
            precio.setText("");

            Toast.makeText(this, "Se guardó el producto satisfactoriamente", Toast.LENGTH_SHORT).show();
        }
    }

    public void verProductoLocal(View v){

        if(isbn.getText().toString().trim().equalsIgnoreCase(""))

            Toast.makeText(MainActivity.this, "Escanea el producto que quieras ver", Toast.LENGTH_LONG).show();

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

    public void modificarProductoLocal(View v){

        if(isbn.getText().toString().trim().equalsIgnoreCase("")||
                nombre.getText().toString().trim().equalsIgnoreCase("")||
                descripcion.getText().toString().trim().equalsIgnoreCase("")||
                precio.getText().toString().trim().equalsIgnoreCase(""))

            Toast.makeText(MainActivity.this, "Debe ESCANEAR y VER el producto", Toast.LENGTH_LONG).show();

        else {

            admin = new AdminSQLiteOpenHelper(MainActivity.this, "administracion", null, 1);
            bd = admin.getWritableDatabase();
            registro = new ContentValues();

            String stIsbn = isbn.getText().toString();
            String stNombre = nombre.getText().toString();
            String stDescripcion = descripcion.getText().toString();
            String stPrecio = precio.getText().toString();

            registro.put("nombre", stNombre);
            registro.put("descripcion", stDescripcion);
            registro.put("precio", stPrecio);

            int cant = bd.update("productos", registro,"isbn=" + stIsbn, null);
            bd.close();

            if(cant==1){
                Toast.makeText(this, "Se modificó el producto", Toast.LENGTH_SHORT).show();
                isbn.setText("");
                nombre.setText("");
                descripcion.setText("");
                precio.setText("");
            } else {
                Toast.makeText(this, "No existe un producto con ese numero, CREE o ESCANEE y VEA el producto nuevamente", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void eliminarProductoLocal(View view){

        if((!nombre.getText().toString().trim().equalsIgnoreCase("")||
               !descripcion.getText().toString().trim().equalsIgnoreCase("")||
                !precio.getText().toString().trim().equalsIgnoreCase(""))&&
                isbn.getText().toString().trim().equalsIgnoreCase("")){

            Toast.makeText(MainActivity.this, "Debes escanear un producto", Toast.LENGTH_LONG).show();}
        else {
            if (isbn.getText().toString().trim().equalsIgnoreCase("") ||
                    nombre.getText().toString().trim().equalsIgnoreCase("") ||
                    descripcion.getText().toString().trim().equalsIgnoreCase("") ||
                    precio.getText().toString().trim().equalsIgnoreCase(""))

                Toast.makeText(MainActivity.this, "Presiona VER para ver el producto que quieres eliminar", Toast.LENGTH_LONG).show();

            else {

                admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
                bd = admin.getWritableDatabase();
                String num = isbn.getText().toString();
                int cant = bd.delete("productos", "isbn=" + num, null);
                bd.close();

                isbn.setText("");
                nombre.setText("");
                descripcion.setText("");
                precio.setText("");

                if (cant == 1)
                    Toast.makeText(this, "Se ha borrado el producto", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "No existe un producto con ese numero", Toast.LENGTH_SHORT).show();
            }
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
    public void crearProducto(View v) {
        cont = 0;
        progreso = new ProgressDialog(this);
        progreso.setMessage("Creando...");
        progreso.show();

        String url="https://servidormoviles.000webhostapp.com/crear.php?isbn="+ isbn.getText().toString()+
                "&nombre="+nombre.getText().toString()+"&descripcion="+descripcion.getText().toString()+
                "&precio="+precio.getText().toString();


        url = url.replace(" ","%20");
        request = Volley.newRequestQueue(this);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);

    }

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

    public void modificarProducto(View view){
        cont=2;
        progreso = new ProgressDialog(this);
        progreso.setMessage("Modificando...");
        progreso.show();

        String url = "https://servidormoviles.000webhostapp.com/modificar.php?";


        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progreso.hide();

                if (response.trim().equalsIgnoreCase("actualiza")) {
                    Toast.makeText(MainActivity.this, "Se ha modificado el producto con exito", Toast.LENGTH_LONG).show();
                    isbn.setText("");
                    nombre.setText("");
                    descripcion.setText("");
                    precio.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "No se a podido modificar el producto", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "No se a podido conectar", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String stIsbn = isbn.getText().toString();
                String stNombre = nombre.getText().toString();
                String stDescripcion = descripcion.getText().toString();
                String stPrecio = precio.getText().toString();

                Map<String, String> parametros = new HashMap<>();

                parametros.put("isbn", stIsbn);
                parametros.put("nombre", stNombre);
                parametros.put("descripcion", stDescripcion);
                parametros.put("precio", stPrecio);

                return parametros;
            }
        };
        request.add(stringRequest);
    }

    public void eliminarProducto(View view){
        cont=3;
        progreso = new ProgressDialog(this);
        progreso.setMessage("Eliminando...");
        progreso.show();

        String url = "https://servidormoviles.000webhostapp.com/eliminar.php?isbn=" + isbn.getText().toString();


        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progreso.hide();

                if (response.trim().equalsIgnoreCase("elimina")) {
                    nombre.setText("");
                    descripcion.setText("");
                    precio.setText("");
                    Toast.makeText(MainActivity.this, "Se ha eliminado con exito", Toast.LENGTH_LONG).show();
                } else {
                    if (response.trim().equalsIgnoreCase("noExiste")) {
                        Toast.makeText(MainActivity.this, "No se a encontrado el producto", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "No se a podido eliminar el producto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "No se a podido conectar", Toast.LENGTH_LONG).show();
            }
        });
        request.add(stringRequest);
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        if(cont==0){
            progreso.hide();
            Toast.makeText(this, "no se ha registrado producto" + error.toString(), Toast.LENGTH_SHORT).show();
            Log.i("Error", error.toString());}

        if(cont==1){
            progreso.hide();
            Toast.makeText(this, "no se ha encontrado producto" + error.toString(), Toast.LENGTH_SHORT).show();
            Log.i("Error", error.toString());
        }
    }

    @Override
    public void onResponse(JSONObject response) {

        if(cont==0){
            Toast.makeText(this, "se ha registrado exitosamente", Toast.LENGTH_SHORT).show();
            progreso.hide();
            isbn.setText("");
            nombre.setText("");
            descripcion.setText("");
            precio.setText("");}

        if(cont==1){
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
}
