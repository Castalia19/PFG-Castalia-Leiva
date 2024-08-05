package com.example.wraistrehab;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;
import com.opencsv.CSVWriter;

public class Utility {
    private static final String[] HEADERS = {"Hora", "Fuerza", "Flex/Ext", "Abd/Aduc", "Rotacion"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static HandlerThread handlerThread = new HandlerThread("CSVWriterThread");
    private static Handler handler;

    static {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public static void writeDataToCsv(final String fileName, final String data, final int column) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Log.i("Csv", "Entra a Utility");

                // Obtener el archivo CSV en la carpeta de documentos
                File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);

                // Crear el archivo si no existe y agregar encabezados
                //Log.i("Csv", "csv Existe: " + csvFile.exists());
                boolean fileExists = csvFile.exists();

                try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true))) {
                    if (!fileExists) {
                        writer.writeNext(HEADERS);
                    }

                    // Obtener la hora actual
                    String currentTime = DATE_FORMAT.format(new Date());

                    // Preparar nueva fila con los datos
                    String[] newRow = new String[5]; // Ahora hay 5 columnas
                    newRow[0] = currentTime;

                    // Si el índice de columna es válido, asignar el dato a la columna correspondiente
                    if (column >= 1 && column <= 4) {
                        newRow[column] = data; // Asignar el dato en la columna correcta
                    }

                    // Añadir nueva fila
                    writer.writeNext(newRow);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
