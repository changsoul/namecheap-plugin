/**
 *    Copyright 2009-2017 Wudao Software Studio(wudaosoft.com)
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.wudaosoft.namecheap_plugin.net;

import java.util.Optional;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

/** 
 * @author Changsoul Wu
 * 
 */
public class CookieUtil {

	public static String getCookieValue(String name, CookieStore cookieStore) {
		
		if (cookieStore == null || name == null)
			return null;
		
		Optional<Cookie> opCookie = cookieStore.getCookies().stream().filter(e -> e.getName().equals(name))
				.findFirst();

		if (opCookie.isPresent()) {
			return opCookie.get().getValue();
		}
		
		return null;
	}
}
