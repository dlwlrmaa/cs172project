package org.ucr.cs172project_indexer;

import java.io.*;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

public class Main {

    private static float dateBoost(String date) {
        float boost = 1.0f;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z uuuu");
        ZonedDateTime dateTime = ZonedDateTime.parse(date, formatter);
        ZonedDateTime currentDate = ZonedDateTime.now();

        Duration diff = Duration.between(dateTime, currentDate);
        boost = 100000.0f / diff.toMinutes();

        return boost;
    }

    private static void addDoc(IndexWriter w, int id, String user, String text, String city, String country,
                               String created_at, double latitude, double longitude, String urls, int url_bool,
                               String url_title, int retweets, int favorites, int replies,
                               int quotes, int followers, int verified) throws IOException {
        float date = dateBoost(created_at);

        Document doc = new Document();
        doc.add(new StoredField("id", id));
        doc.add(new StringField("user", user, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new StringField("city", city, Field.Store.YES));
        doc.add(new StringField("country", country, Field.Store.YES));
        doc.add(new StringField("created_at", created_at, Field.Store.YES));
        doc.add(new StoredField("latitude", latitude));
        doc.add(new StoredField("longitude", longitude));
        doc.add(new StringField("urls", urls, Field.Store.YES));
        doc.add(new StoredField("url_bool", url_bool));
        doc.add(new StringField("url_title", url_title, Field.Store.YES));
        doc.add(new StoredField("retweets", retweets));
        doc.add(new StoredField("favorites", favorites));
        doc.add(new StoredField("replies", replies));
        doc.add(new StoredField("quotes", quotes));
        doc.add(new StoredField("followers", followers));
        doc.add(new StoredField("verified", verified));
        doc.add(new NumericDocValuesField("age", (long) date));
        doc.add(new StoredField("age", date));

        w.addDocument(doc);
    }

    public static void main(String[] args) {
        try {
            String path = "../collect_tweets/tweets_";

            int fileNumber = 1;
            String extension = ".json";

            JSONParser parser = new JSONParser();
            String line = null;

            Object jsonObj;
            JSONObject jsonObject;

            StandardAnalyzer analyzer = new StandardAnalyzer();
            FSDirectory index = FSDirectory.open(Paths.get("../index"));
            FileWriter file = new FileWriter("../index/hashtags.json");
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(index, config);
            HashMap<Integer, Vector<Hashtag>> hashtags = new HashMap<Integer, Vector<Hashtag>>();
            int id = 0;

            JSONArray ht_array;
            JSONObject jsonHashtags;

            File tweets = new File(path + fileNumber + extension);
            while (tweets.isFile()) {
                System.out.println(path + fileNumber + extension);
                Reader reader = new FileReader(path + fileNumber + extension);
                BufferedReader br = new BufferedReader(reader);

                while ((line = br.readLine()) != null) {
                    jsonObj = parser.parse(line);
                    jsonObject = (JSONObject) jsonObj;

                    String user = (String) jsonObject.get("user");
                    String text = (String) jsonObject.get("text");
                    String city = (String) jsonObject.get("city");
                    String country = (String) jsonObject.get("country");
                    String created_at = (String) jsonObject.get("created_at");

                    JSONArray geolocation = (JSONArray) jsonObject.get("geolocation");
                    geolocation = (JSONArray) geolocation.get(0);
                    JSONArray coord1 = (JSONArray) geolocation.get(0);
                    JSONArray coord2 = (JSONArray) geolocation.get(2);
                    CoordinateBoundingBox cbb = new CoordinateBoundingBox((double) coord1.get(0),
                                                                          (double) coord1.get(1),
                                                                          (double) coord2.get(0),
                                                                          (double) coord2.get(1));
                    double[] cbb_array = cbb.Center();

                    Boolean url = (Boolean) jsonObject.get("url_bool");
                    int url_bool;

                    String urls;
                    String url_title;
                    if (url){
                        urls = (String) jsonObject.get("urls");
                        url_title = (String) jsonObject.get("url_title");
                        url_bool = 1;

                        if (url_title == null) {
                            url_title = "";
                        }

                    } else {
                        urls = "";
                        url_title = "";
                        url_bool = 0;
                    }

                    int retweets = ((Long) jsonObject.get("retweets")).intValue();
                    int favorites = ((Long) jsonObject.get("favorites")).intValue();
                    int replies = ((Long) jsonObject.get("replies")).intValue();
                    int quotes = ((Long) jsonObject.get("quotes")).intValue();
                    int followers = ((Long) jsonObject.get("followers")).intValue();
                    int verified = ((Boolean) jsonObject.get("verified")) ? 1 : 0;

                    addDoc(w, id, user, text, city, country, created_at, cbb_array[0], cbb_array[1], urls, url_bool, url_title, retweets, favorites, replies, quotes, followers, verified);

                    ht_array = (JSONArray) jsonObject.get("hashtags");
                    jsonHashtags = new JSONObject();
                    jsonHashtags.put("id", id);
                    jsonHashtags.put("hashtags", ht_array);

                    file.write(jsonHashtags.toJSONString());
                    file.write("\n");

                    /*JSONObject ht_json;
                    String ht_text;
                    JSONArray ht_index;
                    int a, b;
                    Hashtag hashtag;
                    Vector<Hashtag> ht_vector = new Vector<>();

                    for (Object o : ht_array) {
                        ht_json = (JSONObject) o;
                        ht_text = (String) ht_json.get("text");
                        ht_index = (JSONArray) ht_json.get("indices");
                        a = ((Long) ht_index.get(0)).intValue();
                        b = ((Long) ht_index.get(1)).intValue();
                        hashtag = new Hashtag(ht_text, a, b);
                        ht_vector.add(hashtag);
                    }

                    hashtags.put(id, ht_vector);
                    */
                    ++id;
                }

                reader.close();

                ++fileNumber;
                tweets = new File(path + fileNumber + extension);
            }
            w.close();
            file.flush();
            file.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
