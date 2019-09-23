import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.swing.JTable;

public class Test {

	private static Map<String, Integer> charMap = new HashMap<>();
	private static Map<String, Integer> biCharMap = new HashMap<>();
	private static Map<String, Integer> triCharMap = new HashMap<>();
	private static Map<String, Double> unigramModel = new HashMap<>();
	private static Map<String, Double> bigramModel = new HashMap<>();
	private static Map<String, Double> trigramModel = new HashMap<>();
	private static Map<String, Double> trigramLaplaceModel = new HashMap<>();
	private static Map<String, Double> trigramBackoffModel = new HashMap<>();
	private static Map<String, Double> trigramInterpolationModel = new HashMap<>();
	private static Map<String, Double> emptyTrigramModel = new HashMap<>();

	private static String suffix = "en";
	private static String regex = "";

	private static int count = 0;
	private static int charCount = 0;

	private static JTable table = new JTable();
	private static Map<String, ArrayList<Double>> CompletResult = new HashMap<>();
	
	public static void main(String[] args) throws IOException {
		// set the column name
		// train with english data
		for (int i = 0; i < 3; i++) {
			// reset everything
			charCount = 0;
			charMap = new HashMap<String, Integer>();
			biCharMap = new HashMap<String, Integer>();
			triCharMap = new HashMap<String, Integer>();
			unigramModel = new HashMap<String, Double>();
			bigramModel = new HashMap<String, Double>();
			trigramModel = new HashMap<String, Double>();
			trigramLaplaceModel = new HashMap<String, Double>();
			trigramBackoffModel = new HashMap<String, Double>();
			trigramInterpolationModel = new HashMap<String, Double>();
			emptyTrigramModel = new HashMap<>();

			String filename = "";
			if (i == 0) {
				filename = "training.en";
				regex = "[^\\w.,;:!?]";
				suffix = "en";
			} else if (i == 1) {
				filename = "training.es";
				suffix = "es";
			} else if (i == 2) {
				filename = "training.de";
				suffix = "de";
			}

			// Fill the maps
			File file = new File(filename);
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String token = sc.nextLine();
				// token = token.replaceAll(regex, "");
				fillCharMap(token);
				fillBiCharMap(token);
				fillTriCharMap(token);
			}
			sc.close();

			getUnigramModel();
			getBigramModel();
			formEmtyTrigramModel();
			getTrigramModel();
			getLaplaceTriModel();
			getBackOffTriModel();
			getInterpolationModel();
			
			testProbUnigram();
			testProbBigram();
			
			
			writeUnigram();
			// test("unigram_model.en", 1);
			writeBigram();
			writeTrigram();
			writeLaplaceTrigram();
			writeBackoffTrigram();
			writeInterpolationTrigram();
		}
		System.out.println("English: ");
		test("unigram_model.en", 1);
		test("bigram_model.en", 2);
		test("trigram_model.en", 3);
		test("laplace_trigram_model.en", 3);
		test("backoff_trigram_model.en", 3);
		test("interpolation_trigram_model.en", 3);

		System.out.println("Spanish: ");
		test("unigram_model.es", 1);
		test("bigram_model.es", 2);
		test("trigram_model.es", 3);
		test("laplace_trigram_model.es", 3);
		test("backoff_trigram_model.es", 3);
		test("interpolation_trigram_model.es", 3);

		System.out.println("Germany: ");
		test("unigram_model.de", 1);
		test("bigram_model.de", 2);
		test("trigram_model.de", 3);
		test("laplace_trigram_model.de", 3);
		test("backoff_trigram_model.de", 3);
		test("interpolation_trigram_model.de", 3);
		
		printResult();
	}

	

	private static void printResult() throws IOException {
		String line = "";
		FileWriter fw = new FileWriter("PP_Result");
		PrintWriter pw = new PrintWriter(fw);
		for (Map.Entry<String, ArrayList<Double>> entry : CompletResult.entrySet()) {
			line = entry.getKey() + " : ";
			for (Double d : entry.getValue()) {
				line += String.format("%.2f", d) + " ";
			}
			System.out.println(line);
			pw.println(line);	
		}
		
	}



	// ================================================================ Test Probability
	// ===============================================================
	private static void testProbUnigram() {
		double totalProb = 0.0;
		for (Map.Entry<String, Double> entry : unigramModel.entrySet()) {
			totalProb += entry.getValue();
		}
		// System.out.println("Unigram Model total Probability: "+totalProb);
	}
	
	private static void testProbBigram() {
		double totalProb = 0.0;
		ArrayList<Double> result = new ArrayList<>();
		for (Map.Entry<String, Double> entry : bigramModel.entrySet()) {
			totalProb= entry.getValue();
			String base = entry.getKey().charAt(0) + "";
			for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
				String base1 = entry1.getKey().charAt(0) + "";
				if (!entry1.equals(entry) && base1.contentEquals(base) ) {
					totalProb += entry1.getValue();
				}
			}
			result.add(totalProb);
			// System.out.println("Bigram Model total Probability: "+totalProb);
		}

	}

	// ================================================================ Smoothing
	// ===============================================================

	private static void getInterpolationModel() {
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramInterpolationModel.put(element.getKey(),0.0);
		}
		//trigramInterpolationModel = emptyTrigramModel;
		for (Map.Entry<String, Double> entry : trigramInterpolationModel.entrySet()) {
			String key = entry.getKey();
			double pro = 0.0;
//			if (key.contentEquals("hZF")) {
//				System.out.println();
//			}
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				pro += (0.33 * triCharMap.get(key) * 1.0 / baseCount);
			}
			if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				pro += (0.33 * biCharMap.get(key.charAt(1) + "" + key.charAt(2)) * 1.0 / baseCount);
			}
			if (charMap.containsKey(key.charAt(2) + "")) {
				pro += (0.33 * unigramModel.get(""+key.charAt(2)));
			}
			trigramInterpolationModel.put(key, pro);
		}
	}

	private static void getBackOffTriModel() {
		// trigramBackoffModel = emptyTrigramModel;
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramBackoffModel.put(element.getKey(),0.0);
		}
		for (Map.Entry<String, Double> entry : trigramBackoffModel.entrySet()) {
			String key = entry.getKey();
//			if (key.contentEquals("har"))
//				System.out.println();
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				double pro = triCharMap.get(key)*1.0 / baseCount;
				trigramBackoffModel.put(key, pro);
			} else if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				double pro = biCharMap.get(key.charAt(1) + "" + key.charAt(2)) * 1.0 / baseCount;
				trigramBackoffModel.put(key, pro);
			} else {
				double pro = charMap.get(key.charAt(2) + "") * 1.0 / charCount;
				trigramBackoffModel.put(key, pro);
			}
		}

	}

	/**
	 * Generate laplace Trigram model
	 */
	private static void getLaplaceTriModel() {
		// trigramLaplaceModel = emptyTrigramModel;
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramLaplaceModel.put(element.getKey(),0.0);
		}
		double pro = 0.0;
		for (Map.Entry<String, Double> entry : trigramLaplaceModel.entrySet()) {
			pro = 0.0;
			String key = entry.getKey();
			if (triCharMap.containsKey(entry.getKey())) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base) + charMap.size();
				pro = (triCharMap.get(entry.getKey()) + 1) * 1.0 / baseCount;
				// trigramLaplaceModel.put(key, pro);
			} else {
				String base = key.charAt(0) + "" + key.charAt(1);
				if (biCharMap.containsKey(base)) {
					int baseCount = biCharMap.get(base) + charMap.size();
					pro = 1.0 / baseCount;
				} else {
					pro = 1.0 / charMap.size();
				}
			}
			trigramLaplaceModel.put(key, pro);
		}

	}

	// ================================================================ Test
	// ===============================================================
	/**
	 * use this method to test
	 * 
	 * @throws FileNotFoundException
	 */
	private static void test(String filename, int gram) throws FileNotFoundException {
		ArrayList<Double> result = new ArrayList<Double>();
		
		// read in the model
		Map<String, Double> testModel = new HashMap<>();
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] token = line.split("\t");
			testModel.put(token[0], Double.valueOf(token[1]));
		}
		scan.close();

		// read in the test file
		File test = new File("test");
		Scanner in = new Scanner(test);
		double logProp = 0.0;
		int totalCountOfLetters = 0;
		int u = 0;
		while (in.hasNextLine()) {
			u++;
			String line = in.nextLine();
			// line = line.replaceAll(regex, "");
			totalCountOfLetters += 1.0 * line.length() / gram;
			double totalProp = 0;

			for (int i = 0; i < line.length() - gram + 1; i += gram) {
				String key = "";
				if (gram == 3) {
					key = Character.toString(line.charAt(i)) + Character.toString(line.charAt(i + 1))
							+ Character.toString(line.charAt(i + 2));
				} else if (gram == 2) {
					key = Character.toString(line.charAt(i)) + Character.toString(line.charAt(i + 1));
				} else if (gram == 1) {
					key = Character.toString(line.charAt(i));
				}
				double v;
				if (testModel.get(key) == null) {
					v = 0;
				} else {
					v = testModel.get(key);
				}
//				if (v == 0.0)
//					System.out.println();
				double log = Math.log(v) / Math.log(2.0);
				totalProp += log;

//				totalProp *= (1.0 / v);
			}
			double sentencePP = Math.pow(2.0, -(totalProp) / (line.length() / gram * 1.0));
			// System.out.println("S" + u + ": " + sentencePP);
			
			result.add(sentencePP);
			logProp += totalProp;
		}
		
		double perplexity = Math.pow(2.0, -(logProp) / totalCountOfLetters);
//		double perplexity = Math.pow(Math.E, Math.log(logProp)/totalCountOfLetters);
 		// System.out.println(perplexity);
		result.add(perplexity);
		CompletResult.put(filename.substring(0,7) + "_" + filename.substring(filename.length()-2), result);
		in.close();
	}

	// ================================================================ Writing
	// ===============================================================

	private static void writeInterpolationTrigram() throws IOException {
		FileWriter fw = new FileWriter("interpolation_trigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramInterpolationModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
	}

	private static void writeBackoffTrigram() throws IOException {
		FileWriter fw = new FileWriter("backoff_trigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramBackoffModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
		
	}

	private static void writeLaplaceTrigram() throws IOException {
		FileWriter fw = new FileWriter("laplace_trigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramLaplaceModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
//			if (element.getKey().equals("har")) {
//				System.out.println();
//			}
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
	}

	private static void writeTrigram() throws IOException {
		FileWriter fw = new FileWriter("trigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
	}

	/**
	 * write the bigram probability to file 'bigram' model
	 * 
	 * @throws IOException
	 */
	private static void writeBigram() throws IOException {
		FileWriter fw = new FileWriter("bigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = bigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
	}

	/**
	 * write the unigram probability to file 'unigram' model
	 * 
	 * @throws IOException
	 */
	private static void writeUnigram() throws IOException {
		FileWriter fw = new FileWriter("unigram_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = unigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
	}

	// ================================================================ Form Model
	// ===============================================================
	private static void getTrigramModel() {
		// trigramModel = emptyTrigramModel;
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramModel.put(element.getKey(),0.0);
		}
		for (Map.Entry<String, Integer> entry : triCharMap.entrySet()) {
			String key = entry.getKey();
			String base = key.charAt(0) + "" + key.charAt(1);
			int baseCount = biCharMap.get(base);
			double pro = entry.getValue() / baseCount;
			trigramModel.put(key, pro);
		}

	}

	/**
	 * fill the trigramcharmap
	 * 
	 * @param token
	 */
	private static void fillTriCharMap(String token) {
		for (int i = 0; i < token.length() - 2; i++) {
			String first = token.charAt(i) + "";
			String second = token.charAt(i + 1) + "";
			String third = token.charAt(i + 2) + "";
			String triChar = first + second + third;
			if (triCharMap.containsKey(triChar)) {
				triCharMap.put(triChar, triCharMap.get(triChar) + 1);
			} else {
				triCharMap.put(triChar, 1);
			}
		}

	}

	/**
	 * use the data from in BicharMap to generated a bigram model
	 */
	private static void getBigramModel() {
		for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				bigramModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}
		for (Map.Entry<String, Integer> entry : biCharMap.entrySet()) {
			String key = entry.getKey();
			String base = String.valueOf(key.charAt(0));
			int baseCount = charMap.get(base);
			double pro = entry.getValue() * 1.0 / baseCount;
			bigramModel.put(key, pro);
		}

	}

	/**
	 * count the number of two chars
	 * 
	 * @param token
	 */
	private static void fillBiCharMap(String token) {
		for (int i = 0; i < token.length() - 1; i++) {
			String first = token.charAt(i) + "";
			String second = token.charAt(i + 1) + "";
			String biChar = first + second;
			if (biCharMap.containsKey(biChar)) {
				biCharMap.put(biChar, biCharMap.get(biChar) + 1);
			} else {
				biCharMap.put(biChar, 1);
			}
		}

	}

	/**
	 * Use the data get from the file to form a unigram mdoel use the count of each
	 * character divided by the total count store the result in a hashmap with the
	 * char as the key
	 */
	private static void getUnigramModel() {
		for (Map.Entry<String, Integer> entry : charMap.entrySet()) {
			String key = entry.getKey();
			double pro = entry.getValue() * 1.0 / charCount;
			unigramModel.put(key, pro);
		}

	}

	private static void fillCharMap(String token) {

		for (int i = 0; i < token.length(); i++) {
			String c = token.charAt(i) + "";
			charCount++;
			if (charMap.containsKey(c)) {
				charMap.put(c, charMap.get(c) + 1);
			} else {
				charMap.put(c, 1);
			}
		}

	}

	private static void formEmtyTrigramModel() {
		for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				emptyTrigramModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}
	}

}
