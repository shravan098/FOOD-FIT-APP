package com.example.foodfit;

import java.util.List;

public class FoodRequest {

    private List<Content> contents;

    public FoodRequest(String base64Image) {
        this.contents = List.of(
                new Content(List.of(
                        new Part(
                                // 🔹 Updated Prompt
                                "Analyze this food image and return the nutritional information strictly for a 100 gram serving ONLY.\n\n" +
                                        "Return the result strictly in the following JSON format only:\n\n" +
                                        "{\n" +
                                        "  \"foodName\": \"string\",\n" +
                                        "  \"nutrients\": {\n" +
                                        "    \"calories\": \"string\",\n" +
                                        "    \"protein\": \"string\",\n" +
                                        "    \"fat\": \"string\",\n" +
                                        "    \"carbs\": \"string\"\n" +
                                        "  },\n" +
                                        "  \"healthInsights\": \"string\",\n" +
                                        "  \"processingLevel\": \"string\",\n" +
                                        "  \"culturalOrigin\": \"string\",\n" +
                                        "  \"ingredientBreakdown\": \"string\"\n" +
                                        "}\n\n" +
                                        "❌ Do not include any explanation, notes, or text outside this JSON."
                        ),
                        new Part(new InlineData("image/jpeg", base64Image))
                ))
        );
    }

    public List<Content> getContents() {
        return contents;
    }

    // --- Inner Classes ---
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private String text;
        private InlineData inline_data;  // ✅ FIX: snake_case

        // For text
        public Part(String text) {
            this.text = text;
        }

        // For image
        public Part(InlineData inline_data) {  // ✅ FIX
            this.inline_data = inline_data;
        }

        public String getText() {
            return text;
        }

        public InlineData getInline_data() {   // ✅ FIX
            return inline_data;
        }
    }

    public static class InlineData {
        private String mime_type;  // ✅ FIX
        private String data;

        public InlineData(String mime_type, String data) {  // ✅ FIX
            this.mime_type = mime_type;
            this.data = data;
        }

        public String getMime_type() {   // ✅ FIX
            return mime_type;
        }

        public String getData() {
            return data;
        }
    }
}
