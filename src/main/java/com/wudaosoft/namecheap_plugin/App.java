package com.wudaosoft.namecheap_plugin;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.wudaosoft.namecheap_plugin.env.ApplicationArguments;
import com.wudaosoft.namecheap_plugin.env.Base58;
import com.wudaosoft.namecheap_plugin.env.DefaultApplicationArguments;

/**
 * 
 * @author changsoul.wu
 *
 */
public class App {

	public static void main(String[] args) {

		try {
			System.out.println("Namecheap-Plugin started...");

			todo(args);

			System.out.println("Namecheap-Plugin stoped.");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}

	public static void todo(String[] args) throws Exception {

		ApplicationArguments arguments = new DefaultApplicationArguments(args);

		List<String> usernames = arguments.getOptionValues("user");
		List<String> passwords = arguments.getOptionValues("pwd");

		List<String> keys = arguments.getOptionValues("key");
		List<String> values = arguments.getOptionValues("value");

		List<String> domainNames = arguments.getOptionValues("domain");

		List<String> encodes = arguments.getOptionValues("en");

		if (encodes != null && !encodes.isEmpty()) {
			System.out.println(Base58.encode(encodes.get(0).getBytes("UTF-8")));
			return;
		}

		if (usernames == null || passwords == null || domainNames == null || keys == null || usernames.isEmpty()
				|| passwords.isEmpty() || domainNames.isEmpty() || keys.isEmpty())
			return;

		RestApi api = new RestApi(usernames.get(0), passwords.get(0));

		boolean isLogin = api.login();

		if (!isLogin)
			return;

		api.getDomainListPage();

		JSONArray arr = api.getDomainList();

		if (arr.isEmpty())
			return;

		if (arguments.containsOption("add")) {

			System.out.println("Namecheap-Plugin process add...");

			if (keys == null || values == null || keys.isEmpty() || values.isEmpty())
				return;

			for (String domainName : domainNames) {
				if (!api.hasDomainName(arr, domainName))
					continue;

				api.getAdvancedDnsListPage(domainName);

				int hostId = api.getDnsHostId(keys.get(0), RestApi.TEXT_RECORD_TYPE);

				String action = hostId == -1 ? "Add" : "Update";

				api.getAdvancedDNSDnsSecAddUpdateRecordModalPage();

				hostId = api.addOrUpdateHostRecord(hostId, keys.get(0), values.get(0), RestApi.TEXT_RECORD_TYPE,
						domainName);

				System.out.println(action + " text record success. domainName: " + domainName + "  hostId: " + hostId);
			}

		} else if (arguments.containsOption("del")) {

			System.out.println("Namecheap-Plugin process delete...");

			if (keys == null || keys.isEmpty())
				return;

			for (String domainName : domainNames) {
				if (!api.hasDomainName(arr, domainName))
					continue;

				api.getAdvancedDnsListPage(domainName);

				int hostId = api.getDnsHostId(keys.get(0), RestApi.TEXT_RECORD_TYPE);

				if (hostId == -1) {
					System.out.println("Remove text record fail. The domainName[" + domainName + "] text record dosen't exist.");
					continue;
				}

				api.removeDomainDnsRecord(hostId, RestApi.TEXT_RECORD_TYPE, domainName);

				System.out.println("Remove text record success. domainName: " + domainName + "  hostId: " + hostId);
			}
		} else {
			System.out.println("Namecheap-Plugin nothing to do.");
		}

	}
}
