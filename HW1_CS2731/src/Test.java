import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

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
	
	private static String suffix = "en";

	private static int charCount = 0;

	public static void main(String[] args) throws IOException {
		
		
		
		// train with english data
		for (int i = 0; i < 3; i++) {
			
			// reset everything
			charCount = 0;
			charMap = new HashMap<>();
			biCharMap = new HashMap<>();
			triCharMap = new HashMap<>();
			unigramModel = new HashMap<>();
			bigramModel = new HashMap<>();
			trigramModel = new HashMap<>();
			trigramLaplaceModel = new HashMap<>();
			trigramBackoffModel = new HashMap<>();
			trigramInterpolationModel = new HashMap<>();
			
			
			String filename = "";
			if (i == 0) {
				filename = "training.en";
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
				fillCharMap(token);
				fillBiCharMap(token);
				fillTriCharMap(token);
			}
			sc.close();
			
			getUnigramModel();
			getBigramModel();
			getTrigramModel();
			getLaplaceTriModel();
			getBackOffTriModel();
			getInterpolationModel();

			writeUnigram();
			writeBigram();
			writeTrigram();
			writeLaplaceTrigram();
			writeBackoffTrigram();
			writeInterpolationTrigram();
		}
		System.out.println("English: ");
		test("unigram_model_en.txt", 1);
		test("bigram_model_en.txt", 2);
		test("trigram_model_en.txt", 3);
		test("laplace_trigram_model_en.txt", 3);
		test("backoff_trigram_model_en.txt", 3);
		test("interpolation_trigram_model_en.txt", 3);

		System.out.println("Spanish: ");
		test("unigram_model_es.txt", 1);
		test("bigram_model_es.txt", 2);
		test("trigram_model_es.txt", 3);
		test("laplace_trigram_model_es.txt", 3);
		test("backoff_trigram_model_es.txt", 3);
		test("interpolation_trigram_model_es.txt", 3);

		System.out.println("Germany: ");
		test("unigram_model_de.txt", 1);
		test("bigram_model_de.txt", 2);
		test("trigram_model_de.txt", 3);
		test("laplace_trigram_model_de.txt", 3);
		test("backoff_trigram_model_de.txt", 3);
		test("interpolation_trigram_model_de.txt", 3);
	}

	

	//================================================================ Smoothing ===============================================================
	
	private static void getInterpolationModel() {
		for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				trigramInterpolationModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}
		for (Map.Entry<String, Double> entry : trigramInterpolationModel.entrySet()) {
			String key = entry.getKey();
			double pro = 0.0;
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				pro += (0.33 * triCharMap.get(key) / baseCount);
			} 
			if (biCharMap.containsKey(key.charAt(1) + "" + key.charAt(2))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				pro += (0.33 * biCharMap.get(key.charAt(1) + "" + key.charAt(2)) / baseCount);
			}
			if (charMap.containsKey(key.charAt(2)+"")) {
				pro += (0.33 *charMap.get(key.charAt(2)+"") / charCount);	
			}
			trigramInterpolationModel.put(key, pro);
		}
	}
	
	
	private static void getBackOffTriModel() {
		for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				trigramBackoffModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}
		for (Map.Entry<String, Double> entry : trigramBackoffModel.entrySet()) {
			String key = entry.getKey();
			if (triCharMap.containsKey(key)) {
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base);
				double pro = triCharMap.get(key)* 1.0 / baseCount;
				trigramBackoffModel.put(key, pro);
			} else if (biCharMap.containsKey(key.charAt(0) + "" + key.charAt(1))) {
				String base = "" + key.charAt(1);
				int baseCount = charMap.get(base);
				double pro = biCharMap.get(key.charAt(0) + "" + key.charAt(1)) * 1.0 / baseCount;
				trigramBackoffModel.put(key, pro);
			} else {
				double pro = charMap.get(key.charAt(2)+"") * 1.0 / charCount;
				trigramBackoffModel.put(key, pro);
			}
		}
		
	}

	/**
	 * Generate laplace Trigram model
	 */
	private static void getLaplaceTriModel() {
		for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				trigramLaplaceModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}

		for (Map.Entry<String, Double> entry : trigramLaplaceModel.entrySet()) {
			if (triCharMap.containsKey(entry.getKey())) {
				String key = entry.getKey();
				String base = key.charAt(0) + "" + key.charAt(1);
				int baseCount = biCharMap.get(base) + charCount;
				double pro = (triCharMap.get(entry.getKey()) + 1) * 1.0 / baseCount;
				trigramLaplaceModel.put(key, pro);
			} else {
				String key = entry.getKey();
				String base = key.charAt(0) + "" + key.charAt(1);
				double pro = 0.0;
				if (biCharMap.containsKey(base)) {
					int baseCount = biCharMap.get(base) + charCount;
					pro = 1.0 / baseCount;
				} else {
					pro = 1.0 / charCount;
				}
				trigramLaplaceModel.put(key, pro);
			}
		}
	}

	
	//================================================================ Test ===============================================================
	/**
	 * use this method to test
	 * 
	 * @throws FileNotFoundException
	 */
	private static void test(String filename, int gram) throws FileNotFoundException {
		// read in the model
		Map<String, Double> testModel = new HashMap<>();
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] token = line.split("\t");
			testModel.put(token[0], Double.valueOf(token[1]));
		}
		file.delete();
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
			totalCountOfLetters += line.length();
			double totalProp = 0;

			for (int i = 0; i < line.length() - gram; i += gram) {
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
				double log = Math.log(v) / Math.log(10.0);
				totalProp += log;
//				totalProp *= (1.0 / v);
			}
			// double sentencePP = Math.pow(2.0, -(totalProp) / line.length());
			// System.out.println("S" + u + ": " + sentencePP);
			logProp += totalProp;	
		}
		double perplexity = Math.pow(10.0, -(logProp) / totalCountOfLetters);
//		double perplexity = Math.pow(Math.E, Math.log(logProp)/totalCountOfLetters);
		System.out.println(perplexity);
		in.close();
	}
	
	//================================================================ Writing ===============================================================
	
	private static void writeInterpolationTrigram() throws IOException {
		FileWriter fw = new FileWriter("interpolation_trigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramInterpolationModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}
	
	private static void writeBackoffTrigram() throws IOException {
		FileWriter fw = new FileWriter("backoff_trigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramBackoffModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}
	
	private static void writeLaplaceTrigram() throws IOException {
		FileWriter fw = new FileWriter("laplace_trigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramLaplaceModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}

	private static void writeTrigram() throws IOException {
		FileWriter fw = new FileWriter("trigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = trigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}

	/**
	 * write the bigram probability to file 'bigram' model
	 * 
	 * @throws IOException
	 */
	private static void writeBigram() throws IOException {
		FileWriter fw = new FileWriter("bigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = bigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}

	/**
	 * write the unigram probability to file 'unigram' model
	 * 
	 * @throws IOException
	 */
	private static void writeUnigram() throws IOException {
		FileWriter fw = new FileWriter("unigram_model_" + suffix + ".txt");
		PrintWriter pw = new PrintWriter(fw);
		Iterator it = unigramModel.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry) it.next();
			pw.print(element.getKey() + "\t" + element.getValue() + "\n");
		}
		pw.close();
	}

	//================================================================ Form Model ===============================================================
	private static void getTrigramModel() {
		for (Map.Entry<String, Double> entry1 : bigramModel.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : charMap.entrySet()) {
				trigramModel.put(entry1.getKey() + entry2.getKey(), 0.0);
			}
		}
		for (Map.Entry<String, Integer> entry : triCharMap.entrySet()) {
			String key = entry.getKey();
			String base = key.charAt(0) + "" + key.charAt(1);
			int baseCount = biCharMap.get(base);
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

}
