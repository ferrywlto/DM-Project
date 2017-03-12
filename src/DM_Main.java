import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class DM_Main {

	public static void main(String[] args) 
	{
		double min_supp = Double.parseDouble(args[1]);
		double min_conf = Double.parseDouble(args[2]);
		double dis_supp = Double.parseDouble(args[3]);
		double dis_conf = Double.parseDouble(args[4]);
		double dis_interest = Double.parseDouble(args[5]);
		
		System.out.println("Reading data...");
		Hashtable<Integer, TransactionSequence> dataSet = DataReader.ReadDataFile(args[0]);
		Enumeration<Integer> keys = dataSet.keys();
		
		while(keys.hasMoreElements())
		{
			Integer key = keys.nextElement();
			TransactionSequence ts = dataSet.get(key);
			System.out.println("Data of Stock "+ts.SID+"...");
			System.out.println("Stock "+ts.SID+" Sequence:"+ts.getConvertedSequence());
			
			ArrayList<Hashtable<String, Integer>> allItemsets = ts.getLAll(min_supp);
			System.out.println("Interest rules find:");
			for(int i=0; i<allItemsets.size(); i++)
			{
				Hashtable<String, Integer> LItemset = allItemsets.get(i);
				Enumeration<String> fKeys = LItemset.keys();
				while(fKeys.hasMoreElements())
				{
					String fKey = fKeys.nextElement();
					Hashtable<String,double[]> result = ts.calRulesConf(ts.getRules(fKey), min_conf);
					Enumeration<String> rKeys = result.keys();
					while(rKeys.hasMoreElements())
					{
						String rkey = rKeys.nextElement();
						double[] res = result.get(rkey);
						if(res[0] >= dis_supp && res[1] >= dis_conf && res[2] >= dis_interest)					
							System.out.println(rkey+"\t Supp:"+res[0]+"\t Conf:"+res[1]+"\t Inte:"+res[2]);
					}
				}
			}
		}
		System.out.println("Complete.");
	}
}

