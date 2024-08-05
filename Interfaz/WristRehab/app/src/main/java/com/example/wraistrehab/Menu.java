package com.example.wraistrehab;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;

import com.ingenieriajhr.blujhr.BluJhr;

public class Menu extends AppCompatActivity {
    // Elementos a utilizar en la ventana:
    Button fuerza_btn;
    Button flexion_btn;
    Button abduccion_btn;
    Button rotacion_btn;
    Button bluetooth_btn;
    Button internet_btn;
    TextView info_text_view;
    String nombre;
    String ID;

    // Handler para cambiar el color del texto
    private Handler handler;
    private Runnable colorRunnable;
    private boolean isColor1 = true;
    private int COLOR1;
    private int COLOR2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Obtener el TextView
        TextView textView2 = findViewById(R.id.textView2);

        // Obtener los datos enviados por el Intent
        Intent intent = getIntent();
        if (intent != null) {
            nombre = getIntent().getStringExtra("nombre_env");
            ID = getIntent().getStringExtra("id_env");
            // Configurar el texto del TextView
            if (nombre != null) {
                Log.i("Nombre",nombre);
                String mensaje = String.format("¡Hola %s! \nElija el ejercicio", nombre);
                textView2.setText(mensaje);
            }
        }

        // Elementos dentro de la ventana que se crean al crear la ventana:
        fuerza_btn = findViewById(R.id.fuerza_btn);
        flexion_btn = findViewById(R.id.flexion_btn);
        abduccion_btn = findViewById(R.id.abduccion_btn);
        rotacion_btn = findViewById(R.id.rotacion_btn);
        bluetooth_btn = findViewById(R.id.bluetooth_btn);
        internet_btn = findViewById(R.id.internet_btn);
        info_text_view = findViewById(R.id.info_text_view);
        COLOR1 = ContextCompat.getColor(this, R.color.magenta);
        COLOR2 = ContextCompat.getColor(this, R.color.verde_osc);

        // Configuración inicial de los botones
        configurarBotones();

        fuerza_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, Fuerza.class);
            i.putExtra("id_env", GlobalClass.id);
            startActivity(i);
        });

        flexion_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, Flexion.class);
            i.putExtra("id_env", GlobalClass.id);
            startActivity(i);
        });

        abduccion_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, Abduccion.class);
            i.putExtra("id_env", GlobalClass.id);
            startActivity(i);
        });

        rotacion_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, Rotacion.class);
            i.putExtra("id_env", GlobalClass.id);
            startActivity(i);
        });

        bluetooth_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, Bluetooth.class);
            startActivity(i);
        });

        internet_btn.setOnClickListener(view -> {
            GlobalClass.id = ID;
            GlobalClass.nombre = nombre;
            Intent i = new Intent(Menu.this, EnvioDatos.class);
            i.putExtra("id_env", GlobalClass.id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        configurarBotones();
        // Enviar el comando Bluetooth al regresar a la actividad
        if (Bluetooth.blue != null) {
            Bluetooth.blue.bluTx("0");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener el parpadeo cuando la actividad no está visible
        detenerParpadeo();
    }

    private void configurarBotones() {
        int habilitador = (Bluetooth.blue != null) ? 1 : 0;
        //Log.i("InfoMenu", "Blue " + Bluetooth.blue);

        if (habilitador == 0) {
            fuerza_btn.setEnabled(false);
            flexion_btn.setEnabled(false);
            abduccion_btn.setEnabled(false);
            rotacion_btn.setEnabled(false);
            info_text_view.setVisibility(View.VISIBLE);
            iniciarParpadeo();
        } else {
            fuerza_btn.setEnabled(true);
            flexion_btn.setEnabled(true);
            abduccion_btn.setEnabled(true);
            rotacion_btn.setEnabled(true);
            info_text_view.setVisibility(View.INVISIBLE);
            detenerParpadeo();
        }
    }

    private void iniciarParpadeo() {
        if (handler == null) {
            handler = new Handler();
            colorRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isColor1) {
                        info_text_view.setTextColor(COLOR1);
                    } else {
                        info_text_view.setTextColor(COLOR2);
                    }
                    isColor1 = !isColor1;
                    handler.postDelayed(this, 500); // Cambiar color cada 500 ms
                }
            };
            handler.post(colorRunnable);
        }
    }

    private void detenerParpadeo() {
        if (handler != null) {
            handler.removeCallbacks(colorRunnable);
            colorRunnable = null;
            handler = null;
        }
        info_text_view.setTextColor(COLOR1); // Establecer color final cuando se detiene el parpadeo
    }
}




