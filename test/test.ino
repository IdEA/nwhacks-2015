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

#define THRESHOLD 300
int readHertz = 100;
String hostR = "http://128.189.236.224:12000/put/";
bool prevIsFlux = false;

void loop() {
	digitalWrite(13, LOW);
	int result = analogRead(A0);
	Serial.println(result);

	if(result > THRESHOLD) {
		// don't send it
		if(prevIsFlux) {
			prevIsFlux = false;
			// send it
			String complete = hostR + String(0);
			digitalWrite(2, HIGH);
			HttpClient client;
			client.get(complete);
			while (client.available()) {
				char c = client.read();
			}
		}
	} else
		prevIsFlux = true;

	digitalWrite(2, LOW);
	digitalWrite(13, HIGH);
	delay(1000/readHertz);
}

