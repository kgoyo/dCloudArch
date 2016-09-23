package cloud.cave.doubles;

import cloud.cave.common.CaveException;
import cloud.cave.server.Requester;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class ExceptionHttpRequester implements Requester {

    private CaveException e;

    public ExceptionHttpRequester(CaveException e) {
        this.e = e;
    }

    @Override
    public HttpResponse getResponse(String url) throws IOException {
        throw e;
    }

    @Override
    public String responseContentToString(HttpResponse response) throws IOException {
        throw e;
    }

    @Override
    public JSONObject responseContentToJSON(HttpResponse response) throws IOException {
        throw e;
    }
}
