package com.example.wraistrehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private Button accept_btn;
    private EditText id_txt;
    private EditText name_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Objetos pertenecientes a la pantalla:
        accept_btn = (Button) findViewById(R.id.accept_btn);
        id_txt = (EditText) findViewById(R.id.voltage_txt);
        name_txt = (EditText) findViewById(R.id.angle_txt);

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se guardan los datos de los editText:
                String nombre1 = name_txt.getText().toString().trim();
                String id1 = id_txt.getText().toString().trim();

                // Validar campos vacíos
                if (nombre1.isEmpty() || id1.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, complete ambos campos", Toast.LENGTH_SHORT).show();
                    return; // Salir del método si los campos están vacíos
                }

                GlobalClass.id = id1;
                GlobalClass.nombre = nombre1;
                GlobalClass.habilitador = 1;

                // Se cierran todas las ventanas y se vuelve al menú:
                Intent intent = new Intent(getApplicationContext(), Menu.class);
                intent.putExtra("habil", 1);
                intent.putExtra("id_env", GlobalClass.id);
                intent.putExtra("nombre_env", GlobalClass.nombre);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish(); // Se cierra la actividad actual
            }
        });
    }
}
