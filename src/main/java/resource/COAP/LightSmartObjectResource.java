package resource.COAP;

import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightSmartObjectResource extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(LightSmartObjectResource.class);

    private String deviceId;
    private String piano;

    public LightSmartObjectResource(String deviceId, String piano){

        super(5684);

        this.deviceId = String.format("dipi:iot:%s", deviceId);
        this.piano = piano;

        LightActuatorResource lightActuatorResourceMain = new LightActuatorResource(deviceId,"light001");
        LightActuatorResource lightActuatorResourceKitchen = new LightActuatorResource(deviceId,"light002");
        LightActuatorResource lightActuatorResourceBedroom = new LightActuatorResource(deviceId,"light003");
        LightActuatorResource lightActuatorResourceBathroom = new LightActuatorResource(deviceId,"light004");

        logger.info("Defining and adding resources ...");

        //Add resources ....
        this.add(lightActuatorResourceMain);
        this.add(lightActuatorResourceKitchen);
        this.add(lightActuatorResourceBedroom);
        this.add(lightActuatorResourceBathroom);

    }

}
