package it.unibo.cus.model.api;

/**
 * The interface HelperCheckConnection is for the different checks of connection.
 */
public interface HelperCheckConnection {
    boolean checkConnection();
    boolean tryReconnect();
    /**
     * This method returns the name of the timer associated with the connection check.
     * It must be TMS or DBS.
     * @return a String representing the timer name.
     */
    String getTimerName();
    long getTimerPeriod();
    /**
     * This method returns a Runnable that defines the action to be performed when a timeout occurs during the connection check.
     * @return a Runnable representing the timeout action.
     */
    Runnable getTimeoutAction();
}
