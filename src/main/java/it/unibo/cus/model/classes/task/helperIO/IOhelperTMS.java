package it.unibo.cus.model.classes.task.helperIO;

import it.unibo.cus.model.api.HelperInputOutput;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.service.MqttService;

/**
 * Handles input data received from TMS through MQTT.
 *
 * @author Sara Visani
 */
public class IOhelperTMS implements HelperInputOutput {

    private final MqttService mqttService;

    private String lastProcessedPayload;

    public IOhelperTMS() {
        this.mqttService = State.serviceMQTT;
    }

    @Override
    public void read() {
        if (mqttService == null) {
            return;
        }

        String payload = mqttService.getLastPayload();

        if (payload == null || payload.equals(lastProcessedPayload)) {
            return;
        }

        lastProcessedPayload = payload;

        try {
            int level = Integer.parseInt(payload);

            if (level < 0) {
                System.err.println(
                    "Invalid water level received: " + payload
                );
                return;
            }

            State.waterLevel = level;

        } catch (NumberFormatException e) {
            System.err.println(
                "Invalid water level payload: " + payload
            );
        }
    }

    @Override
    public void send() {
        // TMS is receive-only.
    }

    @Override
    public boolean guard() {
        return mqttService != null && mqttService.isConnected();
    }
}