package keywords;

import static keywords.MyValues.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
/**
 * @author WeeYong
 */
public class Weka {
    public Classifier classifier = null;
    
    public void generateARFF(String file, String ARFFfile) {
        try (BufferedReader input = new BufferedReader(new FileReader(file)); FileWriter arff = new FileWriter(ARFFfile, false)) {
            //Record record = new Record(phrase, keyphraseness, Position, (float)Position/MsgLen, phrase.length(), numWords, TF, TFIDF, label)
            arff.write("@RELATION keywords\n");
            arff.write("@ATTRIBUTE keyphraseness NUMERIC\n");
            arff.write("@ATTRIBUTE position NUMERIC\n");
            arff.write("@ATTRIBUTE relativePosition NUMERIC\n");
            arff.write("@ATTRIBUTE numChar NUMERIC\n");
            arff.write("@ATTRIBUTE numWords NUMERIC\n");
            arff.write("@ATTRIBUTE TF NUMERIC\n");
            arff.write("@ATTRIBUTE TFIDF NUMERIC\n");
            arff.write("@ATTRIBUTE class {0,1}\n");
            arff.write("@DATA\n");

            String line;
            while ((line = input.readLine()) != null) {
               String[] features = line.split(", ");
               if (features.length != 9) {
                   Scanner userInput = new Scanner(System.in);
                   System.out.println(line);
                   System.out.println("The above line may have wrong number of features? Enter to continue.");
                   line = userInput.next();
               }
               arff.write(line.substring(line.indexOf(", ") + 2) + "\n");
            }
        }
        catch (IOException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** Train using a specified training file and save the trained model to specified path 
     * @param trainFile arff file containing the training data 
     * @param modelFile file to save the trained model */
    public void train(String trainFile, String modelFile) {
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
            weka.core.SerializationHelper.write(modelFile, classifier);
        }
        catch (IOException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** Perform classification and outputs performance */
    public void test(String modelFile, String testTable, String resultsFile) {
        try {
             // init classifier and clear output file is necessary
            if (classifier == null) {classifier = (Classifier)weka.core.SerializationHelper.read(modelFile);}
            File deleteFile = new File(resultsFile);
            deleteFile.delete();
            
            // Init MySQL and feature generator
            MySQL mysql = new MySQL();
            mysql.connectDB("root", "password", "localhost", DBName);
            FeatureGenerator featGen = new FeatureGenerator();
            featGen.Ndocs = mysql.getNRows(testTable);
            
            // write header for results file
            try (FileWriter results = new FileWriter(resultsFile, true)) {
                results.write("Doc\tcandidateTP\tcandidateTN\tcandidateFP\tcandidateFN\t");
                results.write("Actual Tags\tPredicted Tags\tCandidate Tags\tTP\tNActual\tNPredicted\n");
                results.close();
            }
            
            // grab the test documents, one at a time!
            String currentTestTxt = "temp_test_sample.txt", curTestArff = "temp_test_sample.arff"; // temp files to hold generated features for a test doc
            for (int curDoc = 0; curDoc < featGen.Ndocs; curDoc++) {
                System.out.println(curDoc + "/" + featGen.Ndocs);
                Sample sample = mysql.readSingle(testTable, curDoc);

                // identify candidates, generate features and write these into temp text file
                List<Record> records = featGen.generateRecords(sample);
                try (FileWriter writer = new FileWriter(currentTestTxt, false)) {
                    for (Record record : records) {
                        writer.write(record.phrase + ", " + record.keyphraseness + ", " + record.absPosition + ", " + record.relativePosition + ", " + record.numChars + ", " + record.numWords + ", " + record.TF + ", " + record.TFIDF + ", " + record.label + "\n");                
                    }
                }
                
                // Read in the features for the candidates for the current document
                generateARFF(currentTestTxt, curTestArff);
                BufferedReader testRaw = new BufferedReader(new FileReader(curTestArff));
                Instances testInstances = new Instances(testRaw);
                testInstances.setClassIndex(testInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)
                              
                // init variables
                assert(records.size() == testInstances.numInstances());
                int candidateTP = 0, candidateTN = 0, candidateFP = 0, candidateFN = 0;
                int TP = 0, FP =0;
                HashSet<String> correctSet = new HashSet<>(), candidateSet = new HashSet<>();
                String tags = ' ' + sample.tags + ' '; // prepare the groundtruth tags
                
                // classify the candidates
                for (int i = 0; i < testInstances.numInstances(); i++) {
                    // update candidates statistics
                    String phrase = records.get(i).phrase.replaceAll("\\s+", "-");
                    candidateSet.add(phrase);
                    
                    int pred = (int)classifier.classifyInstance(testInstances.instance(i));
                    int actual = (int)testInstances.instance(i).classValue();
                    if (actual == 1 && pred == 1) {candidateTP = candidateTP + 1;}
                    else if (actual == 0 && pred == 0) {candidateTN = candidateTN + 1;}
                    else if (actual == 0 && pred == 1) {candidateFP = candidateFP + 1;}
                    else if (actual == 1 && pred == 0) {candidateFN = candidateFN + 1;}
                    else {
                        System.out.println("Error! Actual: " + actual + ", Predicted: " + pred);
                        assert(false);
                    }
                    
                    // update groudtruth statistics
                    if (pred == 1 && !correctSet.contains(phrase)) {
                        correctSet.add(phrase);
                        if (tags.contains(' ' + phrase + ' ')) {TP = TP + 1;}
                        else {FP = FP + 1;}
                    }
                   
                }
                
                // print to results file
                try (FileWriter results = new FileWriter(resultsFile, true)) {
                     // candidates statistics
                    int Ntags = tags.split("\\s+").length - 1; // "-1" to correct for extra lenght of 1
                    results.write((curDoc+1) + "\t" + candidateTP + "\t" + candidateTN + "\t" + candidateFP + "\t" + candidateFN + "\t");
                    //int sumPredPositive = candidateTP + candidateFP, sumActualPositive = candidateTP+candidateFN;
                    //results.write(candidateTP + "/" + sumPredPositive + "\t" + candidateTP + "/" + sumActualPositive + "\t");

                    // groundtruth statistics
                    //results.write("Actual Tags\tPredicted Tags\tCandidate Tags\tTP\tNActual\tNPredicted\n");
                    results.write(sample.tags + "\t");
                    for (String s : correctSet) {
                        results.write(s + " ");
                    }
                    results.write("\t");
                    for (String s : candidateSet) {
                        results.write(s + " ");
                    }
                    results.write("\t" + TP + "\t" + Ntags + "\t" + (TP+FP) + "\n"); // precision & recall
                    results.close();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Weka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

    /*public void test(String trainFile, String testFile) {
        try {
            // Read in the train dataset
            BufferedReader trainRaw = new BufferedReader(new FileReader(trainFile));
            Instances trainingInstances = new Instances(trainRaw);
            trainingInstances.setClassIndex(trainingInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)

            // Read in the test dataset
            BufferedReader testRaw = new BufferedReader(new FileReader(testFile));
            Instances testingInstances = new Instances(testRaw);
            testingInstances.setClassIndex(testingInstances.numAttributes()-1); // set the last attribute as the class attribute (IMPORTANT!)

            // checks for classifier
            if (classifier == null) {
                classifier = (Classifier)weka.core.SerializationHelper.read("/some/where/j48.model");
            }
            
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
    }*/