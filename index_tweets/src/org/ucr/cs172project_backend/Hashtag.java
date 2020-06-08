package org.ucr.cs172project_backend;

public class Hashtag {
    private String text;
    private int start;
    private int end;

    public Hashtag(String text, int start, int end){
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public String getText(){
        return this.text;
    }

    public int getStart(){
        return this.start;
    }

    public int getEnd(){
        return this.end;
    }
}
