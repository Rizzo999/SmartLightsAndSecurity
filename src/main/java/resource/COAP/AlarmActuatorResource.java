package resource.COAP;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.*;
import java.util.Optional;


public class AlarmActuatorResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(AlarmActuatorResource.class);

    private static final Number SENSOR_VERSION = 0.1;

    private static final String OBJECT_TITLE = "AlarmActuator";

    private static final String RESOURCE_TYPE = "iot:actuator:alarm";

    private int alarmStatus;

    private String deviceId;

    private ObjectMapper objectMapper;


    public AlarmActuatorResource(String deviceId, String name) {
        super(name);
        this.deviceId = deviceId;


        //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        setObservable(true); // enable observing
        setObserveType(Type.CON); // configure the notification type to CONs

        getAttributes().setTitle(OBJECT_TITLE);
        getAttributes().setObservable(); // mark observable in the Link-Format

        //Specify Resource Attributes
        getAttributes().addAttribute("rt", RESOURCE_TYPE);
        getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());

        // Reset Switch Status Value
        this.alarmStatus = 0;
    }

    private boolean getBooleanSwitchStatus(){
        return this.alarmStatus != 0;
    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBaseName(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setVersion(SENSOR_VERSION);
            senMLRecord.setBooleanValue(getBooleanSwitchStatus());
            senMLRecord.setTime(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            logger.error("Error Generating SenML Record ! Msg: {}", e.getLocalizedMessage());
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        //If the request specify the MediaType as JSON or JSON+SenML
        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(this.alarmStatus), MediaTypeRegistry.TEXT_PLAIN);

    }

    @Override
    public void handlePUT(CoapExchange exchange) {

        try{

            logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(exchange.advanced().getRequest()));
            logger.info("Received PUT Request with body: {}", exchange.getRequestPayload());

            //If the request body is available
            if(exchange.getRequestPayload() != null){

                int submittedValue = Integer.parseInt(new String(exchange.getRequestPayload()));

                //If the value is not correct
                if(submittedValue == 0 || submittedValue == 1){

                    //Update internal status
                    this.alarmStatus = submittedValue;

                    logger.info("Resource Status Updated: {}", this.alarmStatus);

                    exchange.respond(CoAP.ResponseCode.CHANGED);

                    //Notify Observers
                    changed();
                }
                else
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void handlePOST(CoapExchange exchange) {

        //According to CoRE Interface a POST request has an empty body and change the current status
        try{

            logger.info("Request Pretty Print:\n{}", Utils.prettyPrint(exchange.advanced().getRequest()));
            logger.info("Received POST Request with body: {}", exchange.getRequestPayload());

            //Empty request
            if(exchange.getRequestPayload() == null){

                //Update internal status
                this.alarmStatus = (alarmStatus == 1) ? 0 : 1;

                logger.info("Resource Status Updated: {}", this.alarmStatus);

                exchange.respond(CoAP.ResponseCode.CHANGED);

                //Notify Observers
                changed();
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}
