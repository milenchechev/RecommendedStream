package com.chechev.phd.recommendedstream.datatypes;

import com.restfb.Facebook;
import com.restfb.JsonMapper;
import com.restfb.JsonMapper.JsonMappingCompleted;
import com.restfb.json.JsonObject;
import static com.restfb.json.JsonObject.getNames;
import com.restfb.types.Post;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Milen
 */
public class ExtendedPost extends Post { 

    private static final long serialVersionUID = 2269900000639168705L;

    @Facebook
    private String story;
    @Facebook("story_tags")
    private JsonObject rawStoryTags;
    private Map<String, List<MessageTag>> storyTags = new HashMap<String, List<MessageTag>>();
    @Facebook
    private Long sharesCount;

    public String getStory() {
        return story;
    }

    public Map<String, List<Post.MessageTag>> getStoryTags() {
        return storyTags;
    }

    @JsonMappingCompleted
    protected void jsonMappingCompleted(JsonMapper jsonMapper) {
        super.jsonMappingCompleted(jsonMapper);
        storyTags = new HashMap<String, List<MessageTag>>();

        if (rawStoryTags == null) {
            return;
        }

        for (String key : getNames(rawStoryTags)) {
            String storyTagJson = rawStoryTags.getString(key).toString();
            storyTags.put(key, jsonMapper.toJavaList(storyTagJson, MessageTag.class));
        }
    }

    @Override
    public String toString() {
        return super.toString() + "story : " + story + ", story_tags : " + storyTags;
    }
}
