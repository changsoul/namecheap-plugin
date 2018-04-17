/* 
 * Copyright(c)2010-2018 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
 */

package com.wudaosoft.namecheap_plugin;

import org.apache.http.client.protocol.HttpClientContext;

import com.wudaosoft.net.httpclient.HostConfigBuilder;
import com.wudaosoft.net.httpclient.Request;

/**
 * @author Changsoul Wu
 * 
 */
public class RestApi {

	private String username;

	private String password;

	private HttpClientContext sessioin;

	private Request request;
	
	public HttpClientContext getSessioin() {
		return sessioin;
	}

	public RestApi() {
		sessioin = HttpClientContext.create();
		request = Request.createDefault(HostConfigBuilder.create("https://ap.www.namecheap.com")
				.setUserAgent(
						"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Mobile Safari/537.36")
				.build());
	}
	
	public String loginPage() throws Exception {
		return request.get("https://www.namecheap.com/myaccount/login.aspx?ReturnUrl=%2fdashboard").withContext(sessioin).execute();
	}

}
