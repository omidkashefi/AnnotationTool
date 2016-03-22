package edu.pitt.cs.revision.reviewLinking;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ReviewItem {
	private int start;
	private int end;
	private String content;
	private List<ReviewTarget> targets;
	private List<ReviewSolution> solutions;
	private String parentContent;
	private String type;

	public String getType() {
		return type;
	}

	public String getParentContent() {
		return parentContent;
	}

	public void setParentContent(String parentContent) {
		this.parentContent = parentContent;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ReviewItem() {
		targets = new ArrayList<ReviewTarget>();
		solutions = new ArrayList<ReviewSolution>();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getContent() {
		return content;
	}

	public String getTargetStr() {
		String targetStr = "";
		for (ReviewTarget t : targets) {
			targetStr += t.getContent() + "|";
		}
		if (targetStr.length() != 0)
			targetStr = targetStr.substring(0, targetStr.length() - 1);
		return targetStr;
	}

	public void setTarget(String targetStr) {
		this.targets = new ArrayList<ReviewTarget>();
		StringTokenizer stk = new StringTokenizer(targetStr, "|");
		while (stk.hasMoreTokens()) {
			String target = stk.nextToken();
			ReviewTarget rt = new ReviewTarget();
			rt.setContent(target);
			this.targets.add(rt);
		}
	}

	public String getSolutionStr() {
		String solutionStr = "";
		for (ReviewSolution s : solutions) {
			solutionStr += s.getContent() + "|";
		}
		if (solutionStr.length() != 0) {
			solutionStr = solutionStr.substring(0, solutionStr.length() - 1);
		}
		return solutionStr;
	}

	public void setSolutions(String solutionStr) {
		this.solutions = new ArrayList<ReviewSolution>();
		StringTokenizer stk = new StringTokenizer(solutionStr, "|");
		while (stk.hasMoreTokens()) {
			String solution = stk.nextToken();
			ReviewSolution rs = new ReviewSolution();
			rs.setContent(solution);
			this.solutions.add(rs);
		}
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<ReviewTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<ReviewTarget> targets) {
		this.targets = targets;
	}

	public List<ReviewSolution> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<ReviewSolution> solutions) {
		this.solutions = solutions;
	}

	public void addTarget(ReviewTarget target) {
		this.targets.add(target);
	}

	public void addSolution(ReviewSolution solution) {
		this.solutions.add(solution);
	}

	public String toString() {
		String str = "Review Content:" + this.content + "\n";
		str += "Review Type:" + this.type + "\n";
		for (ReviewTarget target : targets) {
			str += "Review Target:" + target.toString() + "\n";
		}
		for (ReviewSolution solution : solutions) {
			str += "Review Solution:" + solution.toString() + "\n";
		}
		return str;
	}
}
