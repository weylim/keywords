package keywords;

import java.sql.SQLException;
import java.util.List;
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
            System.out.println(sample.body);
            
            // Identify candidates and generate the features for a sample doc
            FeatureGenerator featGen = new FeatureGenerator();
            List<Record> records = featGen.generateRecords(sample);
            System.out.println("Nrecords: " + records.size());
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }

        return true;
    }
    
}
