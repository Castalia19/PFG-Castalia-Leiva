package com.example.wraistrehab;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ingenieriajhr.blujhr.BluJhr;
import android.util.Pair;
//import android.util.Log;

public class RotacionJuego extends AppCompatActivity {
    private ImageView balde;
    private ImageView cuerda;
    private ImageView polea;
    private int counter = 0;
    private boolean llego = false;
    private TextView textView;
    private int repetitions;
    private TextView repetitionsTextView;
    private TextView AngleTextView;
    private TextView DifferenceTextView;
    private float bluetoothDataFlexex;
    private float bluetoothDataAbad;
    private TextView dataTextView;
    private float angFlexex;
    private float angAbad;
    private float initialAngleDegrees;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private Handler mainHandler;
    private AppCompatImageButton playButton;
    private AppCompatImageButton resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rotacionjuego);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rotacionjuego), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        repetitionsTextView = findViewById(R.id.repetitionsTextView);
        cuerda = findViewById(R.id.cuerda);
        balde = findViewById(R.id.balde);
        polea = findViewById(R.id.polea_lado2);
        textView = findViewById(R.id.textView);
        dataTextView = findViewById(R.id.dataTextView);
        AngleTextView = findViewById(R.id.AngleTextView);
        DifferenceTextView = findViewById(R.id.DifferenceTextView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.getProgressDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        playButton = findViewById(R.id.button_play);
        resetButton = findViewById(R.id.button_rst);

        String repetitionsStr = getIntent().getStringExtra("repetitions");
        repetitions = Integer.parseInt(repetitionsStr);
        repetitionsTextView.setText("Repeticiones máximas: " + repetitions);

        initialAngleDegrees = -1;

        mainHandler = new Handler(Looper.getMainLooper());

        if (Bluetooth.blue != null) {
            playButton.setOnClickListener(v -> {
                playButton.setVisibility(View.INVISIBLE);
                Bluetooth.blue.bluTx("7"); // Enviar el juego seleccionado para empezar
                startBluetoothDataReception();
            });

            resetButton.setOnClickListener(v -> {
                resetGame();
                Bluetooth.blue.bluTx("7"); // Enviar el juego seleccionado
                startBluetoothDataReception();
            });

            resetButton.setVisibility(View.INVISIBLE);
        }
    }

    private void startBluetoothDataReception() {
        Bluetooth.blue.loadDateRx(new BluJhr.ReceivedData() {
            private String previousDataFlexex = "";
            private String previousDataAbad = "";
            @Override
            public void rxDate(@NonNull String s) {
                mainHandler.post(() -> {
                    try {
                        if (s.contains("/")) {
                            //Dividir la entrada en Flexión/Extensión y Abducción/Aducción
                            String[] partes = s.split("/");
                            angFlexex = Float.parseFloat(partes[0]);
                            angAbad = Float.parseFloat(partes[1]);
                            Pair<String, Float> resultFlexex = processBluetoothData(partes[0], previousDataFlexex);
                            previousDataFlexex = resultFlexex.first;
                            bluetoothDataFlexex = resultFlexex.second;
                            Pair<String, Float> resultAbad = processBluetoothData(partes[1], previousDataAbad);
                            previousDataAbad = resultAbad.first;
                            bluetoothDataAbad = resultAbad.second;
                            //Mostar el dato en el TextView
                            dataTextView.setText("Datos Bluetooth: " + bluetoothDataFlexex + " " + bluetoothDataAbad);

                            movepolea(bluetoothDataFlexex, bluetoothDataAbad);

                            // Guardar el dato en el archivo CSV
                            String id = getIntent().getStringExtra("id_env");
                            //Log.i("Csv", "Id: " + id);
                            id = id + ".csv";
                            Utility.writeDataToCsv(id, String.valueOf(bluetoothDataFlexex) + "/" + String.valueOf(bluetoothDataAbad), 4);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        dataTextView.setText("Datos Bluetooth no válidos");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void movepolea(float angFlexex, float angAbad) {
        // Calcular el ángulo en radianes
        double angleRadians = Math.atan2(angAbad, angFlexex);
        // Convertir a grados y normalizar el ángulo para que esté en el rango de 0 a 360
        float angleDegrees = (float) Math.toDegrees(angleRadians);
        angleDegrees = (angleDegrees + 360) % 360;
        // Si es la primera vez que se llama a la función, establecer el ángulo inicial
        if (initialAngleDegrees == -1) {
            initialAngleDegrees = angleDegrees;
        }

        float angleDifference = Math.abs(angleDegrees - initialAngleDegrees);
        if (angleDifference > 180) {
            angleDifference = 360 - angleDifference;
        }

        AngleTextView.setText("Ángulo ref: " + initialAngleDegrees);
        DifferenceTextView.setText("Diferencia de Ángulo: " + angleDifference);
        // Calcular la nueva posición vertical basada en la diferencia del ángulo
        float newYpositionCuerda = 650 + angleDifference * 0.7f;
        float newYpositionBalde = 730 + (225 / 180f) * angleDifference;
        cuerda.setScaleY(angleDifference * 0.013f);
        // Actualizar la posición de la cuerda
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cuerda.getLayoutParams();
        layoutParams.topMargin = (int) newYpositionCuerda;
        cuerda.setLayoutParams(layoutParams);
        // Actualizar la posición del balde
        ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) balde.getLayoutParams();
        layoutParams2.topMargin = (int) newYpositionBalde;
        balde.setLayoutParams(layoutParams2);

        // Controlar el contador de repeticiones
        if (175 <= angleDifference && angleDifference <= 185) {
            llego = true;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound);
            mediaPlayer.start();
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx("7");
        }
        if (angleDifference <= 15 && llego) {
            counter++;
            llego = false;
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx("7");
            updateProgressBar();
        }
        if (85 <= angleDifference && angleDifference <= 95){
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx("7");
        }
        if (40 <= angleDifference && angleDifference <= 50){
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx("7");
        }
        //Log.i("RotJuego", "Llego: " + llego);
        textView.setText(String.valueOf(counter));
        // Aplicar la rotación a la ImageView
        polea.setRotation(angleDegrees);
    }

    public static Pair<String, Float> processBluetoothData(String datoActual, String previousData) {
//        Log.i("RotJuego", "PreviousData es "+ previousData);
//        Log.i("RotJuego", "DatoActual es "+ datoActual);
        Float bluetoothDataSend = null;
        try {
            bluetoothDataSend = Float.parseFloat(datoActual);
            //Manejo de pérdida de carácter
            if (previousData != null && !previousData.isEmpty() && previousData.length() > 1) {
                char firstChar = previousData.charAt(0);
                String datoActualConcatenated;
                if ((datoActual.charAt(0) == '-') || (firstChar == '.')) {
                    datoActualConcatenated = datoActual;
                } else {
                    datoActualConcatenated = firstChar + "" + datoActual;
                }
                if (Math.abs(Float.parseFloat(previousData) - Float.parseFloat(datoActualConcatenated)) < 0.5) {
                    bluetoothDataSend = Float.parseFloat(datoActualConcatenated);
                    previousData = datoActualConcatenated;
                } else {
                    previousData = datoActual;
                }
            } else {
                previousData = datoActual;
            }
        } catch (NumberFormatException e) {
            previousData = datoActual;
        }
        return new Pair<>(previousData, bluetoothDataSend);
    }

    private void updateProgressBar() {
        int progress = (int) (((float) counter / repetitions) * 100);
        progressBar.setProgress(progress);

        if (progress >= 100) {
            // Detener el envío de datos por Bluetooth
            if (Bluetooth.blue != null) {
                Bluetooth.blue.bluTx("0"); // Enviar el comando para detener el envío de datos
            }
            // Mostrar el botón de reinicio
            resetButton.setVisibility(View.VISIBLE);
        }
    }

    private void resetGame() {
        counter = 0;
        llego = false;
        initialAngleDegrees = -1;
        progressBar.setProgress(0);
        resetButton.setVisibility(View.INVISIBLE);
        textView.setText("0");
        dataTextView.setText("Datos Bluetooth: ");
        AngleTextView.setText("Ángulo ref: ");
        DifferenceTextView.setText("Diferencia de Ángulo: ");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el handler para los datos Bluetooth
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
