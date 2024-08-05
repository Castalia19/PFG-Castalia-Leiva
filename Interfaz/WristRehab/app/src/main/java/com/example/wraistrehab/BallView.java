package com.example.wraistrehab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.media.MediaPlayer;
import java.util.Random;
//import android.util.Log;

public class BallView extends View {
    // Constantes para los límites del rango de ángulos de Bluetooth
    private static final float MIN_ANGLE = -45f;
    private static final float MAX_ANGLE = 45f;
    private float lineY; // Posición de la línea en Y (constante)
    private float lineX; // Posición de la línea en X (variable)
    private float x; // Posición X de la bola
    private float y; // Posición Y de la bola
    private float radius; // Radio de la bola
    private float vx; // Velocidad X de la bola
    private float vy; // Velocidad Y de la bola
    private float ax; // Aceleración X de la bola (gravedad)
    private float ay; // Aceleración Y de la bola (gravedad)
    private Paint paint; // Objeto para dibujar la bola
    private float deltaTime;
    private Paint paintLine = new Paint();
    private long startTime;
    private TextView textView;
    private int count = 0;
    private int subcounter = 0;
    private Context context;
    private BallViewListener listener;
    private Random random = new Random();
    private MediaPlayer mediaPlayer;
    private Handler handler;

    // Constantes para valores iniciales
    private static final float INITIAL_X = 1000;
    private static final float INITIAL_Y = 100;
    private static final float INITIAL_VX = 20;
    private static final float INITIAL_VY = 0;
    private static final float INITIAL_AX = 0;
    private static final float INITIAL_AY = 2 * 9.8f; // Gravedad

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calcular deltaTime (en segundos)
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - startTime) / 1e9f;
        startTime = currentTime;

        // Actualizar la posición de la bola
        updateBallPosition();

        // Calcular los límites de la línea basados en lineX
        float startX = lineX - 200; // Coordenada de inicio 200 unidades a la izquierda de lineX
        float endX = lineX + 200;   // Coordenada de fin 200 unidades a la derecha de lineX

        // Detectar y manejar colisiones con las paredes y la línea
        handleCollisions();

        // Dibujar la bola
        canvas.drawCircle(x, y, radius, paint);

        // Dibujar la línea usando los límites calculados
        canvas.drawLine(startX, lineY, endX, lineY, paintLine);

        // Actualizar el TextView (si existe)
        if (textView != null) {
            textView.setText(String.valueOf(count));
        }

        // Forzar un nuevo dibujo para la animación continua
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Inicializar la posición de la línea en Y
        lineY = h * 0.8f; // Posición de la línea en el 80% de la altura de la vista
        lineX = w / 2f; // Posición inicial de la línea en el centro horizontal de la vista
    }

    private void updateBallPosition() {
        y += vy * deltaTime;
        vy += ay * deltaTime;

        x += vx * deltaTime;
        vx += ax * deltaTime;
        ax = ax * 1.0001f;
    }

    private void handleCollisions() {
        float startX = lineX - 200;
        float endX = lineX + 200;

        if (x + radius <= 0) { // Pared izquierda
            vx *= -1.1; // Incrementar velocidad
            x = radius;
            playRandomSound();
        } else if (x - radius >= getWidth()) { // Pared derecha
            vx *= -1.1; // Incrementar velocidad
            x = getWidth() - radius;
            playRandomSound();
        }

        if (y + radius <= 0) { // Pared superior
            vy *= -1.1; // Incrementar velocidad
            y = radius;
            playRandomSound();
        } else if (y - radius >= lineY - paintLine.getStrokeWidth() / 2 && x >= startX && x <= endX) { // Línea horizontal
            vy *= -1.1; // Incrementar velocidad

            // Calcular el punto de impacto en la línea
            float impactPoint = x - startX;
            float normalizedImpactPoint = impactPoint / (endX - startX); // Normalizar el punto de impacto entre 0 y 1
            float maxAngle = (float) Math.PI / 4; // Ángulo máximo de rebote (45 grados)

            // Calcular el ángulo de rebote basado en el punto de impacto
            float angle = (normalizedImpactPoint - 0.5f) * maxAngle * 2; // Ajustar el ángulo a un rango amplio

            // Ajustar las velocidades basadas en el ángulo
            float newVx = (float) (vx * Math.cos(angle) - vy * Math.sin(angle));
            float newVy = (float) (vx * Math.sin(angle) + vy * Math.cos(angle));

            vx = newVx;
            vy = newVy;

            // Asegurar que la bola no quede atrapada dentro de la línea
            y = lineY - radius; // Ajustar la posición Y para asegurar el rebote
            count++;

            if (listener != null) {
                listener.onCountChanged(count); // Notificar al oyente
            }
            playRandomSound();
        } else if (y - radius >= getHeight()) { // Pared inferior
            resetBallPosition();
        }
        int refresh = 16;
        //Log.i("BallView","Subcounter: "+subcounter);
        if (subcounter >= refresh){
            //Log.i("Flexion", "Reincia subcontador");
            Bluetooth.blue.bluTx("0");
            Bluetooth.blue.bluTx("6");
            subcounter = 0;
        } else {subcounter ++;}
    }
    private void resetBallPosition() {
        x = INITIAL_X;
        y = INITIAL_Y;
        vx = INITIAL_VX;
        vy = INITIAL_VY;
        ax = INITIAL_AX;
        ay = INITIAL_AY;
        count = 0;

        if (listener != null) {
            listener.onCountChanged(count); // Notificar al oyente
        }
    }

    public BallView(Context context) {
        super(context);
        init(context);
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        x = INITIAL_X;
        y = INITIAL_Y;
        radius = 50;
        vx = INITIAL_VX;
        vy = INITIAL_VY;
        ax = INITIAL_AX;
        ay = INITIAL_AY;
        startTime = System.nanoTime();

        // Crear objeto Paint
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        // Configurar la línea
        paintLine.setColor(Color.BLACK); // Color negro
        paintLine.setStrokeWidth(40); // Grosor de la línea
        paintLine.setStyle(Paint.Style.STROKE); // Estilo de línea (contorno)

        // Inicializar el Handler
        handler = new Handler(Looper.getMainLooper());
    }

    public void updateBluetoothData(float bluetoothData) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Ajustar bluetoothData al rango deseado para lineX (por ejemplo, -15 a 45 grados)
                if (bluetoothData <= MIN_ANGLE) {
                    lineX = 0; // Línea en el borde izquierdo
                } else if (bluetoothData >= MAX_ANGLE) {
                    lineX = getWidth(); // Línea en el borde derecho
                } else {
                    float normalizedData = (bluetoothData - MIN_ANGLE) / (MAX_ANGLE - MIN_ANGLE); // Normalizar al rango 0 a 1
                    lineX = normalizedData * getWidth(); // Distribuir linealmente entre el borde izquierdo y derecho
                }
                // Forzar la vista a redibujarse con la nueva posición de la línea
                invalidate();
            }
        });
    }

    public void setListener(BallViewListener listener) {
        this.listener = listener;
    }

    private void playRandomSound() {
        // Liberar el MediaPlayer anterior si existe
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Seleccionar un sonido aleatorio
        int soundId;
        switch (random.nextInt(3)) {
            case 0:
                soundId = R.raw.ball_sound1;
                break;
            case 1:
                soundId = R.raw.ball_sound2;
                break;
            case 2:
            default:
                soundId = R.raw.ball_sound3;
                break;
        }

        // Crear un nuevo MediaPlayer y reproducir el sonido
        mediaPlayer = MediaPlayer.create(context, soundId);
        mediaPlayer.start();
    }
}


