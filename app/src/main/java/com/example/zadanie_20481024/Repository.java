package com.example.zadanie_20481024;

import android.util.JsonReader;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

class Repository {
    //SINGLETON LOGIC
    private static final Repository ourInstance = new Repository();

    private Repository(){}

    static Repository getInstance(){
        return ourInstance;
    }


    //BUSINESS LOGIC
    private final List<PostSummary> postSummariesCache = new ArrayList<>();
    private FullPost postCache;
    private HttpsURLConnection connection;

    /**
     * Warning: this is a blocking method
     * @return response as jason or null if connection failed
     */
    @Nullable
    synchronized private JsonReader httpsGET(String str){
        try {
            URL typicodeEndpoint = new URL("https://jsonplaceholder.typicode.com/"+str);
            connection = (HttpsURLConnection) typicodeEndpoint.openConnection();
            connection.setRequestProperty("User-Agent", "example-app-with-rest-v"+BuildConfig.VERSION_NAME);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            return connection.getResponseCode() == 200 ? new JsonReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Warning: this is a blocking method.
     * @param forceReload true: then this method will not used cached data; false: this method will use cached data when it's available;
     * @return List of post summaries (may be cached) or null if method failed due to network or endpoint error.
     */
    @Nullable
    List<PostSummary> getPostSummaries(boolean forceReload) {
        if(postSummariesCache.isEmpty() || forceReload) {
            JsonReader reader = httpsGET("posts");
            if (reader != null) {
                try {
                    reader.beginArray();
                    postSummariesCache.clear();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        int postId;
                        String title, body;
                        reader.skipValue(); //pomijam user id name
                        reader.skipValue(); //pomijam user id value
                        reader.skipValue(); //pomijam post id name
                        postId = reader.nextInt();
                        reader.skipValue(); //pomijam title name
                        title = reader.nextString();
                        reader.skipValue(); //pomijam body name
                        body = reader.nextString();
                        reader.endObject();
                        postSummariesCache.add(new PostSummary(postId, title, body));
                    }
                    reader.endArray();
                    reader.close();
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else return null;
        }
        return postSummariesCache;
    }

    /**
     * This method will use cache data when possible.
     * @return immutable post object or null if something went wrong
     */
    @Nullable
    FullPost getFullPost(int postId){
        if(postCache!=null && postCache.getPostId() == postId) return postCache;
        String title, body, author, authorEmail;
        List<FullPost.Comment> comments = new ArrayList<>();
        JsonReader reader = httpsGET("posts/" + postId);
        try {
            //fetching general info
            if(reader == null) return null;
            reader.beginObject();
            reader.skipValue();
            int authorId = reader.nextInt();
            reader.skipValue();
            reader.skipValue();
            reader.skipValue();
            title = reader.nextString();
            reader.skipValue();
            body = reader.nextString();
            reader.close();
            connection.disconnect();

            //fetching info on the author
            reader = httpsGET("users/" + authorId);
            if(reader == null) return null;
            reader.beginObject();
            reader.skipValue();
            reader.skipValue();
            reader.skipValue();
            author = reader.nextString();
            reader.skipValue();
            authorEmail = reader.nextString();
            reader.close();
            connection.disconnect();

            //fetching comments
            reader = httpsGET("comments?postId="+postId);
            if(reader == null) return null;
            reader.beginArray();
            while(reader.hasNext()){
                String name, email, commentBody;
                reader.beginObject();
                reader.skipValue();
                reader.skipValue();
                reader.skipValue();
                reader.skipValue();
                reader.skipValue();
                name = reader.nextString();
                reader.skipValue();
                email = reader.nextString();
                reader.skipValue();
                commentBody = reader.nextString();
                reader.endObject();
                comments.add(new FullPost.Comment(name, email, commentBody));
            }
            //finally block closes the reader and connection
            postCache = new FullPost(postId, title, body, author, authorEmail, comments);
            return postCache;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
