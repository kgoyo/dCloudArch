package cloud.cave.config.rabbitMQ;

import cloud.cave.broker.ServerRequestHandler;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.socket.SocketServerRequestHandler;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by kgoyo on 06-10-2016.
 */
public class RabbitServerRequestHandler implements ServerRequestHandler {
    private static final String RPC_QUEUE_NAME = "rpc";
    private QueueingConsumer consumer;
    private Channel channel;
    private ObjectManager objectManager;
    private JSONParser parser;
    private Logger logger;

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
        parser = new JSONParser();
        logger = LoggerFactory.getLogger(RabbitServerRequestHandler.class);

        ConnectionFactory factory = new ConnectionFactory();

        ServerData data = config.get(0);
        factory.setHost(data.getHostName());
        factory.setPort(data.getPortNumber());

        Connection connection = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                BasicProperties props = delivery.getProperties();
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(props.getCorrelationId())
                        .build();

                String message = new String(delivery.getBody());
                JSONObject request = (JSONObject) parser.parse(message);

                //handle request
                JSONObject responseJson = objectManager.getInvoker().handleRequest(request);
                String response = responseJson.toJSONString(); //FIXME maybe use regulare toString()

                channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes());

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
