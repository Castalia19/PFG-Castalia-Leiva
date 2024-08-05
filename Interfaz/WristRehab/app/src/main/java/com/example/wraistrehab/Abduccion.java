package com.example.wraistrehab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ingenieriajhr.blujhr.BluJhr;

//import android.util.Log;
import android.view.WindowManager;

public class Abduccion extends AppCompatActivity implements BallViewListener {

    private BallView ballView;
    private TextView textView;
    private float bluetoothData; // Variable para almacenar los datos recibidos por Bluetooth
    private TextView dataTextView; // Variable para mostrar el dato del Bluetooth
    private Handler handler; // Handler para manejar actualizaciones en el hilo principal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_abduccion);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handler = new Handler(Looper.getMainLooper()); // Inicializar el Handler

        // Ajustar el padding de la vista principal según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.abduccion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener referencia a la vista BallView y TextViews
        ballView = findViewById(R.id.ballView);
        textView = findViewById(R.id.textView);
        dataTextView = findViewById(R.id.dataTextView);

        // Establecer el listener en BallView
        if (ballView != null) {
            ballView.setListener(this);
        }

        // Verificar si la instancia Bluetooth está disponible
        if (Bluetooth.blue != null) {
            // Enviar un comando inicial a través de Bluetooth
            Bluetooth.blue.bluTx("6");

            // Configurar el receptor para los datos Bluetooth
            Bluetooth.blue.loadDateRx(new BluJhr.ReceivedData() {
                private String previousData = ""; // Variable para almacenar el dato anterior
                @Override
                public void rxDate(@NonNull String s) {
                    handler.post(() -> {
                        try {
                            //Log.i("BallView", "La s es " + s);
                            // Convertir el dato recibido a número
                            bluetoothData = Float.parseFloat(s);

                            // Comparar el nuevo dato recibido con el dato anterior (en caso de pérdidas de carácter)
                            //Log.i("BallView", "PreviousData es "+ previousData);

                            if (!previousData.isEmpty() && previousData.length() > 1) {
                                char firstChar = previousData.charAt(0); // Obtener el primer carácter del dato anterior
                                String s_concatenated;

                                if ((s.charAt(0) == '-') || (firstChar == '.')) {
                                    s_concatenated = s;  // Evitar concatenar si s ya es negativo
                                } else {
                                    s_concatenated = firstChar + "" + s;  // Asegurar que se trate como cadena
                                }
                                //Log.i("BallView", "El primer caracter era " + firstChar);
                                //Log.i("BallView", "El nuevo concatenado es "+s_concatenated);

                                // Si la diferencia entre el dato anterior y el dato concatenado es menor a 0.5, asumir que el primer carácter se perdió
                                if (Math.abs(Float.parseFloat(previousData) - Float.parseFloat(s_concatenated)) < 0.5) {
                                    bluetoothData = Float.parseFloat(s_concatenated);
                                    // Guardar el nuevo dato como el dato anterior para la próxima comparación
                                    previousData = s_concatenated;
                                } else {
                                    previousData = s;
                                }
                            } else {
                                previousData = s;
                            }
                            // Mostrar el dato recibido en el TextView
                            dataTextView.setText("Datos Bluetooth: " + bluetoothData);

                            // Enviar el dato al BallView
                            if (ballView != null) {
                                ballView.updateBluetoothData(bluetoothData);
                            }
                            // Guardar el dato en el archivo CSV
                            //Log.i("Csv", "Envío de datos ");
                            String id = getIntent().getStringExtra("id_env");
                            id = id + ".csv";
                            Utility.writeDataToCsv(id, String.valueOf(bluetoothData), 3);

                        } catch (NumberFormatException e) {
                            // Manejar la conversión fallida si el dato no es numérico
                            e.printStackTrace();
                            dataTextView.setText("Datos Bluetooth no válidos");
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onCountChanged(int newCount) {
        handler.post(() -> textView.setText(String.valueOf(newCount)));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el handler para los datos Bluetooth
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
