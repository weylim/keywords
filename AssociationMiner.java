package keywords;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static keywords.MyValues.DBName;

/**
 * @author WeeYong
 */
public class AssociationMiner {
    /** Go through documents in the training set and assign a ID for each of the tags in each document. Then output the tags' IDs to a text file */
    public void generateTagSets(String trainTable, String keyphrasenessTable, String tagSetsFile) {
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            int Ndocs = mysql.getNRows(trainTable);
            
            // open tagSets file
            FileWriter file;
            file = new FileWriter(tagSetsFile, true);

            // grab each document
            for (int i = 0; i < Ndocs; i++) {
                System.out.println(i + "/" + Ndocs);
                
                // grab the tags
                Sample sample = mysql.readSingle(trainTable, i);
                String[] tags = sample.tags.split("\\s+"); // explode via blankspaces to words array
                
                // get ID for each tag and store in array first
                int IDs[] = new int[tags.length];
                for (int j = 0; j < tags.length; j++) {
                    int id = mysql.getInt(keyphrasenessTable, "tag", tags[j], "id");
                    if (id < 1) {
                        Scanner userInput = new Scanner(System.in);
                        System.out.println( "Error! ID: " + id);
                        String error = userInput.next();
                        System.out.println(error);
                    }
                    IDs[j] = id;
                }
                Arrays.sort(IDs);
                for (int j = 0; j < tags.length; j++) {
                    file.write(IDs[j] + " ");
                }
                file.write("\n");
            }
            file.close();
        }
        catch (SQLException ex) {Logger.getLogger(FeatureGenerator.class.getName()).log(Level.SEVERE, null, ex);} 
        catch (IOException ex) {Logger.getLogger(AssociationMiner.class.getName()).log(Level.SEVERE, null, ex);}
    }
    
    /** Given a current tagset (set of tag IDs), find and return associated tag IDs from the association table.
     * Caller can then refer to the corresponding keyphraseness table to get the actual tags (in string)
     * @param associationTable table of tagsets (set of tag IDs) and their support count
     * @param givenTagSet given tagset (set of tag IDs)
     * @param minMatch min number of similar tags between a known tagset and givenTagSet before the 2 sets are considered "associated"
     * @return associated tag IDs */    
    public HashSet<String> associatedTags(String associationTable, String givenTagSet, int minMatch) {
        HashSet<String> associatedTagIDs = new HashSet<>(); 
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            String[] tags = givenTagSet.split("\\s+"); // explode via blankspaces to words array
                   
            HashMap<Integer, Integer> tagSetsHist = new HashMap<>();
            int maxFreq = 1;
            
            // loop through each tag in the given tagset
            for (int i = 0; i < tags.length; i++) {
                // get the IDs of all the tagsets that contains the current tag 
                List<Integer> IDs = mysql.containsSubstr (associationTable, "tagset", " " + tags[i] + " ", "id");
                
                // for each tagset ID, add them to a histogram
                for (int j = 0; j < IDs.size(); j++) {
                    int curID = IDs.get(j);
                    if(!tagSetsHist.containsKey(curID)) {tagSetsHist.put(curID, 1);}
                    else {
                        int freq = tagSetsHist.get(curID) + 1;
                        tagSetsHist.put(curID, freq);
                        if (freq > maxFreq) {maxFreq = freq;}
                    }
                }
            }
            if (maxFreq < minMatch) {return associatedTagIDs;}
            
            // now, grab tagsets that are similar enough
            else {
                for (Map.Entry<Integer, Integer> entry : tagSetsHist.entrySet()) {
                    if (entry.getValue() == maxFreq) {
                        String tagset = mysql.getStr(associationTable, "id", entry.getKey(), "tagset");
                        System.out.println("tagset: " + tagset);
                        if (tagset.length() > 0) {
                            String[] tagsID = tagset.split("\\s+"); // explode via blankspaces to words array
                            associatedTagIDs.addAll(Arrays.asList(tagsID));
                        }
                    }
                }
            }
        }        
        catch (SQLException ex) {Logger.getLogger(AssociationMiner.class.getName()).log(Level.SEVERE, null, ex);}        
        return associatedTagIDs;
    }
}
