package util;

import java.util.LinkedList;
import java.util.List;

public class ParaUtil {
	
	/**
	 * 
	 * @param strTotal
	 * @return
	 * @throws Exception
	 */
	public static int numWithKM(String strTotal) throws Exception {
		char ch = strTotal.charAt(strTotal.length() - 1);
		int total = Integer.parseInt(strTotal.substring(0,
				strTotal.length() - 1));
		switch (ch) {
		case 'k':
		case 'K':
			total *= 1000;
			break;
		case 'm':
		case 'M':
			total *= 1000000;
			break;
		case '0':
			total *= 10;
			break;
		default:
			throw new Exception("unknown format of :" + strTotal);
		}
		return total;
	}
	/**
	 * such as :1-10,20,30,40,50,60,80,100,150,200,300,500
	 * @param input
	 * @return
	 */
	static public List<Integer> regStringNum(String input)
	{
		LinkedList<Integer> list = new LinkedList<Integer>();
        String[] values = input.split(",");
        for(String s:values)
        {
        	int hyphenIdx = s.indexOf('-');//连字符
        	if(hyphenIdx <= 0)
        	{
        		list.add(Integer.parseInt(s));
        		continue;
        	}
        	
        	int begin = Integer.parseInt(s.substring(0, hyphenIdx));
        	int end = Integer.parseInt(s.substring(hyphenIdx+1));
        	for(int i = begin;i<=end;i++)
        	{
        		list.add(i);
        	}
        }
        return list;        
	}
}
