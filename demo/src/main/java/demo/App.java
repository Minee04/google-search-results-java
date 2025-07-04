/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package demo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;

import serpapi.SerpApiSearchException;
import serpapi.GoogleSearch;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws SerpApiSearchException {
        String location = "Huddinge, Stockholm County, Sweden";
        System.out.println("Searching for companies in " + location);
        
        List<JsonObject> allResults = new ArrayList<>();
        int maxResults = 20; // Target number of results
        int currentStart = 0;
        int resultsPerPage = 20; // Google typically returns 20 results per page
        
        System.out.println("Starting to collect results...");

        while (allResults.size() < maxResults) {
            System.out.println("Fetching results " + (currentStart + 1) + " to " + (currentStart + resultsPerPage) + "...");
            
            // parameters for current page
            Map<String, String> parameter = new HashMap<>();
            parameter.put("api_key", "011dd289dd86b6941ae2822a4440e0b132a8d37f5486eabd3fd775c37150a838");
            parameter.put("engine", "google");
            parameter.put("q", "companies");
            parameter.put("location", location);
            parameter.put("google_domain", "google.se");
            parameter.put("gl", "se");
            parameter.put("hl", "sv");
            parameter.put("start", String.valueOf(currentStart));
            parameter.put("tbm", "lcl");
            parameter.put("num", "20");

            // Create search
            GoogleSearch search = new GoogleSearch(parameter);

            try {
                // Execute search
                JsonObject results = search.getJson();
                
                // Check if we have local results
                if (results.has("local_results")) {
                    JsonArray localResults = results.getAsJsonArray("local_results");
                    System.out.println("Found " + localResults.size() + " results on this page");
                    
                    // Add each result to our collection
                    for (JsonElement element : localResults) {
                        JsonObject result = element.getAsJsonObject();
                        allResults.add(result);
                        
                        // Stop if we've reached our target
                        if (allResults.size() >= maxResults) {
                            break;
                        }
                    }
                    
                    // If we got fewer results than expected, we've reached the end
                    if (localResults.size() < resultsPerPage) {
                        System.out.println("Reached end of results (got " + localResults.size() + " instead of " + resultsPerPage + ")");
                        break;
                    }
                } else {
                    System.out.println("No local_results found on this page");
                    break;
                }
                
                currentStart += resultsPerPage;
                
                // Add a small delay to be respectful to the API
                Thread.sleep(1000);
                
            } catch (SerpApiSearchException e) {
                System.out.println("Exception on page starting at " + currentStart + ":");
                System.out.println(e.toString());
                break;
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted");
                break;
            }
        }
        
        System.out.println("\nCollection complete! Total results collected: " + allResults.size());
        
        // Save results to JSON file
        saveResultsToJson(allResults, "companies_results.json");
        
        // Print summary
        printSummary(allResults);
    }
      private static void saveResultsToJson(List<JsonObject> results, String filename) {
        try {
            // Create the final JSON structure
            JsonObject finalJson = new JsonObject();
            finalJson.addProperty("total_results", results.size());
            finalJson.addProperty("location", "Huddinge, Stockholm County, Sweden");
            finalJson.addProperty("search_query", "companies");
            finalJson.addProperty("timestamp", java.time.LocalDateTime.now().toString());
            
            JsonArray resultsArray = new JsonArray();            for (JsonObject result : results) {
                // Create a filtered result with only the specified fields
                JsonObject filteredResult = new JsonObject();
                
                // Include only the specified fields
                if (result.has("rating")) {
                    filteredResult.add("rating", result.get("rating"));
                }
                if (result.has("reviews")) {
                    filteredResult.add("reviews", result.get("reviews"));
                }
                if (result.has("title")) {
                    filteredResult.add("title", result.get("title"));
                }
                if (result.has("type")) {
                    filteredResult.add("type", result.get("type"));
                }
                if (result.has("address")) {
                    filteredResult.add("address", result.get("address"));
                }
                if (result.has("phone")) {
                    filteredResult.add("phone", result.get("phone"));
                }
                
                // Extract Google Maps URL from links.directions
                if (result.has("links") && result.get("links").isJsonObject()) {
                    JsonObject links = result.getAsJsonObject("links");
                    if (links.has("directions")) {
                        filteredResult.add("google_maps_url", links.get("directions"));
                    }
                }
                
                resultsArray.add(filteredResult);
            }
            finalJson.add("companies", resultsArray);
            
            // Write to file with pretty printing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(filename);
            gson.toJson(finalJson, writer);
            writer.close();
            
            System.out.println("Results saved to: " + filename);
            
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }
    
    private static void printSummary(List<JsonObject> results) {
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total companies found: " + results.size());
        
        // Count by type
        Map<String, Integer> typeCount = new HashMap<>();
        for (JsonObject result : results) {
            String type = result.has("type") ? result.get("type").getAsString() : "Unknown";
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        System.out.println("\nCompanies by type:");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println("\nFirst 10 companies:");
        for (int i = 0; i < Math.min(10, results.size()); i++) {
            JsonObject result = results.get(i);
            String title = result.has("title") ? result.get("title").getAsString() : "No title";
            String type = result.has("type") ? result.get("type").getAsString() : "Unknown type";
            double rating = result.has("rating") ? result.get("rating").getAsDouble() : 0.0;
            System.out.println((i + 1) + ". " + title + " (" + type + ") - Rating: " + rating);
        }
    }
}
