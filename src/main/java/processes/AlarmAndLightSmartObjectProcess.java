package processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.COAP.AlarmSmartObjectResource;
import resource.COAP.LightSmartObjectResource;

public class AlarmAndLightSmartObjectProcess {

    private final static Logger logger = LoggerFactory.getLogger(AlarmAndLightSmartObjectProcess.class);

    public static void main (String args[]){

        AlarmSmartObjectResource alarmSmartObjectResource = new AlarmSmartObjectResource("smartDeviceAlarm-001", "001");
        logger.info("Starting Coap Server...");
        alarmSmartObjectResource.start();
        logger.info("Coap Server Started ! Available resources: ");
        alarmSmartObjectResource.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
        });

        LightSmartObjectResource lightSmartObjectResource = new LightSmartObjectResource("smartDeviceLight-001", "001");
        logger.info("Starting Coap Server...");
        lightSmartObjectResource.start();
        logger.info("Coap Server Started ! Available resources: ");
        lightSmartObjectResource.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
        });

    }
}
