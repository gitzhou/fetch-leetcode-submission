package cc.aaron67.fetch.leetcode.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtils {

	private final static Logger logger = Logger.getLogger(HttpUtils.class);

	private static CloseableHttpClient client = HttpClients.createDefault();

	public static CloseableHttpResponse get(String url,
			Map<String, String> headers) {
		if (url == null) {
			return null;
		}
		HttpGet get = new HttpGet(url);
		addHeaders(get, headers);
		return visit(get);
	}

	public static CloseableHttpResponse post(String url,
			Map<String, String> headers, Map<String, String> params) {
		if (url == null) {
			return null;
		}
		HttpPost post = preparePostMethod(url, params);
		addHeaders(post, headers);
		return visit(post);
	}

	private static void addHeaders(HttpUriRequest request,
			Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		Set<String> keys = headers.keySet();
		for (String key : keys) {
			request.addHeader(key, headers.get(key));
		}
	}

	private static HttpPost preparePostMethod(String url,
			Map<String, String> params) {
		HttpPost post = new HttpPost(url);
		if (params != null && params.size() > 0) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			Set<String> keys = params.keySet();
			for (String key : keys) {
				pairs.add(new BasicNameValuePair(key, params.get(key)));
			}
			post.setEntity(new UrlEncodedFormEntity(pairs, Charset
					.forName("UTF-8")));
		}
		return post;
	}

	private static CloseableHttpResponse visit(HttpUriRequest request) {
		CloseableHttpResponse response = null;
		try {
			response = client.execute(request);
			logger.info("=======" + request.getMethod() + "======="
					+ request.getURI().toASCIIString() + "======="
					+ response.getStatusLine() + "=======");
			logger.info(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.abort();
		}
		return response;
	}
}
