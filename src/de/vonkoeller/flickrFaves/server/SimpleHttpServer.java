package de.vonkoeller.flickrFaves.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.vonkoeller.flickrFaves.api.AuthHolder;
import de.vonkoeller.flickrFaves.debug.Tracer;
import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import static de.vonkoeller.flickrFaves.gui.Constants.OAUTH_CALLBACK_PORT;


public class SimpleHttpServer {
    private static HttpServer server;

    public static void start() throws IOException {
        int port = OAUTH_CALLBACK_PORT;
        try {
            InetSocketAddress addr = new InetSocketAddress(port);
            server = HttpServer.create(addr, 0);
        } catch (BindException e) {
            throw new FlickrFaveException("Port " + port + " is already in use. "
                    + "Are there any other instance of FlickrFaves running?", e);
        }

        server.createContext("/", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}

class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        exchange.getRequestBody();
        if (requestMethod.equalsIgnoreCase("GET")) {
            String param = exchange.getRequestURI().getRawQuery();
            param = java.net.URLDecoder.decode(param, "UTF-8");
            Map<String, String> params = queryToMap(param);

            String verifier = params.get("oauth_verifier");
            AuthHolder.setVerifier(verifier);
            Tracer.trace("oauth_verifier: " + verifier);

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);

            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write("Done. You can close this and return to FlickrFaves.".getBytes());

            responseBody.close();
        }
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }
}