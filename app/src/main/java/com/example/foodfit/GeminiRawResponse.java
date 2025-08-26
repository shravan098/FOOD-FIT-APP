package com.example.foodfit;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiRawResponse {

    @SerializedName("candidates")
    private List<Candidate> candidates;

    public String getText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate c = candidates.get(0);
            if (c.content != null && !c.content.parts.isEmpty()) {
                return c.content.parts.get(0).text;
            }
        }
        return "";
    }

    public static class Candidate {
        @SerializedName("content")
        public Content content;
    }

    public static class Content {
        @SerializedName("parts")
        public List<Part> parts;
    }

    public static class Part {
        @SerializedName("text")
        public String text;
    }
}
