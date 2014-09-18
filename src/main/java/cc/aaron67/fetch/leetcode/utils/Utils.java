package cc.aaron67.fetch.leetcode.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class Utils {

	private final static Logger logger = Logger.getLogger(Utils.class);

	public static String getWebPage(String url) throws ClientProtocolException,
			IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
			logger.info(response.getStatusLine() + " <-- " + url);

			HttpEntity entity = response.getEntity();
			Header contentEncoding = response
					.getFirstHeader("Content-Encoding");
			InputStream instream = entity.getContent();
			if (contentEncoding != null
					&& "gzip".equalsIgnoreCase(contentEncoding.getValue())) {
				instream = new GZIPInputStream(instream);
			}
			final ContentType contentType = ContentType.getOrDefault(entity);
			Charset charset = contentType.getCharset();
			BufferedReader br;
			if (charset != null) {
				br = new BufferedReader(
						new InputStreamReader(instream, charset));
			} else {
				br = new BufferedReader(
						new InputStreamReader(instream, "utf-8"));
			}
			String pageContent = IOUtils.toString(br);
			EntityUtils.consume(entity);
			return pageContent;
		} finally {
			response.close();
		}
	}
}
