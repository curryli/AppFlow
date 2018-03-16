package GSP;

import java.io.IOException;

/**
 * GSP序列模式分析算法
 * @author lyq
 *
 */
public class Client {
	public static void main(String[] args) throws IOException{
		//String filePath = "./data/testInput.txt";
		String filePath = "./data/sorted_registerSubmit.csv";
		//String filePath = "./data/sorted_firstday.csv";
		String mapPath = "./data/idx_eid_registerSubmit.json";
		
		//最小支持度阈值
		int minSupportCount = 100;
		//时间最小间隔
		int min_gap = 1;
		//施加最大间隔
		int max_gap = 5;
		
		long timeStart = System.currentTimeMillis();
		 
		GSPTool tool = new GSPTool(filePath, minSupportCount, min_gap, max_gap);
		tool.gspCalculate(mapPath);
		
		System.out.println("Execution time : "+(System.currentTimeMillis()-timeStart)+" ms");
		
		
		System.out.println("Done");
	}
	
	
	 
}