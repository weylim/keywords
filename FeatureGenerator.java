package keywords;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeatureGenerator { 
    public static List<Record> generateRecords(Sample sample) {
        List<Record> records = new ArrayList<>();
        
        // Clean and POS-tag document body
        Texter texter = new Texter();
        String body = texter.clean(sample.body); // clean the text
        String tagged = texter.tag(body); // POS tagged, mixed case
        System.out.println(tagged);
        
        body = body.toLowerCase().replaceAll("\\s+", "-"); // body, lower case, hyphenated
        
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
                int termFreq = texter.substrFreq(phrase, body);
                int label = sample.tags.contains(phrase) ? 1 : 0; // determine if phrase is one of the groundtruth tags
                int Position = i - numWords;
                
                // Instantiate the new record (finally!)
                Record record = new Record(phrase, Position, (float)Position/MsgLen, termFreq, phrase.length(), numWords, label);
                records.add(record);
                
                phrase = ""; // clear phrase
                numWords = 0;
            }
        }
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
