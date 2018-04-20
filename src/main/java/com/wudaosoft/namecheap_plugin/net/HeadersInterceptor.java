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

import java.io.IOException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

/**
 * @author changsoul.wu
 *
 */
public class HeadersInterceptor implements HttpRequestInterceptor {

	private volatile String referer;

	public HeadersInterceptor() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.HttpRequestInterceptor#process(org.apache.http.
	 * HttpRequest, org.apache.http.protocol.HttpContext)
	 */
	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

		if (!request.containsHeader("Accept")) {
			request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		}

		request.addHeader("Accept-Language", "en-US,en;q=0.8");
		request.addHeader("Cache-Control", "no-cache");
		request.addHeader("Pragma", "no-cache");
		
		if (referer != null) {
			request.addHeader("Referer", referer);
		}
		
		String _ncCompliance = CookieUtil.getCookieValue("_NcCompliance", ((HttpClientContext)context).getCookieStore());
		if(_ncCompliance != null)
			request.addHeader("_NcCompliance", _ncCompliance);

		HttpHost target = (HttpHost)context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
		String origin = target.getSchemeName() + "://" + target.getHostName();
		HttpUriRequest ur = (HttpUriRequest) request;
		
		if (request.containsHeader("X-Requested-With")) {
			if (request instanceof HttpEntityEnclosingRequest) {
				
				request.addHeader("Origin", origin);
			} else {
				request.setHeader("Accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
			}
			
		} else if(HttpGet.METHOD_NAME.equals(ur.getMethod())) {
			referer = ur.getURI().toString();
			referer = !referer.startsWith("http") ? origin + referer : referer;
		}
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.167 Safari/537.36");
	}

}
