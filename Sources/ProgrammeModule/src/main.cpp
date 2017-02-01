#include <ESP8266WiFi.h>
#include <Arduino.h>
#include <ESP8266WiFiMulti.h>
#include <String.h>
#include <string>
#include <WiFiClient.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_MCP23008.h>
#include <Servo.h>


// Data wire is plugged into port 13 on the Arduino
// Connect a 4.7K resistor between VCC and the data pin (strong pullup)
#define DHTPIN 13
#define DHTTYPE DHT22
DHT dht(DHTPIN,DHTTYPE);

Servo myservo;  // create servo object to control a servo
Adafruit_MCP23008 mcp; // create an extender object for more GPIO
ESP8266WiFiMulti WiFiMulti; // Use WiFiClient class to create TCP connections
WiFiClient client; // Client define as wifi target

// WiFi credentials.
const char* WIFI_SSID = ""; // The wifi name
const char* WIFI_PASS = ""; // The password
// If it's an open network, no need of PW;   !! Remove WIFI_PASS of WiFiMulti.addAP() func !!

const char* CLIENT_IP = "XX.XX.XX.XX";
const uint16_t CLIENT_PORT = 75; // Don't forget to open a port on your client


/******************************************************************
************************ Globals variables ************************
******************************************************************/

int id = 1; // Use one ID per rooms, don't forget to change between each.

float temp = 20;
float humi = 40;
bool etat_lum = 0;
bool etat_chauffage = 0;
int etat_volet = 1;
int waterConso = 0;

int cmd_lum = 0;
int cmd_volet = 0;
float cmd_temp = 18;

String splitedS[10];  // la trame recu de la raspberry découpé dans un tableau (<"type de trame C","numero de l'objet","etat de l'objet")

/*****************************************************************/
/****************** Déclaration des setup ************************/
/*****************************************************************/

void networkSetup()
{
    Serial.begin(115200);
    while(!Serial) { }

    // We start by connecting to a WiFi network
    WiFiMulti.addAP(WIFI_SSID, WIFI_PASS);
    // If it's an open network, no need of PW
    // !! Remove WIFI_PASS of WiFiMulti.addAP() func !!

    Serial.println();
    Serial.println();
    Serial.print("Wait for WiFi... ");

    while(WiFiMulti.run() != WL_CONNECTED)
    {
        Serial.print(".");
        delay(500);
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    delay(500);

    Serial.print("connecting to ");
    Serial.println(CLIENT_IP);

    if (!client.connect(CLIENT_IP, CLIENT_PORT))
    {
        Serial.println("connection failed");
        Serial.println("wait 5 sec...");
        delay(5000);
    }
    else
    {
        client.println("<a," +String(id)+ ">"); // Send to the client your ID to open or not the connetion
        // if ID already use or not set in interface's parameters, the connexion is closed by the raspberry
    }
}

void pinSetup()
{
    pinMode (0,OUTPUT);
    digitalWrite (0,HIGH);
    myservo.attach(14);
    dht.begin();
}

void extenderSetup()
{
    mcp.begin();      // use default address 0

    mcp.pinMode(0, OUTPUT); //Moteur volet              // Shutter's Servo data
    mcp.pinMode(1, OUTPUT); //Libre                     // free pin
    mcp.pinMode(2, OUTPUT); //Ventilation               // Switch 1:on/0:off for the blower
    mcp.pinMode(3, OUTPUT); //Chaud                     // set the blower as heater
    mcp.pinMode(4, OUTPUT); //Froid                     // set the blower as cooler
    mcp.pinMode(5, INPUT);  //Consomation d'eau         // Water outflow counter
    mcp.pinMode(6, INPUT);  //Libre                     // free pin
    mcp.pinMode(7, OUTPUT); //Lumière                   // Light control

}

/*****************************************************************/
/*****************************************************************/
/*****************************************************************/

void getData()
{
    // Reading temperature or humidity takes about 250 milliseconds!
    // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
    float h = dht.readHumidity();
    humi = h;

    // Read temperature as Celsius (the default)
    float t = dht.readTemperature();
    temp = t;

    // Check if any reads failed and exit early (to try again).
    if (isnan(h) || isnan(t))
    {
        Serial.println("Failed to read from DHT sensor!");
        return;
    }

    // Serial.print("Humidity: ");
    // Serial.print(h);
    // Serial.print(" %\t");
    // Serial.print("Temperature: ");
    // Serial.print(t);
    // Serial.println(" *C ");
}

/*****************************************************************/
/*****************************************************************/

// Use to split the request string into usable datas
int splitString(String request, char separator)
{
    String curentString="                   ";
    memset(splitedS,0,sizeof(splitedS));
    int numChamp = 0, numLettre = 0;

    for (int i = 1 ; i < request.length() ; i++)
    {
        // each time there is a separator the currentString is push in the splitedS tab
        if (request[i] == separator || request[i] == '>')
        {
            curentString.remove(numLettre);
            splitedS[numChamp] = curentString;
            curentString="                   ";

            numLettre=0;
            numChamp++;
        }
        // all consecutive char is puch at the end of the currentString
        else
        {
            curentString.setCharAt(numLettre,request.charAt(i));
            numLettre++;
        }
    }
    return numChamp; // return the number of string in the splitedS tab
}

/*****************************************************************/
/*****************************************************************/

void lightControl()
{
    if (cmd_lum)
    {
        //Serial.println("Lumière alumée");
        mcp.digitalWrite (7,HIGH);
    }
    else
    {
        //Serial.println("Lumière éteinte");
        mcp.digitalWrite (7,LOW);
    }
}

/*****************************************************************/
/*****************************************************************/

void temperatureControl()
{
    //the blower system will turn off when the temperature is close to the target. Here 0.5 °c

    //Serial.print("Ventillation en mode : ");
    if (cmd_temp > (temp+0.5) && etat_chauffage)
    {
        //Serial.println("Chauffage");
        mcp.digitalWrite (2,HIGH);
        mcp.digitalWrite (3,HIGH);
        mcp.digitalWrite (4,LOW);
    }
    else if (cmd_temp < (temp-0.5) && etat_chauffage)
    {
        //Serial.println("Climatisation");
        mcp.digitalWrite (2,HIGH);
        mcp.digitalWrite (3,LOW);
        mcp.digitalWrite (4,HIGH);
    }
    else
    {
        //Serial.println("Arret");
        mcp.digitalWrite (2,LOW);
        mcp.digitalWrite (3,LOW);
        mcp.digitalWrite (4,LOW);
    }

}

/*****************************************************************/
/*****************************************************************/

// This fonction need to be revised to add other datas in the transmisons if necessary
void sendData()
{
    static long timer = 0;
    // to avoid more than 1 getData() each 2 seconds; due to the slowness of DHT22
    if (timer <= millis())
    {
        getData();
        timer = millis() + 2000;
    }
    // datas need to be turned into string before sending
    String toSend = "<r," + String(id) + "," +String(temp)+ "," +String(cmd_temp)+ "," +String(humi)+ "," +String(etat_lum)+ "," +String(etat_volet)+ "," +String(waterConso)+","+ String(etat_chauffage)+">";
    client.println(toSend); // This will send the request to the raspberry
    waterConso = 0;
}

/*****************************************************************/
/*****************************************************************/

void shutterControl()
{
    #define tempsFonc 20
    //this are the differents commands
    #define stop 0
    #define fermer 1
    #define ouvrir 2
    //this are the differents states
    #define entreOuvert 0
    #define ferme 1
    #define ouvert 2
    #define montee 3
    #define descente 4

    #define min 0
    #define max 180
    #define speed 1

    static long shutterTimer = millis();
    long actualTime = millis();
    static int pos = min;

    switch (cmd_volet)
    {
        case stop:
          if (min < pos && pos < max)
            etat_volet = entreOuvert;
          if (pos > max)
            etat_volet = ouvert;
          if (pos < min)
            etat_volet = ferme;
          break;
        case ouvrir :
          if (pos < max)
          {
              if (shutterTimer < actualTime)
              {
                  pos += speed;
                  shutterTimer = actualTime + tempsFonc;
              }
              etat_volet = montee;
          }
          else
          {
              etat_volet = ouvert;
          }
          break;
        case fermer :
          if (min < pos)
          {
              if (shutterTimer < actualTime)
              {
                  pos -= speed;
                  shutterTimer = actualTime + tempsFonc;
              }
              etat_volet = descente;
          }
          else
          {
              etat_volet = ferme;
          }
          break;
        default :
          Serial.println("Erreur case volet");
    }
    if (id == 1)
        myservo.write(pos);
    if (id == 2)
      myservo.write(180-pos); // ferme le volet dans l'autre sens
      // close the shutter in the other way around
}

/*****************************************************************/
/*****************************************************************/

void whatToDo(String cmdIDS, String cmdDataS)
{
    // this are the possibles values of the second field of the frame
    // they are IDs of the devices to control
    #define light 1
    #define volet 2
    #define temperature 3
    #define OnOff_temp 4

    // when the system recive a frame like : <c,IDofCommand,ValueOfCommand> this function occures
    // the IDofCommand is use for the switch, then the new value is push into the variables and call the assotiate contol function
    int cmdID = cmdIDS.toInt();
    switch (cmdID)
    {
        case light : //identifiant pour la lumiere
          cmd_lum = cmdDataS.toInt();
          etat_lum = cmd_lum;
          lightControl();
          break;
        case volet : //identifiant pour les volet
          cmd_volet = cmdDataS.toInt();
          shutterControl();
          break;
        case temperature : //identifiant pour le controle de la température
          cmd_temp = cmdDataS.toFloat();
          temperatureControl();
          break;
        case OnOff_temp :
          etat_chauffage = cmdDataS.toInt();
          break;
    }
    sendData();
}

/*****************************************************************/
/*****************************************************************/

void frameTraitment()
{
    // This function read the buffer char by char from '<' until '>'
    #define closed 0
    #define open 1
    static String request;
    static bool first = true;
    String finalRequest;

    static bool trame = closed;
    char newChar;


    if(first)
    {
        request.reserve(24);
        first = false;
    }
    newChar = client.read();

    if (newChar == '<')
    {
        trame = open;
        request = '<';
    }
    else if (trame == open)
    {
        if ('!' < newChar && newChar < '~')
        {
            request=request+newChar;
            if (newChar == '>')
            {
                trame = closed;
                finalRequest = request;
                request = "";
            }
        }
        else
        {
           request = "";
        }
        if (trame == closed)
        {
            int nbSeparator = splitString(finalRequest, ',');
            for (int i = 0; i < nbSeparator; i++)
            {
                if (splitedS[i] == "c")
                {
                    whatToDo(splitedS[i+1],splitedS[i+2]);
                    finalRequest = "";
                    break;
                }
                else if (splitedS[i] == "q")
                {
                    sendData();
                    finalRequest = "";
                }
            }
        }
    }
}

/*****************************************************************/
/*****************************************************************/

bool wifiTest()
{
    if(WiFi.status() != WL_CONNECTED) // test de connection au reseau wifi
    {
        Serial.println("Disconnected from WiFi");
    }
    return WiFi.status();
}

/*****************************************************************/
/*****************************************************************/

bool testClient()
{
    if (!client.connected())
    {
        Serial.println("Disconnected from Client");
        while (!client.connect(CLIENT_IP, CLIENT_PORT))
        {
            Serial.println("wait 5 sec...");
            delay(5000);
            Serial.println("Reconnection failed");
        }
        client.println("<a," +String(id)+ ">");
        Serial.println("Reconnected to Client");
    }
    return client.connected();
}

/*****************************************************************/
/*****************************************************************/

void waterCounter()
{
    // for a sensor that return pulse for one liter
    if (mcp.digitalRead(5) == true )
    {
        waterConso = 1;
    }
}

/******************************************************************
**************************** Setup ********************************
******************************************************************/

void setup()
{
    networkSetup();
    pinSetup();
    extenderSetup();
}

/******************************************************************
**************************** Loop *********************************
******************************************************************/

void loop()
{
    if(testClient() && wifiTest()) // test de connection au client
    {
        frameTraitment();
        lightControl();
        shutterControl();
        temperatureControl();
        waterCounter();
    }

}
