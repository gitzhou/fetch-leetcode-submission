package cc.aaron67.fetch.leetcode.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import cc.aaron67.fetch.leetcode.utils.HttpUtils;

public class Leetcode {
	public final static String USER_NAME = "";
	public final static String PASSWORD = "";
	public final static String HOME_PAGE_URL = "https://oj.leetcode.com/";
	public final static String LOGIN_PAGE_URL = "https://oj.leetcode.com/accounts/login/";
	public final static String SUBMISSION_PAGE_URL = "https://oj.leetcode.com/submissions/";

	private static Logger logger = Logger.getLogger(Leetcode.class);

	private String csrftoken = "DLs592YH48QUgXUWpa6aoS5nbgGXhl8z";

	public boolean login() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", HOME_PAGE_URL);
		headers.put("Cookie", "csrftoken=" + csrftoken);
		Map<String, String> params = new HashMap<String, String>();
		params.put("login", USER_NAME);
		params.put("password", PASSWORD);
		params.put("csrfmiddlewaretoken", csrftoken);
		CloseableHttpResponse response = HttpUtils.post(LOGIN_PAGE_URL,
				headers, params);
		try {
			if (response.getStatusLine().getStatusCode() == 302) {
				Header csrfCookie = response.getFirstHeader("Set-Cookie");
				for (HeaderElement element : csrfCookie.getElements()) {
					if (element.getName() != null
							&& element.getName().equals("csrftoken")) {
						csrftoken = element.getValue();
					}
				}
				logger.info("csrftoken: " + csrftoken);
				return true;
			}
			return false;
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void fetchSubmissionPage() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", HOME_PAGE_URL);
		headers.put("Cookie", "csrftoken=" + csrftoken);
		CloseableHttpResponse response = HttpUtils.get(SUBMISSION_PAGE_URL,
				headers);
		// TODO
	}
}
