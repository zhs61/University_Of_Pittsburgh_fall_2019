import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

	private static Map<String, Integer> charMap = new HashMap<>();
	private static Map<String, Integer> biCharMap = new HashMap<>();
	private static Map<String, Integer> triCharMap = new HashMap<>();
	private static Map<String, Double> unigramModel = new HashMap<>();
	private static Map<String, Double> bigramModel = new HashMap<>();
	private static Map<String, Double> trigramModel = new HashMap<>();
	
	private static int charCount = 0;
	
	public static void main(String[] args) {

		// read the file line by line
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter the filename: ");
		String filename = scan.next();

		File file = new File(filename);

		try {
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String token = sc.nextLine();
				fillCharMap(token);
				fillBiCharMap(token);
				fillTriCharMap(token);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		getUnigramModel();
		getBigramModel();
		getTrigramModel();
		System.out.println();
	}

	private static void getTrigramModel() {
		for (Map.Entry<String, Integer> entry : triCharMap.entrySet()) {
			String key = entry.getKey();
			String base = key.charAt(0) +"" + key.charAt(1);
			int baseCount = biCharMap.get(base);
			double pro = entry.getValue() * 1.0 / baseCount;
			trigramModel.put(key, pro);
		} 
		
	}

	/**
	 * fill the trigramcharmap 
	 * @param token
	 */
	private static void fillTriCharMap(String token) {
		for (int i = 0; i < token.length()-2; i++) {
			String first = token.charAt(i)+"";
			String second = token.charAt(i+1)+"";
			String third = token.charAt(i+2)+"";
			String triChar = first + second + third;
			if (triCharMap.containsKey(triChar)) {
				triCharMap.put(triChar, triCharMap.get(triChar)+1);
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
	 * @param token
	 */
	private static void fillBiCharMap(String token) {
		for (int i = 0; i < token.length()-1; i++) {
			String first = token.charAt(i)+"";
			String second = token.charAt(i+1) + "";
			String biChar = first + second;
			if (biCharMap.containsKey(biChar)) {
				biCharMap.put(biChar, biCharMap.get(biChar)+1);
			} else {
				biCharMap.put(biChar, 1);
			}
		}
		
	}

	/**
	 * Use the data get from the file to form a unigram mdoel
	 * use the count of each character divided by the total count
	 * store the result in a hashmap with the char as the key
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
				charMap.put(c, charMap.get(c)+1);
			} else {
				charMap.put(c, 1);
			}
		}
		
	}

}
