package edu.pitt.lrdc.cs.revision.model;

public class SubsententialRevisionUnit {
	Span oldDraft;
	Span newDraft;

	// Revision operation and revision purpose
	private int revision_op = -1; // revision operation
	private int revision_purpose = -1; // revision purpose

	
	public SubsententialRevisionUnit(Span od, Span nd, int rp, int ro) {
		this.oldDraft = od;
		this.newDraft = nd;
		this.revision_purpose = rp;
		this.revision_op = ro;
	}
	
	public Span oldDraft() {
		return this.oldDraft;
	}
	
	public Span newDraft() {
		return this.newDraft;
	}
	
	public int RevisionPurpose() {
		return this.revision_purpose;
	}
	
	public int RevisionOperation() {
		return this.revision_op;
	}
	
	public void setRevisionPurpose(int rp) {
		this.revision_purpose = rp;
	}

	public void setRevisionOperation(int op) {
		this.revision_op = op;
	}
}

