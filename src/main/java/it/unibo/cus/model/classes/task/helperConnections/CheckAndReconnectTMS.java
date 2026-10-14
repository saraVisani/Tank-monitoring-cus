package it.unibo.cus.model.classes.task.helperConnections;

import it.unibo.cus.service.MqttService;
import it.unibo.cus.model.api.HelperCheckConnection;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.enums.ProblemsState;

public class CheckAndReconnectTMS implements HelperCheckConnection {

    private final MqttService mqttService;

    public CheckAndReconnectTMS() {
        this.mqttService = new MqttService();
        State.serviceMQTT = this.mqttService;
    }

    @Override
    public boolean checkConnection() {
        return mqttService.isConnected();
    }

    @Override
    public boolean tryReconnect() {
        // The MQTT client manages reconnection automatically.
        return mqttService.isConnected();
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
