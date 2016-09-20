package cloud.cave.server;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class HttpRequester {
    /**
     * does stuff
     * @param url
     * @return the HttpResponse gotten from the doing a http get request to the given url
     * @throws IOException
     */
    public static HttpResponse getResponse(String url) throws IOException {
        //timeout example
        HttpResponse response;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000) //time before SocketTimeoutException
                .setConnectTimeout(3000) //time before ConnectTimeoutException
                .build();
        request.setConfig(requestConfig);

        //time before slow response
        long slowResponseTime = 8000;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (request != null) {
                    request.abort();
                }
            }
        };
        new Timer(true).schedule(task, slowResponseTime);

        response = client.execute(request);
        return response;
    } //can throw other exception like: HttpHostConnectException, ...

    public static String responseContentToString(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String result = "";
        String line = "";
        while((line = rd.readLine()) != null) {
            result+=line;
        }
        return result;
    }

    public static JSONObject responseContentToJSON(HttpResponse response) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(responseContentToString(response));
    }


}
