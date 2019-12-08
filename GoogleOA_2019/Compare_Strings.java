
public class Compare_Strings {

	public static void main(String[] args) {
		int[] result = compareString("abcd,aabc,bd", "aaa,aa");
		for (int i : result) {
			System.out.println(i);
		}
	}

	private static int[] compareString(String A, String B) {
		String[] splitA = A.split(",");
		String[] splitB = B.split(",");
		int[] result = new int[splitB.length];
		
		for (int j = 0; j < splitB.length; j++) {
			String b = splitB[j];
			int occub = 1;
			char c = b.charAt(0);
			if (b.length() > 1) {
				for (int i = 1; i < b.length(); i++) {
					char temp = b.charAt(i);
					if (temp<c) {
						c = temp;
						occub = 1;
					} else if (temp == c) {
						occub++;
					} 
				}
			}
			int numOfResult = 0;
			for (String a : splitA) {
				// find the occurence of the smalllest char
				int occu = 1;
				char ca = a.charAt(0);
				if (a.length() > 1) {
					for (int i = 1; i < a.length(); i++) {
						char temp = a.charAt(i);
						if (temp<ca) {
							ca = temp;
							occu = 1;
						} else if (temp == c) {
							occu++;
						} 
					}
				}
				if (occu < occub) {
					numOfResult++;
				}
			}
			result[j] = numOfResult;
		}
		return result;
	}

}
