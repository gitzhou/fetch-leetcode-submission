package cc.aaron67.fetch.leetcode.model;

public class SubmissionObj {
	private QuestionObj question;
	private CodeObj code;
	private String status;
	private String runtime;
	private String language;

	public QuestionObj getQuestion() {
		return question;
	}

	public void setQuestion(QuestionObj question) {
		this.question = question;
	}

	public CodeObj getCode() {
		return code;
	}

	public void setCode(CodeObj code) {
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
}
