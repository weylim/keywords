package keywords;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static keywords.MyValues.*;
/**
 * @author WeeYong
 */
public class WYdev {
    
    public void dev() {
              
        //FeatureGenerator featGen = new FeatureGenerator();
        
        /* Building keyphraseness table */
        //featGen.buildKeyphraseness(trainTable, keyphrasenessTable);
        
        /* Build Tags ID column */
        
        //featGen.generateRecords(trainTable, keyphrasenessTable, trainFeature);
 
        Weka weka = new Weka();
        //weka.generateARFF(trainFeature, trainArff);
        //weka.train(trainArff, modelFile);
        
        weka.test(modelFile, testTable, trainTable, keyphrasenessTable, resultsFile);
        
        
    }
}
