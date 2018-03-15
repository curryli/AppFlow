package GSP;
/**
 * GSP序列模式分析算法
 * @author lyq
 *
 */
public class Client {
	public static void main(String[] args){
		//String filePath = "./testInput.txt";
		String filePath = "/home/ubuntu/sorted_1000.csv";
		//最小支持度阈值
		int minSupportCount = 50;
		//时间最小间隔
		int min_gap = 1;
		//施加最大间隔
		int max_gap = 5;
		
		long timeStart = System.currentTimeMillis();
		 
		GSPTool tool = new GSPTool(filePath, minSupportCount, min_gap, max_gap);
		tool.gspCalculate();
		
		System.out.println("Execution time : "+(System.currentTimeMillis()-timeStart)+" ms");
		
		
		System.out.println("Done");
	}
	
	
	 
}