package keywords;

public final class MyValues {
    public final static String DBName = "keywords";
    
    // Train
    public final static String trainTable = "train1000";
    public final static String keyphrasenessTable = "keyphraseness1000";
    public final static String trainFeature = "train1000.feature";
    public final static String trainArff = "train1000.arff";
    public final static String modelFile = "train1000_J48.model";
    
    // Test
    public final static String testTable = "train1001_2000";
    public final static String resultsFile = "exp20131128b.result";
    
    // Params used
    // SMOTE percentage = 600
    // spmf minsup = 0.001
}
