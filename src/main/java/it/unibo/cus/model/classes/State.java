package it.unibo.cus.model.classes;

import it.unibo.cus.model.classes.task.helperIO.IOhelperWCS;
import it.unibo.cus.model.enums.ProblemsState;
import it.unibo.cus.model.enums.WorkMode;
import it.unibo.cus.service.MqttService;

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

    /**
     * Desired valve opening percentage.
     *
     * <p>Represents the target opening percentage requested by the control logic.
     * It indicates where the valve should be positioned, not its current physical
     * position.
     *
     * <p><b>Modified by:</b>
     * <ul>
     *   <li>{@link DecideOpening} in automatic mode.</li>
     *   <li>{@link IOhelperDBS} in manual mode.</li>
     *   <li>{@link IOhelperWCS} in manual mode, only after DBS confirmation.</li>
     * </ul>
     *
     * @see #valveCurrentPercentage
     * @see #valveOpenPercentageWCS
     */
    public static int valveOpenPercentage = 0;

    /**
     * Current valve opening percentage.
     *
     * <p>Represents the valve's current physical opening, as reported by WCS.
     * Unlike {@link #valveOpenPercentage}, this variable describes the current
     * position rather than the desired position.
     *
     * <p><b>Modified by:</b>
     * <ul>
     *   <li>{@link IOhelperWCS}.</li>
     * </ul>
     *
     * @see #valveOpenPercentage
     */
    public static int valveCurrentPercentage = 0;

    /**
     * WCS potentiometer opening percentage.
     *
     * <p>Represents the opening percentage requested through the WCS
     * potentiometer while the system is in manual mode.
     *
     * <p><b>Modified by:</b>
     * <ul>
     *   <li>{@link IOhelperWCS} in manual mode.</li>
     * </ul>
     *
     * @see #valveOpenPercentage
     * @see #valveCurrentPercentage
     */
    public static int valveOpenPercentageWCS = 0;

    /**
     * Indicates whether DBS has been inactive for too long.
     *
     * <p>When {@code true}, confirmation requests from WCS are
     * automatically accepted without waiting for the HTML interface.
     *
     * <p>Modified by {@code IOhelperDBS}.
     *
     * @see #sendConfirmHtml
     * @see #htmlAnswer
     */
    public static boolean dbsInactive = false;

    /**
     * HTML confirmation handshake flag.
     *
     * <p>Coordinates the confirmation process between WCS and DBS when WCS
     * requests approval for a valve opening change in manual mode.
     *
     * <p><b>Protocol:</b>
     * <ol>
     *   <li>WCS sets this flag to {@code true} to request confirmation.</li>
     *   <li>DBS processes the request and stores the result in {@link #htmlAnswer}.</li>
     *   <li>DBS sets this flag to {@code false} to signal that the response is ready.</li>
     *   <li>WCS detects the flag reset and reads {@link #htmlAnswer}.</li>
     * </ol>
     *
     * <p><b>Modified by:</b>
     * <ul>
     *   <li>{@link IOhelperWCS} in manual mode, to request confirmation.</li>
     *   <li>{@link IOhelperDBS} in manual mode, to signal that the response is ready.</li>
     * </ul>
     *
     * @see #htmlAnswer
     * @see #dbsInactive
     */
    public static boolean sendConfirmHtml = false;

    /**
     * HTML confirmation result.
     *
     * <p>Indicates whether the HTML interface accepts the valve opening change
     * requested by WCS.
     *
     * <p><b>Possible values:</b>
     * <ul>
     *   <li>{@code true}: the requested change is accepted.</li>
     *   <li>{@code false}: the requested change is rejected.</li>
     * </ul>
     *
     * <p>The result is read by WCS after {@link #sendConfirmHtml} becomes
     * {@code false}, indicating that DBS has completed the confirmation process.
     *
     * <p><b>Modified by:</b>
     * <ul>
     *   <li>{@link IOhelperDBS} in manual mode, after processing the response
     *       from the HTML interface.</li>
     * </ul>
     *
     * @see #sendConfirmHtml
     * @see #valveOpenPercentage
     * @see #dbsInactive
     */
    public static boolean htmlAnswer = false;

    public static float waterLevel = 0;

    public static MqttService serviceMQTT;
}
