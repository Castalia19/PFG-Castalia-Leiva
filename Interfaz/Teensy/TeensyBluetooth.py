import time
import math
import serial
from pyfirmata import Arduino, util

# Configurar la placa Arduino y la comunicación Bluetooth
board = Arduino('COM3')  # Ajusta el puerto según tu sistema
bluetoothSerial = serial.Serial('COM3', 9600)  # Ajusta el puerto según tu sistema

# Configurar los pines analógicos
it = util.Iterator(board)
it.start()
board.analog[0].enable_reporting()
board.analog[2].enable_reporting()
board.analog[3].enable_reporting()

R100 = 99.5  # Valor de la resistencia de protección en ohms
VCC = 3.3    # Valor de la tensión de referencia en voltios
ad = False   # Indica si el movimiento horizontal de la muñeca es aducción
ext = False  # Indica si el movimiento vertical de la muñeca es extensión
pos_resorte = 4  # Selección de la posición del resorte

ref_ang_flexex = 208.08  # Ángulo neutro de referencia del movimiento de flexión y de extensión
ref_ang_abad = 176.22    # Ángulo neutro de referencia del movimiento de aducción y abducción

L1 = 0.01525
L2 = 0.02092
L3 = 0.02885
L4 = 0.03832
ref_ang_resorte1 = 144.39
ref_ang_resorte2 = 137.46
ref_ang_resorte3 = 153.44
ref_ang_resorte4 = 143.59

ang_res = 0.0
fuerza = 0.0

ultimoRecibido = '0'
enviarDatos = False

def leer_potenciometros():
    lectura_pot1 = board.analog[0].read()
    lectura_pot2 = board.analog[2].read()
    lectura_pot3 = board.analog[3].read()
    
    if lectura_pot1 is None:
        lectura_pot1 = 0
    if lectura_pot2 is None:
        lectura_pot2 = 0
    if lectura_pot3 is None:
        lectura_pot3 = 0
    
    pot1 = (lectura_pot1 * 1023.0) * 10970.0
    pot2 = (lectura_pot2 * 1023.0) * 9550.0
    pot3 = (lectura_pot3 * 1023.0) * 10130.0
    
    return pot1, pot2, pot3

def calcular_angulos(pot1, pot2, pot3):
    angulo_pot1 = 0.0251 * pot1 + 39.046
    angulo_pot2 = 0.0284 * pot2 + 45.072
    angulo_pot3 = 0.0269 * pot3 + 41.834
    
    ang_flexex = ref_ang_flexex - angulo_pot1
    ang_abad = angulo_pot2 - ref_ang_abad
    
    ext = ang_flexex > 0
    ad = ang_abad > 0
    
    return ang_flexex, ang_abad, angulo_pot3

def enviar_datos(ultimoRecibido, angulo_pot3, ang_flexex, ang_abad):
    if ultimoRecibido == '1':
        ang_res = (angulo_pot3 - ref_ang_resorte1) * (math.pi / 180.0)
        fuerza = (12551.13371 * L1 * math.sin(ang_res)) / 9.74
        bluetoothSerial.write(f"{fuerza}\n".encode())
    elif ultimoRecibido == '2':
        ang_res = (angulo_pot3 - ref_ang_resorte2) * (math.pi / 180.0)
        fuerza = (12551.13371 * L2 * math.sin(ang_res)) / 9.74
        bluetoothSerial.write(f"{fuerza}\n".encode())
    elif ultimoRecibido == '3':
        ang_res = (angulo_pot3 - ref_ang_resorte3) * (math.pi / 180.0)
        fuerza = (13806.24708 * L3 * math.sin(ang_res)) / 9.74
        bluetoothSerial.write(f"{fuerza}\n".encode())
    elif ultimoRecibido == '4':
        ang_res = (angulo_pot3 - ref_ang_resorte4) * (math.pi / 180.0)
        fuerza = (13806.24708 * L4 * math.sin(ang_res)) / 9.74
        bluetoothSerial.write(f"{fuerza}\n".encode())
    elif ultimoRecibido == '5':
        bluetoothSerial.write(f"{ang_flexex}\n".encode())
    elif ultimoRecibido == '6':
        bluetoothSerial.write(f"{ang_abad}\n".encode())
    elif ultimoRecibido == '7':
        bluetoothSerial.write(f"{ang_flexex}/{ang_abad}\n".encode())

def main():
    while True:
        pot1, pot2, pot3 = leer_potenciometros()
        ang_flexex, ang_abad, angulo_pot3 = calcular_angulos(pot1, pot2, pot3)
        
        if bluetoothSerial.in_waiting > 0:
            recibido = bluetoothSerial.read().decode()
            if '0' <= recibido <= '7':
                ultimoRecibido = recibido
        
        enviar_datos(ultimoRecibido, angulo_pot3, ang_flexex, ang_abad)
        
        time.sleep(0.2)

if __name__ == "__main__":
    main()
