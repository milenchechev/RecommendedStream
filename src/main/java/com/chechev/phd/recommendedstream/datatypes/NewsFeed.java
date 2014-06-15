package com.chechev.phd.recommendedstream.datatypes;

import com.chechev.phd.datacollectors.NewsFeedCollector;
import com.restfb.types.Comment;
import com.restfb.types.NamedFacebookType;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Milen
 */
public class NewsFeed {

    private List<ExtendedPost> postsToVisualize;
    private List<ExtendedPost> posts;
    private long lastUpdate;
    private String owner;
    private boolean isPostListRanked = false;

    public NewsFeed(String owner) {
        this.owner = owner;
        posts = new LinkedList<ExtendedPost>();
        postsToVisualize = new LinkedList<ExtendedPost>();
        lastUpdate = 0;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void addAllPosts(List<ExtendedPost> postList) {
        this.posts.addAll(postList);
        for (ExtendedPost p : postList) {
            if (p.getUpdatedTime().getTime() > lastUpdate) {
                lastUpdate = p.getUpdatedTime().getTime();
            }
        }

    }

    public List<ExtendedPost> getPostToVisualize() {
        if (NewsFeedCollector.trustMap.containsKey(owner)) {
            final HashMap<String, Double> trustMap = NewsFeedCollector.trustMap.get(owner);
            postsToVisualize = new LinkedList<ExtendedPost>(posts);

            Collections.sort(postsToVisualize, new Comparator<ExtendedPost>() {
                HashMap<String, Double> postTrust = new HashMap<String, Double>();
                long tnow = System.currentTimeMillis() / (1000 * 60 * 60);
                public int compare(ExtendedPost o1, ExtendedPost o2) {

                    if (calculateTrust(o1) < calculateTrust(o2)) {
                        return 1;
                    } else if (calculateTrust(o1) == calculateTrust(o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }

                private double calculateTrust(ExtendedPost post) {
                    if (postTrust.containsKey(post.getId())) {
                        return postTrust.get(post.getId());
                    } else {

                        double trust = 0;
                        if (trustMap.containsKey(post.getFrom().getId())) {
                            trust += trustMap.get(post.getFrom().getId());
                        }
                        if (post.getLikes() != null) {
                            for (NamedFacebookType user : post.getLikes().getData()) {
                                if (trustMap.containsKey(user.getId())) {
                                    trust += trustMap.get(user.getId());
                                }
                            }
                        }
                        if (post.getComments() != null) {
                            for (Comment comment : post.getComments().getData()) {
                                if (trustMap.containsKey(comment.getFrom().getId())) {
                                    trust += trustMap.get(comment.getFrom().getId());
                                }
                            }
                        }

                        
                        long tpost = post.getCreatedTime().getTime() / (1000 * 60 * 60);
                        long tupdated = post.getUpdatedTime().getTime() / (1000 * 60 * 60);
                        trust += 0.000001;
                        double timeFactor = 1.0/(tnow-tpost+1) + 1.0/Math.pow(tnow-tupdated+1,2);
                        //trust *= (2 * tnow - tpost - tupdated + 2) / (double) (2*(tnow - tpost + 1) * (tnow - tupdated + 1));
                        trust *= timeFactor/2;
                        postTrust.put(post.getId(), trust);
                        return trust ;
                    }
                }
            });
        }
        return postsToVisualize;
    }

    public void removePost(String id) {
        for (ExtendedPost p : posts) {
            if (p.getId().equals(id)) {
                posts.remove(p);
                break;
            }
        }
    }
}
