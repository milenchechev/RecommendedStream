package com.chechev.phd.datacollectors;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.chechev.phd.recommendedstream.datatypes.AccessLog;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.recommendedstream.datatypes.NewsFeed;
import com.chechev.phd.recommendedstream.datatypes.User;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;

/**
 *
 * @author Milen
 */
public class CollectorManager {

    public static ConcurrentHashMap<String, String> accessTokenToUserMap = new ConcurrentHashMap<String, String>();

    public static String getUserId(String access_token) {
        if (!accessTokenToUserMap.containsKey(access_token)) {
            FacebookClient facebookClient = new DefaultFacebookClient(access_token);
            com.restfb.types.User user = facebookClient.fetchObject("me", com.restfb.types.User.class);
            accessTokenToUserMap.put(access_token, user.getId());
        }
        return accessTokenToUserMap.get(access_token);
    }

    public synchronized static void startDataCollection() {

        //read data from mongodb
        Mongo m = null;
        try {
            Logger.getLogger(CollectorManager.class.getName()).log(Level.INFO, "Open new Mongo");
            m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            DB db = m.getDB("mydb");
            DBCollection coll = db.getCollection("users");
            DBCollection collAccess = db.getCollection("user_access");
            DBCollection collTrust = db.getCollection("user_trust");

            DBCursor cursor = coll.find();
            ObjectMapper mapper = new ObjectMapper();
            while (cursor.hasNext()) {
                DBObject o = cursor.next();
                String userId = (String) o.get("user_id");
                User user = new User(userId);

                //get trust values
                DBObject trustObject = collTrust.findOne(new BasicDBObject("user_id", userId));
                if (trustObject != null) {
                    JsonArray trustArray = new JsonArray(trustObject.get("trust").toString());
                    HashMap<String,Double> trustMap = new HashMap<String, Double>();
                    for(int i = 0 ; i < trustArray.length(); ++i){
                    	final JsonObject object = trustArray.getJsonObject(i);
                        trustMap.put(object.getString("id"), object.getDouble("value"));
                    }
                    NewsFeedCollector.trustMap.put(userId, trustMap);
                }

                //get likes
                /*DBObject likesObject = collLikes.findOne(new BasicDBObject("user_id", userId));
                if (likesObject != null) {
                    List<String> likes = (List<String>) likesObject.get("likes");
                    for (String like : likes) {
                        user.addLike(like);
                    }
                }*/

                //get access log
                DBObject accessLogObject = collAccess.findOne(new BasicDBObject("user_id", userId));
                if (accessLogObject != null) {
                    List<String> accessLog = (List<String>) accessLogObject.get("access_log");
                    AccessLog.setAccessLogFromDB(userId, accessLog);
                }

                //update time
                //long lastUpdate = ((Number) o.get("update_time")).longValue();
                String accessToken = (String) o.get("access_token");
                long lastUpdateUserProfile = ((Number) o.get("update_time_user_profile")).longValue();

                //load posts for user
                DBCollection collDocs = db.getCollection("docs"); 
                BasicDBObject query = new BasicDBObject();
                query.put("updatedTime", new BasicDBObject("$gt", System.currentTimeMillis()-36*60*60*1000)); // here we can reduce the number of the loaded posts by date
                query.put("owner_id", userId);
                DBCursor cursorDocs = collDocs.find(query).sort(new BasicDBObject("updatedTime", -1));

                List<ExtendedPost> listPosts = new LinkedList<ExtendedPost>();
                while (cursorDocs.hasNext()) {
                    DBObject p = cursorDocs.next();
                    //System.out.println(o); 
                    p.removeField("_id"); 
                    p.removeField("$oid");
                    p.removeField("owner_id");
                    p.put("updatedTime", ((Long) p.get("updatedTime")) / 1000);
                    p.put("createdTime", ((Long) p.get("createdTime")) / 1000);
                    ExtendedPost post = mapper.readValue(JSON.serialize(p), ExtendedPost.class);
                    listPosts.add(post);
                }
                Logger.getLogger(CollectorManager.class.getName()).log(Level.INFO, "created threads for user: {0}", userId);
                NewsFeed feed = new NewsFeed(userId);
                feed.addAllPosts(listPosts);
                NewsFeedCollector.userMap.put(userId, feed);
                NewsFeedCollector.userThreadMap.put(userId, new Thread(new NewsFeedCollector(userId, accessToken)));
                NewsFeedCollector.userThreadMap.get(userId).start();


                UserDataCollector.userMap.put(userId, user);
                UserDataCollector.userThreadMap.put(userId, new Thread(new UserDataCollector(userId, accessToken)));
                UserDataCollector.userThreadMap.get(userId).start();
                
            }

        } catch (IOException ex) {
            Logger.getLogger(CollectorManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(CollectorManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static synchronized void addUserToCollect(String accessToken) {

        String userId = CollectorManager.getUserId(accessToken);
        Logger.getLogger(CollectorManager.class.getName()).log(Level.INFO, "Add user to collect{0}", userId);
        if (!NewsFeedCollector.userThreadMap.containsKey(userId)) {
            NewsFeed feed = new NewsFeed(userId);
            NewsFeedCollector.userMap.put(userId, feed);
            NewsFeedCollector.userThreadMap.put(userId, new Thread(new NewsFeedCollector(userId, accessToken)));
            NewsFeedCollector.userThreadMap.get(userId).start();
        } else {
            // For now do nothing
            // probably is good idea to update the access token
        }
        if (!UserDataCollector.userThreadMap.containsKey(userId)) {
            User user = new User(userId);
            UserDataCollector.userMap.put(userId, user);
            UserDataCollector.userThreadMap.put(userId, new Thread(new UserDataCollector(userId, accessToken)));
            UserDataCollector.userThreadMap.get(userId).start();
        } else {
            // For now do nothing
            // probably is good idea to update the access token
        }
    }

    public static void stopDataCollection() {
        for (String userId : NewsFeedCollector.userThreadMap.keySet()) {
            NewsFeedCollector.userThreadMap.get(userId).interrupt();
        }
        for (String userId : UserDataCollector.userThreadMap.keySet()) {
            UserDataCollector.userThreadMap.get(userId).interrupt();
        }
    }
}
