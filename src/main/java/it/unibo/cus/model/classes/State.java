package it.unibo.cus.model.classes;

import it.unibo.cus.model.enums.ProblemsState;
import it.unibo.cus.model.enums.WorkMode;

/**
 * Stores the global state of the system.
 */
public final class State {

    private State() {
        // Prevent instantiation
    }

    public static WorkMode workMode = WorkMode.AUTOMATIC;

    public static ProblemsState problemsState =
            ProblemsState.UNCON_NOT_AVAIL;

    public static int valveOpenPercentage = 0;

    public static int waterLevel = 0;
}
