package waes.tech.assessment.controller;

import com.google.gson.*;
import com.google.inject.*;
import org.apache.commons.codec.binary.*;
import org.slf4j.*;
import waes.tech.assessment.service.*;

import static org.slf4j.LoggerFactory.*;
import static spark.Spark.*;

@Singleton
public class WaesTAController {

    private static final Logger LOG = getLogger(WaesTAController.class.getCanonicalName());
    private static final Gson gson = new Gson();

    private WaesTAService waesTAService;

    @Inject
    WaesTAController(final WaesTAService waesTAService) {
        this.waesTAService = waesTAService;
    }

    public void run() {
        port(8080);
        LOG.info("Up and running...");

        //Health check endpoint.
        get("/ping", (request, response) -> "pong!");

        //Diff endpoint
        get("/v1/diff/:id/", (request, response) ->
                JsonParser.parseString(gson.toJson(waesTAService.getDiffOfElementById(request.params(":id"))))
        );

        //Element creation endpoints
        path("/v1/diff/:id", () -> {
            //I'm assuming that the encoded data is coming as part of a JSON in which there is a key "data" and its value is base64 encoded information.
            post("/right", (request, response) -> {
                JsonObject requestBody = gson.fromJson(request.body(), JsonObject.class);
                String dataAsString = new String(Base64.decodeBase64(String.valueOf(requestBody.get("data"))));
                return waesTAService.saveElement(gson.fromJson(dataAsString, JsonObject.class), request.params(":id"), "right");
            });
            post("/left", (request, response) -> {
                JsonObject requestBody = gson.fromJson(request.body(), JsonObject.class);
                String dataAsString = new String(Base64.decodeBase64(String.valueOf(requestBody.get("data"))));
                return waesTAService.saveElement(gson.fromJson(dataAsString, JsonObject.class), request.params(":id"), "left");
            });
        });

        //Hanlder for 404 errors
        notFound((req, res) ->
                "{\"message\":\"Resource not found.\"}"
        );

        //Hanlder for 500 errors
        internalServerError((req, res) ->
                "{\"message\":\"An internal error occurred.\"}"
        );

        afterAfter(((request, response) -> response.type("application/json")));
    }
}
