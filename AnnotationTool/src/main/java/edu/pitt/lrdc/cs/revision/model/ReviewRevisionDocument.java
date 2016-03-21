package edu.pitt.lrdc.cs.revision.model;

import java.util.ArrayList;
import java.util.List;

public class ReviewRevisionDocument {
	List<ReviewItemRevision> reviewRevisions = new ArrayList<ReviewItemRevision>();
	private String docName;
	
	public String hashCode(ReviewItemRevision rr) {
		String code = rr.getItem().getContent()+"_OLD_"+rr.getOldIndiceStr()+"_NEW_"+rr.getNewIndiceStr();
		return code;
	}
	
	public List<ReviewItemRevision> getReviewRevisions() {
		return reviewRevisions;
	}

	public void setReviewRevisions(List<ReviewItemRevision> reviewRevisions) {
		this.reviewRevisions = reviewRevisions;
	}


	public String getDocName() {
		return docName;
	}


	public void setDocName(String docName) {
		this.docName = docName;
	}


	public void addReviewItemRevision(ReviewItemRevision rr) {
		this.reviewRevisions.add(rr);
	}
}
