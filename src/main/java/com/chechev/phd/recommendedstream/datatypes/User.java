package com.chechev.phd.recommendedstream.datatypes;

import com.chechev.phd.utils.GlobalConstants;
import com.restfb.types.Post;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Milen
 */
public class User {
    private String id;
    String name;
    Set<String> friendsSet;
    private Set<String> likes;
    //List<Comment> comments;
    //List<Share> shares;
    //List<Post> userPosts; // user statuses, photos, etc
    //public static ConcurrentHashMap<String,ExtendedPost> objectsMap = new ConcurrentHashMap<String,ExtendedPost>(GlobalConstants.MAX_OBJECTS);
    //public static ConcurrentHashMap<String,ExtendedPost> newObjectsMap = new ConcurrentHashMap<String,ExtendedPost>(GlobalConstants.MAX_OBJECTS/2);
    public User(String userId) {
        id = userId;
        likes = new TreeSet<String>();
    }
    
    
    public void addLike(String objectId){
            likes.add(objectId);
        
    }
    public boolean isLiked(String objectId){
        return likes.contains(objectId);
    }
    public Set<String> likedObjectSet(){
        return new TreeSet<String>(likes);
    } 
    public boolean isFriendWith(String userId){
        return friendsSet.contains(userId);
    }
    public Iterator<String> friendIterator(){
        return friendsSet.iterator();
    }
    public Iterator<String> likesIterator(){
        return likes.iterator();
    }
}
