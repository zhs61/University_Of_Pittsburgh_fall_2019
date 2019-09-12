import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

	private static Map<Character, Integer> charMap = new HashMap<>();
	private static Map<String, Double> unigramModel = new HashMap<>();
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
			}
			getUnigramMode();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Use the data get from the file to form a unigram mdoel
	 * use the count of each character divided by the total count
	 * store the result in a hashmap with the char as the key
	 */
	private static void getUnigramMode() {
		for (Map.Entry<Character, Integer> entry : charMap.entrySet()) {
			String key = String.valueOf(entry.getKey());
			double pro = entry.getValue() * 1.0 / charCount;
			unigramModel.put(key, pro);
		}
		
	}

	private static void fillCharMap(String token) {
		
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			charCount++;
			if (charMap.containsKey(c)) {
				charMap.put(c, charMap.get(c)+1);
			} else {
				charMap.put(c, 1);
			}
		}
		
	}

}
