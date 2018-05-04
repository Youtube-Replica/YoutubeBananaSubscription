package commands;


import model.Subscription;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class RetrieveSubscription extends Command {
    public static int id = 0;
    public void execute() {
        HashMap<String, Object> props = parameters;

        Channel channel = (Channel) props.get("channel");
        JSONParser parser = new JSONParser();
        id = 0;
        try {
            System.out.println(props);
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            System.out.println(body.toString());
            JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
            id = Integer.parseInt(params.get("id").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
        String response = Subscription.getSubscriptionByID(id); //Gets channels subscribed by id
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//   public static void main(String [] argv) {
//        ArangoDB arangoDB = new ArangoDB.Builder().build();
//        String dbName = "subscriptions";
//        String collectionName = "firstSubscription";
////        int idRead = id;
//
//        //Read Document
//        //haygeely id harod b list ids
//        try {
//            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument(""+id,
//                    BaseDocument.class);
//            System.out.println("Key: " + myDocument.getKey());
//            System.out.println("Attribute a: " + myDocument.getAttribute("IDs"));
//        } catch (ArangoDBException e) {
//            System.err.println("Failed to get document: myKey; " + e.getMessage());
//        }
//
////        try {
////            String query = "FOR t IN firstSubscription FILTER t.name == @name RETURN t";
////            Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
////
////            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
////                    BaseDocument.class);
////
////            for (; cursor.hasNext();) {
////                System.out.println("Key: " + cursor.next());
////                // TODO
////            }
////
//////            cursor.forEachRemaining(aDocument -> {
//////                System.out.println("Key: " + aDocument.getKey());
//////            });
////        } catch (ArangoDBException e) {
////            System.err.println("Failed to execute query. " + e.getMessage());
////        }
////        return dbName;
//    }

}
