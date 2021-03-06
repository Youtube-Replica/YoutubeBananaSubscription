package commands;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.Subscription;

import java.io.IOException;
import java.util.HashMap;

public class DeleteSubscription extends Command {
    public static int id = 0;
    public static int subID = 0;
    public void execute() {
        System.out.println("IN DELETEEEE");
        HashMap<String, Object> props = parameters;
        Channel channel = (Channel) props.get("channel");
        JSONParser parser = new JSONParser();
        id = 0;
        subID = 0;
        try {
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
            System.out.println("Params" + params);
            id = Integer.parseInt(params.get("id").toString());
            System.out.println("id" + id);
            subID = Integer.parseInt(params.get("sub-id").toString());
            System.out.println("sub id"+subID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
        String response = Subscription.deleteSubscriptionByID(id,subID); //Gets channels subscribed by id
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
