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
		//String filePath = "./data/sorted_firstday.csv";
		
		//String filePath = "./data/sorted_registerSubmit.csv";
		//String mapPath = "./data/idx_eid_registerSubmit.json";
		
		String filePath = "./data/sorted_1000.csv";
		String mapPath = "./data/idx_eid_1000.json";
		
		//最小支持度阈值
		int minSupportCount = 50;
		//时间最小间隔
		int min_gap = 2;
		//施加最大间隔
		int max_gap = 4;
		
		long timeStart = System.currentTimeMillis();
		 
		GSPTool tool = new GSPTool(filePath, minSupportCount, min_gap, max_gap);
		tool.gspCalculate(mapPath);
		
		System.out.println("Execution time : "+(System.currentTimeMillis()-timeStart)+" ms");
		
		
		System.out.println("Done");
	}
	
	
	 
}