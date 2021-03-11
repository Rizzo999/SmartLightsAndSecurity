package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;

public class ControllerPolicyManager {

    private final static Logger logger = LoggerFactory.getLogger(ControllerPolicyManager.class);
    private PolicyManager policyManager;

    public ControllerPolicyManager(){
        policyManager = new PolicyManager();
    }

    private void policyManagerOn(){
        policyManager.setStatus(1);
    }

    private void policyManagerOff(){
        policyManager.setStatus(0);
    }

    public static void main (String args[]){
        ControllerPolicyManager controllerPolicyManager = new ControllerPolicyManager();
        Scanner myObj = new Scanner(System.in);

        //System.out.println(controllerPolicyManager.policyManager.getStatus());  // ->check status before

        logger.info("Set policy manager in mode (type on or off only):");
        String mode = myObj.nextLine();

        if (mode.equals("on")){

            controllerPolicyManager.policyManagerOn();

        } else if(mode.equals("off")){

            controllerPolicyManager.policyManagerOff();

        } else {

            logger.info("Bad input: you wrote something different to \"on\" or \"off\" :( ");
            System.exit(0);

        }

        controllerPolicyManager.policyManager.run();

        //System.out.println(controllerPolicyManager.policyManager.getStatus());  // -->check status after

    }
}
