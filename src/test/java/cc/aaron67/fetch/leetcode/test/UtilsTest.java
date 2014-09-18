package cc.aaron67.fetch.leetcode.test;

import org.apache.log4j.Logger;
import org.junit.Test;

import cc.aaron67.fetch.leetcode.utils.Utils;

public class UtilsTest {

	private final static Logger logger = Logger.getLogger(UtilsTest.class);

	@Test
	public void getWebPageTest() {
		String url = "http://oj.leetcode.com/submissions";
		try {
			Utils.getWebPage(url);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
