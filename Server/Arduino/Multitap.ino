#include <SoftwareSerial.h>
#define rxPin 3
#define txPin 2
#define switch1Pin 12
#define switch2Pin 11
#define switch3Pin 10
#define relay1Pin 8
#define relay2Pin 6
#define relay3Pin 5

SoftwareSerial bluetooth(txPin, rxPin); // tx가 3, rx가 2

// 수신된 데이터
char rx_data;

// 스위치 변수
int x1 = 0;
int x2 = 0;
int x3 = 0;

// ACS712 핀 상수
const int analog1Pin = A0;
const int analog2Pin = A1;
const int analog3Pin = A2;

// ACS712 변수
int mVperAmp = 100; // use 100 for 20A Module and 66 for 30A Module
int RawValue1 = 0;
int RawValue2 = 0;
int RawValue3 = 0;
double Voltage1 = 0;
double VRMS1 = 0;
double Amps1 = 0;
double Voltage2 = 0;
double VRMS2 = 0;
double Amps2 = 0;
double Voltage3 = 0;
double VRMS3 = 0;
double Amps3 = 0;

void setup() {
  // put your setup code here, to run once:
	Serial.begin(19200);
	bluetooth.begin(19200);

	pinMode(relay1Pin, OUTPUT);
	pinMode(relay2Pin, OUTPUT);
	pinMode(relay3Pin, OUTPUT);
}

void loop() {

	// 전류 계산
	Voltage1 = getVPP(analog1Pin);
	VRMS1 = (Voltage1 / 2.0) * 0.707;
	Amps1 = (VRMS1 * 1000) / mVperAmp;
	Voltage2 = getVPP(analog2Pin);
	VRMS2 = (Voltage2 / 2.0) * 0.707;
	Amps2 = (VRMS2 * 1000) / mVperAmp;
	Voltage3 = getVPP(analog3Pin);
	VRMS3 = (Voltage3 / 2.0) * 0.707;
	Amps3 = (VRMS3 * 1000) / mVperAmp;

	Serial.print("Amps1 = ");
	Serial.print(Amps1);
	Serial.print(", Amps2 = ");
	Serial.print(Amps2);
	Serial.print(", Amps3 = ");
	Serial.println(Amps3);

  	// put your main code here, to run repeatedly:
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
}

float getVPP(int sensorIn)
{
	float result;

	int readValue;             //value read from the sensor
	int maxValue = 0;          // store max value here
	int minValue = 1024;          // store min value here

	uint32_t start_time = millis();
	while((millis()-start_time) < 1000) //sample for 1 Sec
	{
		readValue = analogRead(sensorIn);
		// see if you have a new maxValue
		if (readValue > maxValue)
		{
			/*record the maximum sensor value*/
			maxValue = readValue;
		}
		if (readValue < minValue)
		{
			/*record the maximum sensor value*/
			minValue = readValue;
		}
	}

	// Subtract min from max
	result = ((maxValue - minValue) * 5.0)/1024.0;

	return result;
}