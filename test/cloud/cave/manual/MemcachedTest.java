package cloud.cave.manual;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by amao on 10/13/16.
 */
public class MemcachedTest {
    public static void main(String[] args) {
        int port = 11211;
        try {
            MemcachedClient mc = new MemcachedClient(new InetSocketAddress("localhost", port));

            String key = "cachetest";

            mc.set(key, 2 , port );

            System.out.println( "The port is: " + mc.get(key) );

            Thread.sleep(3000);

            System.out.println( "The port is: " + mc.get(key) );

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
