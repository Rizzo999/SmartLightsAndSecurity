package resource.MQTT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PresenceSensorResource extends SmartObjectResource<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(PresenceSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:presence";

    public int counter = 0;

    private static final long UPDATE_PERIOD = 1800;

    private static final long TASK_DELAY_TIME = 5000;


    private Timer updateTimer = null;

    private boolean updatePresenceValue;

    public PresenceSensorResource(String id_sensor /*String roomId*/){
        super(id_sensor, RESOURCE_TYPE/*, roomId*/);
        init();
    }

    private void init(){
        try{

            updatePresenceValue = false;
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
                    updatePresenceValue = false;

                    if (counter < n) {
                        notifyUpdate(updatePresenceValue);
                        counter += 1;
                    }
                    else {
                        updatePresenceValue = true;
                        counter = 0;
                        notifyUpdate(updatePresenceValue);
                    }

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updatePresenceValue;
    }


    /*public static void main(String[] args) {

        PresenceSensorResource presenceSensorResource = new PresenceSensorResource("001");
        logger.info("New {} Resource Created with Id: {} ! Presence value: {}",
                presenceSensorResource.getType(),
                presenceSensorResource.getId(),
                presenceSensorResource.loadUpdatedValue());

        //Add Resource Listener
        presenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Presence Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }

        });

    }*/

}