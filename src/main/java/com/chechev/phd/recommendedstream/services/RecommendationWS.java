package com.chechev.phd.recommendedstream.services;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.chechev.phd.datacollectors.CollectorManager;
import com.chechev.phd.datacollectors.NewsFeedCollector;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;

/**
 * REST Web Service
 *
 * @author Milen
 */
@Path("recommendation")
public class RecommendationWS {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of RecommendationWS
     */
    public RecommendationWS() {
    }

    
    @GET
    @Path("like-based")
    @Produces("application/json")
    public static List<ExtendedPost> getLikeBasedRecommendations(@QueryParam("access_token") String access_token) {
        return NewsFeedCollector.userMap.get(CollectorManager.getUserId(access_token)).getPostToVisualize();
    }
    
    @GET
    @Path("friend-based")
    @Produces("application/json")
    public static List<ExtendedPost> getFriendBasedRecommendations(@QueryParam("access_token") String access_token) {
        
        return new LinkedList<ExtendedPost>();
    }
    
    @GET
    @Path("content-based")
    @Produces("application/json")
    public static List<ExtendedPost> getContentBasedRecommendations(@QueryParam("access_token") String access_token) {
        
        return new LinkedList<ExtendedPost>();
    }
}
