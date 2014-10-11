package com.chechev.phd.evaluation;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.chechev.phd.recommendedstream.services.UserEvent;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoRetriever {

	private static final int DELTA_FEED_RETRIEVAL = 10000;

	private static final String TAG = MongoRetriever.class.getSimpleName();

	public List<LinkedList<String>> getDisplayedFeeds(final String userId) {
		final DB db = getDb();

		final DBCollection userLogsCollection = db.getCollection("user_log");
		final List<LinkedList<String>> result = new LinkedList<LinkedList<String>>();

		final BasicDBObject searchObject = new BasicDBObject("user", userId);
		searchObject.put("action", UserEvent.LOAD_POSTS.name().toLowerCase());
		final DBCursor cursor = userLogsCollection.find(searchObject);

		final Set<String> viewedIds = getViewedIds();

		final NavigableMap<Long, LinkedList<String>> timeFeeds = new TreeMap<Long, LinkedList<String>>();

		while (cursor.hasNext()) {
			final DBObject feed = cursor.next();
			final long time = Long.parseLong(feed.get("time").toString());
			final LinkedList<String> feedIds = new LinkedList<String>(
					Arrays.asList(feed.get("post_order").toString().split(";")));
			final Long previousTime = timeFeeds.floorKey(time);
			if (previousTime != null
					&& time - previousTime < DELTA_FEED_RETRIEVAL) {
				timeFeeds.remove(previousTime);
			}
			feedIds.retainAll(viewedIds);
			timeFeeds.put(time, feedIds);
		}

		for (LinkedList<String> ids : timeFeeds.values()) {
			result.add(ids);
		}

		return result;
	}

	private DB getDb() {
		Mongo mongo = null;
		try {
			mongo = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
		} catch (UnknownHostException e) {
			Logger.getLogger(TAG).severe(e.getMessage());
		} catch (MongoException e) {
			Logger.getLogger(TAG).severe(e.getMessage());
		}

		final DB db = mongo.getDB("mydb");
		return db;
	}

	private Set<String> getViewedIds() {
		final DBCollection userLogsCollection = getDb().getCollection(
				"user_log");
		final BasicDBObject searchObject = new BasicDBObject("action",
				UserEvent.MARK_VIEWED.name().toLowerCase());
		final Set<String> viewedIds = new HashSet<String>();
		final DBCursor viewedItemsCursor = userLogsCollection
				.find(searchObject);
		while (viewedItemsCursor.hasNext()) {
			final DBObject viewedItem = viewedItemsCursor.next();
			viewedIds.add(viewedItem.get("post_id").toString());
		}
		return viewedIds;
	}

	public Set<String> getUserApprovedItemIds(final String userId) {
		final Set<String> result = new LinkedHashSet<String>();
		result.addAll(getIdsForUser(userId, UserEvent.COMMENT.name()
				.toLowerCase()));
		result.addAll(getIdsForUser(userId, UserEvent.LIKE.name().toLowerCase()));
		result.addAll(getIdsForUser(userId, UserEvent.MARK_INTERESTING.name()
				.toLowerCase()));
		result.addAll(getIdsForUser(userId, UserEvent.SHARE.name()
				.toLowerCase()));
		return result;
	}

	public Map<String, String> getUsersInfo() {

		final DB db = getDb();
		final DBCollection usersCollection = db.getCollection("users");

		final Map<String, String> users = new HashMap<String, String>();

		final DBCursor cursor = usersCollection.find();
		while (cursor.hasNext()) {
			final DBObject userObject = cursor.next();
			users.put(userObject.get("user_id").toString(),
					userObject.get("access_token").toString());
		}

		return users;
	}

	private Set<String> getIdsForUser(final String userId, final String action) {
		final DB db = getDb();
		final DBCollection userLogsCollection = db.getCollection("user_log");

		final Set<String> result = new HashSet<String>();

		final BasicDBObject searchObject = new BasicDBObject("user", userId);
		searchObject.put("action", action);
		final DBCursor cursor = userLogsCollection.find(searchObject);

		while (cursor.hasNext()) {
			final DBObject object = cursor.next();
			result.add(object.get("post_id").toString());
		}

		return result;
	}

}
