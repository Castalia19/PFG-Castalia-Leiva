#include <SoftwareSerial.h>
SoftwareSerial bluetoothSerial(7, 8); // RX, TX pines 8 y 7 del Teensy.

const float R100 = 99.5;    // Valor de la resistencia de protección en ohms
float pot1;                 // Valor del potenciómetro 1 variable en ohms
float pot2;                 // Valor del potenciómetro 2 variable en ohms
float pot3;                 // Valor del potenciómetro 3 variable en ohms
const float VCC = 3.3;      // Valor de la tensión de referencia en voltios (confirmado por el usuario)
bool ad = 0;                // Valor booleano que indica 1 si el movimiento horizontal de la muñeca es aducción y 0 si es abducción.
bool ext = 0;               // Valor booleano que indica 1 si el movimiento vertical de la muñeca es extensión y 0 si es flexión.
int pos_resorte = 4;        // Selección de la posición del resorte, 1 y 2 usan el resorte 1, 3 y 4 utilizan el resorte 2.

float ref_ang_flexex = 208.08; // Ángulo neutro de referencia del movimiento de flexión y de extensión.
float ref_ang_abad = 176.22;   // Ángulo neutro de referencia del movimiento de aducción y abducción.

float L1 = 0.01525;                  // Longitud del potenciómetro la posición del resorte 1.
float L2 = 0.02092;                  // Longitud del potenciómetro la posición del resorte 1.
float L3 = 0.02885;                  // Longitud del potenciómetro la posición del resorte 1.
float L4 = 0.03832;                  // Longitud del potenciómetro la posición del resorte 1.
float ref_ang_resorte1 = 159.30; // Ángulo de referencia del inicio del resorte 1 en posición 1.
float ref_ang_resorte2 = 147.85; // Ángulo de referencia del inicio del resorte 1 en posición 2.
float ref_ang_resorte3 = 164.10; // Ángulo de referencia del inicio del resorte 2 en posición 3.
float ref_ang_resorte4 = 152.38; // Ángulo de refer57.28encia del inicio del resorte 2 en posición 4.
float ang_res;                  // Variable para identificar el desplazamiento del resorte.
float fuerza = 0;               // Fuerza de agarre, obtenida al variar el potenciómetro 3 dependiendo del resorte utilizado.

char ultimoRecibido = '0'; // Variable para almacenar el último comando recibido
bool enviarDatos = false;  // Variable para controlar el envío de datos

void setup() {
  Serial.begin(9600); // Inicializar el puerto serial
  analogReference(DEFAULT); // Configurar la referencia de voltaje por defecto
  bluetoothSerial.begin(9600); // Inicializar el puerto serial para el módulo Bluetooth HC-05
}

void loop() {

  // Leer los valores analógicos
  int lectura_pot1 = analogRead(A0);
  int lectura_pot2 = analogRead(A2);
  int lectura_pot3 = analogRead(A3);

  // Resistencia de los potenciómetros
  pot1 = (lectura_pot1 / 1023.0) * 10970.0;
  pot2 = (lectura_pot2 / 1023.0) * 9550.0;
  pot3 = (lectura_pot3 / 1023.0) * 10130.0;

  // Calculo los ángulos de cada potenciómetro
  float angulo_pot1 = 0.0251 * pot1 + 39.046;
  float angulo_pot2 = 0.0284 * pot2 + 45.072;
  float angulo_pot3 = (0.0269 * pot3 + 41.834);

  // Cálculo de los ángulos respecto a la referencia de movimiento
  float ang_flexex = ref_ang_flexex - angulo_pot1;
  float ang_abad = angulo_pot2 - ref_ang_abad;
  if (ang_flexex > 0) {
    ext = 1;
  } else {
    ext = 0;
  }
  if (ang_abad > 0) {
    ad = 1;
  } else {
    ad = 0;
  }

  // Verificar si se ha recibido un nuevo comando por Bluetooth
  if (bluetoothSerial.available()) {
    char recibido = bluetoothSerial.read();
    if (recibido >= '0' && recibido <= '7') {
      ultimoRecibido = recibido; // Actualizar el último comando recibido
    }
  }

  // Enviar datos según el último comando recibido
  if (ultimoRecibido == '1') {
    ang_res = (angulo_pot3 - ref_ang_resorte1) * (PI / 180.0); // El potenciómetro del resorte debe pasarse a radianes para calcular el seno.
    fuerza = (12551.13371 * L1 * sin(ang_res))/9.74;
    bluetoothSerial.println(String(fuerza));
  } else if (ultimoRecibido == '2') {
    ang_res = (angulo_pot3 - ref_ang_resorte2) * (PI / 180.0); // El potenciómetro del resorte debe pasarse a radianes para calcular el seno.
    fuerza = (12551.13371 * L2 * sin(ang_res))/9.74;
    bluetoothSerial.println(String(fuerza));
  } else if (ultimoRecibido == '3') {
    ang_res = (angulo_pot3 - ref_ang_resorte3) * (PI / 180.0); // El potenciómetro del resorte debe pasarse a radianes para calcular el seno.
    fuerza = (13806.24708 * L3 * sin(ang_res))/9.74;
    bluetoothSerial.println(String(fuerza));
  } else if (ultimoRecibido == '4') {
    ang_res = (angulo_pot3 - ref_ang_resorte4) * (PI / 180.0); // El potenciómetro del resorte debe pasarse a radianes para calcular el seno.
    fuerza = (13806.24708 * L4 * sin(ang_res))/9.74;
    bluetoothSerial.println(String(fuerza));
  } else if (ultimoRecibido == '5') {
    bluetoothSerial.println(String(ang_flexex));
  } else if (ultimoRecibido == '6') {
    bluetoothSerial.println(String(ang_abad));
  } else if (ultimoRecibido == '7') {
    // Aquí ambos datos separados por un delimitador
    bluetoothSerial.print(String(ang_flexex)); // Primer dato
    bluetoothSerial.print("/"); // Delimitador
    bluetoothSerial.println(String(ang_abad)); // Segundo dato
  }
  Serial.println(fuerza);
  delay(200); // Pequeña pausa para evitar lecturas demasiado rápidas
}
