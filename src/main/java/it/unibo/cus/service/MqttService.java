package it.unibo.cus.service;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Manages the MQTT connection and receives the water level.
 */
public class MqttService {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "CUS-Java";
    private static final String TOPIC_LEVEL = "tank/level";

    private MqttClient client;
    private volatile String lastPayload;

    public synchronized void connect() throws MqttException {
        if (client != null && client.isConnected()) {
            return;
        }

        if (client == null) {
            client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(
                        boolean reconnect,
                        String serverURI) {

                    System.out.println(
                        "MQTT connected to " + serverURI
                        + (reconnect ? " (reconnected)" : "")
                    );

                    try {
                        client.subscribe(TOPIC_LEVEL, 0);
                        System.out.println(
                            "Subscribed to topic: " + TOPIC_LEVEL
                        );
                    } catch (MqttException e) {
                        System.err.println(
                            "Unable to subscribe to "
                            + TOPIC_LEVEL + ": " + e.getMessage()
                        );
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println(
                        "MQTT connection lost: "
                        + (cause != null
                            ? cause.getMessage()
                            : "unknown cause")
                    );
                }

                @Override
                public void messageArrived(
                        String topic,
                        MqttMessage message) {

                    if (!TOPIC_LEVEL.equals(topic)) {
                        return;
                    }

                    lastPayload = new String(
                        message.getPayload(),
                        StandardCharsets.UTF_8
                    ).trim();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // This client only subscribes.
                }
            });
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        client.connect(options);
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Returns the last payload received.
     *
     * @return the last payload, or null if no valid message was received
     */
    public String getLastPayload() {
        return lastPayload;
    }

    public synchronized void disconnect() throws MqttException {
        if (client == null) {
            return;
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } finally {
            client.close();
            client = null;
        }

        System.out.println("Client MQTT closed.");
    }
}