#include <Bridge.h>
#include <HttpClient.h>

//EthernetClient client;

void setup() {
	pinMode(13, OUTPUT);
	pinMode(7, INPUT);

	digitalWrite(13, LOW);
	Bridge.begin();
	Serial.begin(115200);
	digitalWrite(13, HIGH);

}

void loop() {
	int result = analogRead(A0);
	Serial.println(result);
	digitalWrite(13, HIGH);
	delay(100);
	digitalWrite(13, LOW);
	delay(100);


	HttpClient client;
	client.get("http://206.12.53.185:12000/put/" + "30");

	while (client.available()) {
		char c = client.read();
		Serial.print(c);
	}
	Serial.flush();
	delay(600);
	//Serial.println(f);
}
