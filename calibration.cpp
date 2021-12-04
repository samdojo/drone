#include <Arduino.h>
#include <ESP32Servo.h> // ESP32Servo library installed by Library Manager
#include "ESC.h" // RC_ESP library installed by Library Manager

#define ESC_PIN 17
#define MIN_SPEED 1000 // speed just slow enough to turn motor off
#define MAX_SPEED 2000 // speed where my motor drew 3.6 amps at 12v.

ESC myESC (ESC_PIN, 1000, 2000, 500); // ESC_Name (PIN, Minimum Value, Maximum Value, Arm Value)

void setup() {
  Serial.begin(9600);
  pinMode(ESC_PIN, OUTPUT);
  digitalWrite(ESC_PIN, LOW);
  myESC.arm();
  Serial.println("MAX");
  myESC.speed(2000);
  delay(10000);
  Serial.println("MIN");
  myESC.speed(1000);
  delay(10000);
  for (int i=MIN_SPEED; i<MAX_SPEED; i++) {
    myESC.speed(i);
    Serial.println(i);
    delay(100);
  }
}

void loop() {
}

