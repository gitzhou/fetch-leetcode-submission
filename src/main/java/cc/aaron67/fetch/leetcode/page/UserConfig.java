package cc.aaron67.fetch.leetcode.page;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UserConfig {
	// LeetCode OJ 账号
	public final static String USER_NAME = "";

	// LeetCode OJ 密码
	public final static String PASSWORD = "";

	// 本地存储目录（字符串最后有一个 /）
	public final static String DIR_PATH = "/path/to/your/own/directory/";

	// 是否抓取所有的提交代码
	// 为 false 时参考 STATUS 集合的取值
	public final static boolean IS_FETCH_ALL = false;

	// 当 IS_FETCH_ALL 为 false 时，抓取匹配集合中状态的提交代码
	public final static Set<String> STATUS = new HashSet<String>(
			Arrays.asList(new String[] { "Accepted" }));
}
