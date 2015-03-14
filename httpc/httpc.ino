#include <Bridge.h>
#include <HttpClient.h>

void setup() {
	pinMode(13, OUTPUT);
	digitalWrite(13, LOW);
	Bridge.begin();
	Serial.begin(9600);
	while(!Serial);
}

void loop() {
	HttpClient client;
	//client.get("http://arduino.cc/asciilogo.txt");
	client.get("http://206.12.53.185:12000/");

	while (client.available()) {
		char c = client.read();
		Serial.print(c);
	}
	Serial.flush();

	delay(5000);
}
