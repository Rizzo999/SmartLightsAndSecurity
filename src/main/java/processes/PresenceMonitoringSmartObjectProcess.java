package processes;

import resource.MQTT.PresenceMonitoringSmartObject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.MQTT.ContactSensorResource;
import resource.MQTT.PresenceSensorResource;

import java.util.HashMap;
import java.util.UUID;

public class PresenceMonitoringSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(PresenceMonitoringSmartObjectProcess.class);

    private static String MQTT_BROKER_IP = "127.0.0.1";

    private static int MQTT_BROKER_PORT = 1883;

    // 001 --> main room
    // 002 --> kitchen
    // 003 --> bedroom
    // 004 --> bathroom

    public static void main(String[] args) {

        try{

            MqttClientPersistence persistence = new MemoryPersistence();

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);


            // ------------------main-----------------//

            IMqttClient mqttClientMain = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    "device001",
                    persistence);

            mqttClientMain.connect(options);
            PresenceMonitoringSmartObject presenceMonitoringSmartObjectMain = new PresenceMonitoringSmartObject();
            presenceMonitoringSmartObjectMain.init("device001", "001", "001", "0.0.1-beta",  mqttClientMain, new HashMap<>(){
                {
                    put("device001-presence", new PresenceSensorResource("presence001"));
                    put("device001-contact-porta", new ContactSensorResource("contact001_porta"));
                }
            });

            // ------------------kitchen-----------------//

            IMqttClient mqttClientKitchen = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    "device002",
                    persistence);

            mqttClientKitchen.connect(options);
            PresenceMonitoringSmartObject presenceMonitoringSmartObjectKitchen = new PresenceMonitoringSmartObject();
            presenceMonitoringSmartObjectKitchen.init("device002", "001", "002", "0.0.1-beta",  mqttClientKitchen, new HashMap<>(){
                {
                    put("device002-presence", new PresenceSensorResource("presence002"));
                    put("device002-contact-finestra", new ContactSensorResource("contact002_finestra"));
                }
            });

            // --------------------bedroom---------------//

            IMqttClient mqttClientBedroom = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    "device003",
                    persistence);

            mqttClientBedroom.connect(options);
            PresenceMonitoringSmartObject presenceMonitoringSmartObjectBedroom = new PresenceMonitoringSmartObject();
            presenceMonitoringSmartObjectBedroom.init("device003", "001", "003", "0.0.1-beta",  mqttClientBedroom, new HashMap<>(){
                {
                    put("device003-presence", new PresenceSensorResource("presence003"));
                    put("device003-contact-finestra", new ContactSensorResource("contact003_finestra"));
                }
            });

            // -----------------bathroom------------------//

            IMqttClient mqttClientBathroom = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    "device004",
                    persistence);

            mqttClientBathroom.connect(options);
            PresenceMonitoringSmartObject presenceMonitoringSmartObjectBathroom = new PresenceMonitoringSmartObject();
            presenceMonitoringSmartObjectBathroom.init("device004", "001", "004", "0.0.1-beta",  mqttClientBathroom, new HashMap<>(){
                {
                    put("device004presence", new PresenceSensorResource("presence004"));
                    put("device004-contact-finestra", new ContactSensorResource("contact004_finestra"));
                }
            });


            presenceMonitoringSmartObjectMain.start();
            presenceMonitoringSmartObjectKitchen.start();
            presenceMonitoringSmartObjectBedroom.start();
            presenceMonitoringSmartObjectBathroom.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
