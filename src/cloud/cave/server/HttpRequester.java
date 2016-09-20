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

public class HttpRequester implements Requester {

    private final HttpClient client;
    private int timeOut;
    private long slowResponse;
    private JSONParser parser;


    public HttpRequester(int timeOut, long slowResponse) {
        this.timeOut = timeOut;
        this.slowResponse = slowResponse;
        client = HttpClientBuilder.create().build();
        parser = new JSONParser();
    }

    /**
     * does stuff
     * @param url
     * @return the HttpResponse gotten from the doing a http get request to the given url
     * @throws IOException
     */
    public HttpResponse getResponse(String url) throws IOException {
        //timeout example
        HttpResponse response;
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout( timeOut) //time before SocketTimeoutException
                .setConnectTimeout(timeOut) //time before ConnectTimeoutException
                .build();
        request.setConfig(requestConfig);

        //time before slow response
        long slowResponseTime = slowResponse;
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
    }

    public String responseContentToString(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String result = "";
        String line;
        while((line = rd.readLine()) != null) {
            result+=line;
        }
        return result;
    }

    public JSONObject responseContentToJSON(HttpResponse response) throws IOException, ParseException {
        return (JSONObject) parser.parse(responseContentToString(response));
    }


}
