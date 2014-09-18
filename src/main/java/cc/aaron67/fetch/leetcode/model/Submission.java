package cc.aaron67.fetch.leetcode.model;

import java.util.List;

public class Submission {
	private Question question;
	private String status;
	private String runtime;
	private String language;
	private List<String> code;

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
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

	public List<String> getCode() {
		return code;
	}

	public void setCode(List<String> code) {
		this.code = code;
	}
}
