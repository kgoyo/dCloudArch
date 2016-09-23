package cloud.cave.server;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveException;
import cloud.cave.common.CaveSlowResponseException;
import cloud.cave.common.CaveTimeOutException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
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
    public HttpResponse getResponse(String url) throws CaveException {
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

        try {
            response = client.execute(request);
        } catch(HttpHostConnectException | ConnectTimeoutException e) {
            throw new CaveTimeOutException("connection timed out");
        } catch(RequestAbortedException | SocketTimeoutException e) {
            throw new CaveSlowResponseException("response from server, was too slow");
        } catch (IOException e) {
            throw new CaveCantConnectException("unable to connect to service");
        }
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
