import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


public class TransactionSequence 
{
	int SID; // stock ID
	LinkedList<Transaction> transactions;
	
	public TransactionSequence(int _SID)
	{
		this.SID = _SID;
		this.transactions = new LinkedList<Transaction>();
	}
	
	public void addTransaction(String _datetime, String _items)
	{
		this.transactions.add(new Transaction(_datetime, _items));	
	}
	
	public String getConvertedSequence()
	{
		String longSeq = "";
		for(int i = 0; i < transactions.size(); i++)
		{
			Transaction tran = transactions.get(i);
			longSeq += tran.items.replace(',', ' ')+",";
		}
		return longSeq.substring(0, longSeq.lastIndexOf(",")-1);
	}
	
	public ArrayList<String> getAllSubset(String items, ArrayList<String> allSubsets)
	{
		if(!items.contains(","))
			return allSubsets;
		else
		{
			String[] subset = getMinusOneSubset(items);
			for(int i=0; i<subset.length; i++)
			{
				if(!allSubsets.contains(subset[i]))
				{
					allSubsets.add(subset[i]);
					allSubsets = getAllSubset(subset[i], allSubsets);
				}	
			}
		}
		return allSubsets;
	}
	
	public Hashtable<String, ArrayList<ArrayList<String>>> getRules(String items)
	{
		Hashtable<String, ArrayList<ArrayList<String>>> rules = new Hashtable<String, ArrayList<ArrayList<String>>>();
		String[] arrItems = items.split(",");
		ArrayList<String> allSub = new ArrayList<String>();
		allSub = getAllSubset(items, allSub);
		for(int i=0; i<allSub.size(); i++)
		{
			ArrayList<String> RHS = new ArrayList<String>();
			ArrayList<String> LHS = new ArrayList<String>();
			
			String[] arrSub = allSub.get(i).split(",");
			
			for(int j=0; j<arrSub.length; j++)
			{
				LHS.add(arrSub[j]);
			}
			for(int k=0; k<arrItems.length; k++)
			{
				if(!LHS.contains(arrItems[k]))
					RHS.add(arrItems[k]);
			}
			String ruleName = "";
			for(int l=0; l<LHS.size(); l++)
			{
				ruleName += LHS.get(l)+",";
			}
			ruleName = ruleName.substring(0, ruleName.length()-1);
			ruleName += "->";
			for(int r=0; r<RHS.size(); r++)
			{
				ruleName += RHS.get(r)+",";
			}
			ruleName = ruleName.substring(0, ruleName.length()-1);
			ArrayList<ArrayList<String>> ruleItems = new ArrayList<ArrayList<String>>();
			ruleItems.add(LHS);
			ruleItems.add(RHS);
			rules.put(ruleName, ruleItems);
		}
		return rules;
	}

	
	public Hashtable<String, double[]> calRulesConf(Hashtable<String, ArrayList<ArrayList<String>>> rules, double min_conf)
	{
		Hashtable<String, double[]> finalRules = new Hashtable<String, double[]>();
		Enumeration<String> keys = rules.keys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
						
			ArrayList<String> LHS = rules.get(key).get(0);
			ArrayList<String> RHS = rules.get(key).get(1);
			
			int pFindLHS = 0;
			int pFindRHS = 0;
			for(int z=1; z<=12; z++)
			{
				if(LHS.contains(z+"")) pFindLHS++;
				if(pFindLHS>1) break;
			}
			for(int y=1; y<=12; y++)
			{
				if(RHS.contains(y+"")) pFindRHS++;
				if(pFindRHS>1) break;
			}
						
			if(pFindLHS == 0 && pFindRHS == 0) continue;
			if(pFindLHS == 1 && pFindRHS == 1) continue;
			if(pFindLHS > 1 || pFindRHS > 1) continue;		
			
			int LHSFound = 0;
			int RHSFound = 0;
			int wholeFound = 0;
			for(int i=0; i<this.transactions.size(); i++)
			{
				boolean isLHS = true;
				for(int j=0; j<LHS.size(); j++)
				{
					if(!this.transactions.get(i).items.contains(LHS.get(j)))
					{
						isLHS = false;
						break;
					}
				}
				if(isLHS) LHSFound++;
				
				boolean isRHS = true;
				for(int k=0; k<RHS.size(); k++)
				{
					if(!this.transactions.get(i).items.contains(RHS.get(k)))
					{
						isRHS = false;
						break;
					}
				}
				if(isRHS) RHSFound++;
				
				if(isLHS && isRHS) wholeFound++;
			}
			double supp = (double)wholeFound/this.transactions.size();
			double conf = supp/((double)LHSFound/this.transactions.size());
			double interest = conf/ ((double)RHSFound/this.transactions.size());
			
			//String result = "supp:"+supp+" conf:"+conf+" interest:"+interest;
			double[] result = new double[3];
			result[0] = supp;
			result[1] = conf;
			result[2] = interest;
			if(conf >= min_conf)
				finalRules.put(key, result);
		}
		return finalRules;
	}
	
	public ArrayList<Hashtable<String, Integer>> getLAll(double min_supp)
	{
		ArrayList<Hashtable<String, Integer>> allItemsets = new ArrayList<Hashtable<String,Integer>>();
		Hashtable<String, Integer> L1 = getL1(getC1(), min_supp);
		allItemsets.add(L1);
		Hashtable<String, Integer> LTemp = L1;
		while(LTemp.size()>1)
		{
			Hashtable<String,Integer> LNew = pruning(self_join(LTemp), LTemp);
			LNew = getLNext(LNew, min_supp);
			if(!(LNew.size()>1))
				break;
			else
			{
				allItemsets.add(LNew);
				LTemp = LNew;
			}
		}
		return allItemsets;
	}
	
	public Hashtable<String, Integer> getC1()
	{
		Hashtable<String, Integer> h = new Hashtable<String, Integer>();
		for( int i=0; i<this.transactions.size(); i++)
		{
			String[] s = this.transactions.get(i).items.split(",");
			for( int j=0; j<s.length; j++)
			{
				if(!h.containsKey(s[j]))
				{
					h.put(s[j], 1);
				}
				else
				{
					h.put(s[j], h.get(s[j])+1);
				}
			}
		}
		return h;
	}
	
	public Hashtable<String, Integer> getL1(Hashtable<String, Integer> itemsets, double min_supp)
	{
		Enumeration<String> iKeys = itemsets.keys();
		while(iKeys.hasMoreElements())
		{
			String iKey = iKeys.nextElement();
			if((double)itemsets.get(iKey)/this.transactions.size() < min_supp)
			{
				itemsets.remove(iKey);
			}
		}
		return itemsets;
	}

	public Vector<String> self_join(Hashtable<String, Integer> itemsets)
	{
		Vector<String> newItemset = new Vector<String>();
		// self joining
		String[] oldKeys = new String[itemsets.size()];
		itemsets.keySet().toArray(oldKeys);
		for(int i=0; i<oldKeys.length-1; i++)
		{
			for(int j=i+1; j<oldKeys.length; j++)
			{
				String[] sI = oldKeys[i].split(",");
				String[] sJ = oldKeys[j].split(",");
				Vector<String> sV = new Vector<String>();
				for(int m=0; m<sI.length; m++)
				{
					if(!sV.contains(sI[m]))
						sV.add(sI[m]);
				}
				for(int n=0; n<sJ.length; n++)
				{
					if(!sV.contains(sJ[n]))
						sV.add(sJ[n]);
				}
				// remove sets that is not for this round i.e. size > N
				String newItem = "";
				if(sV.size() <= sI.length+1)
				{
					// remove duplicate itemset
					boolean setFound = false;
					for(int y=0; y<newItemset.size(); y++)
					{
						if(isSetEqual(sV.toArray(new String[sV.size()]), newItemset.get(y).split(",")))
						{
							setFound = true;
							break;
						}
					}
					if(!setFound)
					{
						for(int k=0; k<sV.size(); k++)
						{
							newItem += sV.get(k)+",";
						}
						newItemset.add(newItem.substring(0,newItem.length()-1));
					}
				}
			}
		}
		return newItemset;
	}
	
	public boolean isSetEqual(String[] setA, String[] setB)
	{
		int count = 0;
		for(int i=0; i<setA.length; i++)
		{
			for(int j=0; j<setB.length; j++)
			{
				if(setA[i].equals(setB[j]))
					count++;
			}
		}
		return (count == setA.length);
	}
	
	// for each candidate item, if not all N-1 item subset of the item find in L(N-1), prune it.
	public Hashtable<String, Integer> pruning(Vector<String> candidate, Hashtable<String, Integer> preL)
	{
		Hashtable<String, Integer> pruned = new Hashtable<String, Integer>(); 
		for(int i=0; i<candidate.size(); i++)
		{
			String[] minus1Set = getMinusOneSubset(candidate.get(i));
			if(isAllSubsetFound(preL.keySet().toArray(new String[preL.keySet().size()]), minus1Set))
			{
				pruned.put(candidate.get(i),0);
			}
		}
		return pruned;
	}
	
	public Hashtable<String, Integer> getLNext(Hashtable<String, Integer> input, double min_supp)
	{
		Hashtable<String, Integer> LNext = new Hashtable<String, Integer>();
		Enumeration<String> fKeys = input.keys();
		while(fKeys.hasMoreElements())
		{
			String fKey = fKeys.nextElement();
			int suppCount = 0;
			for(int i=0; i<this.transactions.size(); i++)
			{
				String trans = this.transactions.get(i).items;
				int iFind = 0;
				String[] items = fKey.split(",");
				for(int j=0; j<items.length; j++)
				{
					if(trans.contains(items[j]))
						iFind++;
				}
				if(iFind == items.length)
					suppCount++;
			}
			if((double)suppCount/this.transactions.size() >= min_supp)
			{
				LNext.put(fKey, suppCount);
			}
		}
		return LNext;
	}
	
	// get all N-1 item subset from input
	public String[] getMinusOneSubset(String input)
	{
		ArrayList<String> subset = new ArrayList<String>();
		String[] items = input.split(",");
		for(int i=0; i<items.length; i++)
		{
			subset.add(stripSubstring(input, i));
		}
		return subset.toArray(new String[subset.size()]);
	}
	
	public String stripSubstring(String sWithComma, int iIdx2Strip)
	{
		String sNewStr = "";
		String[] sArr = sWithComma.split(",");
		for(int i=0; i<sArr.length; i++)
		{
			if(i != iIdx2Strip)
				sNewStr += sArr[i]+",";
		}
		return sNewStr.substring(0,sNewStr.length()-1);
	}
	
	public boolean isAllSubsetFound(String[] preList, String[] curList)
	{
		int iSubsetFound = 0;
		for(int i=0; i<curList.length; i++)
		{
			for(int j=0; j<preList.length; j++)
			{
				if(isSubset(preList[j], curList[i]))
				{
					iSubsetFound++;
					break;
				}
			}
		}
		return (iSubsetFound == curList.length);
	}
	
	public boolean isSubset(String preItem, String curItem)
	{
		String[] sArr = curItem.split(",");
		for(int i=0; i<sArr.length; i++)
		{
			if(!preItem.contains(sArr[i]))
				return false;
		}
		return true;
	}
}
