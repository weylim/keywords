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
              
        FeatureGenerator featGen = new FeatureGenerator();
        //featGen.buildKeyphraseness(trainTable, keyphrasenessTable);
        featGen.generateRecords(trainTable, keyphrasenessTable, trainFeature);
 
        //Weka naiveBayes = new Weka();
        //naiveBayes.generateARFF(trainFeature, trainArff);
        //naiveBayes.train(trainArff, modelFile);
        
        //weka.test(modelFile, testTable, trainTable, keyphrasenessTable, resultsFile);
        
        
    }
}
