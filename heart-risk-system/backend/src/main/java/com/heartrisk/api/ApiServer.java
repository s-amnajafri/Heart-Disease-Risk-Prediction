package com.heartrisk.api;

import com.heartrisk.service.NotFoundException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

/**
 * Javalin REST server: CORS, the six routes and JSON error handling
 * (400 for invalid input, 404 for unknown resources, 500 for unexpected failures).
 */
public class ApiServer {

    private static final Logger LOGGER = Logger.getLogger(ApiServer.class.getName());
    private static final String ERROR_SENT = "errorSent";

    private final Javalin app;

    /**
     * Configures the server but does not start it.
     *
     * @param handlers endpoint implementations
     */
    public ApiServer(ApiHandlers handlers) {
        app = Javalin.create(config ->
                config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost())));
        app.get("/api/health", handlers::health);
        app.get("/api/doctors", handlers::doctors);
        app.get("/api/patients", handlers::allPatients);
        app.post("/api/patients", handlers::createPatient);
        app.post("/api/predict", handlers::predict);
        app.get("/api/patients/{id}/history", handlers::history);
        app.get("/api/model/metrics", handlers::metrics);
        app.get("/api/report/{assessmentId}", handlers::report);
        registerErrorHandling();
    }

    /**
     * Starts listening.
     *
     * @param port TCP port
     * @return this server
     */
    public ApiServer start(int port) {
        app.start(port);
        return this;
    }

    /** Stops the server. */
    public void stop() {
        app.stop();
    }

    private void registerErrorHandling() {
        app.exception(IllegalArgumentException.class, (e, ctx) -> fail(ctx, 400, e.getMessage()));
        app.exception(NotFoundException.class, (e, ctx) -> fail(ctx, 404, e.getMessage()));
        app.exception(Exception.class, (e, ctx) -> {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            fail(ctx, 500, "Internal server error");
        });
        app.error(404, ctx -> {
            if (ctx.attribute(ERROR_SENT) == null) {
                fail(ctx, 404, "Resource not found: " + ctx.path());
            }
        });
    }

    private static void fail(Context ctx, int status, String message) {
        ctx.attribute(ERROR_SENT, Boolean.TRUE);
        ctx.status(status).json(Map.of("error", message, "status", status));
    }
}
