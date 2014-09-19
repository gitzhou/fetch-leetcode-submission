package cc.aaron67.fetch.leetcode.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	/**
	 * 含有Unicode转义字符（\\uXXXX）的字符串解码
	 * 
	 * @param s
	 *            String 待解码的字符串
	 * @return 解码后的字符串
	 * 
	 * @see http://netwjx.github.io/blog/2012/07/07/encode-and-decode-unicode-escape
	 *      -string/
	 */
	public static String decode(String s) {
		Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
		Matcher m = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find()) {
			m.appendReplacement(sb,
					Character.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
