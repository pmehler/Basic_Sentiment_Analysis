/* Improved Basic Sentiment Analysis
 * SentAnalysisBest.java
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

public class SentAnalysisBest {

	final static String TRAINFOLDERNAME = "train";

	final static File TRAINFOLDER = new File(TRAINFOLDERNAME);

	//count number of occurences of words
	private static Map<String, Integer> negativeCount = new HashMap<>();
	private static Map<String, Integer> positiveCount = new HashMap<>();

	//hashed count of # reviews with different character counts
	private static int[] pos_text_len = new int[500];
	private static int[] neg_text_len = new int[500];

	//count of num pos/neg reviews
	private static double num_positive_reviews = 0;
	private static double num_negative_reviews = 0;

	//count number of reviews with a certain number of exclamation marks
	private static int[] numExclamationMarksPos = new int[500];
	private static int[] numExclamationMarksNeg = new int[500];

	//count number of reviews with a certain number of capital letters
	private static int[] numCapsPos = new int[500];
	private static int[] numCapsNeg = new int[500];


	public static void main(String[] args) throws IOException
	{
		ArrayList<String> files = readFiles(TRAINFOLDER);

		train(files);
		//if command line argument is "evaluate", runs evaluation mode
		if (args.length==1 && args[0].equals("evaluate")){
			evaluate();
		}
		else{//otherwise, runs interactive mode
			@SuppressWarnings("resource")
			Scanner scan = new Scanner(System.in);
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
 * analyzer on them by counting occurences of words, exclamation marks,
 * capital letters, and character counts.
 */
	public static void train(ArrayList<String> files) throws FileNotFoundException
	{
		Map<String, Integer> m;
		char rating;
		Scanner scan;
		int[] exclamationsArr;
		int[] numCapsArr;

		//process each review
		for (String filename: files){
			rating = filename.charAt(filename.indexOf('-') + 1);
			if (rating=='1'){
				//set global vars and increment count for negative review
				num_negative_reviews++;
				m = negativeCount;
				exclamationsArr = numExclamationMarksNeg;
				numCapsArr = numCapsNeg;
			}	else {
				//set global vars and increment count for positive review
				num_positive_reviews++;
				m = positiveCount;
				exclamationsArr = numExclamationMarksPos;
				numCapsArr = numCapsPos;
			}
			//scan each file's text
			scan = new Scanner(new File(TRAINFOLDERNAME + "/" + filename));
			while (scan.hasNext()){
				String text = scan.nextLine();

				//count number of exclamation marks and increment count in global array
				String exclamationsOnly = text.replaceAll("[^!]+", "");
				if (exclamationsOnly.length()<500){
					exclamationsArr[exclamationsOnly.length()]++;
				}

				//count number of capital letters and increment count in global array
				String capsOnly = text.replaceAll("[^A-Z]+", "");
				if(capsOnly.length() < 500){
					numCapsArr[capsOnly.length()]++;
				}

				//count number of characters and increment in global array
				int text_length = text.length();
				//hash to get index in array
				text_length = (int) (text_length/10);
				if (text_length>500) {
					text_length = 499;
				}
				if (rating=='1'){
					neg_text_len[text_length]++;
				}
				else {
					pos_text_len[text_length]++;
				}

				//count word occurences and save in global array
				text = text.toLowerCase();
				String [] words = text.split("[ )('\"/\\:;@,!?.-]+");
				for(String s: words){
					m.put(s, m.getOrDefault(s, 0) + 1);
				}
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

		//probability a review is positive or negative
		double prob_positive = num_positive_reviews / (num_positive_reviews+num_negative_reviews);
		double prob_negative = num_negative_reviews / (num_positive_reviews+num_negative_reviews);

		//set up feature: character count
		int text_length_hash = (int) (text.length()/10);
		if (text_length_hash>500) {
			text_length_hash = 499;
		}

		//set up feature: number of exclamation marks
		int numExclamationMarks = text.replaceAll("[^!]+", "").length();
		if (numExclamationMarks > 500){
			numExclamationMarks = 499;
		}

		//set up feature: number of capital letters
		int numCaps = text.replaceAll("[^A-Z]+", "").length();
		if (numCaps > 500){
			numCaps = 499;
		}

		//calculate feature: single word occurences
		text = text.toLowerCase();
		String [] words = text.split("[ )('\"/\\:;@,!?.-]+");
		int numFeatures = words.length + 3;
		double total_num_unique_pos_words = positiveCount.size();
		double total_num_unique_neg_words = negativeCount.size();
		for(int i=0; i<words.length; i++){
			double prob_word_pos = (positiveCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_pos_words+(smoothing_coef*numFeatures));
			pos_sum = pos_sum + (Math.log(prob_word_pos));
			double prob_word_neg = (negativeCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_neg_words+(smoothing_coef*numFeatures));
			neg_sum = neg_sum + (Math.log(prob_word_neg));
		}

		//calculate feature character count
		double pos_len_prob = (pos_text_len[text_length_hash]+smoothing_coef)/(num_positive_reviews + (smoothing_coef * numFeatures));
		pos_sum += Math.log(pos_len_prob);
		double neg_len_prob = (neg_text_len[text_length_hash]+smoothing_coef)/(num_negative_reviews + (smoothing_coef * numFeatures));
		neg_sum += Math.log(neg_len_prob);

		//calculate exclamation mark feature
		double prob_exclamations_pos = (numExclamationMarksPos[numExclamationMarks] + smoothing_coef)/(num_positive_reviews + (smoothing_coef * numFeatures));
		pos_sum += Math.log(prob_exclamations_pos);
		double prob_exclamations_neg = (numExclamationMarksNeg[numExclamationMarks] + smoothing_coef)/(num_negative_reviews + (smoothing_coef * numFeatures));
		neg_sum += Math.log(prob_exclamations_neg);

		//calculate capital letter feature
		double prob_caps_pos = (numCapsPos[numCaps] + smoothing_coef)/(num_positive_reviews + (smoothing_coef * numFeatures));
		pos_sum += Math.log(prob_caps_pos);
		double prob_caps_neg = (numCapsNeg[numCaps] + smoothing_coef)/(num_negative_reviews + (smoothing_coef * numFeatures));
		neg_sum += Math.log(prob_caps_neg);

		//calculate final probabilities
		double prob_text_pos = pos_sum + Math.log(prob_positive);
		prob_text_pos = prob_text_pos * 0.503;
		double prob_text_neg = neg_sum + Math.log(prob_negative);
		prob_text_neg = prob_text_neg * 0.497;

		//take maximum likelihood as prediction
		if (prob_text_neg > prob_text_pos){
			return "negative";
		} else {
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
		for (String filename: filesToClassify){
				real_rating = filename.charAt(filename.indexOf('-') + 1);
				scan = new Scanner(new File(foldername + "/" + filename));
				while (scan.hasNext()){
					String classified = classify(scan.nextLine());
					if (classified=="negative"){
							totalClassifiedNegative++;
							if (real_rating=='1'){
								numCorrectNegative++;
							}
					}
					else if (classified=="positive"){
							totalClassifiedPositive++;
							if (real_rating=='5'){
								numCorrectPositive++;
							}
					}
				}
			}
			//total number of reviews classified correctly
			double totalCorrect = numCorrectPositive + numCorrectNegative;
			//total number of reviews classified
			double totalClassified = totalClassifiedPositive + totalClassifiedNegative;

			double posPrecision = ((double)numCorrectPositive/totalClassifiedPositive)*100;
			double negPrecision = ((double)numCorrectNegative/totalClassifiedNegative)*100;
			double accuracy = (double)totalCorrect/totalClassified * 100;

			//format and print as percentages
			System.out.println("Accuracy: " + Math.floor(accuracy * 100)/100 + "%");
			System.out.println("Precision (Positive): " + Math.floor(posPrecision*100)/100 + "%");
			System.out.println("Precision (Negative): " + Math.floor(negPrecision*100)/100 + "%");
	}
}
