package com.example.foodfit;

import java.util.List;

public class GeminiResponse {

    private List<Candidate> candidates;

    public List<Candidate> getCandidates() { return candidates; }

    public static class Candidate {
        private Content content;
        public Content getContent() { return content; }
    }

    public static class Content {
        private List<Part> parts;
        public List<Part> getParts() { return parts; }
    }

    public static class Part {
        private String text;
        public String getText() { return text; }
    }
}
