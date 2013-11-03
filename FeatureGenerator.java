package keywords;

import static keywords.MyValues.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureGenerator { 
    public static List<Record> generateRecords(Sample sample) {
       List<Record> records = new ArrayList<>();
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            
            // Clean and POS-tag document body
            Texter texter = new Texter();
            String body = texter.clean(sample.body); // clean the text
            String tagged = texter.tag(body); // POS tagged, mixed case
            
            body = body.toLowerCase().replaceAll("\\s+", "-"); // body, lower case, hyphenated
            System.out.println(body);
            
            String[] words = tagged.split("\\s+"); // explode via blankspaces to words array
            String phrase = "";
            int numWords = 0;
            int MsgLen = words.length;
                    
            // For each NN* phrases, instantiate a record
            for (int i = 0; i < words.length + 1; i++) { 
                if(i < words.length && words[i].matches(".*_NN\\w?")) {
                    words[i] = words[i].replace("\\","");
                    phrase += '-' + words[i].substring(0, words[i].lastIndexOf("_"));
                    numWords++;
                }
                else if (!phrase.isEmpty()) {
                    phrase = phrase.substring(1).toLowerCase(); // remove the hyphen at start and change to lowercase
                    
                    // Calculates the different attributes
                    int keyphraseness = mysql.getFreq(keyphrasenessTable, "tag", phrase, "freq");
                    int Position = i - numWords;    
                    
                    int TF = texter.substrFreq(body, phrase.replace("-"," ")); 
                    System.out.println(phrase + " : " + TF);
                    int DF = mysql.containsSubstr(trainTable, "body", phrase); // number of docs whose body contains the phrase
                    
                    int label = sample.tags.contains(phrase) ? 1 : 0; // determine if phrase is one of the groundtruth tags
                    
                    // Instantiate the new record (finally!)
                    Record record = new Record(phrase, keyphraseness, Position, (float)Position/MsgLen, phrase.length(), numWords, TF, DF, label);
                    records.add(record);
                    
                    phrase = ""; // clear phrase
                    numWords = 0;
                }
            }
        } 
        catch (SQLException ex) {Logger.getLogger(FeatureGenerator.class.getName()).log(Level.SEVERE, null, ex);}
        return records;
    }
    
    
    /** Build a keyphrasessness table using a table containing sample documents 
     * @param samplesTable table containing the sample documents 
     * @param keyphrasenessTable keyphraseness table to be populated. This shall already have been instantiated in the database */
    public static void buildKeyphraseness(String samplesTable, String keyphrasenessTable) throws SQLException {
        MySQL mysql = new MySQL();
        mysql.connectDB("root", "password", "localhost", "keywords");
        
        int Nrows = mysql.getNRows(samplesTable); // get the number of rows in samplesTable
        int Ntags = 0;
        
        // loop through each record in sampleTables
        for (int i = 0; i < Nrows; i++) {
            if (i % 100 == 0) {System.out.println(i + "/" + Nrows);} // report to screen
            
            Sample sample = mysql.readSingle(samplesTable, i);
            String[] tags = sample.tags.split("\\s+"); // get the tags
            for (String tag : tags) {
                int result = mysql.incrementHistogram(keyphrasenessTable, "tag", tag, "freq");
                assert (result == 1 || result == 2);
                Ntags++;
            }               
        }
        
        System.out.println("Samples/Docs processed: " + Nrows);
        System.out.println("Tags processes: " + Ntags);
        System.out.println();
    }
	
}
