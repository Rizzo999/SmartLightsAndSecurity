package resource.MQTT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.TelemetryMessage;


import java.util.Map;

public class PresenceMonitoringSmartObject {
    private static final Logger logger = LoggerFactory.getLogger(PresenceMonitoringSmartObject.class);

    private static final String BASIC_TOPIC = "presence_monitoring_device";

    private static final String TELEMETRY_TOPIC = "telemetry";

    private static final String EVENT_TOPIC = "event";

    private static final String CONTROL_TOPIC = "control";

    private static final String COMMAND_TOPIC = "command";

    private static final String PRESENCE_SENSOR_TOPIC = "presence_sensor";

    private static final String CONTACT_SENSOR_TOPIC = "contact_sensor";

    private String presenceSmartObjId;

    private String idPiano;

    private String idStanza;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private String swVersion;

    private Map<String, SmartObjectResource<?>> resourceMap;

    public PresenceMonitoringSmartObject() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Init the vehicle smart object with its ID, the MQTT Client and the Map of managed resources
     * @param presenceSmartObjId
     * @param mqttClient
     * @param resourceMap
     */

    public void init(String presenceSmartObjId, String idPiano, String idStanza, String swVersion, IMqttClient mqttClient, Map<String, SmartObjectResource<?>> resourceMap){

        this.presenceSmartObjId = presenceSmartObjId;
        this.idPiano = idPiano;
        this.idStanza = idStanza;
        this.swVersion = swVersion;
        this.mqttClient = mqttClient;
        this.resourceMap = resourceMap;

        logger.info("Smart Object {} correctly created ! Resource Number: {}", presenceSmartObjId, resourceMap.keySet().size());
    }

    /**
     * Start smart object behaviour
     */

    public void start(){

        try{

            if(this.mqttClient != null &&
                    this.presenceSmartObjId != null  && this.presenceSmartObjId.length() > 0 &&
                    this.resourceMap != null && resourceMap.keySet().size() > 0){

                logger.info("Starting PresenceMonitoringSmartObject {}....", presenceSmartObjId);

                registerToControlChannel();

                registerToAvailableResources();


            }

        }catch (Exception e){
            logger.error("Error Starting the  PresenceMonitoringSmartObject  ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void registerToControlChannel() {

        try{

            String deviceControlTopic = String.format("%s/%s/%s", BASIC_TOPIC, presenceSmartObjId, CONTROL_TOPIC);

            logger.info("Registering to Control Topic ({}) ... ", deviceControlTopic);

            this.mqttClient.subscribe(deviceControlTopic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if(message != null)
                        logger.info("[CONTROL CHANNEL] -> Control Message Received -> {}", new String(message.getPayload()));
                    else
                        logger.error("[CONTROL CHANNEL] -> Null control message received !");
                }
            });

        }catch (Exception e){
            logger.error("ERROR Registering to Control Channel ! Msg: {}", e.getLocalizedMessage());
        }
    }

    private void registerToAvailableResources(){
        try{

            this.resourceMap.entrySet().forEach(resourceEntry -> {

                if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                    SmartObjectResource smartObjectResource = resourceEntry.getValue();

                    logger.info("Registering to Resource {} (id: {}) notifications ...",
                            smartObjectResource.getType(),
                            smartObjectResource.getId());

                    //Register toPresenceSensorResource Notification

                    if(smartObjectResource.getType().equals(PresenceSensorResource.RESOURCE_TYPE)){

                        PresenceSensorResource presenceSensorResource = (PresenceSensorResource)smartObjectResource;
                        presenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                                try {
                                    if (presenceSensorResource.loadUpdatedValue() == true) {
                                        publishTelemetryData(
                                                String.format("%s/%s/%s/%s", BASIC_TOPIC, presenceSmartObjId, TELEMETRY_TOPIC, PRESENCE_SENSOR_TOPIC/*, resourceEntry.getKey()*/),
                                                new TelemetryMessage<>(smartObjectResource.getType(), smartObjectResource.getId() /*, smartObjectResource.getRoomID()*/ ,updatedValue));
                                    }
                                    else {
                                        // do nothing, because the sensor has to public only if there is presence value on true
                                    }

                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }


                    if(smartObjectResource.getType().equals(ContactSensorResource.RESOURCE_TYPE)){

                        ContactSensorResource contactSensorResource = (ContactSensorResource)smartObjectResource;
                        contactSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, presenceSmartObjId, TELEMETRY_TOPIC, CONTACT_SENSOR_TOPIC/*, resourceEntry.getKey()*/),
                                            new TelemetryMessage<>(smartObjectResource.getType(), smartObjectResource.getId(), /*smartObjectResource.getRoomID(),*/ updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            });

        }catch (Exception e){
            logger.error("Error Registering to Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }

    private void publishTelemetryData(String topic, TelemetryMessage<?> telemetryMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(this.mqttClient != null && this.mqttClient.isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(0);

            mqttClient.publish(topic, mqttMessage);

            //logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }
}
