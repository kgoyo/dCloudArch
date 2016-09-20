package cloud.cave.libraries;

import cloud.cave.server.HttpRequester;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static org.junit.Assert.assertEquals;

/**
 * Created by kgoyo on 15-09-2016.
 */
public class TestApacheHttpClient {
    private CloseableHttpClient httpclient;
    private String url = "http://skycave.baerbak.com:7654/api/v2/auth?loginName=201303609&password=Kappa123";
    private String expected = "{\"success\":true,\"subscription\":{\"groupName\":\"css-14\",\"dateCreated\":\"2016-09-08 13:24 PM UTC\",\"playerName\":\"Graxor Destroyer of worlds\",\"loginName\":\"201303609\",\"region\":\"AARHUS\",\"groupToken\":\"Arsenic154_Nicaragua511\",\"playerID\":\"57d16692a7b11b000529fd35\"},\"message\":\"loginName 201303609 was authenticated\"}";

    @Before
    public void setup() {
        //
    }

    @After
    public void cleanup() {
        //
    }

    @Test
    public void subscriptionServiceResponse () {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        //request header
        request.addHeader("User-Agent", USER_AGENT);
        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String result = "";
            String line = "";
            while((line = rd.readLine()) != null) {
                result+=line;
            }

             assertEquals( expected, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void subscriptionServiceResponseUsingMethod () {

        try {
            HttpRequester http = new HttpRequester(3000,8000);
            HttpResponse response = http.getResponse(url);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String result = "";
            String line = "";
            while((line = rd.readLine()) != null) {
                result+=line;
            }
            assertEquals( expected, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void jsonToStringTest() {
        String url2 = "http://skycave.baerbak.com:7654/api/v2/auth";
        JSONObject expected = new JSONObject();
        expected.put("success",false);
        expected.put("message","loginName or password not given");
        HttpRequester http = new HttpRequester(3000,8000);
        try {
            JSONObject json = http.responseContentToJSON(http.getResponse(url2));
            assertEquals(expected,json);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
