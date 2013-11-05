package keywords;

import java.io.File;
import static keywords.MyValues.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author WeeYong
 */
public class WYdev {
    
    public boolean dev() {
       // Weka naivesBayes = new Weka();
        File file = new File(outputFile);
        file.delete();
        
        MySQL mysql = new MySQL(); // init database interface object
        FeatureGenerator featGen = new FeatureGenerator(); // Init features generator
        
        try {
            mysql.connectDB("root", "password", "localhost", DBName);
            int Ndocs = mysql.getNRows(trainTable);
            featGen.Ndocs = Ndocs;
           
            // grab the documents!
            for (int i = 0; i < Ndocs; i++) {
                //if (i % 1 == 0) {System.out.println(i + "/" + Ndocs);}
                System.out.println(i + "/" + Ndocs);
                Sample sample = mysql.readSingle(trainTable, i);

                // Identify candidates and generate the features for a sample doc
                List<Record> records = featGen.generateRecords(sample);
                try (FileWriter writer = new FileWriter(outputFile, true)) {
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

        return true;
    }
    
    
    
    
    
    public void buildKeyphrasenessTable() {
        try {
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);

            // build keyphraseness table
            FeatureGenerator featGen = new FeatureGenerator();
            featGen.buildKeyphraseness(trainTable, keyphrasenessTable);
        } 
        catch (SQLException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
