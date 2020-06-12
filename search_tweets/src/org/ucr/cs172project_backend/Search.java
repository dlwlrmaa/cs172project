package org.ucr.cs172project_backend;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.springframework.web.bind.annotation.CrossOrigin;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders="*")
public class Search {
    @GetMapping("/search")
    public static List<Document> Search(@RequestParam(required=false, defaultValue="") String query) throws IOException {
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer();
            FSDirectory index = FSDirectory.open(Paths.get("../index"));

            Expression expr = JavascriptCompiler.compile("_score * age");
            SimpleBindings bindings = new SimpleBindings();
            bindings.add(new SortField("_score", SortField.Type.SCORE));
            bindings.add(new SortField("age", SortField.Type.LONG));

            Query q = new FunctionScoreQuery(
                    new QueryParser("text", analyzer).parse(query),
                    expr.getDoubleValuesSource(bindings));
            //Query q = new QueryParser("text", analyzer).parse(query);

            int hitsPerPage = 100;
            IndexReader ireader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(ireader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;
            List<Document> matchingTweets = new ArrayList<>();

            System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                int tweetID = Integer.parseInt(d.get("id"));
                matchingTweets.add(d);
                /*
                System.out.println("ID: " + tweetID);
                System.out.println((i + 1) + ". @" + d.get("user"));
                System.out.println(d.get("text"));
                System.out.println();
                System.out.println(d.get("age"));
                System.out.println(hits[i].score);
                System.out.println(d.get("created_at"));
                System.out.println("Hashtags");*/
                /*for (Hashtag h : hashtags.get(tweetID)) {
                    System.out.println("#" + h.getText());
                }
                System.out.println();*/
            }
            ireader.close();
            index.close();
            return matchingTweets;
        } catch (org.apache.lucene.queryparser.classic.ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}