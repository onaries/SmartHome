#include <SoftwareSerial.h>
#define rxPin 2
#define txPin 3
#define switch1Pin 12 
#define switch2Pin 11
#define switch3Pin 10
#define relay1Pin 7
#define relay2Pin 6
#define relay3Pin 5

SoftwareSerial bluetooth(txPin, rxPin); // tx가 3, rx가 2

// 스위치 버튼 초기화
int oldButton1State = LOW;
int oldButton2State = LOW;
int oldButton3State = LOW;
int x1 = 0;
int x2 = 0;
int x3 = 0;

// 수신된 데이터
char rx_data;

void setup()
{
  Serial.begin( 19200 );    // 9600 is the default baud rate for the
                              // serial Bluetooth module
  bluetooth.begin(19200);

  // 핀모드 초기화
  pinMode(switch1Pin, INPUT);
  pinMode(switch2Pin, INPUT);
  pinMode(switch3Pin, INPUT);
  pinMode(relay1Pin, OUTPUT);
  pinMode(relay2Pin, OUTPUT);
  pinMode(relay3Pin, OUTPUT);
}
 
void loop()
{
      
  if(bluetooth.available() > 0){
    rx_data = bluetooth.read();
   
    if(rx_data == '1'){
      Serial.println("received data 1");
      digitalWrite(relay1Pin, LOW);
      bluetooth.write("1");
      x1 = 0;
    }
    else if(rx_data == '2'){
      Serial.println("received data 2");
      digitalWrite(relay2Pin, LOW);
      bluetooth.write("2");
      x2 = 0;
    }
    else if(rx_data == '3'){
      Serial.println("received data 3");
      digitalWrite(relay3Pin, LOW);
      bluetooth.write("3");
      x3 = 0;
    }
    else if(rx_data == '4'){
      Serial.println("received data 4");
      digitalWrite(relay1Pin, HIGH);
      bluetooth.write("4");
      x1 = 1;
    }
    else if(rx_data == '5'){
      Serial.println("received data 5");
      digitalWrite(relay2Pin, HIGH);
      bluetooth.write("5");
      x2 = 1;
    }
    else if(rx_data == '6'){
      Serial.println("received data 6");
      digitalWrite(relay3Pin, HIGH);
      bluetooth.write("6");
      x3 = 1;
    }
    else if(rx_data == '7'){
      Serial.println("received data 7");
      digitalWrite(relay1Pin, LOW);
      digitalWrite(relay2Pin, LOW);
      digitalWrite(relay3Pin, LOW);
      bluetooth.write("7");
      x1 = 0;
      x2 = 0;
      x3 = 0;
      
    }
    else if(rx_data == '8'){
      Serial.println("received data 8");
      digitalWrite(relay1Pin, HIGH);
      digitalWrite(relay2Pin, HIGH);
      digitalWrite(relay3Pin, HIGH);
      bluetooth.write("8");
      x1 = 1;
      x2 = 1;
      x3 = 1;
    }
    else {
    }
  }

  // 버튼 1 
  if (newButton1State == HIGH && oldButton1State == LOW) {
 
    if (x1 == 0) {
      // Toggle on
      digitalWrite(7, HIGH);
      Serial.println("Multitab 1 off");
      x1 = 1;
 
    } else {
      // Toggle off
      digitalWrite(7, LOW);
      Serial.println("Multitab 1 on");
      x1 = 0;
    }
  }

  // 버튼 2
  if (newButton2State == HIGH && oldButton2State == LOW) {
 
    if (x2 == 0) {
      // Toggle on
      digitalWrite(6, HIGH);
      Serial.println("Multitab 2 off");
      x2 = 1;
 
    } else {
      // Toggle off
      digitalWrite(6, LOW);
      Serial.println("Multitab 2 on");
      x2 = 0;
    }
  }

  // 버튼 3
  if (newButton3State == HIGH && oldButton3State == LOW) {
 
    if (x3 == 0) {
      // Toggle on
      digitalWrite(5, HIGH);
      Serial.println("Multitab 3 off");
      x3 = 1;
 
    } else {
      // Toggle off
      digitalWrite(5, LOW);
      Serial.println("Multitab 3 on");
      x3 = 0;
    }
  }
  oldButton1State = newButton1State;
  oldButton2State = newButton2State;
  oldButton3State = newButton3State;
   
  delay(100);
}
