package jw.load.balance.utils;
/**
 * @author ���ΰ
 * 
 */
public class RoundRobinBalance {
	
	
	    public static int number = -1;// ��ǰSERVER�����,��ʼ��-1
	    
	    
	    /**
	     * ��ȡ�����SERVER���
	     * 
	     * @return
	     */
	    public static Integer next(int length) {
	    	
	    		number = (number + 1) % length;
	            return number;
	    }
}
