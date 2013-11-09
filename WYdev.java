package keywords;

import static keywords.MyValues.*;
/**
 * @author WeeYong
 */
public class WYdev {
    
    public void dev() {
              
        //FeatureGenerator featGen = new FeatureGenerator();
        //featGen.generateRecords(trainTable, keyphrasenessTable, trainTxt);
        
        Weka weka = new Weka();
        //naiveBayes.generateARFF(trainTxt, trainArff);
        //weka.train(trainArff, modelFile);
        
        weka.test(modelFile, testTable, trainTable, keyphrasenessTable, resultsFile);
        
        
    }
}
