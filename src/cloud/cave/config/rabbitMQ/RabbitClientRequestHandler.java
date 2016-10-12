package cloud.cave.config.rabbitMQ;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by kgoyo on 06-10-2016.
 */
public class RabbitClientRequestHandler implements ClientRequestHandler {
    private static final String RPC_QUEUE_NAME = "skycave_rpc_queue";
    private static final String RPC_EXCHANGE_NAME = "skycave_exchange";
    private Connection connection;
    private Channel channel;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private JSONParser parser;

    @Override
    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) throws CaveIPCException {
        JSONObject response = null;
        String corrId = java.util.UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        String message = requestJson.toJSONString();
        try {
            channel.exchangeDeclare(RPC_EXCHANGE_NAME, "direct");
            channel.basicPublish(RPC_EXCHANGE_NAME, "", props, message.getBytes());


        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                String responseAsString = new String(delivery.getBody());
                response = (JSONObject) parser.parse(responseAsString);
                break;
            }
        }
        } catch (IOException e) {
            //throw CaveIPC...
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        parser = new JSONParser();
        ServerData data = config.get(0);
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(data.getHostName());
            factory.setPort(data.getPortNumber());
            connection = factory.newConnection();
            channel = connection.createChannel();

            replyQueueName = channel.queueDeclare().getQueue();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
