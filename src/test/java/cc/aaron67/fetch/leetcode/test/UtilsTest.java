package cc.aaron67.fetch.leetcode.test;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.junit.Test;

import cc.aaron67.fetch.leetcode.utils.HttpUtils;

public class UtilsTest {
	private static Logger logger = Logger.getLogger(UtilsTest.class);

	@Test
	public void testGet() {
		HttpResponse response = HttpUtils.get("https://oj.leetcode.com/", null);
		logger.info(response.toString());
	}
}
