package keywords;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static keywords.MyValues.DBName;

/**
 * @author WeeYong
 */
public class AssociationMiner {
    /** Go through documents in the training set and assign a ID for each of the tags in each document. Then output the tags' IDs to a text file */
    public void generateTagSets(String trainTable, String keyphrasenessTable) {
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            int Ndocs = mysql.getNRows(trainTable);
            
            // grab each document
            for (int i = 0; i < Ndocs; i++) {
                System.out.println(i + "/" + Ndocs);
                
                // grab the tags
                Sample sample = mysql.readSingle(trainTable, i);
                String[] tags = sample.tags.split("\\s+"); // explode via blankspaces to words array
                
                // get ID for each tag and store in array first
                
                
            }
            
        }
        catch (SQLException ex) {Logger.getLogger(FeatureGenerator.class.getName()).log(Level.SEVERE, null, ex);}
    }
}
