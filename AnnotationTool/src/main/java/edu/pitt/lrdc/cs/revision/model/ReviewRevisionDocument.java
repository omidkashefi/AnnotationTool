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
	
	public String getIndiceStr(ArrayList<Integer> indices) {
		String indiceStr = "";
		for (Integer oldIndex : indices) {
			if (oldIndex != -1) {
				indiceStr += Integer.toString(oldIndex) + "\t";
			}
		}
		return indiceStr.trim();
	}
	
	public List<ReviewItemRevision> getRelatedReviews(RevisionUnit ru) {
		String oldIndexStr = getIndiceStr(ru.getOldSentenceIndex());
		String newIndexStr = getIndiceStr(ru.getNewSentenceIndex());
		List<ReviewItemRevision> reviewList = new ArrayList<ReviewItemRevision>();
		for(ReviewItemRevision rr: reviewRevisions) {
			if(rr.getOldIndiceStr().equals(oldIndexStr) && rr.getNewIndiceStr().equals(newIndexStr)) {
				reviewList.add(rr);
			}
		}
		return reviewList;
	}

	public List<ReviewItemRevision> getRelatedReviews(ArrayList<Integer> oldIndices, ArrayList<Integer> newIndices) {
		String oldIndexStr = getIndiceStr(oldIndices);
		String newIndexStr = getIndiceStr(newIndices);
		List<ReviewItemRevision> reviewList = new ArrayList<ReviewItemRevision>();
		for(ReviewItemRevision rr: reviewRevisions) {
			if(rr.getOldIndiceStr().equals(oldIndexStr) && rr.getNewIndiceStr().equals(newIndexStr)) {
				reviewList.add(rr);
			}
		}
		return reviewList;
	}
	
	
	public void removeReviews(ArrayList<Integer> oldIndices, ArrayList<Integer> newIndices) {
		String oldIndexStr = getIndiceStr(oldIndices);
		String newIndexStr = getIndiceStr(newIndices);
		List<ReviewItemRevision> toRemove = new ArrayList<ReviewItemRevision>();
		for(ReviewItemRevision rr: reviewRevisions) {
			if(rr.getOldIndiceStr().equals(oldIndexStr) && rr.getNewIndiceStr().equals(newIndexStr)) {
				toRemove.add(rr);
			}
		}
		for(ReviewItemRevision rr: toRemove) {
			reviewRevisions.remove(rr);
		}
	}
	
	public String getRelatedReviewStr(ArrayList<Integer> oldIndices, ArrayList<Integer> newIndices) {
		List<ReviewItemRevision> reviewList = getRelatedReviews(oldIndices, newIndices);
		String reviewStr = "";
		for(ReviewItemRevision review: reviewList) {
			reviewStr += review.getItem().getContent() + "\n";
		}
		return reviewStr;
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
		rr.setDocName(this.docName);
	}
}
