package um.ece.abox.module.util;


public class Statistics {
	
	public static long total(int[] data) {
		
		long sum = 0;
		for (int i : data)
			sum += i;
	
		return sum;
	}
	
	public static double total(double[] data) {
		
		double sum = 0;
		for (double i : data)
			sum += i;
	
		return sum;
	}
	
	public static long total(long[] data) {
		
		long sum = 0;
		for (long i : data)
			sum += i;
	
		return sum;
	}
	
	public static double average(int[] data) {
		
		double average, sum=0;
		for (int j=0; j<data.length; j++) 
			sum += data[j];		
		
		average = sum / data.length;	
		return average;
	}
	
	public static double average(long[] data) {
		
		double average, sum=0;
		for (int j=0; j<data.length; j++) 
			sum += data[j];		
		
		average = (double)sum / data.length;	
		return average;
	}
	
	public static double average(double[] data) {
		
		double average, sum=0;
		for (int j=0; j<data.length; j++) 
			sum += data[j];		
		
		average = sum / data.length;	
		return average;
	}
	
	public static double variance(int[] data) {
		
		double avg = average(data);
		
		double sum = 0;
		for (double d : data) 
			sum += Math.pow((d-avg), 2);
		
		return sum/(data.length-1);
		
	}
	
	public static double variance(long[] data) {
		
		double avg = average(data);
		
		double sum = 0;
		for (double d : data) 
			sum += Math.pow((d-avg), 2);
		
		return sum/(data.length-1);
	}
	
	public static double variance(double[] data) {
		
		double avg = average(data);
		
		double sum = 0;
		for (double d : data) 
			sum += Math.pow((d-avg), 2);
		
		return sum/(data.length-1);
		
	}
	
	
	public static double stdEv(int[] data) {
		
		return Math.sqrt(variance(data));
	}
	
	public static double stdEv(long[] data) {
		
		return Math.sqrt(variance(data));
	}
	
	public static double stdEv(double[] data) {
		
		return Math.sqrt(variance(data));
	}
	
	public static double stdError(int[] data) {
		
		return stdEv(data)/Math.sqrt(data.length);
	}
	
	public static double stdError(double[] data) {
		
		return stdEv(data)/Math.sqrt(data.length);
	}
	
	public static double distance(int[] v1, int[] v2) {
		
		double result = 0;
		
		for (int i=0; i<v1.length; i++) {
			result += Math.pow((v1[i]-v2[i]), 2);
		}
		
		return Math.sqrt(result);
	}
	
	public static int max(int[] r) {
		
		int max = r[0];

		for (int i=0; i<r.length; i++) {
			if (r[i] > max) {
				max = r[i];
			}
		}
		
		return max;
	}
	
	public static long max(long[] r) {
		
		long max = r[0];
		for (int i=0; i<r.length; i++) {
			if (r[i] > max) {
				max = r[i];
			}
		}
		
		return max;
	}
	
	public static double max(double[] r) {
		
		double max = r[0];
		for (int i=0; i<r.length; i++) {
			if (r[i] > max) {
				max = r[i];
			}
		}
		
		return max;
	}
	
	public static int min(int[] r) {
		
		int min = r[0];
		for (int i=0; i<r.length; i++) {
			if (r[i] < min) 
				min = r[i];
		}
		
		return min;
	}
	
	public static double min(double[] r) {
		
		double min = r[0];
		for (int i=0; i<r.length; i++) {
			if (r[i] < min) 
				min = r[i];
		}
		
		return min;
	}
}
