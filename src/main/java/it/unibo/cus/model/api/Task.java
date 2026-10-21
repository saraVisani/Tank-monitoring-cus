package it.unibo.cus.model.api;

/**
 * Interface for all Task for the scheduler
 */
public interface Task {
    void tick();
    /**
     * When the task need to tick
     * @return
     */
    boolean guard();
}
