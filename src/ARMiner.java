import java.util.Enumeration;
import java.util.Hashtable;


public class ARMiner 
{
	Hashtable<Integer, String> seqDataSet;
	
	public ARMiner(Hashtable<Integer, TransactionSequence> dataSet)
	{
		seqDataSet = new Hashtable<Integer, String>(); 
		Enumeration<Integer> keys = dataSet.keys();
		while(keys.hasMoreElements())
		{
			Integer key = keys.nextElement();
			TransactionSequence tSeq = dataSet.get(key);
			seqDataSet.put(key, tSeq.getConvertedSequence());
		}
	}
	
	public void apriori(TransactionSequence tSeq)
	{
		
	}
	
	public int countSupportSequences(String itemset)
	{
		int count = 0;
		Enumeration<Integer> keys = seqDataSet.keys();
		while(keys.hasMoreElements())
		{
			Integer key = keys.nextElement();
			String tSeq = seqDataSet.get(key);
			if(tSeq.contains(itemset))
				count++;
		}
		return count;
	}
	
	public Hashtable<String,Integer> findFrequentItemset(double min_supp)
	{
		Hashtable<String,Integer> freqItemSet = new Hashtable<String, Integer>();		
		Enumeration<Integer> keys = seqDataSet.keys();
		int iNumSeq = seqDataSet.size();
		while(keys.hasMoreElements())
		{
			Integer key = keys.nextElement();
			String tSeq = seqDataSet.get(key);
			String[] itemsets = tSeq.split(",");
			for(int i=0; i<itemsets.length; i++)
			{
				int iSuppCount = countSupportSequences(itemsets[i]);
				if((double)iSuppCount/iNumSeq >= min_supp)
				{
					freqItemSet.put(itemsets[i], iSuppCount);
				}
			}
		}
		return freqItemSet;
	}
}
