package Model;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Subscriptions {

    //Gets channels' info subscribed by user ID(user ID = _key and maps to array of channels' IDs)
    public static String getSubscriptionByID(int id) {
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "firstSubscription";
        JSONObject subscriptionObjectM = new JSONObject();
        JSONArray subscriptionArray = new JSONArray();
        String subs = "";
        //Read Document
        //haygeely id harod b list ids
        try {
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                    BaseDocument.class);
            ArrayList<Integer> ids = new ArrayList<>();
            ids = (ArrayList) myDocument.getAttribute("ID");

            for (int i = 0; i < ids.size(); i++) {
                try {
                    BaseDocument myDocument2 = arangoDB.db(dbName).collection("Channels").getDocument("" + ids.get(i),
                            BaseDocument.class);
                    JSONObject subscriptionObject = new JSONObject();
                    subscriptionObject.put("Name",myDocument2.getAttribute("Name"));
                    subscriptionObject.put("Category",myDocument2.getAttribute("Category"));
                    subscriptionObject.put("Profile Picture",myDocument2.getAttribute("ProfilePicture"));
                    subscriptionArray.add(subscriptionObject);
                }
                catch (ArangoDBException e) {
                    System.err.println("Failed to get document: myKey; " + e.getMessage());
                }
            }
            subscriptionObjectM.put(id,subscriptionArray);
                } catch (ArangoDBException e) {
                    System.err.println("Failed to get document: myKey; " + e.getMessage());
                }


                return subscriptionObjectM.toString();


    }
    //Add a new channel subscription to user ID (add to the array of channels)
    public static String postSubscriptionByID(int id, int subID){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "firstSubscription";

        ArrayList<Integer> ids = new ArrayList<>();
        //Create Document
        if(arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class) == null) {
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(id+"");
            myObject.addAttribute("ID", subID);
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
            ids = (ArrayList) myDocument2.getAttribute("ID");
            ids.add(subID);
            myDocument2.updateAttribute("ID",ids);
            arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
            arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
            return true+"";
        }
    }

    public static String deleteSubscriptionByID(int id, int subID){
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        String collectionName = "firstSubscription";

        ArrayList<Long> ids = new ArrayList<>();
        //Delete sub from Document
        //Case 1: not the only subscription
        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + id,
                BaseDocument.class);

        ids.addAll((ArrayList<Long>)myDocument2.getAttribute("ID"));
        ids.remove(Long.valueOf(subID));
        myDocument2.updateAttribute("ID",ids);
        arangoDB.db(dbName).collection(collectionName).deleteDocument("" + id);
        arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);

        return true+"";
    }

   // public static void main(String [] argv) {
//        ArangoDB arangoDB = new ArangoDB.Builder().build();
//
//        //Create DB
//        String dbName = "subscriptions";
////        try {
////            arangoDB.createDatabase(dbName);
////            System.out.println("Database created: " + dbName);
////        } catch (ArangoDBException e) {
////            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
////        }
//
//        //Create Collection
//        String collectionName = "firstSubscription";
////        try {
////            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(collectionName);
////            System.out.println("Collection created: " + myArangoCollection.getName());
////        } catch (ArangoDBException e) {
////            System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage());
////        }
//
//        //Create Document
////        BaseDocument myObject = new BaseDocument();
////        ArrayList<Integer> ids = new ArrayList<Integer>();
////        ids.add(1);
////        ids.add(2);
////        ids.add(3);
////        ids.add(4);
////        myObject.setKey("subs");
////        myObject.addAttribute("IDs", ids);
////        try {
////            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
////            System.out.println("Document created");
////        } catch (ArangoDBException e) {
////            System.err.println("Failed to create document. " + e.getMessage());
////        }
//
////                        String query = "FOR t IN firstSubscription " +
////                            "FOR t2 IN Channels" +
////                            "FILTER t.name == @name RETURN t";
////                    Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
////
////                    ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
////                            BaseDocument.class);
////
////                    for (; cursor.hasNext();) {
////                        System.out.println("Key: " + cursor.next());
////                        // TODO
////                    }
////        ArangoCollection collection = arangoDB.db(dbName).collection(collectionName);
////        for (int i = 12; i < 16; i++) {
////            BaseDocument value = new BaseDocument();
////            value.setKey(String.valueOf(i));
////            value.addAttribute("ChannelName", i);
////            collection.insertDocument(value);
////            System.out.println("DONE");
////        }
//
////        try {
////            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument(""+0,
////                    BaseDocument.class);
////           // System.out.println("Key: " + myDocument.getKey());
////            //System.out.println("Attribute a: " + myDocument.getAttribute("ID"));
////            ArrayList<Integer> ids = new ArrayList<>();
////            ids = (ArrayList)myDocument.getAttribute("ID");
////
////        for(int i=0;i<ids.size();i++){
////
////            System.out.println(ids.get(i));
////            try {
////                BaseDocument myDocument2 = arangoDB.db(dbName).collection("Channels").getDocument(""+ids.get(i),
////                        BaseDocument.class);
////                System.out.println("Key: " + myDocument2.getKey());
////                System.out.println("Attribute Name: " + myDocument2.getAttribute("Name"));
////            } catch (ArangoDBException e) {
////                System.err.println("Failed to execute query. " + e.getMessage());
////            }
////        }
////    } catch (ArangoDBException e) {
////        System.err.println("Failed to get document: myKey; " + e.getMessage());
////    }
////    }
//
////        ArrayList<Integer> ids = new ArrayList<>();
////        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + 1,
////                BaseDocument.class);
//////        ids = (ArrayList) myDocument2.getAttribute("ID");
//////        ids.add(0);
////        ((ArrayList) myDocument2.getAttribute("ID")).add(0);
////        System.out.println(myDocument2.getAttribute("ID"));
//
//
////        ArrayList<Integer> ids = new ArrayList<>();
////        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + 1,
////                BaseDocument.class);
////        ids = (ArrayList) myDocument2.getAttribute("ID");
////        ids.add(0);
////        myDocument2.updateAttribute("ID",ids);
////        arangoDB.db(dbName).collection(collectionName).deleteDocument("" + 1);
////        arangoDB.db(dbName).collection(collectionName).insertDocument(myDocument2);
////        System.out.println(myDocument2.getAttribute("ID"));
////
////        BaseDocument myObject = new BaseDocument();
////        myObject.setKey(10+"");
////        myObject.addAttribute("ID", 0);
////        try {
////            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
////            System.out.println("Document created");
////        } catch (ArangoDBException e) {
////            System.err.println("Failed to create document. " + e.getMessage());
////        }
//
////        if(arangoDB.db(dbName).collection(collectionName).getDocument("" + 1,
////                BaseDocument.class) == null) {
////            System.out.println("DONE");
////            BaseDocument myObject = new BaseDocument();
////            myObject.setKey(1+"");
////            myObject.addAttribute("ID", 0);
////            try {
////                arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
////                System.out.println("Document created");
////            } catch (ArangoDBException e) {
////                System.err.println("Failed to create document. " + e.getMessage());
////            }
////        }
////        else{
////            ArrayList<Integer> ids = new ArrayList<>();
////        BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument("" + 4,
////                BaseDocument.class);
////        ids = (ArrayList) myDocument2.getAttribute("ID");
////        ids.add(0);
////        myDocument2.updateAttribute("ID",ids);
////        System.out.println(myDocument2.getAttribute("ID"));
////        }


    //}
}
