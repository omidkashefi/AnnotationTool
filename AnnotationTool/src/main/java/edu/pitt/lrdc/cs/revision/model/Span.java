package edu.pitt.lrdc.cs.revision.model;

public class Span {
	int start = -1;
	int end = -1;
	
	public Span() {
		
	}
	
	public Span(int s, int e) {
		this.start = s;
		this.end = e;
	}
	
	public int start() {
		return this.start;
	}
	
	public int end() {
		return this.end;
	}
	
	public int length() {
		return start - end;
	}
	
	public Boolean contatins(int offset) {
		if ((this.start <= offset) && (this.end >= offset))
			return true;
		
		return false;
	}
	
	public void setStart(int s) {
		this.start = s;
	}

	public void setEnd(int e) {
		this.start = e;
	}
}
