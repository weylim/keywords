package keywords;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
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
}
