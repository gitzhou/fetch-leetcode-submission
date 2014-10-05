# 抓取 LeetCode OJ 个人提交的代码
* Gradle 构建项目
* HttpClient、jsoup 抓取、解析网页

## 使用说明
* 进入`release`文件夹
* 修改`init.properties` 中的配置
```
    #登录账号
	username=your_username

	#登录密码
	password=your_password

	#登录类型，值暂时只能为leetcode或github
	logintype=leetcode

	#抓取的代码在本地的存储目录
	dirpath=/path/to/your/own/directory/

	#是否抓取所有的提交代码
	#为假时参考集合的取值有选择的抓取
	isfetchall=false;

	#抓取与集合中状态匹配的代码
	tags=Accepted,Wrong Answer,Time Limit Exceeded
```
* 命令行中运行 `java -jar FetchLeetcodeSubmission.jar` 抓取代码
* 输出的代码文件，内容依次为
   * `本项目信息`
   * `题目标题`
   * `题目URL`
   * `你提交的代码`
   * `题目内容`

## 项目说明
* 使用`gradle release`可直接打包最新项目到`release`文件夹
```
jar {
    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
    manifest {
        attributes 'Main-Class': 'cc.aaron67.fetch.leetcode.main.Main'
    }
}

task release(type: Copy) {
	from 'build/libs'
	into 'release'
}

task copyConfig(type: Copy) {
	from 'init.properties'
	into 'release'
}

release.dependsOn 'build', 'copyConfig'
```

## TODO
* ~~项目打包~~
* ~~已抓取的记录不再抓取~~
* ~~提交记录的统计信息~~
* 第三方平台授权登录的用户代码抓取
  * ~~GitHub登录~~
  * Facebook登录
  * Google登录
  * LinkedIn登录
* 多线程抓取

## LICENSE
项目基于[GPL协议](http://www.gnu.org/licenses/gpl.html)发布