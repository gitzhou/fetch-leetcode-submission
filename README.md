# 抓取 LeetCode OJ 个人提交的代码
使用
Gradle 构建项目
jsoup、HttpClient 抓取、解析网页

## 说明
* 在 `cc.aaron67.fetch.leetcode.page.UserConfig` 类中配置
```
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
```
* 运行 `cc.aaron67.fetch.leetcode.main.Main` 类中的 `main()` 方法抓取代码
* 输出的代码文件，内容依次为
   * `本项目信息`
   * `题目标题`
   * `题目URL`
   * `你提交的代码`
   * `题目内容`

## TODO
* 第三方平台授权登录的用户代码抓取
* 多线程抓取

## LICENSE
项目基于[GPL协议](http://www.gnu.org/licenses/gpl.html)发布