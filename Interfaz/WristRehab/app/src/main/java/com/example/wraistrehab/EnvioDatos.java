package com.example.wraistrehab;

import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;

public class EnvioDatos extends AppCompatActivity {

    private Button enviarButton;
    private StorageReference mStorageRef;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviodatos);

        // Initialize Firebase Storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Find the button by ID
        enviarButton = findViewById(R.id.enviar);

        // Get the CSV file name from intent extras
        id = getIntent().getStringExtra("id_env");
        id = id + ".csv";

        // Set an OnClickListener to handle button click
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    private void uploadFile() {
        // Path to the CSV file
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), id);
        if (csvFile.exists()) {
            // Create a reference to 'csv_files/filename.csv'
            String path = "csv_files/" + id;
            StorageReference fileRef = mStorageRef.child(path);

            // Upload the file
            Uri file = Uri.fromFile(csvFile);
            fileRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        // File upload succeeded
                        Toast.makeText(EnvioDatos.this, "Archivo subido exitósamente", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        Toast.makeText(EnvioDatos.this, "Error al subir el archivo: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(EnvioDatos.this, "El archivo no existe", Toast.LENGTH_LONG).show();
        }
    }
}


