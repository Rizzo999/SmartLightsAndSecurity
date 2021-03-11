package resource.COAP;

import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmSmartObjectResource extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(AlarmSmartObjectResource.class);

    private String deviceId;
    private String piano;

    public AlarmSmartObjectResource(String deviceId, String piano){

        super(5683);

        this.deviceId = String.format("dipi:iot:%s", deviceId);
        this.piano = piano;

        //Create Resources
        AlarmActuatorResource alarmActuatorResourceMain = new AlarmActuatorResource(deviceId,"alarm001");
        AlarmActuatorResource alarmActuatorResourceKitchen = new AlarmActuatorResource(deviceId,"alarm002");
        AlarmActuatorResource alarmActuatorResourceBedroom = new AlarmActuatorResource(deviceId,"alarm003");
        AlarmActuatorResource alarmActuatorResourceBathroom = new AlarmActuatorResource(deviceId,"alarm004");

        logger.info("Defining and adding resources ...");

        //Add resources to the sd
        this.add(alarmActuatorResourceMain);
        this.add(alarmActuatorResourceKitchen);
        this.add(alarmActuatorResourceBedroom);
        this.add(alarmActuatorResourceBathroom);

    }

}
