package org.ucr.cs172project_backend;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class Search {
    @RequestMapping("/search")
    public String Search(@RequestParam(required=false, defaultValue="") String query) throws IOException, ParseException {
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer();
            FSDirectory index = FSDirectory.open(Paths.get("../index"));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(index, config);

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

            System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                int tweetID = Integer.parseInt(d.get("id"));
                System.out.println("ID: " + tweetID);
                System.out.println((i + 1) + ". @" + d.get("user"));
                System.out.println(d.get("text"));
                System.out.println();
                System.out.println(d.get("age"));
                System.out.println(hits[i].score);
                System.out.println(d.get("created_at"));
                System.out.println("Hashtags");
                /*for (Hashtag h : hashtags.get(tweetID)) {
                    System.out.println("#" + h.getText());
                }
                System.out.println();*/
            }

            ireader.close();
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return "hello world";
    }
}