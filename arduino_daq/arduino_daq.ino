#include <DueTimer.h>

const int SAMPLE_FREQ = 20000;
const int D_OUT_1 = 22;
const int CHANNEL_BUFFER_SIZE = 1000;
const int BUFFER_SIZE = 9 * CHANNEL_BUFFER_SIZE;

byte ping_buffer[BUFFER_SIZE];
byte pong_buffer[BUFFER_SIZE];
byte *current_buffer = ping_buffer;
int idx = 0;

boolean on = false;

void setup() {  
  pinMode(D_OUT_1, OUTPUT);
  SerialUSB.begin(0);
  while(!SerialUSB);
  Timer.getAvailable().attachInterrupt(handler)
    .setFrequency(SAMPLE_FREQ).start();
}

void loop() {
}

void handler() {
  // Sample each of the analog inputs
  for (int chan = 0; chan < 9; chan++) {
    int val = analogRead(chan);
//    int val = 'a' + chan;
    current_buffer[idx + chan * CHANNEL_BUFFER_SIZE] = (byte) ((val >> 8) & 0xff);
    current_buffer[idx + 1 + chan * CHANNEL_BUFFER_SIZE] = (byte) (val & 0xff);
  }
  
  idx += 2;
  
  // Writes the data and swaps buffers
  if (CHANNEL_BUFFER_SIZE == idx) {
    idx = 0;
    
    SerialUSB.write(current_buffer, BUFFER_SIZE);
    
    if (current_buffer == ping_buffer) {
      current_buffer = pong_buffer;
    } else {
      current_buffer = ping_buffer;
    }
  }
  
  // Used to test sample frequency with oscilloscope
  if (on) {
    digitalWrite(D_OUT_1, LOW);
  } else {
    digitalWrite(D_OUT_1, HIGH);
  }
  on = !on;
}
