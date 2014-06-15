package com.chechev.phd.utils;

import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.User;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Milen
 */
public class DownloadData implements Runnable {

    private final static String LIKES_ID = "2409997254";
    private final static String COMMENTS_ID = "2305272732";
    private final static String MY_ACCESS_TOKEN = "AAACeB67jZAkEBALdSGbflNZCbqfZAHwF7ZB6qj3t0mQs4SlOvUmFgTIDWe3WVvRQpEGwy9PYYPBCKy68RQZCOT7ZA0l3AnfJAwaKbUnxbj1AZDZD";
    public String[] userIds;

    public static void main(String[] args) {
        FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);
        Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class, Parameter.with("limit", "500"));
        //Connection<Post> myFeed = facebookClient.fetchConnection("me/feed", Post.class);

        System.out.println("Count of my friends: " + myFriends.getData().size());
        //System.out.println("First item in my feed: " + myFeed.getData().get(0));
        int usersNumber = myFriends.getData().size();
        int arraySize = 1;
        String[] userArr;
        if (arraySize < usersNumber) {
            userArr = new String[arraySize];
        } else {
            userArr = new String[usersNumber];
        }
        usersNumber -= userArr.length;
        int count = 0;
        for (List<User> listUsers : myFriends) {
            for (User user : listUsers) {
                userArr[count] = user.getId();
                count++;
                if (count >= userArr.length) {
                    DownloadData dd = new DownloadData();
                    dd.userIds = userArr;
                    new Thread(dd).start();
                    count = 0;
                    if (arraySize < usersNumber) {
                        userArr = new String[arraySize];
                    } else {
                        userArr = new String[usersNumber];
                    }
                    usersNumber -= userArr.length;
                }
            }
        }
    }

    @Override
    public void run() {


        int tries = 0;
        FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);

        //int counter = 0;
        //long start = System.currentTimeMillis();

        for (int counter = 0; counter < userIds.length; counter++) {
            String myId = userIds[counter];
            try {
                //counter++;
                //System.out.printf("count=%d time=%ds\n", counter, (System.currentTimeMillis() - start) / 1000);

                Connection<ExtendedPost> myFeed = facebookClient.fetchConnection(myId + "/feed", ExtendedPost.class, Parameter.with("limit", "1000"));
                Formatter outLike = new Formatter("output\\" + myId + "_likes.txt", "utf-8");
                //Formatter outComment = new Formatter(new File(user.getId()+"_comment.txt"));
                Formatter out = new Formatter("output\\" + myId + "_output.txt", "utf-8");

                // Connections support paging and are iterable
                List<ExtendedPost> myFeedConnectionPage = myFeed.getData();
                for (ExtendedPost post : myFeedConnectionPage) {
                    //System.out.println("Post: " + post);
                    //System.out.println(post.getStory());
                    //System.out.println(post.getStoryTags());
                    if (post.getApplication() != null && post.getApplication().getId().equals(LIKES_ID)) {
                        //outLike.format("http://www.facebook.com/%s/posts/%s\n", post.getId().split("_")[0], post.getId().split("_")[1]);
                        if (post.getLink() != null || post.getObjectId() != null || post.getDescription() != null || post.getMessage() != null) {
                            outLike.format("%s %s\n", post.getLink(), post.getObjectId());
                        }
                    }
                    if (post.getLink() != null || post.getMessage() != null || post.getPicture() != null
                            || post.getCaption() != null) {

                        out.format(" # id= %s \n", post.getId());
                        if (post.getLink() != null) {
                            out.format(" link= %s\n", post.getLink());
                        }
                        if (post.getMessage() != null && !post.getMessage().equals(post.getLink())) {
                            out.format(" message= %s\n", processString(post.getMessage()));
                        }
                        if (post.getPicture() != null) {
                            out.format(" picture= %s\n", post.getPicture());
                        }
                        if (post.getCaption() != null) {
                            out.format(" caption= %s\n", processString(post.getCaption()));
                        }
                        if (post.getType() != null) {
                            out.format(" type= %s\n", post.getType());
                        }
                        if (post.getFrom() != null && !post.getFrom().getId().equals(myId)) {
                            out.format(" from id= %s name= %s\n", post.getFrom().getId(), post.getFrom().getName());
                        }
                        if (post.getTo() != null && post.getTo().size() > 0) {
                            out.format(" to= %s\n", post.getTo());
                        }
                        out.format("\n");
                    }
//                            if (post.getType().equals("status")) {
                    //System.out.println("fetch "+post.getId());
//                                JsonObject status = facebookClient.fetchObject(post.getId(), JsonObject.class);
//                                if (status.has("story")) {
//                                    out.format(" status story= %s\n", processString(status.getString("story")));
//                                }
                    //if(status.getMessage()!=null && !status.getMessage().equals(post.getMessage())){
                    //    out.format(" status message= %s\n", status.getMessage());
                    //}

                    //                           }

                }
                out.flush();
                outLike.flush();
                //outComment.flush();




                out.close();
                outLike.close();
                System.out.println("user=" + myId);
                // outComment.close();
            } catch (Exception ex) {
                System.out.print(myId+" .");
                
                System.out.println(ex.getMessage());
                if (ex.getMessage().contains("Calls to stream have exceeded the rate of 600 calls per 600 seconds.")) {
                    counter--;
                    System.out.println("again user=" + myId);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(DownloadData.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }else if(tries < 5){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(DownloadData.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    counter--;
                    tries++;
                }
            }

        }
    }

    public static String processString(String str) {
        return str.replaceAll("\r\n", " ").replaceAll("\n", " ");
    }
}
