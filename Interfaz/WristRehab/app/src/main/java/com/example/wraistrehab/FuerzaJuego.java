package com.example.wraistrehab;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.ingenieriajhr.blujhr.BluJhr;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageButton;

public class FuerzaJuego extends AppCompatActivity {
    private TextView selectedLevelTextView;
    private TextView repetitionsTextView;
    private TextView dataTextView;
    private TextView countTextView;
    private TextView boolTextView;
    private ImageView manijaImageView;
    private ImageView globoImageView;
    private ImageView globoImageCero;
    private ImageView[] globoImages; // Arreglo para las imágenes de los globos
    private int repetitions; // Variable para almacenar el número de repeticiones
    String selectedLevelStr;
    String repetitionsStr;
    private int subcounter = 0;
    private int completedRepetitions = 0; // Variable para contar las repeticiones realizadas
    private int selectedLevel; // Variable para almacenar el nivel de fuerza
    private float bluetoothData; // Variable para almacenar los datos recibidos por Bluetooth
    private float force_max; // Variable para almacenar la fuerza máxima de acuerdo al nivel
    private int currentImageIndex = -1; // Variable para identificar el índice para cambiar las imágenes del globo
    private final int minPosition = 225;
    private final int maxPosition = 476;
    private boolean isRepetitionCounted = false; // Variable para asegurar que se cuente una repetición solo una vez por ciclo
    private Handler handler; // Handler para manejar las actualizaciones de la UI
    private AppCompatImageButton playButton;
    private AppCompatImageButton resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuerzajuego);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        selectedLevelTextView = findViewById(R.id.selectedLevelTextView);
        repetitionsTextView = findViewById(R.id.repetitionsTextView);
        dataTextView = findViewById(R.id.dataTextView);
        countTextView = findViewById(R.id.countTextView);
        boolTextView = findViewById(R.id.boolTextView);
        manijaImageView = findViewById(R.id.manija);
        globoImageView = findViewById(R.id.globo_explotando);
        globoImageCero = findViewById(R.id.globo0);
        playButton = findViewById(R.id.button_play);
        resetButton = findViewById(R.id.button_rst);

        globoImages = new ImageView[]{
                findViewById(R.id.globo1),
                findViewById(R.id.globo2),
                findViewById(R.id.globo3),
                findViewById(R.id.globo4),
                findViewById(R.id.globo5)
        };

        globoImageView.setImageResource(R.drawable.balloon_animation);

        selectedLevelStr = getIntent().getStringExtra("selectedLevel");
        repetitionsStr = getIntent().getStringExtra("repetitions");

        selectedLevel = Integer.parseInt(selectedLevelStr);
        repetitions = Integer.parseInt(repetitionsStr);

        selectedLevelTextView.setText("Nivel seleccionado: " + selectedLevel);
        repetitionsTextView.setText("Repeticiones máximas: " + repetitions);
        countTextView.setText("Repeticiones: " + completedRepetitions);

        switch (selectedLevel) {
            case 1:
                force_max = 7.31f;
                break;
            case 2:
                force_max = 15.32f;
                break;
            case 3:
                force_max = 12.53f;
                break;
            case 4:
                force_max = 26.58f;
                break;
            default:
                force_max = 20;
                break;
        }

        handler = new Handler(Looper.getMainLooper());
        // Manejo del Play y del Reset
        if (Bluetooth.blue != null) {
            playButton.setOnClickListener(v -> {
                playButton.setVisibility(View.INVISIBLE);
                Bluetooth.blue.bluTx(selectedLevelStr); // Enviar el nivel seleccionado para empezar
                startBluetoothDataReception();
            });

            resetButton.setOnClickListener(v -> {
                resetGame();
                Bluetooth.blue.bluTx(selectedLevelStr); // Enviar el nivel seleccionado
                startBluetoothDataReception();
            });

            resetButton.setVisibility(View.INVISIBLE);
        }
    }
    //Manejo de Datos por Bluetooth
    private void startBluetoothDataReception() {
        if (Bluetooth.blue != null) {
            Bluetooth.blue.loadDateRx(new BluJhr.ReceivedData() {
                private String previousData = "";
                @Override
                public void rxDate(@NonNull String s) {
                    // Procesar los datos en un hilo separado
                    handler.post(() -> {
                        try {
                            bluetoothData = Float.parseFloat(s);

                            if (!previousData.isEmpty() && previousData.length() > 1) {
                                char firstChar = previousData.charAt(0);
                                String s_concatenated;

                                if ((s.charAt(0) == '-') || (firstChar == '.')) {
                                    s_concatenated = s;
                                } else {
                                    s_concatenated = firstChar + "" + s;
                                }

                                if (Math.abs(Float.parseFloat(previousData) - Float.parseFloat(s_concatenated)) < 0.5) {
                                    bluetoothData = Float.parseFloat(s_concatenated);
                                    previousData = s_concatenated;
                                } else {
                                    previousData = s;
                                }
                            } else {
                                previousData = s;
                            }

                            dataTextView.setText("Datos Bluetooth: " + bluetoothData);

                            moveManija(bluetoothData);

                            String id = getIntent().getStringExtra("id_env");
                            id = id + ".csv";
                            Utility.writeDataToCsv(id, String.valueOf(bluetoothData), 1);

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            dataTextView.setText("Datos Bluetooth no válidos");
                        }
                    });
                }
            });
        }
    }
    // Reinicio de Varaibles
    private void resetGame() {
        if (Bluetooth.blue != null) {
            Bluetooth.blue.bluTx("0"); // Enviar el comando para detener el envío de datos
        }
        completedRepetitions = 0;
        countTextView.setText("Repeticiones: " + completedRepetitions);
        currentImageIndex = -1;
        globoImageView.setVisibility(View.INVISIBLE);
        globoImageCero.setVisibility(View.VISIBLE);
        for (ImageView globoImage : globoImages) {
            globoImage.setVisibility(View.INVISIBLE);
        }
        resetButton.setVisibility(View.INVISIBLE);
    }

    private int assignNumberToImage(int number, int totalNumbers, int totalImages) {
        int usableImages = totalImages - 1;

        int baseBlockSize = totalNumbers / usableImages;
        int extraNumbers = totalNumbers % usableImages;

        int[] imageSizes = new int[totalImages];
        for (int i = 0; i < usableImages; i++) {
            imageSizes[i] = baseBlockSize + (i >= (usableImages - extraNumbers) ? 1 : 0);
        }

        imageSizes[totalImages - 1] = 1;

        int[] imageRanges = new int[totalImages];
        imageRanges[0] = imageSizes[0];
        for (int i = 1; i < totalImages; i++) {
            imageRanges[i] = imageRanges[i - 1] + imageSizes[i];
        }

        if (number == totalNumbers) {
            return totalImages - 1;
        }

        for (int i = 0; i < totalImages; i++) {
            if (number <= imageRanges[i]) {
                return i;
            }
        }

        return -1;
    }
    // Mover la manija de acuerdo a los datos de entrada de Bluetooth
    private void moveManija(float data) {
        int newPosition;

        if (data <= 2) {
            newPosition = minPosition;
            isRepetitionCounted = false;
        } else if (data >= force_max) {
            newPosition = maxPosition;
        } else {
            newPosition = minPosition + Math.round((maxPosition - minPosition) * (data / force_max));
        }

        setMarginTop(manijaImageView, newPosition);

        if (data >= (force_max - 2) && !isRepetitionCounted) {
            completedRepetitions++;
            isRepetitionCounted = true;
            countTextView.setText("Repeticiones: " + completedRepetitions);

            int newImageIndex = assignNumberToImage(completedRepetitions, repetitions, (globoImages.length + 1));
            boolTextView.setText("Índice: " + newImageIndex);

            if (newImageIndex != -1 && newImageIndex != currentImageIndex) {
                if (newImageIndex == globoImages.length) {
                    for (ImageView globoImage : globoImages) {
                        globoImage.setVisibility(View.INVISIBLE);
                    }
                    globoImageView.setVisibility(View.VISIBLE);
                    AnimationDrawable animationDrawable = (AnimationDrawable) globoImageView.getDrawable();
                    animationDrawable.start();
                    resetButton.setVisibility(View.VISIBLE); // Mostrar el botón de reset cuando el globo explote
                    Bluetooth.blue.bluTx("0"); // Enviar el comando para detener el envío de datos
                } else {
                    globoImageCero.setVisibility(View.INVISIBLE);
                    for (ImageView globoImage : globoImages) {
                        globoImage.setVisibility(View.INVISIBLE);
                    }
                    globoImages[newImageIndex].setVisibility(View.VISIBLE);
                }
                currentImageIndex = newImageIndex;
            }
        }
        int refresh = 8;
        if (subcounter >= refresh){
            //Log.i("Flexion", "Reincia subcontador");
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx(selectedLevelStr);
            subcounter = 0;
        } else {subcounter ++;}
    }

    private void setMarginTop(View view, int marginTop) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = marginTop;
        view.setLayoutParams(layoutParams);
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

