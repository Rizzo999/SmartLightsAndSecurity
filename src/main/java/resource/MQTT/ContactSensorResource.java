package resource.MQTT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ContactSensorResource extends SmartObjectResource<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(ContactSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:contact";

    public int counter = 0;

    private static final long UPDATE_PERIOD = 1800;

    private static final long TASK_DELAY_TIME = 5000;


    private Timer updateTimer = null;

    private boolean updateContactValue;

    public ContactSensorResource(String id_sensor/*String roomId*/){
        super(id_sensor, RESOURCE_TYPE/*, roomId*/);
        init();
    }

    private void init(){
        try{

            updateContactValue = false;
            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init Presence Resource Object ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void startPeriodicEventValueUpdateTask(){

        try{

            //logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    int n = getRandomNumber(15,20);
                    updateContactValue = false;

                    if (counter < n) {
                        notifyUpdate(updateContactValue);
                        counter += 1;
                    }
                    else {
                        updateContactValue = true;
                        counter = 0;
                        notifyUpdate(updateContactValue);
                    }

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updateContactValue;
    }


    /*public static void main(String[] args) {

        ContactSensorResource contactSensorResource = new ContactSensorResource("001");
        logger.info("New {} Resource Created with Id: {} ! Presence value: {}",
                contactSensorResource.getType(),
                contactSensorResource.getId(),
                contactSensorResource.loadUpdatedValue());

        //Add Resource Listener
        contactSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Contact Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }

        });

    }*/
}
