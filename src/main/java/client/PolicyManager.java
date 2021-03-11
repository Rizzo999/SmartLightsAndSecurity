package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.TelemetryMessage;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.MQTT.ContactSensorResource;
import resource.MQTT.PresenceSensorResource;
import org.eclipse.californium.core.coap.CoAP.Code;

import java.io.IOException;
import java.util.*;

public class PolicyManager {

    private final static Logger logger = LoggerFactory.getLogger(PolicyManager.class);

    private static String BROKER_ADDRESS = "127.0.0.1";
    private static int BROKER_PORT = 1883;
    private static final String TARGET_TOPIC = "presence_monitoring_device/+/telemetry/#";
    private String clientId = "Rizzini-Giurea-SmartHome-PolicyManager";
    private static ObjectMapper mapper;

    private HashMap<String, String> coap_endpoints_alarm = new HashMap<String, String>();
    private HashMap<String, String> coap_endpoints_light = new HashMap<String, String>();

    private String coapEndpoint;

    Scanner myObj = new Scanner(System.in);

    private int status;  //if status = 0 --> PolicyManager off, if status = 1 --> PolicyManager on

    private int updatedStatus;

    public PolicyManager() {

        this.status = 0;

        coap_endpoints_alarm.put("001", "coap://127.0.0.1:5683/alarm001");
        coap_endpoints_alarm.put("002", "coap://127.0.0.1:5683/alarm002");
        coap_endpoints_alarm.put("003", "coap://127.0.0.1:5683/alarm003");
        coap_endpoints_alarm.put("004", "coap://127.0.0.1:5683/alarm004");

        coap_endpoints_light.put("001", "coap://127.0.0.1:5684/light001");
        coap_endpoints_light.put("002", "coap://127.0.0.1:5684/light002");
        coap_endpoints_light.put("003", "coap://127.0.0.1:5684/light003");
        coap_endpoints_light.put("004", "coap://127.0.0.1:5684/light004");

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void run(){
        //resorce discovery
        updatedStatus = this.getStatus();
        if (updatedStatus == 0) {
            runOffMode();
        }
        else if (updatedStatus == 1) {
            runOnMode();
        }
    }

    public int[] lightsConfOn(){
        int RGB [] = new int[3];
        logger.info("Automatic configuration of the lights to the brightest value --> R = 255, G = 255, B = 255");
        RGB [0] = 255;
        RGB [1] = 255;
        RGB [2] = 255;
        return RGB;
    }

    public int[] lightsConfOff(){
        Scanner myObj = new Scanner(System.in);
        int RGB [] = new int[3];
        logger.info("You can set the color configuration of the lights (typing custom for RGB values) or proceed with the default configuration (typing default for white light) ");
        String input = myObj.nextLine();
        if (input.equals("default")) {
            RGB [0] = 255;
            RGB [1] = 255;
            RGB [2] = 255;
        } else if (input.equals("custom")){
            logger.info("Enter the three values from 0 to 255: first for red, second for green and third for blue");
            for (int i = 0; i < 3; i++ ){
                RGB [i] = Integer.valueOf(myObj.nextLine());
                if (RGB[i] < 0 || RGB[i] > 255) {
                    logger.info("Bad input");
                    System.exit(0);
                }
            }
            logger.info("Lights setted to values {}, {}, {}", RGB[0], RGB[1], RGB[2]);
        } else {
            logger.info("Bad input");
            System.exit(0);
        }
        return RGB;
    }

    public void postLight(String room) {

        Iterator it = coap_endpoints_light.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if ((entry.getKey()).equals(room)){
                coapEndpoint = String.valueOf(entry.getValue());
            }
        }


        CoapClient coapClient = new CoapClient(coapEndpoint);
        Request request = new Request(Code.POST);
        request.setConfirmable(true);
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

            String text = coapResp.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }


    public void putLight(String room, int[] RGB){

        Iterator it = coap_endpoints_light.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if ((entry.getKey()).equals(room)){
                coapEndpoint = String.valueOf(entry.getValue());
            }
        }

        CoapClient coapClient = new CoapClient(coapEndpoint);
        Request request = new Request(Code.PUT);
        String myPayload = Arrays.toString(RGB);
        logger.info("PUT RequestPayload: {}", myPayload);
        request.setPayload(myPayload);
        request.setConfirmable(true);
        logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));
        CoapResponse coapResp = null;
        try {
             coapResp = coapClient.advanced(request);
             //Pretty print for the received response
             logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));
             String text = coapResp.getResponseText();
             logger.info("Payload: {}", text);
             logger.info("Message ID: " + coapResp.advanced().getMID());
             logger.info("Token: " + coapResp.advanced().getTokenString());



        }   catch (ConnectorException | IOException e){
            e.printStackTrace();
        }

    }

    public void postAlarm(String room) {

        Iterator it = coap_endpoints_alarm.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if ((entry.getKey()).equals(room)){
                coapEndpoint = String.valueOf(entry.getValue());
            }
        }

        CoapClient coapClient = new CoapClient(coapEndpoint);
        Request request = new Request(Code.POST);
        request.setConfirmable(true);
        logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(request));

        CoapResponse coapResp = null;

        try {

            coapResp = coapClient.advanced(request);

            logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

            String text = coapResp.getResponseText();
            logger.info("Payload: {}", text);
            logger.info("Message ID: " + coapResp.advanced().getMID());
            logger.info("Token: " + coapResp.advanced().getTokenString());

        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }



    private static Optional<TelemetryMessage<Boolean>> parseTelemetryMessagePayload(MqttMessage mqttMessage){

        try{

            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.ofNullable(mapper.readValue(payloadString, new TypeReference<TelemetryMessage<Boolean>>() {}));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    //on mode
    private void runOnMode(){

        logger.info("Setted policy manager on: if it detects the presence in an area, it triggers the alarm in the area and turns on the area lights");

        int RGB[] = lightsConfOn();

        putLight("001",RGB);
        putLight("002",RGB);
        putLight("003",RGB);
        putLight("004",RGB);


        logger.info("... Working...");

        mapper = new ObjectMapper();

        MqttClientPersistence persistence = new MemoryPersistence();
        try {
            IMqttClient client = new MqttClient(String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT),
                    clientId,
                    persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            client.subscribe(TARGET_TOPIC, (topic, msg) -> {

                //byte[] payload = msg.getPayload();
                //logger.info("Message Received -> Topic: {} - Payload: {}", topic, new String(payload));
                Optional<TelemetryMessage<Boolean>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);
                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(ContactSensorResource.RESOURCE_TYPE)){

                    Boolean newContactValue = telemetryMessageOptional.get().getDataValue();
                    String id = telemetryMessageOptional.get().getId();
                    String room = id.substring(7,10);
                    logger.info("New Contact Telemetry Data Received From Room {}! Contact Value: {}", room, newContactValue);
                    if (newContactValue) {
                        logger.info("Turning on light and alarm in room {}", room);
                        postAlarm(room);
                        postLight(room);

                        logger.info("Attention.. intrusion detected. Do you want turn down the alarm and the lights?");
                        String turnDown = myObj.nextLine();
                        if (turnDown.equals("yes")){
                            postAlarm(room);
                            postLight(room);                                                    }

                    } else {
                        // do nothing
                    }

                }
                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(PresenceSensorResource.RESOURCE_TYPE)){
                    Boolean newPresenceValue = telemetryMessageOptional.get().getDataValue();
                    //String roomId = telemetryMessageOptional.get().getRoom_id();
                    String id = telemetryMessageOptional.get().getId();
                    String room = id.substring(8,11);
                    logger.info("New Presence Telemetry Data Received From Room {}! Presence Value: {}", room, newPresenceValue);
                    if (newPresenceValue) {
                        logger.info("Turning on light and alarm in room {}", room);
                        postAlarm(room);
                        postLight(room);

                        logger.info("Attention.. intrusion detected. Do you want turn down the alarm and the lights?");
                        String turnDown = myObj.nextLine();
                        if (turnDown.equals("yes")){
                            postAlarm(room);
                            postLight(room);
                        }
                    } else {
                        //do nothing
                    }
                }

            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    //off mode
    private void runOffMode(){

        mapper = new ObjectMapper();

        logger.info("Setted policy manager off: it manages the lighting without triggering the alarm. When it no longer detects a presence in the room, it turns off the light");

        int RGB[] = lightsConfOff();

        putLight("001",RGB);
        putLight("002",RGB);
        putLight("003",RGB);
        putLight("004",RGB);

        logger.info("... Working...");

        //do off stuff

        MqttClientPersistence persistence = new MemoryPersistence();
        try {
            IMqttClient client = new MqttClient(String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT),
                    clientId,
                    persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            client.subscribe(TARGET_TOPIC, (topic, msg) -> {

                //byte[] payload = msg.getPayload();
                //logger.info("Message Received -> Topic: {} - Payload: {}", topic, new String(payload));
                Optional<TelemetryMessage<Boolean>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);
                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(ContactSensorResource.RESOURCE_TYPE)){

                    Boolean newContactValue = telemetryMessageOptional.get().getDataValue();
                    //String roomId = telemetryMessageOptional.get().getRoom_id();
                    String id = telemetryMessageOptional.get().getId();
                    String room = id.substring(7,10);
                    logger.info("New Contact Telemetry Data Received From Room {}! Contact Value: {}", room, newContactValue);
                    if (newContactValue) {
                        //accende la luce nella stanza con id =
                        logger.info("Turning on light in room {}", room);
                        postLight(room);
                    }
                    postLight(room);

                }
                if(telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType().equals(PresenceSensorResource.RESOURCE_TYPE)){
                    Boolean newPresenceValue = telemetryMessageOptional.get().getDataValue();
                    //String roomId = telemetryMessageOptional.get().getRoom_id();
                    String id = telemetryMessageOptional.get().getId();
                    String room = id.substring(8,11);
                    logger.info("New Presence Telemetry Data Received From Room {}! Presence Value: {}", room, newPresenceValue);

                    if (newPresenceValue) {
                        //accende la luce
                        logger.info("Turning on light in room {}", room);
                        postLight(room); //accende le luci

                    }

                    postLight(room);

                }

            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
