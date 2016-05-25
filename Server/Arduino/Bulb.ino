#define rxPin 2
#define txPin 3
#define relayPin 7

char rx_data;	// 받은 데이터

void setup()
{
	Serial.begin(19200);
	pinMode(relayPin, OUTPUT);
}

void loop()
{
	if(Serial.available() > 0) {
		rx_data = Serial.read();

		if(rx_data == '1'){
			Serial.println("received data 1");
			digitalWrite(relayPin, LOW);
		}
		else if(rx_data == '2'){
			Serial.println("received data 2");
			digitalWrite(relayPin, HIGH);
		}
	}
}