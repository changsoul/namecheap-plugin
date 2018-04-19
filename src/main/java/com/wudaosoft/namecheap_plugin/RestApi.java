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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wudaosoft.namecheap_plugin.env.Base58;
import com.wudaosoft.namecheap_plugin.net.HeadersInterceptor;
import com.wudaosoft.net.httpclient.HostConfigBuilder;
import com.wudaosoft.net.httpclient.Request;

/**
 * @author Changsoul Wu
 * 
 */
public class RestApi {
	
	public static final int TEXT_RECORD_TYPE = 5;
	
	private static final Logger log = LoggerFactory.getLogger(RestApi.class);
	
	private static final String LOGIN_PAGE_URL = "https://www.namecheap.com/myaccount/login.aspx?ReturnUrl=%2fdashboard";
	
	private static final String SESSION_HANDLER_AJAX_URL = "https://www.namecheap.com/cart/ajax/SessionHandler.ashx?_=";
	
	private static final String GET_DOMAIN_LIST_PAGE_URL = "/";
	
	private static final String GET_DOMAIN_LIST_AJAX_URL = "/Domains/GetDomainList";
	
	private static final String ADVANCED_DNS_LIST_PAGE_URL = "/domains/domaincontrolpanel/%s/advancedns";
	
	private static final String ADVANCED_DNS_SEC_ADD_UPDATE_RECORD_MODAL_PAGE_URL = "/Domains/Dns/GetAdvancedDNSDnsSecAddUpdateRecordModal";
	
	private static final String GET_ADVANCED_DNS_INFO_AJAX_URL = "/Domains/dns/GetAdvancedDnsInfo";
	
	private static final String REMOVE_DOMAIN_DNS_RECORD_AJAX_URL = "/Domains/dns/RemoveDomainDnsRecord";
	
	private static final String ADD_OR_UPDATE_HOST_RECORD_AJAX_URL = "/Domains/dns/AddOrUpdateHostRecord";

	private static final int TIME_OUT = 1000 * 50;
	
	private String username;

	private String password;

	private HttpClientContext session;

	private Request request;
	
	public HttpClientContext getSession() {
		return session;
	}

	public RestApi(String username, String password) {
		this.username = Base58.decodeToString(username);
		this.password = Base58.decodeToString(password);
		this.session = HttpClientContext.create();
		this.request = Request.custom()
				.setHostConfig(HostConfigBuilder.create("https://ap.www.namecheap.com")
						.setConnectTimeout(TIME_OUT)
						.setSocketTimeout(TIME_OUT)
						.build())
				.setRequestInterceptor(new HeadersInterceptor())
				.build();
	}
	
	public String loginPage() throws Exception {
		return request.get(LOGIN_PAGE_URL).withContext(session).execute();
	}
	
	public Map<String, String> loginParams() throws Exception {
		
		JSONObject obj = request.get(SESSION_HANDLER_AJAX_URL + new Date().getTime()).withAjax().withContext(session).json();
		
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
		
		int code = request.post(LOGIN_PAGE_URL, params).withContext(session).noResult();
		
		return code == 301;
	}
	
	public String getAdvancedDnsListPage(String domainName) throws Exception {
		
		String rs = request.get(String.format(ADVANCED_DNS_LIST_PAGE_URL, domainName)).withContext(session).execute();
		
		return rs;
	}
	
	public String getAdvancedDNSDnsSecAddUpdateRecordModalPage() throws Exception {
		
		String rs = request.get(ADVANCED_DNS_SEC_ADD_UPDATE_RECORD_MODAL_PAGE_URL).withContext(session).execute();
		
		return rs;
	}
	
	public String getDomainListPage() throws Exception {
		
		String rs = request.get(GET_DOMAIN_LIST_PAGE_URL).withContext(session).execute();
		
		return rs;
	}
	
	public JSONArray getDomainList() throws Exception {
		
		String payLoad = "{\"gridStateModel\":{\"ServerChunkSize\":1000,\"LastAvailableChunkIndex\":0,\"IsLazyLoading\":true,\"TotalServerItemsCount\":null},\"isOverViewPage\":\"true\"}";
		
		JSONObject obj = request.post(GET_DOMAIN_LIST_AJAX_URL).withStringBody(payLoad).withAjax().withContext(session).json();
		
		return obj.getJSONObject("Result").getJSONArray("Data");
	}
	
	public JSONArray getAdvancedDnsInfo() throws Exception {
		
		Map<String, String> params = new HashMap<>();
		params.put("domainName", "woailoli.com");
		params.put("fillTransferInfo", "false");
		
		JSONObject obj = request.get(GET_ADVANCED_DNS_INFO_AJAX_URL, params).withAjax().withContext(session).json();
		
		return obj.getJSONObject("Result").getJSONObject("CustomHostRecords").getJSONArray("Records");
	}
	
	public boolean hasDomainName(JSONArray domainList, String domainName) throws Exception {
		domainName = domainName.toLowerCase();
		
		for(int i = 0; i < domainList.size(); i++) {
			String item = domainList.getString(i);
			if(item.contains(domainName)) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getDnsHostId(String key, int recordType) throws Exception {
		JSONArray arr = getAdvancedDnsInfo();
		
		for(int i = 0; i < arr.size(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			if(key.equals(obj.getString("Host")) && recordType == obj.getIntValue("RecordType")) {
				return obj.getIntValue("HostId"); 
			}
		}

		return -1;
	}
	
	public boolean removeDomainDnsRecord(int hostId, int recordType, String domainName) throws Exception {
		
		JSONObject params = new JSONObject(true);
		params.put("hostId", hostId);
		params.put("recordType", recordType);
		params.put("domainName", domainName);
		
		String payLoad = JSON.toJSONString(params);
		
		JSONObject obj = request.post(REMOVE_DOMAIN_DNS_RECORD_AJAX_URL).withStringBody(payLoad).withAjax().withContext(session).json();
		
		return "true".equals(obj.get("Result"));
	}
	
	public int addOrUpdateHostRecord(int hostId, String host, String value, int recordType, String domainName) throws Exception {
		
		JSONObject params = new JSONObject(true);
		JSONObject model = new JSONObject(true);
		
		model.put("HostId", hostId);
		model.put("Host", host);
		model.put("Data", value);
		model.put("RecordType", recordType);
		model.put("Ttl", 1799);
		
		params.put("model", model);
		params.put("domainName", domainName);
		params.put("isAddNewProcess", hostId == -1 ? true : false);
		
		String payLoad = JSON.toJSONString(params);
		
		log.debug("addOrUpdateHostRecord payLoad:" + payLoad);
		
		JSONObject obj = request.post(ADD_OR_UPDATE_HOST_RECORD_AJAX_URL).withStringBody(payLoad).withAjax().withContext(session).json();
		
		log.debug("addOrUpdateHostRecord result:" + obj);
		
		return obj.getJSONArray("Result").getJSONObject(0).getIntValue("HostId");
	}
}
