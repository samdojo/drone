#include <Arduino.h>
#include <WiFi.h>
#include <ESP32Servo.h> // ESP32Servo library installed by Library Manager
#include "ESC.h" // RC_ESP library installed by Library Manager
#include "AsyncUDP.h"

#define ESC_PIN_FRONT_LEFT 19
#define ESC_PIN_FRONT_RIGHT 14
#define ESC_PIN_BACK_LEFT 25
#define ESC_PIN_BACK_RIGHT 17
//#define OTHER 4

#define MIN_SPEED 1090
#define MAX_SPEED 1500

#define SSID "DRONE"
#define PORT 8989

ESC esc_front_left (ESC_PIN_FRONT_LEFT, 1000, 2000, 500); // ESC_Name (PIN, Minimum Value, Maximum Value, Arm Value)
ESC esc_front_right (ESC_PIN_FRONT_RIGHT, 1000, 2000, 500);
ESC esc_back_left (ESC_PIN_BACK_LEFT, 1000, 2000, 500);
ESC esc_back_right (ESC_PIN_BACK_RIGHT, 1000, 2000, 500);

AsyncUDP udp;

struct Point {
  double x;
  double y;
  double z;
};

void init_ESCs() {
  pinMode(ESC_PIN_FRONT_LEFT, OUTPUT);
  pinMode(ESC_PIN_FRONT_RIGHT, OUTPUT);
  pinMode(ESC_PIN_BACK_LEFT, OUTPUT);
  pinMode(ESC_PIN_BACK_RIGHT, OUTPUT);

  esc_front_left.arm();
  esc_front_left.speed(1000);
  
  esc_front_right.arm();
  esc_front_right.speed(1000);
  
  esc_back_left.arm();
  esc_back_left.speed(1000);
  
  esc_back_right.arm();
  esc_back_right.speed(1000);
}

void setup() {
  Serial.begin(9600);
  Serial.println("starting...");

  init_ESCs();

  WiFi.softAP(SSID);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("server IP address: ");
  Serial.println(IP);

  udp.listen(PORT);
  udp.onPacket([](AsyncUDPPacket packet) {
    const char* raw_msg = reinterpret_cast<char*>(packet.data());
    String msg(raw_msg);
    Serial.println(msg);
    int speed = msg.substring(2).toInt();
    constexpr float percent_speed = (MAX_SPEED - MIN_SPEED) / 100.0;
    int new_speed = int(percent_speed * speed) + MIN_SPEED;
    String command = msg.substring(0, 2);
    if (command == "FL") {
      esc_front_left.speed(new_speed);
    }
    else if (command == "FR") {
      esc_front_right.speed(new_speed);
    }
    else if (command == "BL") {
      esc_back_left.speed(new_speed);
    }
    else if (command == "BR") {
      esc_back_right.speed(new_speed);
    }
    else if (command == "ON") {
      esc_front_left.speed(MIN_SPEED);
      esc_front_right.speed(MIN_SPEED);
      esc_back_left.speed(MIN_SPEED);
      esc_back_right.speed(MIN_SPEED);
    }
    else if (command == "OF") {
      esc_front_left.speed(0);
      esc_front_right.speed(0);
      esc_back_left.speed(0);
      esc_back_right.speed(0);
    }
    else {
      Serial.println("ERROR: recieved unknown command");
    }
  });
  delay(8000);
  Serial.println("...ready");
}

double sqr(double num) {
  return num * num;
}

double length_squared(Point a, Point b) {
  return sqr(b.x - a.x) + sqr(b.y - a.y) + sqr(b.z - a.z);
}

double error(Point current_position, Point desired_position, Point calibrated_position) {
  double numerator = length_squared(current_position, desired_position)
                   + length_squared(calibrated_position, desired_position)
                   - length_squared(current_position, calibrated_position);
  double denominator = 2.0 * sqrt(length_squared(calibrated_position, desired_position));
  return numerator / denominator;
}

double error_slope(double current_error) {
  static double previous_error = 0.0;
  double slope = current_error - previous_error;
  previous_error = current_error;
  return slope;
}

double error_integral(double current_error) {
  static double total_error = 0.0;
  total_error += current_error;
  return total_error;
}

void loop() {
}
