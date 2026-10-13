package it.unibo.cus.model.classes.helperConnections;

import it.unibo.cus.model.api.HelperCheckConnection;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.enums.ProblemsState;

public class CheckAndReconnectTMS implements HelperCheckConnection {

    @Override
    public boolean checkConnection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkConnection'");
    }

    @Override
    public boolean tryReconnect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tryReconnect'");
    }

    @Override
    public String getTimerName() {
        return "TMS";
    }

    @Override
    public long getTimerPeriod() {
        return 300;
    }

    @Override
    public Runnable getTimeoutAction() {
        return () -> {
            switch (State.problemsState) {
                case OK:
                    State.problemsState = ProblemsState.UNCONNECTED;
                    break;
                case NOT_AVAILABLE:
                    State.problemsState = ProblemsState.UNCON_NOT_AVAIL;
                    break;
                default:
                    break;
            }
        };
    }

}
