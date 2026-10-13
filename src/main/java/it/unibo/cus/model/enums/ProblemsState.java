package it.unibo.cus.model.enums;

/**
 * Enum ProblemsState this enum is for the error state of the system with OK to signal if there is no error, UNCONNECTED to signal
 * no connections with TMS, NOT_AVAILABLE no connection with DBS, the last one for both errors.
 */
public enum ProblemsState {
    OK,
    UNCONNECTED,
    NOT_AVAILABLE,
    UNCON_NOT_AVAIL;

    @Override
    public String toString() {
        return switch (this) {
            case OK -> "Ok";
            case UNCONNECTED -> "Unconnected";
            case NOT_AVAILABLE -> "Not available";
            case UNCON_NOT_AVAIL -> "Unconnected and not available";
        };
    }
}
