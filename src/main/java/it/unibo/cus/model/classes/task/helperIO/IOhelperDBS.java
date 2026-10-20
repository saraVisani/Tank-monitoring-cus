package it.unibo.cus.model.classes.task.helperIO;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import it.unibo.cus.model.api.HelperInputOutput;
import it.unibo.cus.model.classes.State;
import it.unibo.cus.model.classes.task.helperConnections.CheckAndReconnectDBS;
import it.unibo.cus.model.enums.WorkMode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class IOhelperDBS implements HelperInputOutput {
    
    private static HttpServer server;

    public IOhelperDBS() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/command", new CommandHandler());
            server.setExecutor(null); 
            server.start();
            System.out.println("HTTP Server for DBS started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read() {
    
    }

    @Override
    public void send() {

    }

    @Override
    public boolean guard() {
        return true;
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            CheckAndReconnectDBS.ping(); 

            String response = String.format(
                "{\"waterLevel\": %d, \"valveOpenPercentage\": %d, \"workMode\": \"%s\", \"problemsState\": \"%s\"}",
                State.waterLevel,          
                State.valveOpenPercentage, 
                State.workMode.name(),     
                State.problemsState.toString()
            );

            // Attach the labels to the postal package to tell Python
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    static class CommandHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            CheckAndReconnectDBS.ping();

            if ("POST".equals(exchange.getRequestMethod())) {
                
                String body = new String(exchange.getRequestBody().readAllBytes());
                
                if (body.contains("workMode=MANUAL")) {
                    State.workMode = WorkMode.MANUAL;
                } 
                else if (body.contains("workMode=AUTOMATIC")) {
                    State.workMode = WorkMode.AUTOMATIC;
                }
                
                if (body.contains("valve=")) {
                    try {
                        String valStr = body.split("valve=")[1].split("&")[0];
                        State.valveOpenPercentage = Integer.parseInt(valStr);
                    } catch (Exception e) {
                        //If there is an error in reading the number, it is ignored to prevent a crash.
                    }
                }
                
                String response = "OK";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}