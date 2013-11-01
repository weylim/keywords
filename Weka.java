package keywords;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
/**
 *
 * @author WeeYong
 */
public class Weka {
    public Classifier classifier;
    
    public boolean train(String trainFile) {
        try {
            // read in the training file
            BufferedReader raw = new BufferedReader(new FileReader(trainFile));
            Instances trainingInstances = new Instances(raw);
            trainingInstances.setClassIndex(trainingInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)
            
            // Init the classifier
            // common classifiers NaiveBayes, Logistic, RandomForest, MultilayerPerceptron, SMO, Bagging, AdaBoostM1, IBk (knn)
            classifier = new NaiveBayes();
            
            // Starts the training
            classifier.buildClassifier(trainingInstances);
        }
        catch (IOException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    
    public boolean test(String trainFile, String testFile) {
        try {
            // Read in the train dataset
            BufferedReader trainRaw = new BufferedReader(new FileReader(trainFile));
            Instances trainingInstances = new Instances(trainRaw);
            trainingInstances.setClassIndex(trainingInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)

            // Read in the test dataset
            BufferedReader testRaw = new BufferedReader(new FileReader(testFile));
            Instances testingInstances = new Instances(testRaw);
            testingInstances.setClassIndex(testingInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)

            // Starts the testing
            Evaluation eval1;
            eval1 = new Evaluation(trainingInstances);
            eval1.evaluateModel(classifier, testingInstances);

            // Prints put results
            System.out.println(eval1.toSummaryString("\nResults\n======\n", false));
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
}