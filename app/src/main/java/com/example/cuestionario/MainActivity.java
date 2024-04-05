package com.example.cuestionario;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCorreo, editTextId, editTextNombre, editTextEdad, editTextPais;
    private Button buttonEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCorreo = findViewById(R.id.editTextCorreo);
        editTextId = findViewById(R.id.editTextId);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextEdad = findViewById(R.id.editTextEdad);
        editTextPais = findViewById(R.id.editTextPais);
        buttonEnviar = findViewById(R.id.buttonEnviar);

        // Configuración del OnClickListener
        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarDatos(); // Llama al método enviarDatos() existente en la clase MainActivity
            }
        });
    }

    // Método para enviar datos
    public void enviarDatos() {
        String correo = editTextCorreo.getText().toString().trim();
        String id = editTextId.getText().toString().trim();
        String nombre = editTextNombre.getText().toString().trim();
        String edad = editTextEdad.getText().toString().trim();
        String pais = editTextPais.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("correo", correo);
            jsonObject.put("id", id);
            jsonObject.put("nombre", nombre);
            jsonObject.put("edad", edad);
            jsonObject.put("pais", pais);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendDataToServer().execute(jsonObject.toString());
    }

    private class SendDataToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://localhost/nocheAPI/api.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                OutputStream os = connection.getOutputStream();
                os.write(params[0].getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line).append('\n');
                }
                br.close();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("mensaje")) {
                        String mensaje = jsonResponse.getString("mensaje");
                        Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    } else if (jsonResponse.has("error")) {
                        String error = jsonResponse.getString("error");
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error: Respuesta no válida del servidor", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error: No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
