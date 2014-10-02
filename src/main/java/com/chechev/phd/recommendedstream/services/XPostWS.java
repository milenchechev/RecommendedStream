package com.chechev.phd.recommendedstream.services;

import static com.chechev.phd.recommendedstream.services.UserEvent.COMMENT;
import static com.chechev.phd.recommendedstream.services.UserEvent.LIKE;
import static com.chechev.phd.recommendedstream.services.UserEvent.MARK_INTERESTING;
import static com.chechev.phd.recommendedstream.services.UserEvent.MARK_NOT_INTERESTING;
import static com.chechev.phd.recommendedstream.services.UserEvent.SHARE;
import static com.chechev.phd.recommendedstream.services.UserEvent.UNLIKE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;

import com.chechev.phd.datacollectors.CollectorManager;
import com.chechev.phd.datacollectors.NewsFeedCollector;
import com.chechev.phd.recommendedstream.datatypes.AccessLog;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.search.DataSearcher;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;

/**
 * REST Web Service
 *
 * @author Milen
 */
@Path("data/posts")
public class XPostWS {

    private static final String TAG = XPostWS.class.getSimpleName();
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of XPostWS
     */
    public XPostWS() {
    }

    @GET
    @Path("search")
    @Produces("application/json")
    public static List<ExtendedPost> searchPosts(@QueryParam("access_token") String access_token, @QueryParam("query") String query, @DefaultValue("0") @QueryParam("since") long since, @DefaultValue("100") @QueryParam("count") int count) {
        List<ExtendedPost> resultList = new LinkedList<ExtendedPost>();
        new DataSearcher().getPosts(query, CollectorManager.getUserId(access_token), count);
        return resultList;
    }

    @GET
    @Path("statistics")
    @Produces("application/json")
    public static List<String> statistics() {
        List<String> list = new LinkedList<String>();
        Mongo m = null;
        try {
            Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "Open new Mongo");
            m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            DB db = m.getDB("mydb");
            DBCollection collUsers = db.getCollection("users");
            DBCollection collUserProfiles = db.getCollection("user_profile");
            DBCollection collUserLikes = db.getCollection("user_likes");
            DBCollection collUserFriends = db.getCollection("user_friends");
            DBCollection collAccessLog = db.getCollection("user_access");
            DBCollection collFeed = db.getCollection("feed");
            DBCollection collDocs = db.getCollection("docs");
            DBCursor cursor = collUsers.find();
            list.add("USERS:");
            while (cursor.hasNext()) {
                String userId = cursor.next().get("user_id").toString();
                list.add(userId + " docs: " + collDocs.find(new BasicDBObject("owner_id", userId)).size());
            }
            list.add("");
            list.add("ACCESS LOG:");
            cursor = collAccessLog.find();
            while (cursor.hasNext()) {
                String json = cursor.next().toString();
                String user = json.substring(json.indexOf("user_id") + 12, json.indexOf("\"", json.indexOf("user_id") + 12));
                String accessLog = json.substring(json.indexOf("access_log") + 16, json.indexOf("]", json.indexOf("access_log") + 16));
                list.add("user: " + user + " access: " + accessLog);
            }

            /* list.add("");
             list.add("USER PROFILE:");
             cursor = collUserProfiles.find();
             while(cursor.hasNext()){
             String json = cursor.next().toString();
             list.add(json);
             }

             list.add("");
             list.add("USER LIKES:");
             cursor = collUserLikes.find();
             while(cursor.hasNext()){
             String json = cursor.next().toString();
             list.add(json);
             }

             list.add("");
             list.add("USER FRIENDS:");
             cursor = collUserFriends.find();
             while(cursor.hasNext()){
             String json = cursor.next().toString();
             list.add(json);
             } */

            list.add("");
            list.add("FEED:");
            cursor = collFeed.find();
            list.add(cursor.size() + "");
            /*int count = 0;
             while(cursor.hasNext()){
             cursor.next();
             count++;
             }
             list.add(count+" feeds");*/

            list.add("");
            list.add("DOCS:");
            cursor = collDocs.find();
            list.add(cursor.size() + "");
            /*while(cursor.hasNext()){
             String json = cursor.next().toString();
             list.add(json);
             }*/

            list.add("");
            list.add("END!");
        } catch (UnknownHostException ex) {
            Logger.getLogger(XPostWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(XPostWS.class.getName()).log(Level.SEVERE, null, ex);
        }


        return list;
    }

    /**
     * Retrieves representation of an instance of com.chechev.phd.recommendedstream.XPostWS
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Path("get")
    @Produces("application/json")
    public static List<ExtendedPost> getPosts(@QueryParam("access_token") String access_token, @QueryParam("query") String query, @DefaultValue("0") @QueryParam("since") long since, @DefaultValue("false") @QueryParam("initial_load") boolean isInitialLoad) {
        String userId = CollectorManager.getUserId(access_token);
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "{0} get posts with access_token={1}", new String[]{userId, access_token});

        if (!NewsFeedCollector.userThreadMap.containsKey(userId)) {
            FacebookClient client = new DefaultFacebookClient(access_token);
            AccessToken extendedAccessToken = client.obtainExtendedAccessToken(GlobalConstants.APP_ID, GlobalConstants.APP_SECRET, access_token);
            CollectorManager.addUserToCollect(extendedAccessToken.getAccessToken());
        }
        String user = CollectorManager.getUserId(access_token);
        AccessLog.addAccessEntry(user, System.currentTimeMillis());

        if (query.trim().equals("")) {
            int counter = 0;
            while (counter < 500 && !(NewsFeedCollector.userMap.containsKey(user) && !NewsFeedCollector.userMap.get(user).getPostToVisualize().isEmpty())) {
                counter++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(XPostWS.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
            if (!NewsFeedCollector.userMap.containsKey(user)) {
                return new LinkedList<ExtendedPost>();
            }

            List<ExtendedPost> list = NewsFeedCollector.userMap.get(user).getPostToVisualize();
            Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "{0} all posts={1}", new String[]{userId, list.size() + ""});

            List<ExtendedPost> resultList = list;
            if (resultList.size() > 100) {
                resultList = resultList.subList(0, 100);
            }
            if (isInitialLoad && resultList.size() > 0) {
                writePostOrderAtMongoDB(resultList, userId);
            }

            Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "{0} visualized post number = {1}", new Object[]{user, resultList.size()});
            return resultList;
        } else {

            List<ExtendedPost> resultList = new DataSearcher().getPosts(query, userId, 100);
            if (isInitialLoad && resultList.size() > 0) {
                Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "load posts to ui");
                Mongo m = getActionMongo();
                DB db = m.getDB("mydb");
                DBCollection collLog = db.getCollection("user_log");
                BasicDBObject tmpObj = new BasicDBObject();
                tmpObj.append("action", "search");
                tmpObj.append("query", query);
                tmpObj.append("user", userId);
                tmpObj.append("time", System.currentTimeMillis() - 1);
                collLog.insert(tmpObj);
                writePostOrderAtMongoDB(resultList, userId);
            }
            return resultList;
        }
    }

    private static void writePostOrderAtMongoDB(List<ExtendedPost> resultList, String userId) {
        Mongo m = getActionMongo();
        DB db = m.getDB("mydb");
        DBCollection collLog = db.getCollection("user_log");
        BasicDBObject tmpObj = new BasicDBObject();
        tmpObj.append("action", UserEvent.LOAD_POSTS.name().toLowerCase());
        StringBuilder builder = new StringBuilder();
        for (ExtendedPost post : resultList) {
            builder.append(";").append(post.getId());
        }
        tmpObj.append("post_order", builder.substring(1));
        tmpObj.append("user", userId);
        tmpObj.append("time", System.currentTimeMillis());
        collLog.insert(tmpObj);
    }

    @GET
    @Path("update")
    @Produces("application/json")
    public String verifyUpdateData(String line) {
        MultivaluedMap<String, String> paramsMap = this.context.getQueryParameters();
        if (!paramsMap.containsKey("hub.verify_token") || !paramsMap.get("hub.verify_token").get(0).equals("parola_riba_mech")) {
            System.out.println("problem with real time verification " + paramsMap.get("hub.verify_token"));
            return null;
        } else {
            System.out.println("real-time verification passed");
        }
        return paramsMap.get("hub.challenge").get(0);
    }

    @POST
    @Path("update")
    @Consumes("application/json")
    public Response updateData(String text) {

        System.out.println(text);
        System.out.println("updateData");

        return Response.status(201).entity("success").build();
    }
    /*@POST
     @Path("pesho")
     @Consumes("application/json")
     public Response initializeUser(String accessToken) {
     Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "## {0}", accessToken);
     FacebookClient client = new DefaultFacebookClient(accessToken);
     AccessToken extendedAccessToken = client.obtainExtendedAccessToken(GlobalConstants.APP_ID, GlobalConstants.APP_SECRET, accessToken);
     CollectorManager.getUserId(extendedAccessToken.getAccessToken());
     return Response.status(201).build();
     }*/

    @GET
    @Path("like")
    @Produces("application/json")
    public static String likePost(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(LIKE, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("unlike")
    @Produces("application/json")
    public static String unlikePost(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(UNLIKE, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("comment")
    @Produces("application/json")
    public static String commentPost(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(COMMENT, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("share")
    @Produces("application/json")
    public static String sharePost(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(SHARE, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("unmark")
    @Produces(MediaType.APPLICATION_JSON)
    public static String unmarkPost(@QueryParam("user_id") String userId, @QueryParam("post_id") String postId) {
        final Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection userLogs = db.getCollection("user_log");

        final DBObject searchObject = new BasicDBObject("post_id", postId);
        searchObject.put("action", "mark_interesting");
        searchObject.put("user", userId);
        userLogs.remove(searchObject);

        searchObject.put("action", "mark_not_interesting");
        userLogs.remove(searchObject);

        return "";
    }
    
    @GET
    @Path("viewed")
    @Produces(MediaType.APPLICATION_JSON)
    public static String markViewed(@QueryParam("access_token") String accessToken, @QueryParam("ids") String ids) {
    	final Mongo mongo = getActionMongo();
    	final DB db = mongo.getDB("mydb");
    	final JsonArray array = new JsonArray(ids);
    	for(int i = 0; i < array.length(); ++i){
    		addLogEntry(accessToken, UserEvent.MARK_VIEWED.name().toLowerCase(), array.getString(i), db.getCollection("user_log"));
    	}
    	return "";
    }

    @GET
    @Path("markInteresting")
    @Produces("application/json")
    public static String markInteresting(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(MARK_INTERESTING, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("isnew")
    @Produces("application/json")
    public static String isNew(@QueryParam("access_token") String accessToken) {
        String userId = CollectorManager.getUserId(accessToken);
        if (NewsFeedCollector.userMap.containsKey(userId) && !NewsFeedCollector.userMap.get(userId).getPostToVisualize().isEmpty()) {
            return "false";
        } else {
            return "true";
        }
    }

    @GET
    @Path("markNotInteresting")
    @Produces("application/json")
    public static String markNotInteresting(@QueryParam("access_token") String accessToken, @QueryParam("post_id") String postId, @QueryParam("collection") String collection) {
        logUserEvent(MARK_NOT_INTERESTING, accessToken, postId, collection);
        return "";
    }

    @GET
    @Path("friends/update/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String updateFriendsOrder(@PathParam("id") String userId, @QueryParam("friends") String friends, @QueryParam("changedFriend") String changedFriend) {
        final JsonArray userFriends = new JsonArray(friends);

        final Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection userTrust = db.getCollection("user_trust");

        final BasicDBObject searchObject = new BasicDBObject("user_id", userId);
        final DBObject trust = userTrust.findOne(searchObject);

        if (trust == null) {
            return "";
        }

        String nextFriendId = null;
        String prevFriendId = null;
        int changedFriendIndex = -1;

        for (int i = 0; i < userFriends.length(); ++i) {
            if (userFriends.getString(i).equals(changedFriend)) {
                if (i + 1 < userFriends.length()) {
                    nextFriendId = userFriends.getString(i + 1);
                }
                if (i - 1 >= 0) {
                    prevFriendId = userFriends.getString(i - 1);
                }
                changedFriendIndex = i;
                break;
            }
        }

        double nextFriendTrustRate = 0.0;
        double prevFriendTrustRate = 0.0;
        double changedFriendTrustRate = 0.0;
        double changedFriendOldTrustRate = 0.0;

        final JsonArray trustedFriends = new JsonArray(trust.get("trust").toString());

        for (int i = 0; i < trustedFriends.length(); ++i) {
            final JsonObject object = trustedFriends.getJsonObject(i);
            final String friendId = object.getString("id");
            final double trustRate = object.getDouble("value");
            if (friendId.equals(nextFriendId)) {
                nextFriendTrustRate = trustRate;
            } else if (friendId.equals(prevFriendId)) {
                prevFriendTrustRate = trustRate;
            } else if (friendId.equals(changedFriend)) {
                changedFriendOldTrustRate = trustRate;
            }
        }

        if (nextFriendId != null && prevFriendId != null) {
            changedFriendTrustRate = (prevFriendTrustRate + nextFriendTrustRate) / 2;
        } else if (nextFriendId != null) {
            changedFriendTrustRate = nextFriendTrustRate + 0.0001;
        } else {
            changedFriendTrustRate = prevFriendTrustRate - 0.0001;
        }

        for (int i = 0; i < trustedFriends.length(); ++i) {
            final JsonObject object = trustedFriends.getJsonObject(i);
            final String friendId = object.getString("id");
            if (friendId.equals(changedFriend)) {
                object.put("value", changedFriendTrustRate);
                break;
            }
        }


        sort(trustedFriends);

        final DBObject updated = new BasicDBObject(trust.toMap());
        updated.put("trust", trustedFriends.toString());
        userTrust.update(trust, updated);
        NewsFeedCollector.trustMap.get(userId).put(changedFriend, changedFriendTrustRate);
        final DBCollection explicitTrust = db.getCollection("explicit_user_trust");
        final DBObject explicitTrustObject = new BasicDBObject();
        explicitTrustObject.put("user_id", userId);
        explicitTrustObject.put("friend_id", changedFriend);
        explicitTrustObject.put("end_position", changedFriendIndex);
        explicitTrustObject.put("weightDelta", changedFriendTrustRate - changedFriendOldTrustRate);
        explicitTrust.insert(explicitTrustObject);

        return "";
    }

    private static void sort(final JsonArray array) {
        for (int i = 0; i < array.length(); ++i) {
            final JsonObject current = array.getJsonObject(i);
            final int minIndex = findMaxIndex(array, i);
            final JsonObject minObject = array.getJsonObject(minIndex);
            array.put(i, minObject);
            array.put(minIndex, current);
        }
    }

    private static int findMaxIndex(final JsonArray array, int start) {
        double maxValue = Double.MIN_VALUE;
        int result = start;

        for (int i = start; i < array.length(); ++i) {
            final JsonObject current = array.getJsonObject(i);
            if (current.getDouble("value") > maxValue) {
                maxValue = current.getDouble("value");
                result = i;
            }
        }

        return result;
    }

    @GET
    @Path("friends/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getFriends(@PathParam("id") String userId) {
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "retrieving friends for " + userId);

        final JsonObject result = new JsonObject();

        Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection userTrust = db.getCollection("user_trust");

        final BasicDBObject searchObject = new BasicDBObject("user_id", userId);
        final DBObject friends = userTrust.findOne(searchObject);
        if (friends == null) {
            result.put("friends", new JsonArray().toString());
        } else {
            JsonArray friendsInfo = new JsonArray();
            try {
                friendsInfo = new JsonArray(new String(friends.get("trust").toString().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(TAG).severe(e.getMessage());
            }
            sort(friendsInfo);
            result.put("friends", friendsInfo);
        }

        try {
            return new String(result.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(TAG).severe(e.getMessage());
            return result.toString();
        }
    }

    @GET
    @Path("friends_ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getFriendsIds(@PathParam("id") String userId) {
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "retrieving friends for " + userId);

        final JsonObject result = new JsonObject();

        Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection userTrust = db.getCollection("user_friends");

        final BasicDBObject searchObject = new BasicDBObject("user_id", userId);
        final DBObject friends = userTrust.findOne(searchObject);
        if (friends == null) {
            result.put("friends", new JsonArray().toString());
        } else {
            JsonArray friendsInfo = new JsonArray();
            try {
                friendsInfo = new JsonArray(new String(friends.get("friends").toString().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(TAG).severe(e.getMessage());
            }
            result.put("friends", friendsInfo);
        }

        try {
            return new String(result.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(TAG).severe(e.getMessage());
            return result.toString();
        }
    }

    private static void sortMovies(final JsonArray array, String userId) {
        Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection userTrustCol = db.getCollection("user_trust");
        DBObject userTrust = userTrustCol.findOne(new BasicDBObject("user_id", userId));
        JsonArray friendsTrust = new JsonArray(userTrust.get("trust").toString());
        Map<String, Double> friendTrustMap = new HashMap<String, Double>();
        for (int i = 0; i < friendsTrust.length(); ++i) {
            final JsonObject current = friendsTrust.getJsonObject(i);
            friendTrustMap.put(current.getString("id"), current.getDouble("value"));
        }

        for (int i = 0; i < array.length(); ++i) {
            final JsonObject current = array.getJsonObject(i);
            final JsonObject values = current.getJsonObject("value");
            JsonArray friendIds = values.optJsonArray("friendId");
            if (friendIds == null) {
                friendIds = new JsonArray();
                final String fId = values.optString("friendId");
                if (fId != null) {
                    friendIds.put(fId);
                }
            }
            double friendRating = 0;
            for (int j = 0; j < friendIds.length(); ++j) {
                String fIds = friendIds.getString(j);
                String[] fId = fIds.split(",");
                for (String id : fId) {
                    Double trust = friendTrustMap.get(id);
                    if (trust != null) {
                        friendRating += friendTrustMap.get(id);
                    }
                }
            }
            values.append("friendRating", friendRating * 3);
            final int minIndex = findMaxMovieIndex(array, i);
            final JsonObject minObject = array.getJsonObject(minIndex);
            array.put(i, minObject);
            array.put(minIndex, current);
        }
    }

    private static int findMaxMovieIndex(final JsonArray array, int start) {
        double maxValue = 0;
        int result = start;

        for (int i = start; i < array.length(); ++i) {
            final JsonObject current = array.getJsonObject(i);
            final JsonObject values = current.getJsonObject("value");
            double imdbRating = 0;
            double friendRating = 0;
            try {
                friendRating = Double.parseDouble(values.getString("friendRating"));
                imdbRating = Double.parseDouble(values.getString("rating"));
            } catch (Exception e) {
            }
            final double rating = imdbRating + friendRating;
            if (rating > maxValue) {
                maxValue = rating;
                result = i;
            }
        }

        return result;
    }

    @GET
    @Path("movies/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getMovies(@PathParam("id") String userId) {
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "retrieving movies for " + userId);

        final JsonObject result = new JsonObject();

        Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");
        final DBCollection moviesCol = db.getCollection("movies");

        String map = "function () {" + "if (this.userId != this.friendId)" + "{emit(this.id, this);}" + "};";
        String reduce = "function (key, values) {" +
                            "var res = {};" +
                            "res.name = values[0].name;" +
                            "res.id = key;" +
                            "res.userId = values[0].userId;" +
                            "res.poster = values[0].poster;" +
                            "res.rating = values[0].rating;" +
                            "res.imdbId = values[0].imdbId;" +
                            "res.friendId = [];" +
                            "for (var i = 0; i<values.length; ++i) {" +
                                "res.friendId.push(String(values[i].friendId));" +
                            "}" +
                            "return res;}";

        final BasicDBObject searchObject = new BasicDBObject("userId", userId);
        MapReduceCommand mapReduceComand = new MapReduceCommand(moviesCol, map, reduce, null,
                MapReduceCommand.OutputType.INLINE, searchObject);
        final MapReduceOutput moviesReduced = moviesCol.mapReduce(mapReduceComand);
        JsonArray moviesInfo = new JsonArray();
        for (DBObject movie : moviesReduced.results()) {
            moviesInfo.put(new JsonObject(movie.toString()));
        }
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO,
                "retrieved " + moviesInfo.length() + " movies for user " + userId);
        if (moviesInfo.length() == 0) {
            result.put("movies", new JsonArray().toString());
        } else {
            sortMovies(moviesInfo, userId);
            result.put("movies", moviesInfo);
        }

        try {
            return new String(result.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(TAG).severe(e.getMessage());
            return result.toString();
        }
    }

    @GET
    @Path("/interesting/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getInterestingPosts(@PathParam("id") String userId) {
        return getMarkedPosts(userId, UserEvent.MARK_INTERESTING);
    }

    @GET
    @Path("/uninteresting/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public static String getNotInterestingPosts(@PathParam("id") String userId) {
        return getMarkedPosts(userId, UserEvent.MARK_NOT_INTERESTING);
    }

    private static String getMarkedPosts(final String userId, final UserEvent event) {
        final Mongo mongo = getActionMongo();
        final DB db = mongo.getDB("mydb");

        final DBCollection userLogs = db.getCollection("user_log");

        final DBCollection feed = db.getCollection("docs");
        final DBObject feedSearchObject = new BasicDBObject();

        final DBObject searchObject = new BasicDBObject();
        searchObject.put("user", userId);
        searchObject.put("action", event.name().toLowerCase());


        final JsonArray result = new JsonArray();

        final DBCursor markedPostsCursor = userLogs.find(searchObject);
        while (markedPostsCursor.hasNext()) {
            final DBObject next = markedPostsCursor.next();

            feedSearchObject.put("id", next.get("post_id"));
            final DBObject feedObject = feed.findOne(feedSearchObject);

            if (feedObject != null) {
                try {
                    result.put(new String(feedObject.toString().getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    Logger.getLogger(TAG).severe(e.getMessage());
                }
            }
        }

        try {
            return new String(result.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(TAG).severe(e.getMessage());
            return result.toString();
        }
    }

    private static void logUserEvent(final UserEvent event, final String accessToken, final String postId, final String collection) {
        Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, event.name().toLowerCase() + " " + postId);
        Mongo m = getActionMongo();
        DB db = m.getDB("mydb");
        DBCollection coll = db.getCollection(collection);

        DBCollection collLog = db.getCollection("user_log");
        addLogEntry(accessToken, event.name().toLowerCase(), postId, collLog);
        updatePost(accessToken, postId, coll);
    }

    private static void updatePost(String accessToken, String postId, DBCollection coll) {
        try {
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
            ExtendedPost post = facebookClient.fetchObject(postId, ExtendedPost.class, Parameter.with("fields", "likes.limit(100),id,from,picture,description,created_time,message,message_tags,story,story_tags,name,object_id,updated_time,type,to,with_tags,comments.limit(50),icon,application,privacy,status_type,link,caption,place,source"));
            Logger.getLogger(XPostWS.class.getName()).log(Level.INFO, "new post fetched " + post + " " + post.getComments());
            BasicDBObject obj = new BasicDBObject("id", postId);
            obj.append("owner_id", CollectorManager.getUserId(accessToken));
            BasicDBObject tmpObj = new BasicDBObject();
            ObjectMapper mapper = new ObjectMapper();
            tmpObj.putAll(mapper.readValue(mapper.writeValueAsString(post), HashMap.class));
            tmpObj.put("owner_id", CollectorManager.getUserId(accessToken));
            DBObject oldObject = coll.findAndModify(obj, tmpObj);

            //update solr
            DataSearcher searcher = new DataSearcher();
            searcher.deletePost(((ObjectId) oldObject.get("_id")).toStringMongod());
            NewsFeedCollector.userMap.get(CollectorManager.getUserId(accessToken)).removePost(post.getId());
            tmpObj.put("_id", oldObject.get("_id"));

            searcher.addPost(tmpObj);

            List<ExtendedPost> list = new LinkedList<ExtendedPost>();
            list.add(post);
            NewsFeedCollector.userMap.get(CollectorManager.getUserId(accessToken)).addAllPosts(list);
        } catch (IOException ex) {
            Logger.getLogger(XPostWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addLogEntry(String accessToken, String action, String postId, DBCollection collLog) {
        BasicDBObject tmpObj = new BasicDBObject();
        tmpObj.append("action", action);
        tmpObj.append("post_id", postId);
        tmpObj.append("user", CollectorManager.getUserId(accessToken));
        tmpObj.append("time", System.currentTimeMillis());
        collLog.insert(tmpObj);
    }
    private static Mongo actionMongo;

    private static Mongo getActionMongo() {
        if (actionMongo == null) {
            try {
                actionMongo = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            } catch (UnknownHostException e) {
                Logger.getLogger(TAG).severe(e.getMessage());
            } catch (MongoException e) {
                Logger.getLogger(TAG).severe(e.getMessage());
            }
        }
        return actionMongo;
    }
}
