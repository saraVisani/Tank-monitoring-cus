package it.unibo.cus.model.classes.task.helperConnections;

import it.unibo.cus.model.api.HelperCheckConnection;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.enums.ProblemsState;

public class CheckAndReconnectDBS implements HelperCheckConnection {

    private static long lastHeartbeat = System.currentTimeMillis();
    
    private static final long TIMEOUT_THRESHOLD = 3000; 

    public static synchronized void ping() {
        lastHeartbeat = System.currentTimeMillis();
    }

    @Override
    public boolean checkConnection() {
        //Calculates the difference between the current time and the time of the last received signal. If the difference is less than 3 seconds, it returns "true" (all OK). Otherwise, it returns "false" (disconnected).
        return (System.currentTimeMillis() - lastHeartbeat) < TIMEOUT_THRESHOLD;
    }

    @Override
    public boolean tryReconnect() {
        return checkConnection();
    }

    @Override
    public String getTimerName() {
        return "DBS";
    }

    @Override
    public long getTimerPeriod() {
        return 300;
    }

    // timer expired -> ALLARM
    @Override
    public Runnable getTimeoutAction() {
        return () -> {
            switch (State.problemsState) {
                case OK:
                    State.problemsState = ProblemsState.NOT_AVAILABLE;
                    break;
                case UNCONNECTED:
                    State.problemsState = ProblemsState.UNCON_NOT_AVAIL;
                    break;
                default:
                    break;
            }
        };
    }
}