import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Main {

	private static Map<String, Integer> charMap = new HashMap<>();
	private static Map<String, Integer> biCharMap = new HashMap<>();
	private static Map<String, Integer> triCharMap = new HashMap<>();
	private static Map<String, Double> unigramModel = new HashMap<>();
	private static Map<String, Double> bigramModel = new HashMap<>();
	private static Map<String, Double> trigramModel = new HashMap<>();
	private static String suffix = "en";

	private static int charCount = 0;

	public static void main(String[] args) throws IOException {

		// read the file line by line
		Scanner scan = new Scanner(System.in);
		boolean terminate = true;
		while (terminate) {
			System.out.print("choose the training file: \n1. englist\n2. Spanish\n3. German\n4. Quit\n5. Test\n");
			int fileNum = scan.nextInt();
			String filename = "training.en";
			switch (fileNum) {
			case 1:
				filename = "training.en";
				suffix = "en";
				break;
			case 2:
				filename = "training.es";
				suffix = "es";
				break;
			case 3:
				filename = "training.de";
				suffix = "de";
				break;
			case 4:
				System.exit(0);
				break;
			case 5:
				System.out.println("1. unigram_en");
				test("bigram_model_en.txt", 2);
				break;
			default:
				filename = "training.en";
				suffix = "en";
				break;
			}
			charMap = new HashMap<>();
			biCharMap = new HashMap<>();
			triCharMap = new HashMap<>();
			unigramModel = new HashMap<>();
			bigramModel = new HashMap<>();
			trigramModel = new HashMap<>();
			File file = new File(filename);

			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String token = sc.nextLine();
				fillCharMap(token);
				fillBiCharMap(token);
				fillTriCharMap(token);
			}
			getUnigramModel();
			getBigramModel();
			getTrigramModel();

			writeUnigram();
			writeBigram();
			writeTrigram();

		}
	}

	/**
	 * use this method to test
	 * @throws FileNotFoundException 
	 */
	private static void test(String filename, int gram) throws FileNotFoundException {
		Map<String, Double> testModel = new HashMap<>();
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] token = line.split("\t");
			testModel.put(token[0], Double.valueOf(token[1]));
		}
		file = new File("test");
		scan = new Scanner(file);
		double logProp = 0.0;
		int totalCountOfLetters = 0;
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			double totalProp = 1.0;
			for (int i = 0; i < line.length()-gram; i+=gram ) {
				String key = Character.toString(line.charAt(i)) + Character.toString(line.charAt(i+1));
				double v;
				if (testModel.get(key)==null) 
					v = 0;
				else v = testModel.get(key);
				totalProp *= v;
				totalCountOfLetters += gram;
			}
			logProp += (Math.log(totalProp)/Math.log(2));
		}
		double perplexity = Math.pow(2, -(logProp)/totalCountOfLetters);
		System.out.println(perplexity);
		
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

	private static void getTrigramModel() {
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
