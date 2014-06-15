package com.chechev.phd.datacollectors;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.chechev.phd.recommendedstream.datatypes.AccessLog;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.recommendedstream.datatypes.User;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.Comment;
import com.restfb.types.NamedFacebookType;

/**
 *
 * @author Milen
 */
public class UserDataCollector implements Runnable {

    private String userId;
    private String accessToken;
    public static ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<String, User>(GlobalConstants.MAX_APP_USERS);
    public static ConcurrentHashMap<String, Thread> userThreadMap = new ConcurrentHashMap<String, Thread>(GlobalConstants.MAX_APP_USERS);
    public static HashSet<String> invalidUserSet = new HashSet<String>(GlobalConstants.MAX_APP_USERS);
    public static int turns = 0;

    public UserDataCollector(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        boolean isInitialLoad = true;
        User user;
        if (!userMap.containsKey(userId)) {
            user = new User(userId);
        } else {
            user = userMap.get(userId);
        }

        try {
            Logger.getLogger(UserDataCollector.class.getName()).log(Level.INFO, "Open new Mongo");
            Mongo m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            DB db = m.getDB("mydb");
            DBCollection collUsers = db.getCollection("users");
            DBCollection collUserProfiles = db.getCollection("user_profile");
            DBCollection collUserLikes = db.getCollection("user_likes");
            DBCollection collLikedObjects = db.getCollection("liked_objects");
            DBCollection collUserFriends = db.getCollection("user_friends");
            DBCollection collAccessLog = db.getCollection("user_access");
            DBCollection collFeed = db.getCollection("feed");
            DBCollection collTrust = db.getCollection("user_trust");
            DBCollection collExplicitTrust = db.getCollection("explicit_user_trust");
            collFeed.createIndex(new BasicDBObject("id", 1));

            // Thread sleep is added to be more polite
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken);

            ExecutorService feedThreadPool = Executors.newFixedThreadPool(30);

            while (true) {
                long start = System.currentTimeMillis();


                DBObject o = collUsers.findOne(new BasicDBObject("user_id", userId));
                if (o == null) {
                    o = new BasicDBObject();
                }

                o.put("user_id", userId);
                o.put("access_token", accessToken);
                o.put("update_time_user_profile", System.currentTimeMillis());

                if (collUsers.findAndModify(new BasicDBObject("user_id", userId), o) == null) {
                    collUsers.insert(o);
                }

                //write access log
                o = collAccessLog.findOne(new BasicDBObject("user_id", userId));
                if (o == null) {
                    o = new BasicDBObject();
                }
                o.put("user_id", userId);
                o.put("access_log", AccessLog.getAccessLogs(userId));

                if (collAccessLog.findAndModify(new BasicDBObject("user_id", userId), o) == null) {
                    collAccessLog.insert(o);
                }

                //collect and write user profile
                com.restfb.types.User currentUser = facebookClient.fetchObject(userId, com.restfb.types.User.class);
                o = collUserProfiles.findOne(new BasicDBObject("id", userId));
                ObjectMapper mapper = new ObjectMapper();
                String str = mapper.writeValueAsString(currentUser);
                HashMap map = mapper.readValue(str, HashMap.class);
                if (o == null) {
                    o = new BasicDBObject();
                }
                o.putAll(map);

                if (collUserProfiles.findAndModify(new BasicDBObject("user_id", userId), o) == null) {
                    collUserProfiles.insert(o);
                }

                //collect and store friend list
                Connection<com.restfb.types.User> friendsConnection = facebookClient.fetchConnection(userId + "/friends", com.restfb.types.User.class, Parameter.with("limit", "2000"));
                List<com.restfb.types.User> friendsList = friendsConnection.getData();
                Map<String, String> friendsMap = new LinkedHashMap<String, String>();
                for (com.restfb.types.User friend : friendsList) {
                    friendsMap.put(friend.getId(), friend.getName());
                }
                o = collUserFriends.findOne(new BasicDBObject("user_id", userId));
                if (o == null) {
                    o = new BasicDBObject();
                }

                o.put("user_id", userId);
                o.put("friends", friendsMap.keySet());

                if (collUserFriends.findAndModify(new BasicDBObject("user_id", userId), o) == null) {
                    collUserFriends.insert(o);
                }

                friendsMap.put(userId, currentUser.getName());
                //give a chanse to user news feed to obtain the data before to load up the system
                Thread.sleep(5 * 1000);

                //collect and store friends feed
                LinkedList<Thread> threadList = new LinkedList<Thread>();
                int count = 0;
                CountDownLatch countDown = new CountDownLatch(friendsMap.size());
                ConcurrentLinkedQueue<ExtendedPost> queue = new ConcurrentLinkedQueue<ExtendedPost>();
                for (String friendId : friendsMap.keySet()) {

                    if (!isInitialLoad) {
                        Thread.sleep(1000);
                    }

                    feedThreadPool.execute(new Helper(friendId, accessToken, countDown, queue));

                    count++;
                    if (count % 500 == 0) {
                        if (isInitialLoad) {
                            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "MORE THAN 500 FRIENDS! {0} ", new Object[]{userId});
                            break;
                        }
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0} friends feed limit= {1} time={2}", new Object[]{userId, count, System.currentTimeMillis() - start});
                        long waitTime = 600 * 1000 - (System.currentTimeMillis() - start);
                        if (waitTime > 0) {
                            Thread.sleep(waitTime);
                        }
                    }
                }
                countDown.await(1, TimeUnit.MINUTES);

                HashMap<String, Integer> likedFriendsPosts = new HashMap<String, Integer>();
                HashMap<String, Integer> commentedFriendsPosts = new HashMap<String, Integer>();
                HashMap<String, Integer> sharedFriendsPosts = new HashMap<String, Integer>();
                HashMap<String, Integer> commonActivityUsers = new HashMap<String, Integer>();
                HashMap<String, Integer> postsPerUser = new HashMap<String, Integer>();

                while (!queue.isEmpty()) {
                    ExtendedPost post = queue.poll();
                    o = new BasicDBObject();
                    o.putAll(mapper.readValue(mapper.writeValueAsString(post), HashMap.class));

                    if (collFeed.findAndModify(new BasicDBObject("id", post.getId()), o) == null) {
                        collFeed.insert(o);
                    }
                    accumulateUserActivity(likedFriendsPosts, commentedFriendsPosts, sharedFriendsPosts, commonActivityUsers, postsPerUser, post);
                    // TODO: add to news feed every post which is less then 24 hours old
                }

                //get explicit trust from MongoDB
                DBCursor cursor = collExplicitTrust.find(new BasicDBObject("user_id", userId));
                HashMap<String, Double> explicitTrust = new HashMap<String, Double>();
                while (cursor.hasNext()) {
                    DBObject explicit = cursor.next();
                    if (!explicitTrust.containsKey(explicit.get("friend_id"))) {
                        explicitTrust.put((String) explicit.get("friend_id"), (Double) explicit.get("weightDelta"));
                    } else {
                        explicitTrust.put((String) explicit.get("friend_id"), explicitTrust.get(explicit.get("friend_id")) + (Double) explicit.get("weightDelta"));
                    }
                }
                //calculate trust for friends
                HashMap<String, Double> trust = new HashMap<String, Double>();
                double minTrust = 1;
                LinkedList<String> noInformationFriends = new LinkedList<String>();
                for (String friendId : friendsMap.keySet()) {
                    double trustValue = 0;
                    if (postsPerUser.containsKey(friendId)) {
                        if (likedFriendsPosts.containsKey(friendId)) {
                            trustValue += likedFriendsPosts.get(friendId) / (double) postsPerUser.get(friendId);
                        }
                        if (commentedFriendsPosts.containsKey(friendId)) {
                            trustValue += commentedFriendsPosts.get(friendId) / (double) postsPerUser.get(friendId);
                        }
                    }
                    if (commonActivityUsers.containsKey(userId) && commonActivityUsers.containsKey(friendId)) {
                        trustValue += commonActivityUsers.get(friendId) / (double) commonActivityUsers.get(userId);
                    }
                    if (trustValue == 0) {
                        noInformationFriends.add(friendId);
                        continue;
                    }

                    if (explicitTrust.containsKey(friendId)) {
                        trustValue += explicitTrust.get(friendId);
                    }

                    if (minTrust > trustValue) {
                        minTrust = trustValue;
                    }

                    trust.put(friendId, trustValue);
                }
                double step = minTrust/(noInformationFriends.size()+1);
                for(String friendId : noInformationFriends){
                    minTrust -= step;
                    trust.put(friendId, minTrust+(explicitTrust.containsKey(friendId)?explicitTrust.get(friendId):0));
                }
                NewsFeedCollector.trustMap.put(userId, trust);
                TreeSet<Entry<String, Double>> set = new TreeSet<Entry<String, Double>>(new Comparator<Entry<String, Double>>() {
                    @Override
                    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                        if (o1.getValue() < o2.getValue()) {
                            return 1;
                        } else if (o1.getValue() == o2.getValue()) {
                            return 0;
                        }
                        return -1;
                    }
                });
                set.addAll(trust.entrySet());
                BasicDBObject searchObject = new BasicDBObject();
                searchObject.put("user_id", userId);
                BasicDBObject newObject = new BasicDBObject();
                newObject.put("user_id", userId);

                final JsonArray trustArray = new JsonArray();
                for (Entry<String, Double> entry : set) {
                    final JsonObject entryObject = new JsonObject();
                    entryObject.put("id", entry.getKey());
                    entryObject.put("value", entry.getValue());
                    entryObject.put("name", friendsMap.get(entry.getKey()));
                    trustArray.put(entryObject);
                }
                newObject.put("trust", trustArray.toString());
                if (collTrust.findAndModify(searchObject, newObject) == null) {
                    collTrust.insert(newObject);
                }

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0} stored friends feed count: {1} time= {2}", new Object[]{userId, count, System.currentTimeMillis() - start});

                // collect all likes
                Logger.getLogger(UserDataCollector.class.getName()).log(Level.INFO, "load user {0} likes", userId);
                List<FqlObject> originalListObjectId = facebookClient.executeQuery("SELECT object_id FROM like WHERE user_id=" + userId + " LIMIT 1000", FqlObject.class);
                o = collUserLikes.findOne(new BasicDBObject("user_id", userId));
                if (o == null) {
                    o = new BasicDBObject();
                }
                o.put("user_id", userId);
                o.put("likes", originalListObjectId.toString());
                if (collUserLikes.findAndModify(new BasicDBObject("user_id", userId), o) == null) {
                    collUserLikes.insert(o);
                }
                //wait some time before to get all 1000 liked objects
                Thread.sleep(300 * 1000);
                String objectId = null;
                for (FqlObject fqlObj : originalListObjectId) {
                    try {
                        objectId = fqlObj.object_id;
                        Logger.getLogger(UserDataCollector.class.getName()).log(Level.FINER, "{0} get info for object {1}", new String[]{userId, fqlObj.object_id});
                        ExtendedPost fetchPost = facebookClient.fetchObject(fqlObj.object_id, ExtendedPost.class, Parameter.with("fields", "from,likes.limit(500),comments.limit(500)"));
                        BasicDBObject dbObject = new BasicDBObject(mapper.readValue(mapper.writeValueAsString(fetchPost), HashMap.class));
                        if (collLikedObjects.findAndModify(new BasicDBObject("id", objectId), dbObject) == null) {
                            collLikedObjects.insert(dbObject);
                        }

                    } catch (com.restfb.exception.FacebookGraphException ex) {
                        Logger.getLogger(UserDataCollector.class.getName()).log(Level.WARNING, userId + " liked object wasnt retrieved" + objectId);
                    }
                    Thread.sleep(2000);
                }



                //20 min
                Thread.sleep(20 * 60 * 1000);

                start = System.currentTimeMillis();
                isInitialLoad = false;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UserDataCollector.class.getName()).log(Level.SEVERE, userId + " ", ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(UserDataCollector.class.getName()).log(Level.SEVERE, userId + " ", ex);
        } catch (MongoException ex) {
            Logger.getLogger(UserDataCollector.class.getName()).log(Level.SEVERE, userId + " ", ex);
        } catch (FacebookOAuthException ex) {
            Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.SEVERE, "Problem with data collector for id" + userId, ex);
            //userThreadMap.remove(userId); // TODO: add user in removed list and try to add it back later
            //invalidUserSet.add(userId);
        }

    }

    private void accumulateUserActivity(HashMap<String, Integer> likedFriendsPosts,
            HashMap<String, Integer> commentedFriendsPosts,
            HashMap<String, Integer> sharedFriendsPosts,
            HashMap<String, Integer> commonActivityUsers,
            HashMap<String, Integer> postsPerUser,
            ExtendedPost post) {
        HashSet<String> activeUsers = new HashSet<String>();
        boolean isActionedByUser = false;
        if (!postsPerUser.containsKey(post.getFrom().getId())) {
            postsPerUser.put(post.getFrom().getId(), 0);
        }
        postsPerUser.put(post.getFrom().getId(), postsPerUser.get(post.getFrom().getId()) + 1);
        if (post.getLikes() != null) {
            for (NamedFacebookType likedUser : post.getLikes().getData()) {
                activeUsers.add(likedUser.getId());
                if (likedUser.getId().equals(userId)) {
                    isActionedByUser = true;
                    if (!likedFriendsPosts.containsKey(post.getFrom().getId())) {
                        likedFriendsPosts.put(post.getFrom().getId(), 0);
                    }
                    likedFriendsPosts.put(post.getFrom().getId(), likedFriendsPosts.get(post.getFrom().getId()) + 1);
                }
            }
        }
        if (post.getComments() != null) {
            for (Comment comment : post.getComments().getData()) {
                activeUsers.add(comment.getFrom().getId());
                if (comment.getFrom().getId().equals(userId) & !isActionedByUser) {
                    isActionedByUser = true;
                    if (!commentedFriendsPosts.containsKey(post.getFrom().getId())) {
                        commentedFriendsPosts.put(post.getFrom().getId(), 0);
                    }
                    commentedFriendsPosts.put(post.getFrom().getId(), commentedFriendsPosts.get(post.getFrom().getId()) + 1);
                }
            }
        }
        if (isActionedByUser) {
            for (String id : activeUsers) {
                if (!commonActivityUsers.containsKey(id)) {
                    commonActivityUsers.put(id, 0);
                }
                commonActivityUsers.put(id, commonActivityUsers.get(id) + 1);
            }
        }
    }

    private static class Helper implements Runnable {

        String friendId;
        String accessToken;
        CountDownLatch countDown;
        ConcurrentLinkedQueue<ExtendedPost> queue;

        public Helper(String friendId, String accessToken, CountDownLatch countDown, ConcurrentLinkedQueue queue) {
            this.friendId = friendId;
            this.accessToken = accessToken;
            this.countDown = countDown;
            this.queue = queue;
        }

        @Override
        public void run() {
            boolean success = false;
            int tries = 0;


            while (!success && tries < 5) {
                try {
                    tries++;
                    FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
                    ObjectMapper mapper = new ObjectMapper();


                    Connection<ExtendedPost> feedConnection = facebookClient.fetchConnection(friendId + "/feed", ExtendedPost.class, Parameter.with("fields", "likes.limit(200),id,from,picture,description,created_time,message,message_tags,story,story_tags,name,object_id,updated_time,type,to,with_tags,comments.limit(100),icon,application,privacy,status_type,link,caption,place,source"), Parameter.with("limit", "1000"));
                    List<ExtendedPost> feedList = feedConnection.getData();

                    Logger.getLogger(UserDataCollector.class.getName()).log(Level.FINEST, queue.size() + " collect feed from: " + friendId);
                    for (ExtendedPost post : feedList) {
                        queue.add(post);
                    }
                    if (feedList.size() == 0) {
                        Logger.getLogger(UserDataCollector.class.getName()).log(Level.INFO, "NO FEED FOR:" + friendId);
                    }
                    success = true;
                } catch (Exception ex) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex1) {
                        return;
                    }
                    if (ex.getMessage().contains("600")) {
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException ex1) {
                            return;
                        }
                    }
                    Logger.getLogger(UserDataCollector.class.getName()).log(Level.WARNING, "exception at user feed collection:" + friendId + " retry..." + ex.getMessage());
                }
            }
            countDown.countDown();
        }
    }

    public static class FqlObject {

        @Facebook
        String object_id;

        @Override
        public String toString() {
            return String.format("%s", object_id);
        }
    }
}
