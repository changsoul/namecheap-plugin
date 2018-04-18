/* 
 * Copyright(c)2010-2018 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
 */

package com.wudaosoft.namecheap_plugin;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.protocol.HttpClientContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wudaosoft.net.httpclient.HostConfigBuilder;
import com.wudaosoft.net.httpclient.Request;

/**
 * @author Changsoul Wu
 * 
 */
public class RestApi {
	
	public static final String TEXT_RECORD_TYPE = "5";
	
	private static final String LOGIN_PAGE_URL = "https://www.namecheap.com/myaccount/login.aspx?ReturnUrl=%2fdashboard";
	
	private static final String SESSION_HANDLER_AJAX_URL = "https://www.namecheap.com/cart/ajax/SessionHandler.ashx?_=";
	
	private static final String GET_DOMAIN_LIST_AJAX_URL = "/Domains/GetDomainList";
	
	private static final String GET_ADVANCED_DNS_INFO_AJAX_URL = "/Domains/dns/GetAdvancedDnsInfo";
	
	private static final String REMOVE_DOMAIN_DNS_RECORD_AJAX_URL = "/Domains/dns/RemoveDomainDnsRecord";
	
	private static final String ADD_OR_UPDATE_HOST_RECORD_AJAX_URL = "/Domains/dns/AddOrUpdateHostRecord";

	private String username;

	private String password;

	private HttpClientContext sessioin;

	private Request request;
	
	public HttpClientContext getSessioin() {
		return sessioin;
	}

	public RestApi(String username, String password) {
		this.username = username;
		this.password = password;
		this.sessioin = HttpClientContext.create();
		this.request = Request.createDefault(HostConfigBuilder.create("https://ap.www.namecheap.com")
				.setUserAgent(
						"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Mobile Safari/537.36")
				.build());
	}
	
	public String loginPage() throws Exception {
		return request.get(LOGIN_PAGE_URL).withContext(sessioin).execute();
	}
	
	public Map<String, String> loginParams() throws Exception {
		
		JSONObject obj = request.get(SESSION_HANDLER_AJAX_URL + new Date().getTime()).withAjax().withContext(sessioin).json();
		
		String html = loginPage();
		Document doc = Jsoup.parse(html);
		
		doc.select("input[name=\"LoginUserName\"]").last().val(username);
		doc.select("input[name=\"LoginPassword\"]").last().val(password);
		doc.select("form").append(String.format("<input type=\"hidden\" name=\"sessionEncryptValue\" id=\"sessionEncryptValue\" value=\"%s\">", obj.getString("SessionKey")));
		
		Elements els = doc.select("form[name=\"aspnetForm\"] input");
		
		return els.stream().collect(Collectors.toMap((e) -> e.attr("name"), Element::val, (key1, key2) -> key2, LinkedHashMap::new));
	}
	
	public boolean login() throws Exception {
		
		Map<String, String> params = loginParams();
		
		int code = request.post(LOGIN_PAGE_URL, params).withContext(sessioin).noResult();
		
		return code == 301;
	}
	
	public JSONArray getDomainList() throws Exception {
		
		String payLoad = "{\"gridStateModel\":{\"ServerChunkSize\":1000,\"LastAvailableChunkIndex\":0,\"IsLazyLoading\":true,\"TotalServerItemsCount\":null},\"isOverViewPage\":\"true\"}";
		
		JSONObject obj = request.post(GET_DOMAIN_LIST_AJAX_URL).withStringBody(payLoad).withAjax().withContext(sessioin).json();
		
		return obj.getJSONObject("Result").getJSONArray("Data");
	}
	
	public JSONArray getAdvancedDnsInfo() throws Exception {
		
		Map<String, String> params = new HashMap<>();
		params.put("domainName", "woailoli.com");
		params.put("fillTransferInfo", "false");
		
		JSONObject obj = request.get(GET_ADVANCED_DNS_INFO_AJAX_URL, params).withAjax().withContext(sessioin).json();
		
		return obj.getJSONObject("Result").getJSONObject("CustomHostRecords").getJSONArray("Records");
	}
	
	public boolean removeDomainDnsRecord(String hostId, String recordType, String domainName) throws Exception {
		
		Map<String, Object> params = new HashMap<>();
		params.put("hostId", hostId);
		params.put("recordType", recordType);
		params.put("domainName", domainName);
		
		String payLoad = JSON.toJSONString(params);
		
		JSONObject obj = request.post(REMOVE_DOMAIN_DNS_RECORD_AJAX_URL).withStringBody(payLoad).withAjax().withContext(sessioin).json();
		
		return "true".equals(obj.get("Result"));
	}
	
	public int addOrUpdateHostRecord(String host, String value, String recordType, String domainName) throws Exception {
		
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> model = new HashMap<>();
		
		model.put("HostId", -1);
		model.put("Host", host);
		model.put("Data", value);
		model.put("RecordType", recordType);
		model.put("Ttl", 1799);
		
		params.put("model", model);
		params.put("domainName", domainName);
		params.put("isAddNewProcess", true);
		
		String payLoad = JSON.toJSONString(params);
		
		JSONObject obj = request.post(ADD_OR_UPDATE_HOST_RECORD_AJAX_URL).withStringBody(payLoad).withAjax().withContext(sessioin).json();
		
		System.out.println("addOrUpdateHostRecord result:" + obj);
		
		return obj.getJSONArray("Result").getJSONObject(0).getIntValue("HostId");
	}

}
