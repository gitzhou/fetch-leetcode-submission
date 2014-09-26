package cc.aaron67.fetch.leetcode.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.aaron67.fetch.leetcode.model.QuestionObj;
import cc.aaron67.fetch.leetcode.model.SubmissionObj;
import cc.aaron67.fetch.leetcode.utils.Config;
import cc.aaron67.fetch.leetcode.utils.HttpUtils;
import cc.aaron67.fetch.leetcode.utils.Utils;

public class Leetcode {
	public final static String HOME_PAGE_URL = "https://oj.leetcode.com";
	public final static String LOGIN_PAGE_URL = "https://oj.leetcode.com/accounts/login/";
	public final static String LOGIN_VIA_GITHUB_PAGE_URL = "https://oj.leetcode.com/accounts/github/login/";
	public final static String SUBMISSION_PAGE_URL = "https://oj.leetcode.com/submissions/";

	private static Logger logger = Logger.getLogger(Leetcode.class);

	private String csrftoken = "DLs592YH48QUgXUWpa6aoS5nbgGXhl8z";
	private String phpsessid = "i8hgu33c6cquwgxhmle7ic88wvha1ii6";

	private Set<String> tags = null;

	public Leetcode() {
		tags = new HashSet<String>(Arrays.asList(Config.get("tags").split(",")));
	}

	public void process() {
		logger.info("抓取开始");
		if (login()) {
			try {
				int pageIndex = 1;
				while (true) {
					String location = SUBMISSION_PAGE_URL;
					if (pageIndex > 1) {
						location = SUBMISSION_PAGE_URL + pageIndex++ + "/";
					} else {
						++pageIndex;
					}
					Elements submissions = Jsoup.parse(fetchPage(location)) // 提交页面
							.select("table[id=result_testcases]") // 提交记录表格
							.select("tbody") // 去掉表头
							.select("tr"); // 表格数据的每一行
					if (submissions.size() == 0) {
						break;
					}
					for (Element tr : submissions) { // 对每一次的提交
						Elements tds = tr.select("td");

						// 筛选抓取的记录
						String status = tds.get(2).select("a").get(0).select("strong").text();
						if (Config.get("isfetchall").equals("false") && !tags.contains(status)) {
							continue;
						}

						SubmissionObj so = new SubmissionObj();
						logger.info("抓取 [" + tds.get(0).text() + "] 的提交 >>>> ");
						// 题目详细信息
						QuestionObj qo = buildQuestionObj(HOME_PAGE_URL
								+ tds.get(1).select("a").attr("href"));
						so.setQuestion(qo);
						// 代码执行状态
						so.setStatus(status);
						// 代码执行时间
						so.setRuntime(tds.get(3).text());
						// 代码语言
						so.setLanguage(tds.get(4).text());
						// 代码内容
						so.setCode(buildCode(HOME_PAGE_URL + tds.get(2).select("a").attr("href"),
								so.getLanguage()));
						// 提交记录在服务端ID号
						String id = tds.get(2).select("a").attr("href");
						int firstLast = id.length() - 1, secondLast = id.lastIndexOf('/',
								firstLast - 1);
						so.setServerID(id.substring(secondLast + 1, firstLast));
						// 持久化到硬盘
						writeSubmissionToDisk(so);
					} // for
				} // while
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		logger.info("抓取结束");
	}

	private boolean login() {
		switch (Config.get("logintype")) {
		case "leetcode":
			return loginLeetcode();
		case "github":
			return loginViaGithub();
		default:
			return false;
		}
	}

	private boolean loginLeetcode() {
		logger.info("直接登录 LeetCode OJ");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", HOME_PAGE_URL);
		headers.put("Cookie", "csrftoken=" + csrftoken);
		Map<String, String> params = new HashMap<String, String>();
		params.put("login", Config.get("username"));
		params.put("password", Config.get("password"));
		params.put("csrfmiddlewaretoken", csrftoken);
		CloseableHttpResponse response = HttpUtils.post(LOGIN_PAGE_URL, headers, params);
		try {
			if (response.getStatusLine().getStatusCode() == 302) {
				for (Header header : response.getHeaders("Set-Cookie")) {
					for (HeaderElement element : header.getElements()) {
						if (element.getName() != null && element.getName().equals("csrftoken")) {
							csrftoken = element.getValue();
						} else if (element.getName() != null
								&& element.getName().equals("PHPSESSID")) {
							phpsessid = element.getValue();
						}
					}
				}
				logger.info("登录成功");
				return true;
			}
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		logger.info("登录失败");
		return false;
	}

	/**
	 * 通过GitHub登录
	 * 
	 * @return
	 */
	public boolean loginViaGithub() {
		logger.info("通过 GitHub 登录 LeetCode OJ");
		// **** 登录GitHub ****
		// 获取GitHub登录页面
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", "https://github.com/");
		CloseableHttpResponse response = HttpUtils.get("https://github.com/login/", headers);
		try {
			Document doc = Jsoup.parse(HttpUtils.fetchWebpage(response));
			String _gh_sess = "";
			Header ghSessCookie = response.getLastHeader("Set-Cookie");
			for (HeaderElement element : ghSessCookie.getElements()) {
				if (element.getName() != null && element.getName().equals("_gh_sess")) {
					_gh_sess = element.getValue();
				}
			}
			// post登录GitHub
			headers = new HashMap<String, String>();
			headers.put("Cookie", "_gh_sess=" + _gh_sess + ";logged_in=no");
			Map<String, String> params = new HashMap<String, String>();
			params.put("utf8", doc.select("input[name=utf8]").val());
			params.put("authenticity_token", doc.select("input[name=authenticity_token]").val());
			params.put("commit", "Sign in");
			params.put("login", Config.get("username"));
			params.put("password", Config.get("password"));
			String dotcom_user = "";
			String logged_in = "";
			String user_session = "";
			response = HttpUtils.post("https://github.com/session", headers, params);
			for (Header h : response.getHeaders("Set-Cookie")) {
				for (HeaderElement element : h.getElements()) {
					if (element != null && element.getName().equals("_gh_sess")) {
						_gh_sess = element.getValue();
					} else if (element != null && element.getName().equals("dotcom_user")) {
						dotcom_user = element.getValue();
					} else if (element != null && element.getName().equals("logged_in")) {
						logged_in = element.getValue();
					} else if (element != null && element.getName().equals("user_session")) {
						user_session = element.getValue();
					}
				}
			}
			// **** 登录LeetCode ****
			// GET ==> LOGIN_VIA_GITHUB_PAGE_URL
			headers.put("Referer", HOME_PAGE_URL);
			headers.put("Cookie", "csrftoken=" + csrftoken + ";PHPSESSID=" + phpsessid);
			response = HttpUtils.getWithoutAutoRedirect(LOGIN_VIA_GITHUB_PAGE_URL, headers);
			if (response.getStatusLine().getStatusCode() == 302) {
				for (Header h : response.getHeaders("Set-Cookie")) {
					for (HeaderElement he : h.getElements()) {
						if (he != null && he.getName().equals("PHPSESSID")) {
							phpsessid = he.getValue();
						}
					}
				}
				// 302 ==> github.com/login/oauth/...
				headers.put("Referer", LOGIN_VIA_GITHUB_PAGE_URL);
				String location = response.getFirstHeader("Location").getValue();
				headers.put("Cookie", "_gh_sess=" + _gh_sess + ";dotcom_user=" + dotcom_user
						+ ";logged_in=" + logged_in + ";user_session=" + user_session);
				response = HttpUtils.getWithoutAutoRedirect(location, headers);
				if (response.getStatusLine().getStatusCode() == 302) {
					for (Header h : response.getHeaders("Set-Cookie")) {
						for (HeaderElement element : h.getElements()) {
							if (element != null && element.getName().equals("_gh_sess")) {
								_gh_sess = element.getValue();
							} else if (element != null && element.getName().equals("user_session")) {
								user_session = element.getValue();
							}
						}
					}
					// 302 ==>
					// oj.leetcode.com/accounts/github/login/callback/...
					headers.put("Referer", location);
					headers.put("Cookie", "csrftoken=" + csrftoken + ";PHPSESSID=" + phpsessid);
					location = response.getFirstHeader("Location").getValue();
					String messages = "";
					response = HttpUtils.getWithoutAutoRedirect(location, headers);
					if (response.getStatusLine().getStatusCode() == 302) {
						for (Header h : response.getHeaders("Set-Cookie")) {
							for (HeaderElement he : h.getElements()) {
								if (he != null && he.getName().equals("csrftoken")) {
									csrftoken = he.getValue();
								} else if (he != null && he.getName().equals("PHPSESSID")) {
									phpsessid = he.getValue();
								} else if (he != null && he.getName().equals("messages")) {
									messages = he.getValue();
								}
							}
						}
						// 302 ==> oj.leetcode.com/problems/
						headers.put("Cookie", "csrftoken=" + csrftoken + ";PHPSESSID=" + phpsessid
								+ ";messages=" + messages);
						headers.put("Referer", location);
						location = response.getFirstHeader("Location").getValue();
						response = HttpUtils.getWithoutAutoRedirect(location, headers);
						if (response.getStatusLine().getStatusCode() == 200) {
							for (Header h : response.getHeaders("Set-Cookie")) {
								for (HeaderElement he : h.getElements()) {
									if (he != null && he.getName().equals("messages")) {
										messages = he.getValue();
									}
								}
							}
							logger.info("登录成功");
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		logger.info("登录失败");
		return false;
	}

	/**
	 * 抓取地址为url页面的源码
	 */
	private String fetchPage(String url) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Referer", SUBMISSION_PAGE_URL);
		headers.put("Cookie", "csrftoken=" + csrftoken + ";PHPSESSID=" + phpsessid);
		CloseableHttpResponse response = HttpUtils.getWithoutAutoRedirect(url, headers);
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
		String content = doc.select("div[class=question-content]").text();
		StringBuilder contentBuilder = new StringBuilder();
		for (int i = 0; i < content.length(); ++i) {
			if (Character.isUpperCase(content.charAt(i)) && i > 0 && content.charAt(i - 1) == ' ') {
				contentBuilder.append(System.getProperty("line.separator"));
			}
			contentBuilder.append(content.charAt(i));
		}
		qo.setContent(contentBuilder.toString());
		return qo;
	}

	private String buildCode(String url, String language) {
		Elements es = Jsoup.parse(fetchPage(url)).getElementsByTag("script");
		String code = null;
		for (Element e : es) {
			int indexFrom = e.toString().indexOf("scope.code." + language + " = '");
			if (indexFrom > -1) {
				int indexTo = e.toString().indexOf("scope.$apply();");
				code = e.toString().substring(
						indexFrom + ("scope.code." + language + " = '").length(), indexTo - 7);
			}
		}
		return Utils.decode(code);
	}

	private void writeSubmissionToDisk(SubmissionObj so) {
		File file = new File(Config.get("dirpath") + so.getQuestion().getTitle());
		if (!file.exists() && !file.mkdirs()) {
			return;
		}
		String filePath = Config.get("dirpath") + so.getQuestion().getTitle() + "/"
				+ so.getStatus().replace(' ', '-');
		if (so.getStatus().equals("Accepted")) {
			filePath += "-" + so.getRuntime().replaceAll(" ", "");
		}
		filePath += "-" + so.getServerID() + so.getCodeExtension();
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filePath, false),
					"UTF-8");
			osw.write(so.getCodeWithComment());
			osw.close();
		} catch (IOException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}
	}
}
