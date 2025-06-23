package demo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ExcelExporter {
    
    public static void main(String[] args) {
        convertJsonToCsv("companies_results.json", "companies_results.csv");
    }
    
    private static void convertJsonToCsv(String jsonFilename, String csvFilename) {
        try {
            // Read JSON file
            Gson gson = new Gson();
            JsonObject jsonData = gson.fromJson(new FileReader(jsonFilename), JsonObject.class);
            JsonArray companies = jsonData.getAsJsonArray("companies");
            
            // Create CSV file
            FileWriter csvWriter = new FileWriter(csvFilename);
            
            // Write header
            csvWriter.append("Title,Type,Rating,Reviews,Address,Phone,Google_Maps_URL\n");
            
            // Write data rows
            for (int i = 0; i < companies.size(); i++) {
                JsonObject company = companies.get(i).getAsJsonObject();
                
                String title = getStringValue(company, "title");
                String type = getStringValue(company, "type");
                String rating = getStringValue(company, "rating");
                String reviews = getStringValue(company, "reviews");
                String address = getStringValue(company, "address");
                String phone = getStringValue(company, "phone");
                String googleMapsUrl = getStringValue(company, "google_maps_url");
                
                // Escape CSV values (handle commas and quotes)
                csvWriter.append(escapeCsv(title))
                         .append(",")
                         .append(escapeCsv(type))
                         .append(",")
                         .append(escapeCsv(rating))
                         .append(",")
                         .append(escapeCsv(reviews))
                         .append(",")
                         .append(escapeCsv(address))
                         .append(",")
                         .append(escapeCsv(phone))
                         .append(",")
                         .append(escapeCsv(googleMapsUrl))
                         .append("\n");
            }
            
            csvWriter.close();
            System.out.println("CSV file created successfully: " + csvFilename);
            System.out.println("Total companies exported: " + companies.size());
            
        } catch (IOException e) {
            System.out.println("Error converting JSON to CSV: " + e.getMessage());
        }
    }
    
    private static String getStringValue(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }
    
    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // If value contains comma, newline, or quote, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
