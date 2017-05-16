package cc.aaron67.fetch.leetcode.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtils {

    private final static Logger logger = Logger.getLogger(HttpUtils.class);

    /**
     * HTTP GET
     * 
     * @param url
     * @param headers
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse get(String url, Map<String, String> headers) {
        if (url == null) {
            return null;
        }
        HttpGet get = new HttpGet(url);
        addHeaders(get, headers);
        return visit(get, true);
    }

    /**
     * HTTP GET Without Handling HTTP redirects automatically
     * 
     * @param url
     * @param headers
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse getWithoutAutoRedirect(String url, Map<String, String> headers) {
        if (url == null) {
            return null;
        }
        HttpGet get = new HttpGet(url);
        addHeaders(get, headers);
        return visit(get, false);
    }

    /**
     * HTTP POST
     * 
     * @param url
     * @param headers
     * @param params
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse post(String url, Map<String, String> headers, Map<String, String> params) {
        if (url == null) {
            return null;
        }
        HttpPost post = preparePostMethod(url, params);
        addHeaders(post, headers);
        return visit(post, true);
    }

    /**
     * 抓取网页源码
     * 
     * @param response
     * @return String
     */
    public static String fetchWebpage(CloseableHttpResponse response) {
        String html = null;
        try {
            ContentType ct = ContentType.getOrDefault(response.getEntity());
            String charset = "UTF-8";
            if (ct.getCharset() != null && !charset.equals(ct.getCharset().name())) {
                charset = ct.getCharset().name();
            }
            html = EntityUtils.toString(response.getEntity(), charset);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return html;
    }

    private static void addHeaders(HttpUriRequest request, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            request.addHeader(key, headers.get(key));
        }
    }

    private static HttpPost preparePostMethod(String url, Map<String, String> params) {
        HttpPost post = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            Set<String> keys = params.keySet();
            for (String key : keys) {
                pairs.add(new BasicNameValuePair(key, params.get(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(pairs, Charset.forName("UTF-8")));
        }
        return post;
    }

    private static CloseableHttpResponse visit(HttpUriRequest request, boolean autoRedirect) {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient client = null;
            if (autoRedirect) {
                client = HttpClients.createDefault();
            } else {
                client = HttpClients.custom().disableRedirectHandling().build();
            }
            response = client.execute(request);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}
