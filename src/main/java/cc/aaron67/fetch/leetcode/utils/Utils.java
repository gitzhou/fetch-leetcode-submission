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

	/**
	 * 替换字符串
	 * 
	 * @param source
	 *            String 母字符串
	 * @param from
	 *            String 原始字符串
	 * @param to
	 *            Character 目标字符
	 * @return String 替换后的字符串
	 */
	public static String replace(String source, String from, Character to) {
		if (source == null || from == null || to == null) {
			return null;
		}
		StringBuffer bf = new StringBuffer("");
		int index = -1;
		while ((index = source.indexOf(from)) != -1) {
			bf.append(source.substring(0, index) + to);
			source = source.substring(index + from.length());
			index = source.indexOf(from);
		}
		bf.append(source);
		return bf.toString();
	}
}
