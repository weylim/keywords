package keywords;

import static keywords.MyValues.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
        MySQL mysql = new MySQL(); // init database interface object
        try {
            // grab a sample
            mysql.connectDB("root", "password", "localhost", DBName);
            Sample sample = mysql.readSingle(trainTable, 15);
            
            // Identify candidates and generate the features for a sample doc
            FeatureGenerator featGen = new FeatureGenerator();
            List<Record> records = featGen.generateRecords(sample);
            System.out.println("Nrecords: " + records.size());
            
            PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
            for (Record record : records) {
                writer.println(record.phrase + ", " + record.keyphraseness + ", " + record.absPosition + ", " + record.relativePosition + ", " + record.numChars + ", " + record.numWords + ", " + record.TF + ", " + record.TFIDF + ", " + record.label);                
            }
           
            writer.close();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
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
            featGen.buildKeyphraseness("train1000", "keyphraseness1000");
        } 
        catch (SQLException ex) {
            Logger.getLogger(WYdev.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
