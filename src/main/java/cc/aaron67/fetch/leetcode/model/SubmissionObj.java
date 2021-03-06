package cc.aaron67.fetch.leetcode.model;

public class SubmissionObj {
    private String questionTitle; // 该提交对应的题目标题
    private String questionURL; // 题目的URL
    private String code; // 代码
    private String status; // 代码运行状态
    private String runtime; // 代码执行时间
    private String language; // 代码语言
    private String serverID; // 提交记录在服务端的ID号

    /**
     * 返回代码文件的扩展名
     */
    public String getCodeExtension() {
        String extension;
        switch (language) {
        case "cpp":
            extension = ".cpp";
            break;
        case "java":
            extension = ".java";
            break;
        case "python":
            extension = ".py";
            break;
        case "c":
            extension = ".c";
            break;
        case "csharp":
            extension = ".cs";
            break;
        case "javascript":
            extension = ".js";
            break;
        case "ruby":
            extension = ".rb";
            break;
        case "mysql":
            extension = ".sql";
            break;
        default:
            extension = "";
        }
        return extension;
    }

    /**
     * 返回带有额外注释的代码内容
     */
    public String getCodeWithComment() {
        String comment;
        switch (language) {
        case "cpp":
            comment = "// ";
            break;
        case "java":
            comment = "// ";
            break;
        case "python":
            comment = "# ";
            break;
        case "c":
            comment = "// ";
            break;
        case "csharp":
            comment = "// ";
            break;
        case "javascript":
            comment = "// ";
            break;
        case "ruby":
            comment = "# ";
            break;
        case "mysql":
            comment = "# ";
            break;
        default:
            return code;
        }

        StringBuilder builder = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        builder.append(comment).append(lineSeparator);
        builder.append(comment).append("Generated by fetch-leetcode-submission project on GitHub.")
                .append(lineSeparator);
        builder.append(comment).append("https://github.com/gitzhou/fetch-leetcode-submission").append(lineSeparator);
        builder.append(comment).append("Contact Me: aaron67[AT]aaron67.cc").append(lineSeparator);
        builder.append(comment).append(lineSeparator);
        builder.append(comment).append(questionTitle).append(lineSeparator);
        builder.append(comment).append(questionURL).append(lineSeparator);
        builder.append(comment).append(lineSeparator).append(lineSeparator);
        builder.append(code).append(lineSeparator).append(lineSeparator);
        return builder.toString();
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionURL() {
        return questionURL;
    }

    public void setQuestionURL(String questionURL) {
        this.questionURL = questionURL;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }
}
