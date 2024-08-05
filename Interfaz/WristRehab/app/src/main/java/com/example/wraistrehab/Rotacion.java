package com.example.wraistrehab;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Rotacion extends AppCompatActivity {

    private AppCompatButton lastSelectedButton = null;
    private String selectedRotation = "";
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rotacion);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        editText = findViewById(R.id.editTextText);

        AppCompatButton accept_btn = findViewById(R.id.button);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rotacion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accept_btn.setOnClickListener(v -> {
            String repetitionsStr = editText.getText().toString();
            if (repetitionsStr.isEmpty()) {
                Toast.makeText(Rotacion.this, "Por favor ingrese el número de repeticiones", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = getIntent().getStringExtra("id_env");
            Intent intent = new Intent(Rotacion.this, RotacionJuego.class);
            intent.putExtra("selectedRotation", selectedRotation);
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
        selectedRotation = level;
    }
}




