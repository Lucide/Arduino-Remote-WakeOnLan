#include <Ethernet.h>
#include <EthernetUDP.h>

#include <BlockDriver.h>
#include <FreeStack.h>
#include <MinimumSerial.h>
#include <SdFat.h>
#include <SdFatConfig.h>
#include <SysCall.h>
#include <sdios.h>

//-CONFIGURATION--------------------------------------------

// logSd filename
#define FILENAME "logSd.txt"
// MAC address
#define MAC 0xBA, 0xD0, 0xC0, 0xFF, 0xEE, 0x00

// remote trigger server's ip address
#define IP_TRIGGER 1, 1, 1, 1
// remote trigger server's port
#define PORT_TRIGGER 14096
// string the program will look for on the response
// to tell if the arduino is still logged in the captive portal
#define RESPONSE_ID "accept"
// local network broadcast ip
#define IP_BROADCAST 255, 255, 255, 255
// the mac addres the WOL packet will be sent to
#define MAC_TARGET 0xFE, 0xED, 0xBE, 0xEF, 0xBA, 0xBE

// captive portal's ip address
#define IP_CAPTIVE 10, 1, 0, 1
// captive portal's username and password url-encoded
#define USERNAME "username"
#define PASSWORD "password"

// pins
uint8_t const sdChip= 4;
uint8_t const ethernetChip= 10;
uint8_t const errorLedPin= 9;

#define DEBUG

#define HTTP

//-UTILS----------------------------------------------------

#define B_TRUE(bitmask) (flags)|= (bitmask)
#define B_FALSE(bitmask) (flags)&= ~(bitmask)
#define B_READ(bitmask) ((flags) & (bitmask))
#define ERROR_LED(status) digitalWrite(errorLedPin, (status))

#ifdef DEBUG
#define DEBUG_INIT()  \
  Serial.begin(9600); \
  delay(1000)
#define DEBUG_PRINT(...) Serial.print(__VA_ARGS__)
#define DEBUG_PRINTLN(...) Serial.println(__VA_ARGS__)
#else
#define DEBUG_INIT()
#define DEBUG_PRINT(...)
#define DEBUG_PRINTLN(...)
#endif

#ifdef HTTP
#define RESPONSE_PRINTLN(S) \
  logSd(F("{"));            \
  logSd(S);                 \
  logSd(F("}"))
#define REQUEST_PRINTLN(S) \
  logSd(F("REQ: "));       \
  logSd(S)
#else
#define RESPONSE_PRINTLN(S)
#endif

//-DATA-----------------------------------------------------

// flags a connection check request is pending
#define B_HTTP_PENDING B00000001
// flags a connection check is pending
#define B_ID_PENDING B00000010

// various bit flags stored as 8bit pack
uint8_t flags= 0;
// carriage return flags for logSd()
enum EndLine {
  CN= 0, // continue
  NL= 1  // newline
};
// file system object.
SdFat sd;
// logSd file.
SdFile file;
// ethernet client object
EthernetClient captiveClient;
EthernetClient triggerClient;
// servers
IPAddress const captiveServer(IP_CAPTIVE);
IPAddress const triggerServer(IP_TRIGGER);
// millis() at the last connection, triggerTime starts shifted by 30s
uint32_t lastCaptiveTime= 0;
uint32_t lastTriggerTime= 30000;
// state machines' state to detect "HTTP/1.1" and "Content-Length: 1621"
uint8_t *const states= (uint8_t[]){0, 0};

void chipSelect(uint8_t const chip) {
  digitalWrite(14 - chip, HIGH);
  digitalWrite(chip, LOW);
  delay(1);
}

void initSd() {
  uint8_t attempt;
  DEBUG_PRINTLN(F("itializing sd"));
  // initialize at the highest speed supported by the board that is
  // not over 50 MHz. Try a lower speed if SPI errors occur.
  for(attempt= 0; attempt < 3; attempt++) {
    if(sd.begin(sdChip, SD_SCK_MHZ(50))) {
      DEBUG_PRINTLN(F("sd card initialized"));
      return;
    }
    DEBUG_PRINTLN(F("attempt to initialize sd card failed"));
    delay(5000);
  }
  DEBUG_PRINTLN(F("sd card initialization failed"));
}

template<class T>
void logSd(T msg, EndLine const endLine= NL) {
  uint8_t attempt;
  char fileName[13]= FILENAME;
  chipSelect(sdChip);
  for(attempt= 0; attempt < 3; attempt++) {
    if(file.open(fileName, O_WRONLY | O_CREAT | O_APPEND)) {
      if(endLine) {
        file.println(msg);
        DEBUG_PRINTLN(msg);
      } else {
        file.print(msg);
        DEBUG_PRINT(msg);
      }
      if(!file.getWriteError()) {
        file.close();
        break;
      }
      DEBUG_PRINTLN(F("write failed"));
      file.close();
    } else {
      DEBUG_PRINTLN(F("file open failed"));
    }
    // reinitializes ethernet if the second attempt fails
    if(attempt == 1) {
      initSd();
    } else {
      delay(1000);
    }
  }
  if(attempt == 3) {
    ERROR_LED(HIGH);
    DEBUG_PRINTLN(F("log to sd failed"));
  }
  chipSelect(ethernetChip);
}

void initEthernet() {
  uint8_t mac[]= {MAC};
  Ethernet.init(ethernetChip);
  // loops indefinitely as there's no point continuing without connection
  while(true) {
    logSd(F("initializing ethernet"));
    if(!Ethernet.begin(mac)) {
      logSd(F("failed to configure using DHCP"));
      if(Ethernet.hardwareStatus() == EthernetNoHardware) {
        logSd(F("ethernet shield not found"));
      } else {
        if(Ethernet.linkStatus() == LinkOFF) {
          logSd(F("ethernet cable disconnected"));
        }
      }
      delay(5000);
    } else {
      break;
    }
  }
  logSd(F("connected, ip "), CN);
  logSd(Ethernet.localIP());
  // give the Ethernet shield a second to initialize
  delay(1000);
}

bool connectTo(EthernetClient &client, IPAddress const &ip, uint16_t const port, uint8_t insist= true) {
  uint8_t attempt;
  client.stop();
  for(attempt= 0; attempt < (insist ? 3 : 1); attempt++) {
    if(client.connect(ip, port)) {
      logSd(F("connected to "), CN);
      logSd(ip);
      return true;
    } else {
      logSd(F("attempt to "), CN);
      logSd(ip, CN);
      logSd(F(" failed, retrying"));
      // reinitializes ethernet if the second attempt fails
      if(attempt == 1) {
        initEthernet();
      } else {
        delay(1000);
      }
    }
  }
  if(attempt == 3 || !insist) {
    logSd(F("connection to "), CN);
    logSd(ip, CN);
    logSd(F(" failed"));
    return false;
  }
}

bool strstr(uint8_t const index, uint8_t const *const buffer, uint8_t const length, char const word[]) {
  uint8_t i, k, *s= states + index, t= strlen(word);

  if(*s > 0) {
    for(k= 0; k < length && *s < t && buffer[k] == word[*s]; k++, (*s)++) {
      if(*s == t - 1) {
        *s= 0;
        return true;
      }
    }
  }
  for(i= 0; i < length; i++) {
    for(*s= 0, k= 0; i + k < length && *s < t && buffer[i + k] == word[*s]; k++, (*s)++) {
      if(*s == t - 1) {
        *s= 0;
        return true;
      }
    }
    if(i + k == length) {
      break;
    }
  }
  return false;
}

void elaborate() {
  uint16_t length= captiveClient.available();
  if(length > 0) {
    // buffer max size is 80+1
    length= length > 80 ? 80 : length;
    // adds 0 terminator for ready print
    uint8_t buffer[length + 1];
    buffer[length]= 0;
    captiveClient.read(buffer, length);
    RESPONSE_PRINTLN((char *)buffer);
    if(strstr(0, buffer, length, "HTTP/1.1")) {
      if(B_READ(B_HTTP_PENDING)) {
        // the HTTP header i was waiting for has arrived, now i can search for the ID. i'm not expecting any more HTTP headers meanwhile
        B_FALSE(B_HTTP_PENDING);
        B_TRUE(B_ID_PENDING);
      }
      if(B_READ(B_ID_PENDING)) {
        // i received another HTTP header before finding the ID, now i'm sure the ID wasn't there
        logSd(F("disconnected, logging in"));
        if(connectTo(captiveClient, captiveServer, 80)) {
          logSd(F("sending POST data"));
          captiveClient.print(F("POST /login HTTP/1.1\r\nHost: "));
          captiveClient.println(captiveServer);
          captiveClient.print(F("Connection: close\r\nContent-Length: "));
          captiveClient.println(25 + strlen(USERNAME) + 10 + strlen(PASSWORD));
          captiveClient.print(F("\r\ndst=&popup=true&username=" USERNAME "&password=" PASSWORD));
          // expecting a HTTP response
          B_TRUE(B_HTTP_PENDING);
          lastCaptiveTime= millis();
        }
      }
    }
    if(B_READ(B_ID_PENDING)) {
      if(strstr(1, buffer, length, RESPONSE_ID)) {
        // i found the ID, everything goes back to normal
        logSd(F("logged"));
        B_FALSE(B_ID_PENDING);
      }
    }
  }
}

void setup() {
  // disable all SPI
  pinMode(sdChip, OUTPUT);
  digitalWrite(sdChip, HIGH);
  pinMode(ethernetChip, OUTPUT);
  digitalWrite(ethernetChip, HIGH);
  // enables led status
  pinMode(errorLedPin, OUTPUT);
  ERROR_LED(LOW);

  // open serial communications and wait for port to open:
  DEBUG_INIT();

  // initialize sd
  chipSelect(sdChip);
  initSd();
  // chipSelect(ethernetChip);
  logSd(F("-BOOTING-"));
  // initialize ethernet after sd for logging
  initEthernet();
}

void loop() {
  switch(Ethernet.maintain()) {
  case 1: // renewed fail
    logSd(F("error: renewed fail"));
    break;
  case 2: // renewed success
    logSd(F("renewed success"));
    logSd(F("ip "), CN);
    logSd(Ethernet.localIP());
    break;
  case 3: // rebind fail
    logSd(F("error: rebind fail"));
    break;
  case 4: // rebind success
    logSd(F("rebind success"));
    logSd(F("ip "), CN);
    logSd(Ethernet.localIP());
    break;
  default: // nothing happened
    // checks login status every 60 seconds
    if(millis() - lastCaptiveTime > 60000) {
      logSd(F("checking login status"));
      if(connectTo(captiveClient, captiveServer, 80)) {
        captiveClient.print(F("GET /login HTTP/1.1\r\nHost: "));
        captiveClient.println(captiveServer);
        captiveClient.println(F("Connection: close\r\n"));
        // expecting a HTTP response
        B_TRUE(B_HTTP_PENDING);
      }
      lastCaptiveTime= millis();
    }

    elaborate();

    // check for trigger server every 60 seconds
    if(millis() - lastTriggerTime > 60000) {
      logSd(F("checking for trigger server"));
      if(connectTo(triggerClient, triggerServer, PORT_TRIGGER, false)) {
        uint8_t ipBroadcast[]= {IP_BROADCAST},
                macTarget[]= {MAC_TARGET},
                preamble[]= {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
        // ethernet UDP client object
        EthernetUDP udpClient;

        if(udpClient.begin(7)) {
          udpClient.beginPacket(ipBroadcast, 7);
          udpClient.write(preamble, 6);
          for(int i= 0; i < 16; i++) {
            udpClient.write(macTarget, 6);
          }
          udpClient.endPacket();
          triggerClient.println(F("lalilulelo"));
          logSd(F("WOL sent"));
        } else {
          logSd(F("udp client failed"));
        }
        udpClient.stop();
      }
      lastTriggerTime= millis();
    }
  }
}