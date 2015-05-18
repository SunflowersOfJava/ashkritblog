package util;

import org.junit.Test;

public class ParaUtilTest {

	@Test
	public void test() {
		String input = "1-10,20,30,40,50,60,80,100,150,200,300,500";
		for(int i:ParaUtil.regStringNum(input))
		{
			System.out.print(i+" ");
		}
		System.out.println();
	}

}
