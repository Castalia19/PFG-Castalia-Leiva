package com.example.wraistrehab;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ingenieriajhr.blujhr.BluJhr;
//import android.util.Log;

public class Flexion extends AppCompatActivity {
    private ImageView image_Ufo;
    private ImageView image_asteroide1;
    private ImageView image_asteroide2;
    private ImageView image_asteroide3;
    private ImageView image_asteroide4;
    private ImageView image_asteroide5;
    private ImageView image_asteroide6;
    private ImageView image_asteroide7;
    private ImageView image_asteroide8;
    private int counter = 0;
    private int subcounter = 0;
    private TextView textView;
    private float bluetoothData; // Variable para almacenar los datos recibidos por Bluetooth
    private TextView dataTextView; // Variable para mostrar el dato del Bluetooth
    private Handler asteroidHandler;
    private Runnable asteroidRunnable;
    private Handler bluetoothHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flexion);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.flexion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        image_Ufo = findViewById(R.id.Ufo);
        image_asteroide1 = findViewById(R.id.asteroide1);
        image_asteroide2 = findViewById(R.id.asteroide2);
        image_asteroide3 = findViewById(R.id.asteroide3);
        image_asteroide4 = findViewById(R.id.asteroide4);
        image_asteroide5 = findViewById(R.id.asteroide5);
        image_asteroide6 = findViewById(R.id.asteroide6);
        image_asteroide7 = findViewById(R.id.asteroide7);
        image_asteroide8 = findViewById(R.id.asteroide8);
        textView = findViewById(R.id.textView);
        dataTextView = findViewById(R.id.dataTextView);

        // Inicializar el handler y runnable para actualizar la posición de los asteroides
        asteroidHandler = new Handler();
        asteroidRunnable = new Runnable() {
            @Override
            public void run() {
                setupAsteroids();
                asteroidHandler.postDelayed(this, 16); // Repetir cada 16 milisegundos (aprox. 60 FPS)
            }
        };
        asteroidHandler.post(asteroidRunnable); // Iniciar el movimiento
        // Handler para los datos Bluetooth
        bluetoothHandler = new Handler();
        // Verificar si la instancia Bluetooth está disponible

    // Configurar el receptor para los datos Bluetooth
        if (Bluetooth.blue != null) {
            Bluetooth.blue.bluTx("5"); // Enviar el juego seleccionado para empezar
            Bluetooth.blue.loadDateRx(new BluJhr.ReceivedData() {
                private String previousData = ""; // Variable para almacenar el dato anterior

                @Override
                public void rxDate(@NonNull String s) {
                    //Log.i("Flexion", "Dato Bluetooth" + s);
                    bluetoothHandler.post(() -> {
                        try {
                            // Convertir el dato recibido a número
                            bluetoothData = Float.parseFloat(s);
                            // Comparar el nuevo dato recibido con el dato anterior (en caso de pérdidas de carácter)
                            if (!previousData.isEmpty() && previousData.length() > 1) {
                                char firstChar = previousData.charAt(0); // Obtener el primer carácter del dato anterior
                                String s_concatenated;

                                if ((s.charAt(0) == '-') || (firstChar == '.')) {
                                    s_concatenated = s;  // Evitar concatenar si s ya es negativo
                                } else {
                                    s_concatenated = firstChar + "" + s;  // Asegurar que se trate como cadena
                                }
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

                            // Actualizar la posición vertical basada en el dato Bluetooth
                            updateUfoPosition(bluetoothData);

                            // Guardar el dato en el archivo CSV
                            //Log.i("Csv", "Envío de datos ");
                            String id = getIntent().getStringExtra("id_env");
                            id = id + ".csv";
                            Utility.writeDataToCsv(id, String.valueOf(bluetoothData), 2);

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


    // Método para actualizar la posición de los asteroides y verificar colisiones
    private void updateAsteroidsPosition(ImageView asteroidImageView) {
        if (asteroidImageView != null) {
            // Obtener las posiciones actuales de los asteroides y del Ufo
            float ufoLeft = image_Ufo.getX();
            float ufoRight = image_Ufo.getX() + image_Ufo.getWidth();
            float ufoTop = image_Ufo.getY();
            float ufoBottom = image_Ufo.getY() + image_Ufo.getHeight();

            float asteroidLeft = asteroidImageView.getX();
            float asteroidRight = asteroidImageView.getX() + asteroidImageView.getWidth();
            float asteroidTop = asteroidImageView.getY();
            float asteroidBottom = asteroidImageView.getY() + asteroidImageView.getHeight();

            // Calcular las nuevas posiciones de los asteroides
            float newXposition = asteroidLeft - 5; // Mover a la izquierda

            // Reposicionar el asteroide si se sale de la pantalla
            if (newXposition < -asteroidImageView.getWidth()) {
                newXposition = 2000;
                counter++;
            }

            // Verificar colisión con el Ufo
            if (isCollision(ufoLeft, ufoRight, ufoTop, ufoBottom, asteroidLeft, asteroidRight, asteroidTop, asteroidBottom)) {
                // Reiniciar el contador al detectar colisión
                counter = 0;
                //return; // Salir de la función para evitar reposicionar el asteroide
            }

            // Establecer las nuevas posiciones
            asteroidImageView.setX(newXposition);

            // Actualizar el texto del contador
            textView.setText(String.valueOf(counter));
        }
    }
    // Método para verificar colisión entre dos imágenes
    private boolean isCollision(float left1, float right1, float top1, float bottom1,
                                float left2, float right2, float top2, float bottom2) {
        return !(right1 < left2 || left1 > right2 || bottom1 < top2 || top1 > bottom2);
    }

    // Llamar a updateAsteroidsPosition() para cada asteroide en onCreate() u otro lugar adecuado
    private void setupAsteroids() {
        updateAsteroidsPosition(image_asteroide1);
        updateAsteroidsPosition(image_asteroide2);
        updateAsteroidsPosition(image_asteroide3);
        updateAsteroidsPosition(image_asteroide4);
        updateAsteroidsPosition(image_asteroide5);
        updateAsteroidsPosition(image_asteroide6);
        updateAsteroidsPosition(image_asteroide7);
        updateAsteroidsPosition(image_asteroide8);
    }

    private void updateUfoPosition(float bluetoothData) {
        if (image_Ufo != null) {
            // Limitar el rango de bluetoothData entre -90 y 90
            bluetoothData = Math.max(-90, Math.min(bluetoothData, 90));

            // Definir las posiciones en píxeles
            float bottomPosition = 1000;  // Posición inferior en píxeles
            float topPosition = 200;      // Posición superior en píxeles
            float midPosition = (bottomPosition + topPosition) / 2;  // Posición media en píxeles

            // Calcular la nueva posición vertical del Ufo
            float newYposition = bottomPosition - ((bluetoothData + 90) / 180) * (bottomPosition - topPosition);

            // Establecer la nueva posición vertical del Ufo
            image_Ufo.setY(newYposition);
            int refresh = 8;
            //Log.i("Flexion", "Subcounter " + subcounter);
            if (subcounter >= refresh){
                //Log.i("Flexion", "Reincia subcontador");
                Bluetooth.blue.bluTx("0");
                Bluetooth.blue.bluTx("5");
                subcounter = 0;
            } else {subcounter ++;}
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el runnable y liberar recursos
        if (asteroidHandler != null && asteroidRunnable != null) {
            asteroidHandler.removeCallbacks(asteroidRunnable);
        }
        // Detener el handler para los datos Bluetooth
        if (bluetoothHandler != null) {
            bluetoothHandler.removeCallbacksAndMessages(null);
        }
    }
}




