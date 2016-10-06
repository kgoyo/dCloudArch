package cloud.cave.common;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by amao on 10/6/16.
 */
public class RabbitMaltaQuest {
    private final static String QUEUE_NAME = "MaltaQuest";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String msg = "Fuck you, Malta!";
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            System.out.println("Sent message");

            channel.close();
            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
class RabbitMaltaQuestRecieve {
    private final static String QUEUE_NAME = "MaltaQuest";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            System.out.println("Waiting for messages");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                }
            };

            channel.basicConsume(QUEUE_NAME, true, consumer);

            channel.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
