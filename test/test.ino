#include <Bridge.h>
#include <HttpClient.h>

void setup() {
	pinMode(13, OUTPUT);
	pinMode(7, INPUT);
	pinMode(2, OUTPUT);

	digitalWrite(13, LOW);
	Serial.begin(115200);
	Serial.println("Starting bridge...");
	Bridge.begin();
	digitalWrite(13, HIGH);

}
int readHertz = 30;
int putHertz = 5;
String host = "http://206.12.53.185:12000/";
String hostR = "http://206.12.53.185:12000/put/";
String buffer = "";
//IPAddress ip(206, 12, 53, 185);
//int port = 12000;
int counter = 0;
void loop() {
	digitalWrite(13, LOW);
	counter++;
	int result = analogRead(A0);
	Serial.println(result);

	buffer += String(result) + ',';

	if(counter % (readHertz / putHertz) == 0) {
		Serial.println("putHertz tick");
		counter = 0;
		if(buffer.length() > 0) {
			String complete = hostR + buffer;

			Serial.println("Start send: " + complete);
			digitalWrite(2, HIGH);
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
	digitalWrite(2, LOW);
	digitalWrite(13, HIGH);
	delay(1000/readHertz);
}



