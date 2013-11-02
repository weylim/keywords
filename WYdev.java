package keywords;

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
        
    static String DBName = "keywords";
    
    public boolean dev() {
       // Weka naivesBayes = new Weka();
        MySQL mysql = new MySQL(); // init database interface object
        try {
            // grab a sample
            mysql.connectDB("root", "password", "localhost", DBName);
            Sample sample = mysql.readSingle("train1000", 15);
            
            // build keyphraseness table
            FeatureGenerator featGen = new FeatureGenerator();
            featGen.buildKeyphraseness("train15", "keyphraseness1000");
            assert(false);
            
            // Identify candidates and generate the features for a sample doc
            List<Record> records = featGen.generateRecords(sample);
            System.out.println("Nrecords: " + records.size());
            
            PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
            writer.println("Tags: " + sample.tags);
            for (Record record : records) {
                writer.println(record.phrase + " (" + record.label + ")");
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
