package cc.aaron67.fetch.leetcode.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.aaron67.fetch.leetcode.model.CodeObj;
import cc.aaron67.fetch.leetcode.model.QuestionObj;
import cc.aaron67.fetch.leetcode.model.SubmissionObj;
import cc.aaron67.fetch.leetcode.utils.HttpUtils;

public class Leetcode {
	public final static String USER_NAME = "";
	public final static String PASSWORD = "";
	public final static String HOME_PAGE_URL = "https://oj.leetcode.com";
	public final static String LOGIN_PAGE_URL = "https://oj.leetcode.com/accounts/login/";
	public final static String SUBMISSION_PAGE_URL = "https://oj.leetcode.com/submissions/";

	private static Logger logger = Logger.getLogger(Leetcode.class);

	private String csrftoken = "DLs592YH48QUgXUWpa6aoS5nbgGXhl8z";

	public void process() {
		logger.info("抓取开始");
		if (login()) {
			try {
				Elements submissions = Jsoup
						.parse(fetchPage(SUBMISSION_PAGE_URL)) // 提交页面
						.select("table[id=result_testcases]") // 提交记录表格
						.select("tbody") // 去掉表头
						.select("tr"); // 表格数据的每一行
				for (Element tr : submissions) { // 对每一次的提交
					Elements tds = tr.select("td");
					SubmissionObj so = new SubmissionObj();
					logger.info(">>>> " + tds.get(1).select("a").text());
					// 题目详细信息
					QuestionObj qo = buildQuestionObj(HOME_PAGE_URL
							+ tds.get(1).select("a").attr("href"));
					so.setQuestion(qo);
					// 代码内容
					CodeObj co = buildCodeObj(HOME_PAGE_URL
							+ tds.get(2).select("a").attr("href"));
					so.setCode(co);
					// 代码执行状态
					so.setStatus(tds.get(2).select("a").get(0).select("strong")
							.text());
					// 代码执行时间
					so.setRuntime(tds.get(3).text());
					// 代码语言
					so.setLanguage(tds.get(4).text());
					// 持久化到硬盘
					// TODO
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			logger.info("抓取结束");
		}
	}

	private boolean login() {
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
				logger.info("登录成功");
				return true;
			}
			logger.info("登录失败");
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

	private String fetchPage(String url) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", HOME_PAGE_URL);
		headers.put("Cookie", "csrftoken=" + csrftoken);
		CloseableHttpResponse response = HttpUtils.get(url, headers);
		try {
			return HttpUtils.fetchWebpage(response);
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private QuestionObj buildQuestionObj(String url) {
		QuestionObj qo = new QuestionObj();
		qo.setUrl(url);
		Document doc = Jsoup.parse(fetchPage(url));
		qo.setTitle(doc.select("div[class=question-title]").select("h3").text());
		qo.setContent(doc.select("div[class=question-content]").text());
		return qo;
	}

	private CodeObj buildCodeObj(String url) {
		return null;
	}

}
