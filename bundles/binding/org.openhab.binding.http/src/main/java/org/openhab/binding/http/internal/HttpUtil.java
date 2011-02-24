/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2011, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */

package org.openhab.binding.http.internal;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Some common methods to be used in both HTTP-In-Binding and HTTP-Out-Binding
 * 
 * @author Thomas.Eichstaedt-Engelen
 * @since 0.6.0
 */
public class HttpUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	/**
	 * Excutes the given <code>url</code> with the given <code>httpMethod</code>
	 * 
	 * @param httpMethod the HTTP method to use
	 * @param url the url to execute (in milliseconds)
	 * @param timeout the socket timeout to wait for data
	 * 
	 * @return the response body or <code>NULL</code> when the request went wrong
	 */
	public static String executeUrl(String httpMethod, String url, int timeout) {

		HttpClient client = new HttpClient();
		HttpMethod method = HttpUtil.createHttpMethod(httpMethod, url);

		method.getParams().setSoTimeout(timeout);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));

		// TODO: teichsta: we should enhance to item-config to set username and
		// password for HTTP-Authentication
		//
		// Credentials credentials =
		// new UsernamePasswordCredentials(username, password);
		// client.getState().setCredentials(AuthScope.ANY, credentials);

		if (logger.isDebugEnabled()) {
			try {
				logger.debug("About to execute '" + method.getURI().toString()
						+ "'");
			} catch (URIException e) {
				logger.debug(e.getLocalizedMessage());
			}
		}

		try {

			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("Method failed: " + method.getStatusLine());
			}

			String responseBody = method.getResponseBodyAsString();

			if (!responseBody.isEmpty()) {
				logger.debug(new String(responseBody));
			}
			
			return responseBody;

		}
		catch (HttpException he) {
			logger.error("Fatal protocol violation: ", he);
		}
		catch (IOException e) {
			logger.error("Fatal transport error: ", e);
		}
		finally {
			method.releaseConnection();
		}
		
		return null;
	}

	/**
	 * Factory method to create a {@link HttpMethod}-object according to the 
	 * given String <code>httpMethod</codde>
	 * 
	 * @param httpMethodString the name of the {@link HttpMethod} to create
	 * @param url
	 * 
	 * @return an object of type {@link GetMethod}, {@link PutMethod}, 
	 * {@link PostMethod} or {@link DeleteMethod}
	 * @throws IllegalArgumentException if <code>httpMethod</code> is none of
	 * <code>GET</code>, <code>PUT</code>, <code>POST</POST> or <code>DELETE</code>
	 */
	public static HttpMethod createHttpMethod(String httpMethodString, String url) {
		
		if ("GET".equals(httpMethodString)) {
			return new GetMethod(url);
		}
		else if ("PUT".equals(httpMethodString)) {
	        return new PutMethod(url);
		}
		else if ("POST".equals(httpMethodString)) {
	        return new PostMethod(url);
		}
		else if ("DELETE".equals(httpMethodString)) {
	        return new DeleteMethod(url);
		}
		else {
			throw new IllegalArgumentException("given httpMethod '" + httpMethodString + "' is unknown");
		}
	}

}