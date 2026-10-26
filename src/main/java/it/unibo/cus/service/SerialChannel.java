package it.unibo.cus.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialChannel {

    private final SerialPort serialPort;

    /*
     * Contiene i caratteri dell'ultimo messaggio ancora incompleto.
     *
     * Esempio:
     *
     * chunk 1 -> "OK-Take off-22.96"
     * buffer  -> "OK-Take off-22.96"
     *
     * chunk 2 -> "-41.46-0.00\n"
     * buffer  -> "OK-Take off-22.96-41.46-0.00"
     *
     * Solo quando arriva '\n' il messaggio viene inserito nella queue.
     */
    private final StringBuilder buffer = new StringBuilder();

    /*
     * Coda FIFO dei messaggi completi.
     *
     * ConcurrentLinkedQueue permette a update() e al message
     * handler di lavorare anche da thread diversi.
     */
    private final Queue<String> messages = new ConcurrentLinkedQueue<>();

    public SerialChannel(String portName, int baudRate)
            throws SerialPortException {

        serialPort = new SerialPort(portName);

        serialPort.openPort();

        serialPort.setParams(
                baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
        );
    }

    /**
     * Legge tutti i byte attualmente disponibili sulla seriale.
     *
     * Questo metodo NON assume che una readString() corrisponda
     * ad un messaggio completo.
     *
     * Un messaggio può quindi essere:
     *
     *   - contenuto interamente in un chunk
     *   - diviso tra più chunk
     *   - contenere più messaggi nello stesso chunk
     */
    public void update() throws SerialPortException {

        int available = serialPort.getInputBufferBytesCount();

        if (available <= 0) {
            return;
        }

        String data = serialPort.readString(available).replace("\r", "");

        if (data == null || data.isEmpty()) {
            return;
        }

        processData(data);
    }

    /**
     * Elabora i caratteri ricevuti.
     */
    private void processData(String data) {

        for (char c : data.toCharArray()) {

            /*
             * Arduino normalmente invia:
             *
             * message + "\n"
             *
             * Se arriva CRLF gestiamo comunque entrambi.
             */
            if (c == '\r') {
                continue;
            }

            if (c == '\n') {

                /*
                 * Evita di inserire messaggi vuoti nella coda.
                 */
                if (buffer.length() == 0) {
                    continue;
                }

                String message = buffer.toString();

                messages.add(message);

                buffer.setLength(0);

            } else {

                buffer.append(c);
            }
        }
    }

    /**
     * Indica se esiste almeno un messaggio completo da leggere.
     */
    public boolean hasMsg() {
        return !messages.isEmpty();
    }

    /**
     * Restituisce il prossimo messaggio in ordine FIFO.
     *
     * Restituisce null se la coda è vuota.
     */
    public String readMsg() {
        return messages.poll();
    }

    /**
     * Numero di messaggi attualmente in attesa.
     */
    public int getPendingMessages() {
        return messages.size();
    }

    /**
     * Invia un messaggio ad Arduino.
     */
    public void send(String msg) throws SerialPortException {

        if (msg == null || msg.isEmpty()) {
            return;
        }

        if (!serialPort.isOpened()) {
            return;
        }

        System.out.println(msg);

        serialPort.writeString(msg + "\n");
    }

    /**
     * Chiude la porta seriale.
     */
    public void close() throws SerialPortException {

        if (serialPort != null && serialPort.isOpened()) {
            serialPort.closePort();
        }
    }

    /**
     * Indica se la porta è aperta.
     */
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpened();
    }
}