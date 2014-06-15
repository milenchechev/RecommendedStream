package com.chechev.phd.recommendedstream.datatypes;

/**
 *
 * @author Milen
 */
public class Like {
    String objectId;
    long timestamp; // it's possible estimated
    String owner;
    String friendId;

    public Like(String objectId, long timestamp, String owner, String friendId) {
        this.objectId = objectId;
        this.timestamp = timestamp;
        this.owner = owner;
        this.friendId = friendId;
    }
    
    
}
