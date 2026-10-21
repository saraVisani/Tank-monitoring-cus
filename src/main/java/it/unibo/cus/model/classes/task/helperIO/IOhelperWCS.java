package it.unibo.cus.model.classes.task.helperIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unibo.cus.model.api.HelperInputOutput;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.enums.ProblemsState;
import it.unibo.cus.model.enums.WorkMode;
import it.unibo.cus.service.SerialChannel;
import jssc.SerialPortException;

public class IOhelperWCS implements HelperInputOutput{
    private SerialChannel serial;
    private boolean resolved = true;
    private boolean talking = false;

    public IOhelperWCS(){
        try {
            serial = new SerialChannel("COM8", 9600);
        } catch (Exception e) {
            serial = null;
        }
    }

    @Override
    public void read() {
        try {
            serial.update();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (serial.hasMsg()) {
            String msg = serial.readMsg();
            if (msg == null || msg.isBlank()) {
                return;
            }
            parseMessage(msg);
        }
    }

    @Override
    public void send() {
        if(!resolved){
            if(State.workMode == WorkMode.AUTOMATIC){
                resolved = true;
            } else {
                if(!talking){
                    State.sendConfirmHtml = true;
                    talking = true;
                    return;
                }
                if(!State.sendConfirmHtml){
                    talking = false;
                    resolved = true;
                    if(!State.htmlAnswer){
                        State.valveOpenPercentage = State.valveOpenPercentageWCS;
                    }
                } else {
                    return;
                }
            }
        }
        String message = State.workMode.toString() + "-" + State.problemsState.toString();
        if(!(State.problemsState == ProblemsState.UNCONNECTED || State.problemsState == ProblemsState.UNCON_NOT_AVAIL)){
            message += "-" + State.waterLevel;
        }
        if(!(State.problemsState == ProblemsState.NOT_AVAILABLE || State.problemsState == ProblemsState.UNCON_NOT_AVAIL)){
            message += "-" + State.valveOpenPercentage;
        }
        try {
            serial.send(message);
        } catch (SerialPortException e) {
            System.err.println("Problem with serial line");
        }
    }

    @Override
    public boolean guard() {
        return serial != null && serial.isOpen();
    }

    /**
     * Splits a message using '-'.
     */
    @SuppressWarnings("null")
    private List<String> splitByDash(String part) {

        if (part == null || part.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(part.split("-"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Type of message
     * WorkState-ProblemState-valvePositionCurrently-valveFinalPosition
     * @param msg
     */
    private void parseMessage(String msg){
        msg = msg.trim();
        List<String> parts = splitByDash(msg);
        WorkMode job = WorkMode.toEnum(parts.get(0));
        ProblemsState prob = ProblemsState.toEnum(parts.get(1));
        int posNow, finalPos;
        try {
            posNow = Integer.parseInt(parts.get(2));
        } catch (Exception e) {
            posNow = -1;
        }
        try {
            finalPos = Integer.parseInt(parts.get(3));
        } catch (Exception e) {
            finalPos = -1;
        }
        if(!(job == null || job == State.workMode)){
            State.workMode = job;
        }
        if(!(prob == null || prob == State.problemsState)){
            State.problemsState = prob;
        }
        if(posNow != -1){
            State.valveCurrentPercentage = posNow;
        }
        if(finalPos != -1){
            if(finalPos != State.valveOpenPercentage){
                if(State.dbsInactive){
                    State.valveOpenPercentage = finalPos;
                } else if(!State.sendConfirmHtml){
                    resolved = false;
                    State.sendConfirmHtml = true;
                }
            }
            State.valveOpenPercentageWCS = finalPos;
        }
    }

}
