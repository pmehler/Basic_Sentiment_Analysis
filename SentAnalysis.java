/* Basic Sentiment Analysis
 * SentAnalysis.java
 * Emma Neary and Peter Mehler
 * All group members were present and contributing during all work on this project.
 * We did not give or recieve unauthorized aid on this assignment.
 */

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SentAnalysis {

	final static String TRAINFOLDERNAME = "train";

	final static File TRAINFOLDER = new File(TRAINFOLDERNAME);

	// Hashmaps containing the counts of every word
	private static Map<String, Integer> negativeCount = new HashMap<>();
	private static Map<String, Integer> positiveCount = new HashMap<>();

	private static double num_positive_reviews = 0;
	private static double num_negative_reviews = 0;

	public static void main(String[] args) throws IOException
	{
		ArrayList<String> files = readFiles(TRAINFOLDER);

		train(files);
		//if command line argument is "evaluate", runs evaluation mode
		if (args.length==1 && args[0].equals("evaluate")){
			evaluate();
		}
		else{//otherwise, runs interactive mode
			@SuppressWarnings("resource")Scanner scan = new Scanner(System.in);
			System.out.print("Text to classify>> ");
			String textToClassify = scan.nextLine();
			System.out.println("Result: "+classify(textToClassify));
		}

	}

	/*
	 * Takes as parameter the name of a folder and returns a list of filenames (Strings)
	 * in the folder.
	 */
	public static ArrayList<String> readFiles(File folder){

		System.out.println("Populating list of files");

		//List to store filenames in folder
		ArrayList<String> filelist = new ArrayList<String>();

		for (File fileEntry : folder.listFiles()) {
	        String filename = fileEntry.getName();
	        filelist.add(filename);
		}

		return filelist;
	}


	/*
	 * Takes as a parameter a list of filenames and trains Naive Bayes sentiment
	 * analyzer on them by counting occurences of words.
	 */
	public static void train(ArrayList<String> files) throws FileNotFoundException
	{
		Map<String, Integer> m;
		char rating;
		Scanner scan;

		//loop through filenames
		for (String filename: files){
			rating = filename.charAt(filename.indexOf('-') + 1);
			if (rating=='1'){	//negative rating
				num_negative_reviews++;
				m = negativeCount;
			} else {	//positive rating
				num_positive_reviews++;
				m = positiveCount;
			}
			scan = new Scanner(new File(TRAINFOLDERNAME + "/" + filename));
			// extract words without punctuation
			scan.useDelimiter(("[ )('\"/\\:;@,!?.-]+"));
			String s;
			//Loop through words and add to hashmap
			while (scan.hasNext()){
				s = scan.next();
				s = s.toLowerCase();
				m.put(s, m.getOrDefault(s, 0) + 1);
			}
		scan.close();
		}
	}


	/*
	 * Classifier: Classifies the input text (type: String) as positive or negative
	 */
	public static String classify(String text)
	{
		double smoothing_coef = 0.07;

		//store sum of features probability
		double pos_sum = 0;
		double neg_sum = 0;

		text = text.toLowerCase();
		String [] words = text.split("[ )('\"/\\:;@,!?.-]+");
		double total_num_unique_pos_words = positiveCount.size();
		double total_num_unique_neg_words = negativeCount.size();

		//loop through words and calculate the combined probabilities of each
		//word being in a pos/neg review
		for(int i=0; i<words.length; i++){
			double prob_word_pos = (positiveCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_pos_words+(smoothing_coef*words.length));
			pos_sum = pos_sum + (Math.log(prob_word_pos));
			double prob_word_neg = (negativeCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_neg_words+(smoothing_coef*words.length));
			neg_sum = neg_sum + (Math.log(prob_word_neg));
		}

		//calculate final probabilities
		double prob_positive = num_positive_reviews / (num_positive_reviews+num_negative_reviews);
		double prob_negative = num_negative_reviews / (num_positive_reviews+num_negative_reviews);

		double prob_text_pos = pos_sum + Math.log(prob_positive);
		double prob_text_neg = neg_sum + Math.log(prob_negative);

		//maximum likelihood
		if (prob_text_neg > prob_text_pos){
			return "negative";
		}
		else{
			return "positive";
		}
	}


	/*
	* Runs sentiment analyzer on test files and prints
	* accuracy and precision of results
	*/
	public static void evaluate() throws FileNotFoundException
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);

		System.out.print("Enter folder name of files to classify: ");
		String foldername = scan.nextLine();
		File folder = new File(foldername);

		ArrayList<String> filesToClassify = readFiles(folder);

		int totalClassifiedPositive = 0; //number of reviews classified positive
		int numCorrectPositive = 0; //number of positive reviews classified positive
		int totalClassifiedNegative = 0; //number of reviews classified negative
		int numCorrectNegative = 0; //number of negative reviews classified negative
		char real_rating;

		//Loop through files and evaluate classification of extracted text
		for (String filename: filesToClassify){
				real_rating = filename.charAt(filename.indexOf('-') + 1);
				scan = new Scanner(new File(foldername + "/" + filename));
				while (scan.hasNext()){
					String classified = classify(scan.nextLine());
					if (classified=="negative"){
						totalClassifiedNegative++;
						if (real_rating=='1'){//correct!
							numCorrectNegative++;
						}
					}
					else if (classified=="positive"){
						totalClassifiedPositive++;
						if (real_rating=='5'){//correct!
							numCorrectPositive++;
						}
					}
				}
			}
			//total number of reviews classified correctly
			double totalCorrect = numCorrectPositive + numCorrectNegative;
			//total number of reviews classified
			double totalClassified = totalClassifiedPositive + totalClassifiedNegative;

			double posPrecision = ((double)numCorrectPositive/(double)totalClassifiedPositive)*100;
			double negPrecision = ((double)numCorrectNegative/(double)totalClassifiedNegative)*100;
			double accuracy = (double)totalCorrect/totalClassified * 100;

			//format and print as percentages
			System.out.println("Accuracy: " + Math.floor(accuracy * 100)/100 + "%");
			System.out.println("Precision (Positive): " + Math.floor(posPrecision*100)/100 + "%");
			System.out.println("Precision (Negative): " + Math.floor(negPrecision*100)/100 + "%");
	}
}
