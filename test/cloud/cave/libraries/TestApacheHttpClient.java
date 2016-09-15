package cloud.cave.libraries;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
        String url = "http://skycave.baerbak.com:7654/api/v2/auth?loginName=201303609&password=Kappa123";
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
           
            String expected = "{\"success\":true,\"subscription\":{\"groupName\":\"css-14\",\"dateCreated\":\"2016-09-08 13:24 PM UTC\",\"playerName\":\"Graxor Destroyer of worlds\",\"loginName\":\"201303609\",\"region\":\"AARHUS\",\"groupToken\":\"Arsenic154_Nicaragua511\",\"playerID\":\"57d16692a7b11b000529fd35\"},\"message\":\"loginName 201303609 was authenticated\"}";
            assertEquals( expected, result);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //expected return

    }
}
