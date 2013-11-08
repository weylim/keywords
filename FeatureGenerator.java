package keywords;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static keywords.MyValues.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureGenerator { 
    public int Ndocs;
    Texter texter = new Texter();
    
    public void generateRecords(String trainTable, String keyphrasenessTable, String featuresFile) {
        File file = new File(featuresFile);
        file.delete();

        try {
            MySQL mysql = new MySQL(); // init database interface object
            mysql.connectDB("root", "password", "localhost", DBName);
            Ndocs = mysql.getNRows(trainTable);
           
            // grab the documents!
            for (int i = 0; i < Ndocs; i++) {
                System.out.println(i + "/" + Ndocs);
                Sample sample = mysql.readSingle(trainTable, i);

                // Identify candidates and generate the features for a sample doc
                List<Record> records = generateRecords(sample, trainTable, keyphrasenessTable);
                try (FileWriter writer = new FileWriter(featuresFile, true)) {
                    writer.write("Document " + i + "\n");
                    for (Record record : records) {
                        writer.write(record.phrase + ", " + record.keyphraseness + ", " + record.absPosition + ", " + record.relativePosition + ", " + record.numChars + ", " + record.numWords + ", " + record.TF + ", " + record.TFIDF + ", " + record.label + "\n");                
                    }                
                }
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    /** Identify candidate phrases in a specified sample document and generates the features for each of these candidate phrases
     * @param sample sample document to identify the candidate phrases and generate features for each of them 
     * @param trainTable train table in database where the specified sample came from 
     * @param keyphrasenessTable reference keypharsenessTable to refer to while generating the features 
     * @return List<Record> where a Record is essentially the feature vector of a candidate phrase */
    public List<Record> generateRecords(Sample sample, String trainTable, String keyphrasenessTable) {
        List<Record> records = new ArrayList<>();
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            
            // Clean and POS-tag document body
            String body = texter.clean(sample.body); // clean the text
            String tagged = texter.tag(body); // POS tagged, mixed case
            
            body = body.toLowerCase(); // body change to lower case for easier search for phrases TF later
            
            String[] words = tagged.split("\\s+"); // explode via blankspaces to words array
            String phrase = "";
            int numWords = 0;
            int MsgLen = words.length;
            String tags = ' ' + sample.tags + ' ';
                    
            // For each NN* phrases, instantiate a record
            for (int i = 0; i < words.length + 1; i++) { 
                if(i < words.length && words[i].matches(".*_NN\\w?")) {
                    words[i] = words[i].replace("\\","");
                    phrase += '-' + words[i].substring(0, words[i].lastIndexOf("_")); // phrase is hyphenated to easier match the tags
                    numWords++;
                }
                else if (!phrase.isEmpty() && !phrase.matches("-*")) {                    
                    phrase = phrase.substring(1).toLowerCase(); // remove the hyphen at start and change to lowercase
                    int label = tags.contains(' ' + phrase + ' ') ? 1 : 0; // determine if phrase is one of the groundtruth tags
                    
                    /* Calculates the different attributes */
                    int keyphraseness = mysql.getFreq(keyphrasenessTable, "tag", phrase, "freq");
                    int Position = i - numWords;    
                    
                    phrase = phrase.replace("-"," "); // replace the hyphens between words to single space, AFTER checking for label and keyphraseness
                    double TF = texter.substrFreq(body, phrase); 
                    if (TF <= 0) {TF = 1;} // force TF to be at least 1
                    TF = Math.log10(TF + 1); // TF cannot be 0
                    
                    double IDF = mysql.containsSubstr(trainTable, "body", phrase); // number of docs whose body contains the phrase
                    IDF = Math.log10(Ndocs/(1+IDF));
                    double TFIDF = TF * IDF;
                    
                    /* Instantiate the new record (finally!) */
                    Record record = new Record(phrase, keyphraseness, Position, (float)Position/MsgLen, phrase.length(), numWords, TF, TFIDF, label);
                    records.add(record);
                    
                    phrase = ""; // clear phrase
                    numWords = 0;
                }
            }
            mysql.disconnect();
        }
        catch (SQLException ex) {Logger.getLogger(FeatureGenerator.class.getName()).log(Level.SEVERE, null, ex);}
        return records;
    }
    
    
    /** Build a keyphrasessness table for the tags from a table containing sample documents 
     * @param samplesTable table containing the sample documents 
     * @param keyphrasenessTable keyphraseness table to be populated. This shall already have been instantiated in the database */
    public void buildKeyphraseness(String samplesTable, String keyphrasenessTable) throws SQLException {
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


/*public void buildKeyphrasenessTable(String trainTable, String keyphrasenessTable) {
    try {
        MySQL mysql = new MySQL();
        mysql.connectDB("root", "password", "localhost", DBName);

        // build keyphraseness table
        FeatureGenerator featGen = new FeatureGenerator();
        featGen.buildKeyphraseness(trainTable, keyphrasenessTable);
        mysql.disconnect();
    } 
    catch (SQLException ex) {
        Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
    }
}*/