package jw.load.balance.utils;
/**
 * @author 殷佳伟
 * 
 */
public class RoundRobinBalance {
	
	
	    public static int number = -1;// 当前SERVER的序号,开始是-1
	    
	    
	    /**
	     * 获取请求的SERVER序号
	     * 
	     * @return
	     */
	    public static Integer next(int length) {
	    	
	    		number = (number + 1) % length;
	            return number;
	    }
}
