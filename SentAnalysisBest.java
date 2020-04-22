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

	private static int[] pos_text_len = new int[1000];
	private static int[] neg_text_len = new int[1000];

	private static double num_positive_reviews = 0;
	private static double num_negative_reviews = 0;

	private static double total_num_pos_words = 0;
	private static double total_num_neg_words = 0;


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

		/*
		for (String fileEntry : filelist) {
	        System.out.println(fileEntry);
		}

		System.out.println(filelist.size());
		*/


		return filelist;
	}




	/*
	 * TO DO
	 * Trainer: Reads text from data files in folder datafolder and stores counts
	 * to be used to compute probabilities for the Bayesian formula.
	 * You may modify the method header (return type, parameters) as you see fit.
	 */
	public static void train(ArrayList<String> files) throws FileNotFoundException
	{
			Map<String, Integer> m;
			char rating;
			Scanner scan;

			for (String filename: files){
					rating = filename.charAt(filename.indexOf('-') + 1);
					if (rating=='1'){
						num_negative_reviews++;
						m = negativeCount;
					} 
					else {
						num_positive_reviews++;
						m = positiveCount;
					}
					scan = new Scanner(new File(TRAINFOLDERNAME + "/" + filename));
				
					while (scan.hasNext()){
						String text = scan.nextLine();

						int text_length = text.length();
						//hash
						text_length = (int) (text_length/10);
						

						if (rating=='1'){
							neg_text_len[text_length]++;
						} 
						else {
							pos_text_len[text_length]++;
						}

						text = text.toLowerCase();

						String [] words = text.split("[ )('\"/\\:;@,!?.-]+");
						
						for(int i=0; i<words.length; i++){
							m.put(words[i], m.getOrDefault(words[i], 0) + 1);
						}
					}
				scan.close();
			}

			//System.out.println("num pos: " + num_positive_reviews);
			//System.out.println("num neg: " + num_negative_reviews);

/*
			for(int i=0; i<(neg_text_len.length-700); i++){
				System.out.println("bin number: " + i);
				System.out.println("negative text length: " + neg_text_len[i]);	
				System.out.println("positive text length: " + pos_text_len[i]);
				System.out.println();
				}
*/
	}





	/*
	 * Classifier: Classifies the input text (type: String) as positive or negative
	 */
	public static String classify(String text)
	{
		String result="";

		double smoothing_coef = 0.001;

		int text_length_hash = (int) (text.length()/10);
		double pos_len_prob = (pos_text_len[text_length_hash]+smoothing_coef)/num_positive_reviews;
		double neg_len_prob = (neg_text_len[text_length_hash]+smoothing_coef)/num_negative_reviews;

		//System.out.println("positive length hash: " + pos_text_len[text_length_hash]);
		//System.out.println("negative length hash: " + neg_text_len[text_length_hash]);

		double pos_sum = 0;
		double neg_sum = 0;

		text = text.toLowerCase();

		String [] words = text.split("[ )('\"/\\:;@,!?.-]+");


		/*
			for(int i=0; i<words.length; i++){
			System.out.println(words[i]);
		}*/
		double total_num_unique_pos_words = positiveCount.size();
		double total_num_unique_neg_words = negativeCount.size();


		double prob_positive = num_positive_reviews / (num_positive_reviews+num_negative_reviews);
		for(int i=0; i<words.length; i++){
			double prob_word_pos = (positiveCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_pos_words+(smoothing_coef*words.length));
			pos_sum = pos_sum + (Math.log(prob_word_pos));

/*
			System.out.println("word: " + words[i]);
			System.out.println("occurences count: " + positiveCount.getOrDefault(words[i],0));

			System.out.println("prob word pos: " + prob_word_pos);
			System.out.println("num unique pos words" + total_num_unique_pos_words);
			System.out.println("length of words" + words.length);
			System.out.println("log: " + (Math.log(prob_word_pos) / Math.log(2)));
			System.out.println("product: " + pos_sum);

			System.out.println();

*/
		}
		double prob_text_pos = pos_sum + Math.log(prob_positive);

		// New Feature
		prob_text_pos += Math.log(pos_len_prob);
		//System.out.println("positive log: " + Math.log(pos_len_prob));

		double prob_negative = num_negative_reviews / (num_positive_reviews+num_negative_reviews);
		for(int i=0; i<words.length; i++){
			double prob_word_neg = (negativeCount.getOrDefault(words[i],0)+smoothing_coef)/(total_num_unique_neg_words+(smoothing_coef*words.length));
			neg_sum = neg_sum + (Math.log(prob_word_neg));

/*
			System.out.println("word: " + words[i]);
			System.out.println("occurences count: " + negativeCount.getOrDefault(words[i],0));
			System.out.println("num unique neg words" + total_num_unique_neg_words);
			System.out.println("prob word neg: " + prob_word_neg);
			System.out.println("length of words" + words.length);


			System.out.println("log: " + (Math.log(prob_word_neg) / Math.log(2)));
			System.out.println("product: " + neg_sum);

			System.out.println();
*/
		}
		double prob_text_neg = neg_sum + Math.log(prob_negative);
		//System.out.println("negative log: " + Math.log(neg_len_prob));

		// New Feature
		prob_text_neg += Math.log(neg_len_prob);

		/*
		System.out.println("prob_negative: " + prob_negative);
		System.out.println("prob_positive: " + prob_positive);
		*/
		if (prob_text_neg > prob_text_pos){
			result = "negative";
		}
		else{
			result = "positive";
		}
		/*
		System.out.println("prob pos: " + prob_text_pos);
		System.out.println("prob neg: " + prob_text_neg);
		*/
		return result;
	}




	/*
	 * TO DO
	 * Classifier: Classifies all of the files in the input folder (type: File) as positive or negative
	 * You may modify the method header (return type, parameters) as you like.
	 */
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

/*

• Accuracy: number correctly classified / total classified

• Precision: number correctly classified as positive / total classified as positive
	       number correctly classified as negative / total classified as negative

Total classified pos, neg: 15642 11540
num correct pos, neg: 11718 9259
Total correct, total classified: 20977.0 27182.0
Accuracy: 77.17239349569567
Precision (Positive): 74.91369390103567
Precision (Negative): 80.23396880415945
*/

	}
}
