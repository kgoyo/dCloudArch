package cloud.cave.server;

import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;


/**
 * Created by kgoyo on 20-09-2016.
 */
public interface Requester {
    HttpResponse getResponse(String url) throws IOException;
    String responseContentToString(HttpResponse response) throws IOException;
    JSONObject responseContentToJSON(HttpResponse response) throws IOException, ParseException;
}
