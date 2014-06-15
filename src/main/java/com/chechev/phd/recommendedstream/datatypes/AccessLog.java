/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chechev.phd.recommendedstream.datatypes;

import com.chechev.phd.utils.GlobalConstants;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Milen
 */
public class AccessLog implements Serializable{
    private LinkedList<AccessEntry> accessTime;
    private String userId;
    //private List<Like> likes;
    //private List<Comment> comments;
    //private List<Comment> shares;
    //private List<String> explicitFeedback; // TODO
    
    private static HashMap<String, AccessLog> userAccessMap = new HashMap<String, AccessLog>(GlobalConstants.MAX_APP_USERS);
    private AccessLog(String userId){
        this.userId = userId;
        accessTime = new LinkedList<AccessEntry>();
        //likes = new LinkedList<Like>();
        //comments = new LinkedList<Comment>();
    }
    private AccessLog(String userId, List<String> accessEntries){
        this.userId = userId;
        accessTime = new LinkedList<AccessEntry>();
        for(String accessEntry: accessEntries){
            String[] arr = accessEntry.split(" ");
            if(arr.length!=2){
                continue;
            }
            accessTime.add(new AccessEntry(Long.parseLong(arr[0]), Integer.parseInt(arr[1])));
        }
        //likes = new LinkedList<Like>();
        //comments = new LinkedList<Comment>();
    }
    public static synchronized AccessLog getAccessLogObject(String user){
        if(!userAccessMap.containsKey(user)){
            userAccessMap.put(user,new AccessLog(user));
        }
        return userAccessMap.get(user);
    }
    public static void setAccessLogFromDB(String userId,List<String> accessEntries){
        userAccessMap.put(userId, new AccessLog(userId,accessEntries));
    }
    public static void addAccessEntry(String userId, long timestamp){
        if(getAccessLogObject(userId).accessTime.size()>0){
            AccessEntry lastEntry = getAccessLogObject(userId).accessTime.getLast();
            if(lastEntry.accessTime+lastEntry.duration*1000 + GlobalConstants.UPDATE_PAGE_TIME*1000 + 10000 > timestamp){
                lastEntry.duration=(int)((timestamp-lastEntry.accessTime)/1000);
                getAccessLogObject(userId).accessTime.removeLast();
                getAccessLogObject(userId).accessTime.add(lastEntry);
            }else{
                getAccessLogObject(userId).accessTime.add(new AccessEntry(timestamp,0));
            }
        }else{
            getAccessLogObject(userId).accessTime.add(new AccessEntry(timestamp,0));
        }
        
    }
    public static List<String> getAccessLogs(String userId){
        LinkedList<String> list = new LinkedList<String>();
        for(AccessEntry entry : getAccessLogObject(userId).accessTime){
            list.add(entry.toString());
        };
        return list;
    }
    
    public static class AccessEntry implements Serializable{
        long accessTime;
        int duration;

        public AccessEntry(long accessTime, int duration) {
            this.accessTime = accessTime;
            this.duration = duration;
        }

        public long getAccessTime() {
            return accessTime;
        }

        public int getDuration() {
            return duration;
        }

        public void setAccessTime(long accessTime) {
            this.accessTime = accessTime;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
        public String toString(){
            return accessTime+" "+duration;
        }
        
    }
}
