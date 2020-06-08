package org.ucr.cs172project_backend;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.ucr.cs172project_backend.CoordinateBoundingBox;

public class Main {

    private static void addDoc(IndexWriter w, int id, String user, String text, String city, String country,
                               String created_at, double latitude, double longitude, String urls, int url_bool,
                               String url_title, int retweets, int favorites, int replies,
                               int quotes, int followers, int verified) throws IOException {
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
        w.addDocument(doc);
    }

    public static void main(String[] args) throws IOException, ParseException {
        try {
            String path = "../collect_tweets/tweets_";
            int fileNumber = 1;
            String extension = ".json";

            JSONParser parser = new JSONParser();
            String line = null;

            Object jsonObj;
            JSONObject jsonObject;

            StandardAnalyzer analyzer = new StandardAnalyzer();
            Directory index = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(index, config);
            HashMap<Integer, Vector<Hashtag>> hashtags = new HashMap<Integer, Vector<Hashtag>>();
            int id = 0;

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

                    JSONArray ht_array = (JSONArray) jsonObject.get("hashtags");
                    JSONObject ht_json;
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

                    ++id;
                }

                reader.close();

                ++fileNumber;
                tweets = new File(path + fileNumber + extension);
            }
            w.close();

            String querystr = "black lives matter";
            Query q = new QueryParser("text", analyzer).parse(querystr);

            int hitsPerPage = 100;
            IndexReader ireader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(ireader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                int tweetID = Integer.parseInt(d.get("id"));
                System.out.println("ID: " + tweetID);
                System.out.println((i + 1) + ". @" + d.get("user"));
                System.out.println(d.get("text"));
                System.out.println();

                System.out.println("Hashtags");
                for (Hashtag h : hashtags.get(tweetID)) {
                    System.out.println("#" + h.getText());
                }
                System.out.println();
            }

            ireader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
