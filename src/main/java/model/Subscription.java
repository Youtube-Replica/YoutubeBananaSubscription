package model;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Subscription {
    static String dbName = "scalable";
    static String collectionName = "subscription";
    //Gets channels' info subscribed by user ID(user ID = _key and maps to array of channels' IDs)
    public static String getSubscriptionByID(int id) {
        ArangoDB arangoDB = new ArangoDB.Builder().build();

        JSONObject subscriptionObjectM = new JSONObject();
        JSONArray subscriptionArray = new JSONArray();

        //haygeely id harod b list ids
        try {
            System.out.println("in try");
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);

            if(myDocument != null){
                ArrayList<Integer> ids = new ArrayList<Integer>();
                System.out.println("arraylist");

                ids = (ArrayList) myDocument.getAttribute("id-sub");
                System.out.println("Ids found:" + ids);

                for (int i = 0; i < ids.size(); i++) {
                    try {
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("channel").getDocument("" + ids.get(i),
                                BaseDocument.class);
                        if(myDocument2 != null){
                            JSONObject subscriptionObject = new JSONObject();

                            System.out.println("myDoc 2" + myDocument2);

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
                        System.err.println("Failed to get document: myKey; " + e.getMessage());
                    }
                }
                subscriptionObjectM.put(id,subscriptionArray);
            }

        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }


        return subscriptionObjectM.toString();

    }
    //Add a new channel subscription to user ID (add to the array of channels)
    public static String postSubscriptionByID(int id, int subID){
        ArangoDB arangoDB = new ArangoDB.Builder().build();

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
                System.out.println("Document created");
            } catch (ArangoDBException e) {
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
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        ArrayList<Long> ids = new ArrayList<>();
        //Delete sub from Document
        //Case 1: not the only subscription
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
