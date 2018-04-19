/* 
 * Copyright(c)2010-2018 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
 */ 
 
package com.wudaosoft.namecheap_plugin;

import com.wudaosoft.namecheap_plugin.env.Base58;

/** 
 * @author Changsoul Wu
 * 
 */
public class Test {
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(Base58.encode("fuck".getBytes("utf-8")));
		System.out.println(Base58.encode("you".getBytes("utf-8")));
	}

}
