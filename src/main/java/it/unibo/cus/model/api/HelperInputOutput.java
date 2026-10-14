package it.unibo.cus.model.api;

public interface HelperInputOutput {
    /**
     * This method is responsible for reading data from the input source.
     * It implements all specific parsing and processing of the input data.
     * The implementation of this method should handle any exceptions that may occur during the reading process.
     * It should also ensure that the input data is valid and meets the expected format.
     * The method should be designed to be efficient and should not block the main execution thread.
     */
    void read();
    /**
     * This method is responsible for sending data to the output destination.
     * It implements all specific formatting and transmission of the output data.
     * The implementation of this method should handle any exceptions that may occur during the sending process.
     * It should also ensure that the output data is valid and meets the expected format.
     * The method should be designed to be efficient and should not block the main execution thread.
     */
    void send();
    /**
     * This method is responsible for determining whether the task should be executed or not.
     * @return true if the task should be executed, false otherwise.
     */
    boolean guard();
}
