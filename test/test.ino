#include <Bridge.h>
#include <HttpClient.h>

void setup() {
	pinMode(13, OUTPUT);
	pinMode(7, INPUT);

	digitalWrite(13, LOW);
	Serial.begin(115200);
	Serial.println("Starting bridge...");
	Bridge.begin();
	digitalWrite(13, HIGH);

}

String host = "http://206.12.53.185:12000/";
String hostR = "http://206.12.53.185:12000/put/";
String buffer = "";
//IPAddress ip(206, 12, 53, 185);
//int port = 12000;
int counter = 0;
void loop() {
	counter++;
	int result = analogRead(A0);
	Serial.println(result);
	digitalWrite(13, HIGH);
	delay(100);
	digitalWrite(13, LOW);
	delay(100);

	buffer += String(result) + ',';
	/*
	HttpClient client;
	Serial.println("default get" + host);
	client.get(host);
	while (client.available()) {
		char c = client.read();
		Serial.print(c);
	}
	Serial.flush();
	*/

	if(counter % 2 == 0) {
		counter = 0;
		if(buffer.length() > 0) {
			String complete = hostR + buffer;

			Serial.println("Start send: " + complete);
			HttpClient client;
			client.get(complete);
			//client.getAsynchronously(host);
			while (client.available()) {
				char c = client.read();
				Serial.print(c);
			}
			Serial.flush();
			buffer = "";
		}
	}
	

	delay(10000);
}
