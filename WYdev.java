package keywords;

import static keywords.MyValues.*;
/**
 * @author WeeYong
 */
public class WYdev {
    
    public void dev() {
              
        //FeatureGenerator featGen = new FeatureGenerator();
        //featGen.generateRecords(trainTxt);
        
        Weka naiveBayes = new Weka();
        //naiveBayes.generateARFF(trainTxt, trainArff);
        //naiveBayes.train(trainArff, modelFile);
        
        naiveBayes.test(modelFile, testTable, resultsFile);
        
        
    }
}
