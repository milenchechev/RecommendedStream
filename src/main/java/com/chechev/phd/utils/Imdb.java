package com.chechev.phd.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import com.chechev.phd.recommendedstream.datatypes.ImdbMovie;

/**
 * Retrieves movie information by its title from imdb
 * 
 * @author ventsislavdimitrov
 *
 */
public class Imdb {

    private static String URLbyTitle = "http://www.omdbapi.com/?t=";

    public static ImdbMovie getMovie(String title) {
        DefaultHttpClient client = new DefaultHttpClient();

        try {
            HttpGet request = new HttpGet(URLbyTitle + URLEncoder.encode(title, "UTF-8"));
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == Status.OK.getStatusCode()) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(response.getEntity().getContent(), ImdbMovie.class);
            }
        } catch (ClientProtocolException e) {
            Logger.getLogger(Imdb.class.getName()).log(Level.WARNING, e.getMessage());
        } catch (IOException e) {
            Logger.getLogger(Imdb.class.getName()).log(Level.WARNING, e.getMessage());
        } finally {
            // Important: Close the connect
            client.getConnectionManager().shutdown();
        }
        return new ImdbMovie();
    }
}
