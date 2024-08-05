package com.example.wraistrehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Fuerza extends AppCompatActivity {

    private AppCompatButton lastSelectedButton = null;
    private String selectedLevel = "";
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fuerza);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        editText = findViewById(R.id.editTextText);
        AppCompatButton nivel1_btn = findViewById(R.id.nivel1_btn);
        AppCompatButton nivel2_btn = findViewById(R.id.nivel2_btn);
        AppCompatButton nivel3_btn = findViewById(R.id.nivel3_btn);
        AppCompatButton nivel4_btn = findViewById(R.id.nivel4_btn);
        AppCompatButton accept_btn = findViewById(R.id.button);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fuerza), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nivel1_btn.setOnClickListener(v -> selectButton(nivel1_btn, "1"));
        nivel2_btn.setOnClickListener(v -> selectButton(nivel2_btn, "2"));
        nivel3_btn.setOnClickListener(v -> selectButton(nivel3_btn, "3"));
        nivel4_btn.setOnClickListener(v -> selectButton(nivel4_btn, "4"));

        accept_btn.setOnClickListener(v -> {
            String repetitionsStr = editText.getText().toString();
            if (selectedLevel.isEmpty()) {
                Toast.makeText(Fuerza.this, "Por favor seleccione un nivel", Toast.LENGTH_SHORT).show();
                return;
            }
            if (repetitionsStr.isEmpty()) {
                Toast.makeText(Fuerza.this, "Por favor ingrese el número de repeticiones", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = getIntent().getStringExtra("id_env");
            Intent intent = new Intent(Fuerza.this, FuerzaJuego.class);
            intent.putExtra("selectedLevel", selectedLevel);
            intent.putExtra("repetitions", repetitionsStr);
            intent.putExtra("id_env", GlobalClass.id);
            startActivity(intent);
        });
    }

    private void selectButton(AppCompatButton button, String level) {
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
        }
        button.setSelected(true);
        lastSelectedButton = button;
        selectedLevel = level;
    }
}




