package cc.aaron67.fetch.leetcode.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.aaron67.fetch.leetcode.model.SubmissionObj;
import cc.aaron67.fetch.leetcode.utils.Config;
import cc.aaron67.fetch.leetcode.utils.HttpUtils;
import cc.aaron67.fetch.leetcode.utils.Utils;

public class Leetcode {
    public final static String HOME_PAGE_URL = "https://leetcode.com";
    public final static String LOGIN_PAGE_URL = "https://leetcode.com/accounts/login/";
    public final static String LOGIN_VIA_GITHUB_PAGE_URL = "https://leetcode.com/accounts/github/login/";
    public final static String SUBMISSIONS_API_FORMAT = "/api/submissions/?offset=%d&limit=%d";

    private static Logger logger = Logger.getLogger(Leetcode.class);

    private String csrftoken = "DLs592YH48QUgXUWpa6aoS5nbgGXhl8z";
    private String leetcodeSessionID = "";

    private Set<String> tags = null;
    // 已抓取到本地的提交记录的ID
    private Set<String> ids = new HashSet<String>();

    // 统计数据
    private Set<String> passedProblems = new HashSet<String>();
    private int totalSubmissions = 0;
    private int totalAccepted = 0;

    public Leetcode() {
        tags = new HashSet<String>(Arrays.asList(Config.get("tags").split(",")));
        loadLocalSubmissionID();
    }

    public void process() {
        logger.info("抓取开始");
        if (login()) {
            try {
                JSONParser parser = new JSONParser();
                int offset = 0, limit = 20;
                boolean hasNext = true;
                while (hasNext) {
                    String api = String.format(HOME_PAGE_URL + SUBMISSIONS_API_FORMAT, offset, limit);
                    JSONObject response = (JSONObject) parser.parse(fetchPage(api));
                    hasNext = (boolean) response.get("has_next");
                    JSONArray submissions = (JSONArray) response.get("submissions_dump");
                    for (Object obj : submissions) { // 对每一次的提交
                        JSONObject s = (JSONObject) obj;
                        ++totalSubmissions;
                        String url = s.get("url").toString();
                        // 提交记录在服务端ID号
                        int firstLast = url.length() - 1, secondLast = url.lastIndexOf('/', firstLast - 1);
                        String id = url.substring(secondLast + 1, firstLast);
                        // 本次提交状态
                        String status = s.get("status_display").toString();
                        if (status.equals("Accepted")) {
                            passedProblems.add(s.get("title").toString());
                            ++totalAccepted;
                        }
                        // 筛选抓取的记录
                        if (Config.get("isfetchall").equals("false") && !tags.contains(status) || ids.contains(id)) {
                            logger.info("跳过 [" + s.get("time").toString() + "] 的提交 >>>> ");
                            continue;
                        }
                        ids.add(id);

                        SubmissionObj so = new SubmissionObj();
                        logger.info("抓取 [" + s.get("time").toString() + "] 的提交 >>>> ");
                        // 题目标题
                        so.setQuestionTitle(s.get("title").toString());
                        // 代码执行状态
                        so.setStatus(status);
                        // 代码执行时间
                        so.setRuntime(s.get("runtime").toString());
                        // 代码语言
                        so.setLanguage(s.get("lang").toString().toLowerCase());
                        // 代码内容和题目地址
                        buildCodeAndQuestionURL(HOME_PAGE_URL + url, so);
                        // 提交记录在服务端ID号
                        so.setServerID(id);
                        // 持久化到硬盘
                        writeSubmissionToDisk(so);
                    } // for
                    offset += limit;
                } // while
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        syncLocalSubmissionID();
        logger.info("抓取结束");
        handleStatistics();
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
                        } else if (element.getName() != null && element.getName().equals("LEETCODE_SESSION")) {
                            leetcodeSessionID = element.getValue();
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
    private boolean loginViaGithub() {
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
            headers.put("Cookie", "csrftoken=" + csrftoken + ";LEETCODE_SESSION=" + leetcodeSessionID);
            response = HttpUtils.getWithoutAutoRedirect(LOGIN_VIA_GITHUB_PAGE_URL, headers);
            if (response.getStatusLine().getStatusCode() == 302) {
                for (Header h : response.getHeaders("Set-Cookie")) {
                    for (HeaderElement he : h.getElements()) {
                        if (he != null && he.getName().equals("LEETCODE_SESSION")) {
                            leetcodeSessionID = he.getValue();
                        }
                    }
                }
                // 302 ==> github.com/login/oauth/...
                headers.put("Referer", LOGIN_VIA_GITHUB_PAGE_URL);
                String location = response.getFirstHeader("Location").getValue();
                headers.put("Cookie", "_gh_sess=" + _gh_sess + ";dotcom_user=" + dotcom_user + ";logged_in=" + logged_in
                        + ";user_session=" + user_session);
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
                    // 302 ==> leetcode.com/accounts/github/login/callback/..
                    headers.put("Referer", location);
                    headers.put("Cookie", "csrftoken=" + csrftoken + ";LEETCODE_SESSION=" + leetcodeSessionID);
                    location = response.getFirstHeader("Location").getValue();
                    String messages = "";
                    response = HttpUtils.getWithoutAutoRedirect(location, headers);
                    if (response.getStatusLine().getStatusCode() == 302) {
                        for (Header h : response.getHeaders("Set-Cookie")) {
                            for (HeaderElement he : h.getElements()) {
                                if (he != null && he.getName().equals("csrftoken")) {
                                    csrftoken = he.getValue();
                                } else if (he != null && he.getName().equals("LEETCODE_SESSION")) {
                                    leetcodeSessionID = he.getValue();
                                } else if (he != null && he.getName().equals("messages")) {
                                    messages = he.getValue();
                                }
                            }
                        }
                        // 302 ==> leetcode.com/
                        headers.put("Cookie", "csrftoken=" + csrftoken + ";LEETCODE_SESSION=" + leetcodeSessionID
                                + ";messages=" + messages);
                        headers.put("Referer", location);
                        location = response.getFirstHeader("Location").getValue();
                        response = HttpUtils.getWithoutAutoRedirect(HOME_PAGE_URL + location, headers);
                        if (response.getStatusLine().getStatusCode() == 200) {
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
     * 抓取地址为URL页面的源码
     */
    private String fetchPage(String url) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", "csrftoken=" + csrftoken + ";LEETCODE_SESSION=" + leetcodeSessionID);
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

    private void buildCodeAndQuestionURL(String url, SubmissionObj so) {
        Document doc = Jsoup.parse(fetchPage(url));
        // code
        Elements es = doc.getElementsByTag("script");
        String code = null;
        for (Element e : es) {
            int indexFrom = e.toString().indexOf("submissionCode: '");
            if (indexFrom > -1) {
                int indexTo = e.toString().indexOf("editCodeUrl");
                code = e.toString().substring(indexFrom + ("submissionCode: '").length(), indexTo - 5);
            }
        }
        so.setCode(Utils.decode(code));
        // URL
        so.setQuestionURL(HOME_PAGE_URL + doc.select("h4").select("a").get(0).attr("href"));
    }

    private void writeSubmissionToDisk(SubmissionObj so) {
        File file = new File(Config.get("dirpath") + so.getQuestionTitle());
        if (!file.exists() && !file.mkdirs()) {
            return;
        }
        String filePath = Config.get("dirpath") + so.getQuestionTitle() + "/" + so.getStatus().replace(' ', '-');
        if (so.getStatus().equals("Accepted")) {
            filePath += "-" + so.getRuntime().replaceAll(" ", "");
        }
        filePath += "-" + so.getServerID() + so.getCodeExtension();
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filePath, false), "UTF-8");
            osw.write(so.getCodeWithComment());
            osw.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载已抓取到本地的提交记录的ID
     */
    private void loadLocalSubmissionID() {
        try {
            logger.info("加载已抓取到本地的提交记录的ID");
            File file = new File(System.getProperty("user.dir") + "/ids.txt");
            if (!file.exists()) {
                return;
            }
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                ids.add(line);
            }
            br.close();
        } catch (Exception e) {
            logger.error("读文件出错" + System.getProperty("line.separator") + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 本次抓取结束后，同步更新本地的提交记录的ID
     */
    private void syncLocalSubmissionID() {
        try {
            logger.info("同步更新本地的提交记录的ID");
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(System.getProperty("user.dir") + "/ids.txt", false), "UTF-8");
            Iterator<String> iter = ids.iterator();
            while (iter.hasNext()) {
                osw.write(iter.next() + System.getProperty("line.separator"));
            }
            osw.close();
        } catch (Exception e) {
            logger.error("写文件出错" + System.getProperty("line.separator") + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 打印统计信息到屏幕并写硬盘
     */
    private void handleStatistics() {
        try {
            // 打印屏幕
            logger.info("============= Statistics ==============");
            logger.info(String.format("%22s", "Total Submissions:  ") + totalSubmissions);
            logger.info(String.format("%22s", "Solved Problems:  ") + passedProblems.size());
            logger.info(String.format("%22s", "Total Accepted:  ") + totalAccepted);
            if (totalSubmissions != 0) {
                logger.info(String.format("%22s", "AC Rates:  ")
                        + new BigDecimal((totalAccepted * 1.0 / totalSubmissions) * 100).setScale(2,
                                RoundingMode.HALF_UP)
                        + "%");
            }
            logger.info("=======================================");

            // 写文件
            logger.info("更新本地的统计信息");
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(Config.get("dirpath") + "statistics.txt", false), "UTF-8");
            osw.write("============= Statistics ==============" + System.getProperty("line.separator"));
            osw.write(String.format("%22s", "Total Submissions:  ") + totalSubmissions
                    + System.getProperty("line.separator"));
            osw.write(String.format("%22s", "Solved Problems:  ") + passedProblems.size()
                    + System.getProperty("line.separator"));
            osw.write(
                    String.format("%22s", "Total Accepted:  ") + totalAccepted + System.getProperty("line.separator"));
            if (totalSubmissions != 0) {
                osw.write(String.format("%22s", "AC Rates:  ")
                        + new BigDecimal((totalAccepted * 1.0 / totalSubmissions) * 100).setScale(2,
                                RoundingMode.HALF_UP)
                        + "%" + System.getProperty("line.separator"));
            }
            osw.write("=======================================" + System.getProperty("line.separator"));
            osw.close();
        } catch (Exception e) {
            logger.error("写文件出错" + System.getProperty("line.separator") + e.getMessage());
            e.printStackTrace();
        }
    }
}
