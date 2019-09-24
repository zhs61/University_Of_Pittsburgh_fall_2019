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
	private static Map<String, Integer> lastTwoLetters = new HashMap<String, Integer>();
	private static Map<String, Integer> lastOneLetters = new HashMap<String, Integer>();
	private static Map<String, Double> improve = new HashMap<String, Double>();
	
	private static String suffix = "en";
	private static String regex = "";

	private static int count = 0;
	private static int charCount = 0;

	// private static JTable table = new JTable();
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
			lastTwoLetters = new HashMap<String, Integer>();
			lastOneLetters = new HashMap<String, Integer>();

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
				getLastTwo(token);
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
			// test
			// getTestModel();
			
			testProbUnigram();
			System.out.println("Test Probability: Unigram_" + suffix + ", done!");
			testProbBigram();
			System.out.println("Test Probability: Bigram_" + suffix + ", done!");
			testProbTrigram(trigramModel);
			System.out.println("Test Probability: Trigram_" + suffix + ", done!");
			testProbTrigram(trigramLaplaceModel);
			System.out.println("Test Probability: Laplace Trigram_" + suffix + ", done!");
			testProbTrigram(trigramBackoffModel);
			System.out.println("Test Probability: Backoff Trigram_" + suffix + ", done!");
//			testProbInterTrigram(trigramInterpolationModel);
//			System.out.println("Test Probability: Interpolation Trigram_" + suffix + ", done!");

			writeUnigram();
			// test("unigram_model.en", 1);
			writeBigram();
			writeTrigram();
			writeLaplaceTrigram();
			writeBackoffTrigram();
			writeInterpolationTrigram();
			// test
			// writeImprove();
		}
		// System.out.println("English: ");
		test("unigram_model.en", 1);
		test("bigram_model.en", 2);
		test("trigram_model.en", 3);
		test("laplace_trigram_model.en", 3);
		test("backoff_trigram_model.en", 3);
		test("interpolation_trigram_model.en", 3);
		// test("test_model.en",3);

		// System.out.println("Spanish: ");
		test("unigram_model.es", 1);
		test("bigram_model.es", 2);
		test("trigram_model.es", 3);
		test("laplace_trigram_model.es", 3);
		test("backoff_trigram_model.es", 3);
		test("interpolation_trigram_model.es", 3);

		// System.out.println("Germany: ");
		test("unigram_model.de", 1);
		test("bigram_model.de", 2);
		test("trigram_model.de", 3);
		test("laplace_trigram_model.de", 3);
		test("backoff_trigram_model.de", 3);
		test("interpolation_trigram_model.de", 3);

		printResult();
	}



// ==========================================================================SandBox ================================================================
	private static void getTestModel() {
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			improve.put(element.getKey(), 0.0);
		}
		double pro = 0.0;
		for (Map.Entry<String, Double> entry : improve.entrySet()) {
			String key = entry.getKey();
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				if (lastTwoLetters.containsKey(base)) {
					baseCount -= lastTwoLetters.get(base);
				}
				pro += 0.66 * triCharMap.get(key) * 1.0 / baseCount;
				if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
					base = "" + key.charAt(1);
					baseCount = charMap.get(base);
					if (lastOneLetters.containsKey(base)) {
						baseCount -= lastOneLetters.get(base);
					}
					pro += 0.33 * biCharMap.get(key.charAt(1) + "" + key.charAt(2)) * 1.0 / baseCount;
				}else {
					pro += 0.33 * charMap.get(key.charAt(2) + "") * 1.0 / charCount;
				}
			
			} else {
				if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
					String base = "" + key.charAt(1);
					int baseCount = charMap.get(base);
					if (lastOneLetters.containsKey(base)) {
						baseCount -= lastOneLetters.get(base);
					}
					pro += 0.66 * biCharMap.get(key.charAt(1) + "" + key.charAt(2)) * 1.0 / baseCount;
					pro += 0.33 * charMap.get(key.charAt(2) + "") * 1.0 / charCount;
				} else {
					pro += charMap.get(key.charAt(2) + "") * 1.0 / charCount;
				}
			}
			improve.put(key, pro);
		}
		
	}
	private static void writeImprove() throws IOException {
		FileWriter fw = new FileWriter("test_model." + suffix);
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = improve.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
		fw.close();
		
	}

	// ================================================================SandBox ==========================================================================
	// ================================================================ Test
	// Probability
	// ===============================================================

//	private static void testProbInterTrigram(Map<String, Double> trigramModel) {
//		double totalProb = 0.0;
//		for (Map.Entry<String, Double> entry : trigramModel.entrySet()) {
//			String key = entry.getKey();
//			totalProb = 0.0;
//			String base = entry.getKey().charAt(0) + "" + entry.getKey().charAt(1);
//			// if the tri chars exist in text, the prob should be 1.0
//			if (triCharMap.containsKey(key)) {
//				for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
//					key = base + entry1.getKey();
//					if (trigramModel.containsKey(key))
//						totalProb += trigramModel.get(key);
//				}
//				if (totalProb < 0.99 &&  totalProb > 1.01)
//					System.out.println("Trigram Model total Probability: " + entry.getKey() + totalProb);
//			} else if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) { // expect prob = 0.666
//				for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
//					key = base + entry1.getKey();
//					if (trigramModel.containsKey(key))
//						totalProb += trigramModel.get(key);
//				}
//				if (totalProb < 0.65 &&  totalProb > 0.67)
//					System.out.println("Trigram Model total Probability: " + entry.getKey() + totalProb);
//			} else { // expect prob = 0.33
//				for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
//					key = base + entry1.getKey();
//					if (trigramModel.containsKey(key))
//						totalProb += trigramModel.get(key);
//				}
//				//if (totalProb < 0.32 &&  totalProb > 0.34)
//					System.out.println("Trigram Model total Probability: " + entry.getKey() + totalProb);
//			}
//		}
//	}

	private static void testProbTrigram(Map<String, Double> trigramModel) {
		double totalProb = 0.0;
		ArrayList<Double> result = new ArrayList<>();
		// Map<String, Integer> dup = new HashMap<String, Integer>();
		for (Map.Entry<String, Double> entry : trigramModel.entrySet()) {
			totalProb = 0.0;
			String base = entry.getKey().charAt(0) + "" + entry.getKey().charAt(1);
			// if (base.contentEquals("pä")) {
			// System.out.println();
			// }
			for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
				String key = base + entry1.getKey();
				if (trigramModel.containsKey(key))
					totalProb += trigramModel.get(key);
			}
			// if (dup.containsKey(base))
			// continue;
			// dup.put(base, 1);
			// for (Map.Entry<String, Double> entry1 : trigramModel.entrySet()) {
			// String base1 = entry1.getKey().charAt(0) + "" + entry1.getKey().charAt(1);
			// if (!entry1.equals(entry) && base1.contentEquals(base)) {
			// totalProb += entry1.getValue();
			// }
			// }
			if (totalProb < 0.99) {
				System.out.println();
			}

			result.add(totalProb);
			if (totalProb < 0.99 && totalProb > 1.01)
				System.out.println("Trigram Model total Probability: " + entry.getKey() + totalProb);
		}
	}

	private static void testProbUnigram() {
		double totalProb = 0.0;
		for (Map.Entry<String, Double> entry : unigramModel.entrySet()) {
			totalProb += entry.getValue();
		}
		if (totalProb < 0.99 && totalProb > 1.01)
			System.out.println("Unigram Model total Probability: " + totalProb);
	}

	private static void testProbBigram() {
		double totalProb = 0.0;
		ArrayList<Double> result = new ArrayList<>();
		for (Map.Entry<String, Double> entry : bigramModel.entrySet()) {
			totalProb = entry.getValue();
			String base = entry.getKey().charAt(0) + "";
			for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
				String base1 = entry1.getKey().charAt(0) + "";
				if (!entry1.equals(entry) && base1.contentEquals(base)) {
					totalProb += entry1.getValue();
				}
			}
			result.add(totalProb);
			if (totalProb < 0.99 && totalProb > 1.01)
				System.out.println("Bigram Model total Probability: " + entry.getKey() + totalProb);
		}

	}

	// ================================================================ Smoothing
	// ===============================================================

	private static void getInterpolationModel() {
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramInterpolationModel.put(element.getKey(), 0.0);
		}
		// trigramInterpolationModel = emptyTrigramModel;
		for (Map.Entry<String, Double> entry : trigramInterpolationModel.entrySet()) {
			String key = entry.getKey();
			double pro = 0.0;
			// String tt = key.charAt(0) + "" + key.charAt(1);
			// if (tt.contentEquals("Jä")) {
			// System.out.println();
			// }
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				if (lastTwoLetters.containsKey(base)) {
					baseCount -= lastTwoLetters.get(base);
				}
				pro += ((1.0 / 3.0) * triCharMap.get(key) * 1.0 / baseCount);
			}
			if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				if (lastOneLetters.containsKey(base)) {
					baseCount -= lastOneLetters.get(base);
				}
				pro += ((1.0 / 3.0) * biCharMap.get(key.charAt(1) + "" + key.charAt(2)) * 1.0 / baseCount);
			}
			if (charMap.containsKey(key.charAt(2) + "")) {
				pro += ((1.0 / 3.0) * charMap.get("" + key.charAt(2)) * 1.0 / charCount);
			}
			// pro = trigramModel.get(key) * 1.0/3.0 + bigramModel.get(key.charAt(1) + "" +
			// key.charAt(2)) * 1.0/3.0 + unigramModel.get(key.charAt(2)+"") * 1.0/3.0;
			trigramInterpolationModel.put(key, pro);
		}
	}

	private static void getBackOffTriModel() {
		// trigramBackoffModel = emptyTrigramModel;
		for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
			trigramBackoffModel.put(element.getKey(), 0.0);
		}
		for (Map.Entry<String, Double> entry : trigramBackoffModel.entrySet()) {
			String key = entry.getKey();
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				if (lastTwoLetters.containsKey(base)) {
					baseCount -= lastTwoLetters.get(base);
				}
				double pro = triCharMap.get(key) * 1.0 / baseCount;
				trigramBackoffModel.put(key, pro);
			} else if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				if (lastOneLetters.containsKey(base)) {
					baseCount -= lastOneLetters.get(base);
				}
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
			trigramLaplaceModel.put(element.getKey(), 0.0);
		}
		double pro = 0.0;
		for (Map.Entry<String, Double> entry : trigramLaplaceModel.entrySet()) {
			pro = 0.0;
			String key = entry.getKey();
			// if (key.contentEquals("a. ")) {
			// System.out.println();
			// }
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base) + charMap.size();
				if (lastTwoLetters.containsKey(base)) {
					baseCount -= lastTwoLetters.get(base);
				}
				pro = (triCharMap.get(key) + 1) * 1.0 / baseCount;
				// trigramLaplaceModel.put(key, pro);
			} else {
				String base = key.charAt(0) + "" + key.charAt(1);
				if (biCharMap.containsKey(base)) {
					int baseCount = biCharMap.get(base) + charMap.size();
					if (lastTwoLetters.containsKey(base)) {
						baseCount -= lastTwoLetters.get(base);
					}
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
		double sentenceP = 0.0;
		while (in.hasNextLine()) {
			u++;
			String line = in.nextLine();
			// line = line.replaceAll(regex, "");
			totalCountOfLetters += 1.0 * line.length() / gram;
			double totalProp = 0;

			for (int i = 0; i < line.length() - gram + 1; i++) {
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
				double log = Math.log(v) / Math.log(2.0);
				totalProp += log;
			}
			double sentencePP = Math.pow(2.0, -(totalProp) / (line.length()* 1.0));
			sentenceP += sentencePP;
			// System.out.println("S" + u + ": " + sentencePP);
			// logProp += totalProp;
			result.add(sentencePP);
			// logProp += sentencePP;
		}
		double perplexity = sentenceP / u;
		// double perplexity = Math.pow(2.0, -(logProp) / totalCountOfLetters);
		System.out.println(filename + "_mean: " + perplexity);
		result.add(perplexity);
		CompletResult.put(filename.substring(0, 7) + "_" + filename.substring(filename.length() - 2), result);
		in.close();
	}

	// ================================================================ Form Model
	// ===============================================================
	private static void getTrigramModel() {
		// trigramModel = emptyTrigramModel;
		// for (Map.Entry<String, Double> element : emptyTrigramModel.entrySet()) {
		// trigramModel.put(element.getKey(),0.0);
		// }
		for (Map.Entry<String, Integer> entry : triCharMap.entrySet()) {
			String key = entry.getKey();

			String base = key.charAt(0) + "" + key.charAt(1);
			// if (base.contentEquals("e)")) {
			// System.out.println();
			// }
			int baseCount = biCharMap.get(base);
			if (lastTwoLetters.containsKey(base)) {
				baseCount -= lastTwoLetters.get(base);
			}
			double pro = entry.getValue() * 1.0 / baseCount;
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
		// for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
		// for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
		// bigramModel.put(entry1.getKey() + entry2.getKey(), 0.0);
		// }
		// }
		for (Map.Entry<String, Integer> entry : biCharMap.entrySet()) {
			String key = entry.getKey();
			String base = String.valueOf(key.charAt(0));
			int baseCount = charMap.get(base);
			if (lastOneLetters.containsKey(base)) {
				baseCount -= lastOneLetters.get(base);
			}
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
		// for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
		// for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
		// emptyTrigramModel.put(entry1.getKey() + entry2.getKey(), 0.0);
		// }
		// }
		for (Map.Entry<String, Integer> entry1 : charMap.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				for (Map.Entry<String, Integer> entry3 : charMap.entrySet()) {
					emptyTrigramModel.put(entry1.getKey() + entry2.getKey() + entry3.getKey(), 0.0);
				}
			}
		}
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
			// if (element.getKey().equals("har")) {
			// System.out.println();
			// }
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
		pw.close();
	}

	// ==============================================================helper method
	// ==============================================================

	private static void getLastTwo(String token) {
		if (token.length() >= 2) {
			String lastTwo = token.substring(token.length() - 2);
			if (lastTwoLetters.containsKey(lastTwo)) {
				lastTwoLetters.put(lastTwo, lastTwoLetters.get(lastTwo) + 1);
			} else {
				lastTwoLetters.put(lastTwo, 1);
			}
		}
		if (token.length() >= 1) {
			String lastOne = token.substring(token.length() - 1);
			if (lastOneLetters.containsKey(lastOne)) {
				lastOneLetters.put(lastOne, lastOneLetters.get(lastOne) + 1);
			} else {
				lastOneLetters.put(lastOne, 1);
			}
		}
	}
}
