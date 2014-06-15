package com.chechev.phd.datacollectors;

import com.chechev.phd.recommendedstream.datatypes.NewsFeed;
import com.chechev.phd.search.DataSearcher;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Milen
 */
public class NewsFeedCollector implements Runnable {

    private String userId;
    private String accessToken;
    public static ConcurrentHashMap<String, NewsFeed> userMap = new ConcurrentHashMap<String, NewsFeed>(GlobalConstants.MAX_APP_USERS);
    public static ConcurrentHashMap<String, Thread> userThreadMap = new ConcurrentHashMap<String, Thread>(GlobalConstants.MAX_APP_USERS);
    public static HashSet<String> invalidUserSet = new HashSet<String>(GlobalConstants.MAX_APP_USERS);
    public static ConcurrentHashMap<String, HashMap<String, Double>> trustMap = new ConcurrentHashMap<String, HashMap<String, Double>>();

    public NewsFeedCollector(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    @Override
    public void run() { // this thread is added to userThreadMap before to be executed
        NewsFeed newsFeed;
        if (userMap.containsKey(userId)) {
            newsFeed = userMap.get(userId);
        } else {
            newsFeed = new NewsFeed(userId);
        }
        Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "start thread for news feed user: {0} newsfeed: {1} thread: {2}", new Object[]{userId, newsFeed, this});
        Mongo m = null;
        try {
            Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "Open new Mongo");
            m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            DB db = m.getDB("mydb");
            DBCollection coll = db.getCollection("docs");
            BasicDBObject keyObj = new BasicDBObject("id", 1);
            keyObj.append("owner_id", 1);
            coll.createIndex(keyObj);
            DataSearcher dataSearcher = new DataSearcher();

            while (true) {
                try {

                    FacebookClient facebookClient = new DefaultFacebookClient(accessToken);

                    while (true) {
                        long start = System.currentTimeMillis();
                        long since = newsFeed.getLastUpdate() / 1000;//- 24*60000;
                        if (since < 0) {
                            since = 0;
                        }
                        Connection<ExtendedPost> myFeed = facebookClient.fetchConnection(userId + "/home", ExtendedPost.class, Parameter.with("fields", "likes.limit(100),id,from,picture,description,created_time,message,message_tags,story,story_tags,name,object_id,updated_time,type,to,with_tags,comments.limit(50),icon,application,privacy,status_type,link,caption,place,source"), Parameter.with("limit", "120"));//, userMap.get(userId).getLastUpdate() / 1000));

                        List<ExtendedPost> inputList = myFeed.getData();
                        List<ExtendedPost> list = new LinkedList<ExtendedPost>();

                        ObjectMapper mapper = new ObjectMapper();
                        int updatedPosts = 0;
                        for (ExtendedPost p : inputList) {
                            //Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, p.toString());
                            //Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "1 " + (System.currentTimeMillis()-start));
                            String str = mapper.writeValueAsString(p);

                            //there are some postst start with 0_ - they are duplicates! Delete them as workaround
                            if (p.getId().startsWith("0_") || (p.getPicture() == null && p.getMessage() == null && p.getCaption() == null && p.getDescription() == null && p.getName() == null)) {
                                continue;
                            }
                            HashMap map = mapper.readValue(str, HashMap.class);
                            map.put("owner_id", userId);
                            DBObject o = new BasicDBObject(map);
                            BasicDBObject tmp = new BasicDBObject("id", p.getId());
                            tmp.append("owner_id", userId);

                            DBObject oldObject = coll.findAndModify(tmp, o);
                            //Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "updated_time = " + map.get("updatedTime").toString());
                            if (oldObject == null) {
                                coll.insert(o);
                                list.add(p);
                                dataSearcher.addPost(o);
                            } else if (((Long) oldObject.get("updatedTime")).longValue() != ((Long) o.get("updatedTime")).longValue()) {
                                dataSearcher.deletePost(((ObjectId) oldObject.get("_id")).toStringMongod());
                                userMap.get(userId).removePost(p.getId());
                                o.put("_id", oldObject.get("_id"));
                                list.add(p);
                                dataSearcher.addPost(o);
                                updatedPosts++;
                            }
                            //Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "3 " + (System.currentTimeMillis()-start));

                        }
                        dataSearcher.commit();

                        newsFeed.addAllPosts(list);
                        userMap.put(userId, newsFeed);
                        Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.INFO, "{0} updated posts {1} . load time=  {2} ms", new Object[]{userId, updatedPosts, System.currentTimeMillis() - start});
                        try {
                            Thread.sleep(120 * 1000);
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }

                } catch (FacebookOAuthException ex) {
                    Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.SEVERE, "Problem with user id" + userId, ex);
                    try {
                        Thread.sleep(120 * 1000);
                    } catch (InterruptedException ex1) {
                        return;
                    }
                    //userThreadMap.remove(userId); // TODO: add user in removed list and try to add it back later
                    //invalidUserSet.add(userId);
                    break;
                } catch (Exception ex) {
                    Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.SEVERE, userId+" ", ex);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex1) {
                        return;
                    }
                    if (ex instanceof MongoException) {
                        try {
                            Thread.sleep(120 * 1000);
                        } catch (InterruptedException ex1) {
                            return;
                        }
                        //userThreadMap.remove(userId);
                        //invalidUserSet.add(userId);
                        break;
                    }
                }
            }


        } catch (UnknownHostException ex) {
            Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.SEVERE, userId+" ", ex);
        } catch (MongoException ex) {
            Logger.getLogger(NewsFeedCollector.class.getName()).log(Level.SEVERE, userId+" ", ex);
        }
    }
}
