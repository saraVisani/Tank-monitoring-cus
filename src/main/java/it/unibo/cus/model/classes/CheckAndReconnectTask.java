
package it.unibo.cus.model.classes;

import it.unibo.cus.model.api.Task;
import it.unibo.cus.model.api.HelperCheckConnection;
import it.unibo.cus.model.classes.helperConnections.*;
import it.unibo.cus.model.enums.ProblemsState;

import java.util.List;

/**
 * Checks connections and attempts to reconnect when they are unavailable.
 * Each helper has its own timer and timeout action.
 */
public class CheckAndReconnectTask implements Task {

    private final List<HelperCheckConnection> helpers = List.of(
        new CheckAndReconnectTMS()
        // new CheckAndReconnectDBS()
    );

    private final List<Timer> timers = helpers.stream()
        .map(helper -> new Timer(
            helper.getTimerName(),
            helper.getTimerPeriod(),
            helper.getTimeoutAction()
        ))
        .toList();

    @Override
    public void tick() {
        for (int i = 0; i < helpers.size(); i++) {
            HelperCheckConnection helper = helpers.get(i);
            Timer timer = timers.get(i);
            if (helper.checkConnection()) {
                timer.reset();
                updateProblemsState(helper.getTimerName());
            } else {
                if (!timer.isRunning() && !timer.isCompleted()) {
                    timer.start();
                }
                helper.tryReconnect();
            }
            timer.update();
        }
    }

    private void updateProblemsState(String helperName) {
        switch (State.problemsState) {
            case UNCONNECTED:
                if(helperName.equalsIgnoreCase("DBS")){
                    break;
                }
                State.problemsState = ProblemsState.OK;
                break;
            case NOT_AVAILABLE:
                if(helperName.equalsIgnoreCase("TMS")){
                    break;
                }
                State.problemsState = ProblemsState.OK;
                break;
            case UNCON_NOT_AVAIL:
                if (helperName.equalsIgnoreCase("TMS")) {
                    State.problemsState = ProblemsState.NOT_AVAILABLE;
                } else if (helperName.equalsIgnoreCase("DBS")) {
                    State.problemsState = ProblemsState.UNCONNECTED;
                }
                break;
            case OK:
        }
    }

    @Override
    public boolean guard() {
        return true;
    }
}
