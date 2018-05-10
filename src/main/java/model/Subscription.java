package model;

import Client.Client;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Subscription {
    static ArangoDB arangoDB;
    static Subscription instance = new Subscription();
    static String dbName = "subscriptions";
    static String collectionName = "firstSubscription";

    private Subscription(){
        arangoDB = new ArangoDB.Builder().build();
    }

    public static Subscription getInstance(){
        return Subscription.instance;
    }

    public void setDB(int i){
        arangoDB = new ArangoDB.Builder().maxConnections(i).build();
    }
    //Gets channels' info subscribed by user ID(user ID = _key and maps to array of channels' IDs)
    public static String getSubscriptionByID(int id) {
        JSONObject subscriptionObjectM = new JSONObject();
        JSONArray subscriptionArray = new JSONArray();

        try {
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);

            if(myDocument != null){
                ArrayList<Integer> ids = new ArrayList<Integer>();
                ids = (ArrayList) myDocument.getAttribute("id-sub");

                for (int i = 0; i < ids.size(); i++) {
                    try {
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("channel").getDocument("" + ids.get(i),
                                BaseDocument.class);
                        if(myDocument2 != null){
                            JSONObject subscriptionObject = new JSONObject();

                            subscriptionObject.put("channel_id",ids.get(i));
                            subscriptionObject.put("info",myDocument2.getAttribute("info"));
                            subscriptionObject.put("subscriptions",myDocument2.getAttribute("subscriptions"));
                            subscriptionObject.put("watched_videos",myDocument2.getAttribute("watched_videos"));
                            subscriptionObject.put("blocked_channels",myDocument2.getAttribute("blocked_channels"));
                            subscriptionObject.put("notifications",myDocument2.getAttribute("notifications"));

                            subscriptionArray.add(subscriptionObject);
                        }

                    }
                    catch (ArangoDBException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
                        System.err.println("Failed to get document: myKey; " + e.getMessage());
                    }
                }
                subscriptionObjectM.put(id,subscriptionArray);
            }

        } catch (ArangoDBException e) {
            Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        return subscriptionObjectM.toString();

    }
    //Add a new channel subscription to user ID (add to the array of channels)
    public static String postSubscriptionByID(int id, int subID){
        ArrayList<Integer> ids = new ArrayList<>();
        //Create Document
        if(arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class) == null) {
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(id+"");
            ids.add(subID);
            myObject.addAttribute("id-sub", ids);
            try {
                arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
                Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Information> Document created ", CharsetUtil.UTF_8));
                System.out.println("Document created");
            } catch (ArangoDBException e) {
                Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to create document " + e.getMessage(), CharsetUtil.UTF_8));
                System.err.println("Failed to create document. " + e.getMessage());
            }
            return true+"";
        }
        else{
            BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            ids = (ArrayList) myDocument2.getAttribute("id-sub");
            ids.add(subID);
            myDocument2.updateAttribute("id-sub",ids);
            arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
            return true+"";
        }
    }

    public static String deleteSubscriptionByID(int id, int subID){
        ArrayList<Long> ids = new ArrayList<>();
        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class);
        if(myDocument2!=null){
            ids.addAll((ArrayList<Long>)myDocument2.getAttribute("id-sub"));
            ids.remove(Long.valueOf(subID));
            myDocument2.updateAttribute("id-sub",ids);
            arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
        }

        return true+"";
    }

}
