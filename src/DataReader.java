import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
 
public class DataReader 
{
	public static Hashtable<Integer, TransactionSequence> ReadDataFile(String _dataFile)
	{
		Hashtable<Integer, TransactionSequence> sequences = new Hashtable<Integer, TransactionSequence>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(_dataFile));
			String sEntry = "";
			while((sEntry = br.readLine()) != null)
			{
				int iDeIdx = sEntry.indexOf(",");
				int stockID = Integer.parseInt(sEntry.substring(0, iDeIdx));
				String data = sEntry.substring(iDeIdx+1);
				
				if(sequences.get(stockID) == null)
				{
					sequences.put(stockID, new TransactionSequence(stockID));
				}
				TransactionSequence seq = sequences.get(stockID);
				
				int iDeIdx2 = data.indexOf(",");
				String date = data.substring(0, iDeIdx2);
				String items = data.substring(iDeIdx2+1);
				seq.addTransaction(date, items);				
			}
			br.close();
			br = null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(0);
		}
		return sequences;
	}
}