
// Define various ADC prescaler
const byte shake[] = {0,179,162,110,72};
uint16_t temp = 0;
uint8_t tempAvg = 0;
uint8_t fanSpeed = 0;

void setup() {
  DDRB |= B00111111;
  analogWrite(6, 0);
  Serial.begin(250000);
  while(Serial.available()) Serial.read(); //Flush serial buffer
  DIDR0 = B11111111; //Turns off digital input on pins A0-A5 (PORTC) to decrease noise to ADC and current load
  while(Serial.read()); //wait for command from GUI
  Serial.write(shake, 5); //Sent hand shake
  delay(10);
  while(Serial.read()); //Wait for reply
  delay(10);
  while(Serial.available()) Serial.read(); //Flush serial buffer
  while(Serial.read()){ //Stream temperature data until given stop command
    temp = 0; //Reset temp reading
    for(int a = 0; a<64; a++){ //Get average of 64 readings
      temp += analogRead(2);
    }
    tempAvg = temp >> 8;
    if(temp < 36000) { //If temp is greater than 40oC, turn on fan
      if(temp > 30500){ //If temp is below 50oC use intermediate speed
        fanSpeed = (36000-temp)/23 + 14; //14 is min on
      }
      else fanSpeed = 255;
      analogWrite(6, fanSpeed);

      if(temp < 25500){ //If temp is above 60oC, sound alarm
          for(int b=0; b<3500; b++){ //Generate tone for 0.1 seconds
            PORTB |= B00010000;
            delayMicroseconds(128);
            PORTB &= B11101111;
            delayMicroseconds(128);
          }
      }
    }
    else{
      analogWrite(6,0);
    }
    Serial.write(tempAvg);
  }
}

void loop() {

}
