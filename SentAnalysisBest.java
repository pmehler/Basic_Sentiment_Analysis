/*
 * Please see submission instructions for what to write here.
 */

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SentAnalysisBest {

	final static String TRAINFOLDERNAME = "train";

	final static File TRAINFOLDER = new File("train");

	private static Map<String, Integer> negativeCount = new HashMap<>();
	private static Map<String, Integer> positiveCount = new HashMap<>();

	private static int[] pos_text_len = new int[500];
	private static int[] neg_text_len = new int[500];

	private static double num_positive_reviews = 0;
	private static double num_negative_reviews = 0;

	private static double total_num_pos_words = 0;
	private static double total_num_neg_words = 0;

	private static int[] numExclamationMarksPos = new int[500];
	private static int[] numExclamationMarksNeg = new int[500];

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
			while (!textToClassify.equals("quit")){
				System.out.print("Text to classify>> ");
				textToClassify = scan.nextLine();
				System.out.println("Result: "+classify(textToClassify));
			}

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


	public static void train(ArrayList<String> files) throws FileNotFoundException
	{
		Map<String, Integer> m;
		char rating;
		Scanner scan;
		int[] exclamations;
		int[] numCapsArr;

		for (String filename: files){
			rating = filename.charAt(filename.indexOf('-') + 1);
			if (rating=='1'){
				num_negative_reviews++;
				m = negativeCount;
				exclamations = numExclamationMarksNeg;
				numCapsArr = numCapsNeg;
			}
			else {
				num_positive_reviews++;
				m = positiveCount;
				exclamations = numExclamationMarksPos;
				numCapsArr = numCapsPos;
			}
			scan = new Scanner(new File(TRAINFOLDERNAME + "/" + filename));

			while (scan.hasNext()){
				String text = scan.nextLine();
				String capsOnly = text.replaceAll("[^A-Z]+", "");
				String exclamationsOnly = text.replaceAll("[^!]+", "");
				int numExclamationsinReview = exclamationsOnly.length();
				if (numExclamationsinReview<500){
					exclamations[numExclamationsinReview]++;
				}
				if(capsOnly.length() < 500){
					numCapsArr[capsOnly.length()]++;
				}
				int text_length = text.length();
				//hash
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
		String result="";

		double smoothing_coef = 0.001;

		int text_length_hash = (int) (text.length()/10);
		if (text_length_hash>500) {
			text_length_hash = 499;
		}
		double pos_len_prob = (pos_text_len[text_length_hash]+smoothing_coef)/num_positive_reviews;
		double neg_len_prob = (neg_text_len[text_length_hash]+smoothing_coef)/num_negative_reviews;

		double pos_sum = 0;
		double neg_sum = 0;

		int numCaps = text.replaceAll("[^A-Z]+", "").length();
		int numExclamationMarks = text.replaceAll("[^!]+", "").length();
		text = text.toLowerCase();
		String [] words = text.split("[ )('\"/\\:;@,!?.-]+");

		double total_num_unique_pos_words = positiveCount.size();
		double total_num_unique_neg_words = negativeCount.size();

		double prob_positive = num_positive_reviews / (num_positive_reviews+num_negative_reviews);
		for(int i=0; i<words.length; i++){
			double prob_word_pos = (positiveCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_pos_words+(smoothing_coef*words.length));
			pos_sum = pos_sum + (Math.log(prob_word_pos));
		}

		int numExclamCount = 0;
		if (numExclamationMarks < 500){
			numExclamCount = numExclamationMarksPos[numExclamationMarks];
		}

		double prob_exclamations_pos = (numExclamCount + smoothing_coef)/(num_positive_reviews + (smoothing_coef * words.length));
		
		int numCapsCount = 0;
		if (numCaps < 500){
			numCapsCount = numCapsPos[numCaps];
		}
		double prob_caps_pos = (numCapsCount + smoothing_coef)/(num_positive_reviews + (smoothing_coef * words.length));

		double prob_text_pos = pos_sum + Math.log(prob_positive);
		prob_text_pos = prob_text_pos*0.503;

		// New Feature
		prob_text_pos += Math.log(pos_len_prob);
		prob_text_pos += Math.log(prob_exclamations_pos);
		prob_text_pos += Math.log(prob_caps_pos);

		double prob_negative = num_negative_reviews / (num_positive_reviews+num_negative_reviews);
		for(int i=0; i<words.length; i++){
			double prob_word_neg = (negativeCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_neg_words+(smoothing_coef*words.length));
			neg_sum = neg_sum + (Math.log(prob_word_neg));
		}

		double prob_text_neg = neg_sum + Math.log(prob_negative);
		prob_text_neg = prob_text_neg*0.497;


		numExclamCount = 0;
		if (numExclamationMarks < 500){
			numExclamCount = numExclamationMarksNeg[numExclamationMarks];
		}
		double prob_exclamations_neg = (numExclamCount + smoothing_coef)/(num_negative_reviews + (smoothing_coef * words.length));
		numCapsCount = 0;
		if (numCaps < 500){
			numCapsCount = numCapsNeg[numCaps];
		}
		double prob_caps_neg = (numCapsCount + smoothing_coef)/(num_negative_reviews + (smoothing_coef * words.length));

		// New Feature
		prob_text_neg += Math.log(neg_len_prob);
		prob_text_neg += Math.log(prob_exclamations_neg);
		prob_text_neg += Math.log(prob_caps_neg);

		if (prob_text_neg > prob_text_pos){
			result = "negative";
		}
		else{
			result = "positive";
		}
		return result;
	}


	public static void evaluate() throws FileNotFoundException
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);

		System.out.print("Enter folder name of files to classify: ");
		String foldername = scan.nextLine();
		File folder = new File(foldername);

		ArrayList<String> filesToClassify = readFiles(folder);

		int totalClassifiedPositive = 0;
		int numCorrectPositive = 0;
		int totalClassifiedNegative = 0;
		int numCorrectNegative = 0;
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

			double totalCorrect = numCorrectPositive + numCorrectNegative;
			double totalClassified = totalClassifiedPositive + totalClassifiedNegative;
			System.out.println("Total classified pos, neg: " + totalClassifiedPositive + " " +totalClassifiedNegative);
			System.out.println("num correct pos, neg: " + numCorrectPositive + " " + numCorrectNegative);
			System.out.println("Total correct, total classified: " +totalCorrect + " " + totalClassified);

			System.out.println("Accuracy: " + (totalCorrect/totalClassified) * 100);
			System.out.println("Precision (Positive): " + ((double)numCorrectPositive/(double)totalClassifiedPositive)*100);
			System.out.println("Precision (Negative): " + ((double)numCorrectNegative/(double)totalClassifiedNegative)*100);
	}
}
