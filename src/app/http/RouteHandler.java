package app.http;

import java.io.IOException;

@FunctionalInterface
public interface RouteHandler {
    void handle(RequestContext ctx) throws IOException;
}
