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

    private enum HandshakeState {
        /** No pending handshake. */
        IDLE,
        /** Waiting for confirmation of a work mode change. */
        STATE,
        /** Waiting for confirmation of a valve opening change. */
        VALVE,
        /** Waiting for confirmation of both work mode and valve changes. */
        ALL;
    }

    /** Current handshake state, tracking all pending WCS requests. */
    private HandshakeState handshake = HandshakeState.IDLE;
    private SerialChannel serial;

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
        // In automatic mode, valve confirmation is no longer required.
        if(this.isWaitingValve() && State.workMode == WorkMode.AUTOMATIC && State.workModeWCS == WorkMode.AUTOMATIC){
            this.resolveHandshake(HandshakeState.VALVE);
        }
        if(this.isWaiting()){
            if(!State.sendConfirmHtml){
                // DBS has completed all pending confirmations.
                this.resolveHandshake(HandshakeState.ALL);
                if (State.htmlAnswerValve) {
                    State.valveOpenPercentage = State.valveOpenPercentageWCS;
                } else {
                    State.valveOpenPercentageWCS = State.valveOpenPercentage;
                }
                if (State.htmlAnswerState) {
                    State.workMode = State.workModeWCS;
                } else {
                    State.workModeWCS = State.workMode;
                }
            } else {
                // Keep WCS informed about the pending handshake.
                String handMsg = switch (handshake) {
                    case STATE -> "Handshake-State";
                    case VALVE -> "Handshake-Valve";
                    case ALL -> "Handshake-All";
                    case IDLE -> "";
                };
                try {
                    serial.send(handMsg);
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        // Send the regular system status once no handshake is pending.
        String message = State.workMode.toString() + "-" + State.problemsState.toStringForCommunication();
        // Water level is omitted when TMS is disconnected.
        if (State.problemsState != ProblemsState.UNCONNECTED
                && State.problemsState != ProblemsState.UNCON_NOT_AVAIL) {
            message += "-" + State.waterLevel;
        }

        // Include the valve target only when it is available in the current state.
        if (State.problemsState != ProblemsState.UNCON_NOT_AVAIL
                && (State.workMode == WorkMode.AUTOMATIC
                || State.problemsState != ProblemsState.NOT_AVAILABLE)) {
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

    private boolean isWaiting(){
        return handshake != HandshakeState.IDLE;
    }

    private boolean isWaitingValve(){
        return handshake == HandshakeState.VALVE || handshake == HandshakeState.ALL;
    }

    /**
     * Resolves the specified pending request.
     * If both requests are pending, only the resolved request is removed.
     *
     * @param newState type of request to resolve
     */
    private void resolveHandshake(HandshakeState newState){
        switch (newState) {
                case VALVE:
                    switch (this.handshake) {
                        case VALVE:
                            this.handshake = HandshakeState.IDLE;
                            break;
                        case ALL:
                            this.handshake = HandshakeState.STATE;
                            break;
                        default:
                            break;
                    }
                    break;
                case STATE:
                    switch (this.handshake) {
                        case STATE:
                            this.handshake = HandshakeState.IDLE;
                            break;
                        case ALL:
                            this.handshake = HandshakeState.VALVE;
                            break;
                        default:
                            break;
                    }
                    break;
                case ALL:
                    this.handshake = HandshakeState.IDLE;
                default:
                    break;
            }
    }

    /**
     * Adds a pending request to the handshake.
     * If a different request is already pending, both are combined into ALL.
     *
     * @param newState type of request to add
     */
    private void addHandshake(HandshakeState newState){
        switch (newState) {
            case VALVE:
                switch (this.handshake) {
                    case STATE:
                        this.handshake = HandshakeState.ALL;
                        break;
                    case ALL:
                        break;
                    default:
                        this.handshake = newState;
                        break;
                }
                break;
            case STATE:
                switch (this.handshake) {
                    case VALVE:
                        this.handshake = HandshakeState.ALL;
                        break;
                    case ALL:
                        break;
                    default:
                        this.handshake = newState;
                        break;
                }
                break;
            default:
                break;
        }
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
     * Parses a message received from WCS.
     *
     *<p><b>Message formats:</b>
     * <ul>
     *   <li><p><b>Manual mode:</b>
     *      <ul>
     *          <li>WorkMode-valvePositionCurrently-valveFinalPosition</li>
     *      </ul></li>
     *   <li><p><b>Automatic mode:</b>
     *          <ul>
     *          <li>WorkMode-valvePositionCurrently</li>
     *      </ul></li>
     *   <li><p><b>Handshake:</b>
     *          <ul>
     *          <li>WorkMode-valvePositionCurrently-valveFinalPosition</li>
     *      </ul></li>
     * </ul>
     * The final valve position is optional in handshake messages.
     *
     * @param msg message received from WCS
     */
    private void parseMessage(String msg){
        msg = msg.trim();
        List<String> parts = splitByDash(msg);
        if(parts.isEmpty()){
            return;
        }
        WorkMode job = WorkMode.toEnum(parts.get(0));

        int posNow, finalPos;

        if(parts.size() >= 2){
            try {
                posNow = Integer.parseInt(parts.get(1));
            } catch (Exception e) {
                posNow = -1;
            }
        } else {
            posNow = -1;
        }

        if(parts.size() >= 3){
            try {
                finalPos = Integer.parseInt(parts.get(2));
            } catch (Exception e) {
                finalPos = -1;
            }
        } else {
            finalPos = -1;
        }

        if(job != null){
            if(job != State.workMode){
                if(State.dbsInactive){
                    State.workMode = job;
                } else {
                    State.sendConfirmHtml = true;
                    this.addHandshake(HandshakeState.STATE);
                }
            }
            State.workModeWCS = job;
        }
        if(posNow != -1){
            State.valveCurrentPercentage = posNow;
        }
        if(finalPos != -1){
            if(finalPos != State.valveOpenPercentage){
                if(State.dbsInactive){
                    State.valveOpenPercentage = finalPos;
                } else {
                    this.addHandshake(HandshakeState.VALVE);
                    State.sendConfirmHtml = true;
                }
            }
            State.valveOpenPercentageWCS = finalPos;
        }
    }

}
