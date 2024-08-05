package com.example.wraistrehab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenieriajhr.blujhr.BluJhr;

import java.util.ArrayList;

public class Bluetooth extends AppCompatActivity {

    public static BluJhr blue; // Hacer la instancia estática para acceder desde otras actividades
    ArrayList<String> devicesBluetooth = new ArrayList<>(); // Lista de dispositivos Bluetooth emparejados
    LinearLayout viewConn; // Layout que se muestra al estar conectado
    ListView listDeviceBluetooth; // ListView para mostrar los dispositivos Bluetooth disponibles
    Button buttonSend; // Botón para enviar datos por Bluetooth
    TextView consola; // TextView para mostrar los datos recibidos
    EditText edtTx; // EditText para ingresar datos a enviar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        // Evitar que la pantalla se suspenda
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Inicializar la instancia de BluJhr
        blue = new BluJhr(this);
        blue.onBluetooth(); // Activar Bluetooth si no está activado

        // Enlazar los elementos de la interfaz con sus IDs
        listDeviceBluetooth = findViewById(R.id.listDeviceBluetooth);
        viewConn = findViewById(R.id.viewConn);
        buttonSend = findViewById(R.id.buttonSend);
        consola = findViewById(R.id.consola);
        edtTx = findViewById(R.id.edtTx);

        // Configurar el click listener para la lista de dispositivos Bluetooth
        listDeviceBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!devicesBluetooth.isEmpty()) {
                    blue.connect(devicesBluetooth.get(i)); // Conectar al dispositivo seleccionado

                    // Configurar el listener para el estado de conexión
                    blue.setDataLoadFinishedListener(new BluJhr.ConnectedBluetooth() {
                        @Override
                        public void onConnectState(@NonNull BluJhr.Connected connected) {
                            if (connected == BluJhr.Connected.True) {
                                Toast.makeText(getApplicationContext(), "Dispositivo conectado", Toast.LENGTH_SHORT).show();
                                listDeviceBluetooth.setVisibility(View.GONE);
                                viewConn.setVisibility(View.VISIBLE);
                                rxReceived(); // Configurar el receptor de datos
                            } else {
                                if (connected == BluJhr.Connected.Pending) {
                                    Toast.makeText(getApplicationContext(), "Emparejando", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (connected == BluJhr.Connected.False) {
                                        Toast.makeText(getApplicationContext(), "Conexión fallida", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (connected == BluJhr.Connected.Disconnect) {
                                            Toast.makeText(getApplicationContext(), "Desconectando", Toast.LENGTH_SHORT).show();
                                            listDeviceBluetooth.setVisibility(View.VISIBLE);
                                            viewConn.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        // Configurar el click listener para el botón de enviar
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blue.bluTx(edtTx.getText().toString()); // Enviar el texto ingresado por Bluetooth
            }
        });

        // Configurar el long click listener para el botón de enviar
        buttonSend.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                blue.closeConnection(); // Cerrar la conexión Bluetooth
                return false;
            }
        });
    }

    // Configurar el receptor de datos Bluetooth
    private void rxReceived() {
        blue.loadDateRx(new BluJhr.ReceivedData() {
            @Override
            public void rxDate(@NonNull String s) {
                consola.setText(consola.getText().toString() + s); // Mostrar los datos recibidos en el TextView
            }
        });
    }

    // Manejar los permisos de Bluetooth
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (blue.checkPermissions(requestCode, grantResults)) {
            Toast.makeText(this, "Salir", Toast.LENGTH_SHORT).show();
            blue.initializeBluetooth();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                blue.initializeBluetooth();
            } else {
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Manejar el resultado de la actividad de configuración de Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!blue.stateBluetoooth() && requestCode == 100) {
            blue.initializeBluetooth();
        } else {
            if (requestCode == 100) {
                devicesBluetooth = blue.deviceBluetooth(); // Obtener la lista de dispositivos emparejados
                if (!devicesBluetooth.isEmpty()) {
                    ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, devicesBluetooth);
                    listDeviceBluetooth.setAdapter(adapter); // Mostrar la lista de dispositivos en el ListView
                } else {
                    Toast.makeText(this, "No hay dispositivos vinculados", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


