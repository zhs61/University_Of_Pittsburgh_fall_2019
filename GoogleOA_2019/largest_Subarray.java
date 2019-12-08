
public class largest_Subarray {

	public static void main(String[] args) {
		int[] result = LargestSubArray(new int[] {1, 4, 3, 2 ,5}, 4);
		for (int i : result) {
			System.out.println(i);
		}
	}

	private static int[] LargestSubArray(int[] A, int k) {
		if (A.length == k) {
			return A;
		}
		int[] tempMax = new int[k];
		tempMax[0] = A[0];
		tempMax[1] = A[1];
		tempMax[2] = A[2];
		tempMax[3] = A[3];
		for (int i = 1; i <= A.length-4; i++) {
			int[] temp = new int[k];
			temp[0] = A[i];
			temp[1] = A[i+1];
			temp[2] = A[i+2];
			temp[3] = A[i+3];
			for (int j = 0; j < k; j++) {
				if (tempMax[j] < temp[j]) {
					tempMax = temp;
				} 
			}
		}
		return tempMax;
	}


}
