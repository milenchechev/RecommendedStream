package com.chechev.phd.recommendedstream.datatypes;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Entity for retrieving information from imdb api
 * 
 * @author ventsislavdimitrov
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImdbMovie {

    @JsonProperty("Title")
    private String title = "";
    @JsonProperty("Year")
    private String year = "";
    @JsonProperty("Genre")
    private String genre = "";
    @JsonProperty("Poster")
    private String poster = "";
    @JsonProperty("imdbRating")
    private String imdbRating = "";
    @JsonProperty("imdbID")
    private String imdbId = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }
}
