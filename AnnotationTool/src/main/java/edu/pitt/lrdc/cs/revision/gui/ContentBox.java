package edu.pitt.lrdc.cs.revision.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Utilities;

import org.apache.poi.poifs.property.Parent;

import edu.pitt.lrdc.cs.revision.model.RevisionOp;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

public class ContentBox extends Box {
	JTextArea newSentence;
	JTextArea oldSentence;
	Map<Integer, Integer> newStrToHighlight;
	Map<Integer, Integer> oldStrToHighlight;
	Map<Integer, Integer> matchingToHighlight;
	Map<Integer, Integer> matchingToHighlightReverse;

	private ArrayList<SubsententialRevisionUnit> subsententialUnits;
	SubsententialRevisionUnit currentUnit = null;
	
	private AdvBaseLevelPanelV4 parentPanel;

	public ContentBox(int axis) {
		super(axis);

		subsententialUnits = new ArrayList<SubsententialRevisionUnit>();
		
		newSentence = new JTextArea("Sentence from NEW version:\n");
		oldSentence = new JTextArea("Sentence from the OLD version:\n");
		newSentence.setRows(3);
		oldSentence.setRows(3);
		oldSentence.setLineWrap(true);
		newSentence.setLineWrap(true);
		newSentence.setEditable(false);
		oldSentence.setEditable(false);

		newStrToHighlight = new HashMap<Integer, Integer>();
		oldStrToHighlight = new HashMap<Integer, Integer>();

		matchingToHighlight = new HashMap<Integer, Integer>();
		matchingToHighlightReverse = new HashMap<Integer, Integer>();

		oldSentence.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int oldOffset = oldSentence.viewToModel(e.getPoint());

				Integer matchingIndex = null;
				Highlighter oldHighlighter = oldSentence.getHighlighter();
				for (Highlighter.Highlight h : oldHighlighter.getHighlights()) {
					oldHighlighter.removeHighlight(h);
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);

					if (h.getStartOffset() <= oldOffset && h.getEndOffset() >= oldOffset) {
						painter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
						matchingIndex = matchingToHighlight.get(h.getStartOffset());
					}

					try {
						oldHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
				
				//revert the other box highlights
				Highlighter newHighlighter = newSentence.getHighlighter();
				for (Highlighter.Highlight h : newHighlighter.getHighlights()) {
					newHighlighter.removeHighlight(h);
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);

					if (matchingIndex != null && h.getStartOffset() == matchingIndex) {
						painter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
					}

					try {
						newHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
			}
		});

		newSentence.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int newOffset = newSentence.viewToModel(e.getPoint());

				Integer matchingIndex = null;
				Highlighter newHighlighter = newSentence.getHighlighter();
				for (Highlighter.Highlight h : newHighlighter.getHighlights()) {
					newHighlighter.removeHighlight(h);
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);

					if (h.getStartOffset() <= newOffset && h.getEndOffset() >= newOffset) {
						painter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
						matchingIndex = matchingToHighlightReverse.get(h.getStartOffset());
					}

					try {
						newHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
				
				//revert the other box highlights
				Highlighter oldHighlighter = oldSentence.getHighlighter();
				for (Highlighter.Highlight h : oldHighlighter.getHighlights()) {
					oldHighlighter.removeHighlight(h);
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);

					if (matchingIndex != null && h.getStartOffset() == matchingIndex) {
						painter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
					}
					
					try {
						oldHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}

			}
		});

		JScrollPane newPane = new JScrollPane(newSentence);
		JScrollPane oldPane = new JScrollPane(oldSentence);

		add(oldPane);
		add(newPane);
		
	}
	
	public ContentBox(int axis, AdvBaseLevelPanelV4 parent) {
		super(axis);

		this.parentPanel = parent;
		
		subsententialUnits = new ArrayList<SubsententialRevisionUnit>();
		
		newSentence = new JTextArea("Sentence from NEW version:\n");
		oldSentence = new JTextArea("Sentence from the OLD version:\n");
		newSentence.setRows(3);
		oldSentence.setRows(3);
		oldSentence.setLineWrap(true);
		newSentence.setLineWrap(true);
		newSentence.setEditable(false);
		oldSentence.setEditable(false);

		newStrToHighlight = new HashMap<Integer, Integer>();
		oldStrToHighlight = new HashMap<Integer, Integer>();

		matchingToHighlight = new HashMap<Integer, Integer>();
		matchingToHighlightReverse = new HashMap<Integer, Integer>();

		//mouse event on old draft box
		oldSentence.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				//store previous selection's annotation
				storeSubSententialAnnotation();
				
				//Default selection color
				Color selectColor = Color.darkGray;
				
				//Default color for non-annotated parts
				Color revisionColor = Color.gray;
	
				//get mouse pointer to sentence offset
				int oldOffset = oldSentence.viewToModel(e.getPoint());
				
				//subsentential revision span
				Span oldDraftHighlightSpan = new Span();
				
				//matching pair index in new draft
				Integer matchingIndex = null;
				
				//matching subsentential revision unit
				SubsententialRevisionUnit matchingSSR = null;

				//clicked subsentential revision unit
				SubsententialRevisionUnit clickedSSR = null;

				//reset the annotation box selection per click
				parentPanel.annotateBox.reload(matchingSSR);

				Highlighter oldHighlighter = oldSentence.getHighlighter();
				for (Highlighter.Highlight h : oldHighlighter.getHighlights()) {

					oldHighlighter.removeHighlight(h);

					//Default color for non-annotated parts
					revisionColor = Color.gray;
					
					//check if highlighted area is already annotated
					for (SubsententialRevisionUnit sru : subsententialUnits) {
						if (sru.oldDraft.contatins(h.getStartOffset())) {
							//matching area is already annotated
							matchingSSR = sru;
							//use corresponding color
							revisionColor = ColorConstants.getColor(sru.RevisionPurpose());
							break;
						}
						matchingSSR = null;
					}
					
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(revisionColor);

					//if a subsentential revision is clicked 
					if (h.getStartOffset() <= oldOffset && h.getEndOffset() >= oldOffset) {
						
						painter = new DefaultHighlighter.DefaultHighlightPainter(selectColor);

						//---- find the corresponding subsentential revision in new draft to highlight and annotate
						
						//highlight the matching phrase
						matchingIndex = matchingToHighlight.get(h.getStartOffset());

						//set old draft span
						oldDraftHighlightSpan = new Span(h.getStartOffset(), h.getEndOffset());
						
						//set clicked subsentential unit
						clickedSSR = matchingSSR;
					}

					//highlight
					try {
						oldHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
				
				//set the annotation box selection for the clicked area
				parentPanel.annotateBox.reload(clickedSSR);
				
				//--- looking for matching pair in new draft
				
				Span newDraftHighlightSpan = new Span();

				Highlighter newHighlighter = newSentence.getHighlighter();
				for (Highlighter.Highlight h : newHighlighter.getHighlights()) {

					//Default color for non-annotated parts
					revisionColor = Color.gray;

					newHighlighter.removeHighlight(h);
					
					//if any of subsentential revisions already annotated
					for (SubsententialRevisionUnit sru : subsententialUnits) {
						//use corresponding color
						if (sru.newDraft.contatins(h.getStartOffset())) {
							revisionColor = ColorConstants.getColor(sru.RevisionPurpose());
						}
					}


					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(revisionColor);

					//if corresponding subsentential revision found
					if (matchingIndex != null && h.getStartOffset() == matchingIndex) {
						
						painter = new DefaultHighlighter.DefaultHighlightPainter(selectColor);

						//set new draft matching span
						newDraftHighlightSpan = new Span(h.getStartOffset(), h.getEndOffset());
					}

					//highlight
					try {
						newHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
				
				//a subsentential revision being selected
				if (oldDraftHighlightSpan.length() != 0 || newDraftHighlightSpan.length() != 0) {
					int revision_op = RevisionOp.MODIFY;
					if (oldDraftHighlightSpan.length() != 0)
						revision_op = RevisionOp.ADD;
					if (newDraftHighlightSpan.length() != 0)
						revision_op = RevisionOp.DELETE;
					
					
					//store current selection as un-annotated
					currentUnit = new SubsententialRevisionUnit(oldDraftHighlightSpan, newDraftHighlightSpan, -1, revision_op);
				}
				else {
					currentUnit = null;
				}
					
			}
		});

		//mouse event on new draft box
		newSentence.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
				//store previous selection's annotation
				storeSubSententialAnnotation();
				
				//Default selection color
				Color selectColor = Color.darkGray;
				
				//Default color for non-annotated parts
				Color revisionColor = Color.gray;
	
				//get mouse pointer to sentence offset
				int newOffset = newSentence.viewToModel(e.getPoint());
				
				//subsentential revision span
				Span newDraftHighlightSpan = new Span();
				
				//matching pair index in new draft
				Integer matchingIndex = null;
				
				//matching subsentential revision unit
				SubsententialRevisionUnit matchingSSR = null;

				//clicked subsentential revision unit
				SubsententialRevisionUnit clickedSSR = null;

				//reset the annotation box selection per click
				parentPanel.annotateBox.reload(matchingSSR);

				Highlighter newHighlighter = newSentence.getHighlighter();
				for (Highlighter.Highlight h : newHighlighter.getHighlights()) {

					newHighlighter.removeHighlight(h);

					//Default color for non-annotated parts
					revisionColor = Color.gray;
					
					//check if highlighted area is already annotated
					for (SubsententialRevisionUnit sru : subsententialUnits) {
						if (sru.newDraft.contatins(h.getStartOffset())) {
							//matching area is already annotated
							matchingSSR = sru;
							//use corresponding color
							revisionColor = ColorConstants.getColor(sru.RevisionPurpose());
							break;
						}
						matchingSSR = null;
					}
					
					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(revisionColor);

					//if a subsentential revision is clicked 
					if (h.getStartOffset() <= newOffset && h.getEndOffset() >= newOffset) {
						
						painter = new DefaultHighlighter.DefaultHighlightPainter(selectColor);

						//---- find the corresponding subsentential revision in new draft to highlight and annotate
						
						//highlight the matching phrase
						matchingIndex = matchingToHighlightReverse.get(h.getStartOffset());

						//set old draft span
						newDraftHighlightSpan = new Span(h.getStartOffset(), h.getEndOffset());
						
						//set clicked subsentential unit
						clickedSSR = matchingSSR;
					}

					//highlight
					try {
						newHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}

				//set the annotation box selection for the clicked area
				parentPanel.annotateBox.reload(clickedSSR);
				
				//--- looking for matching pair in old draft
				
				Span oldDraftHighlightSpan = new Span();

				Highlighter oldHighlighter = oldSentence.getHighlighter();
				for (Highlighter.Highlight h : oldHighlighter.getHighlights()) {

					//Default color for non-annotated parts
					revisionColor = Color.gray;

					oldHighlighter.removeHighlight(h);
					
					//if any of subsentential revisions already annotated
					for (SubsententialRevisionUnit sru : subsententialUnits) {
						//use corresponding color
						if (sru.oldDraft.contatins(h.getStartOffset())) {
							revisionColor = ColorConstants.getColor(sru.RevisionPurpose());
						}
					}


					HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(revisionColor);

					//if corresponding subsentential revision found
					if (matchingIndex != null && h.getStartOffset() == matchingIndex) {
						
						painter = new DefaultHighlighter.DefaultHighlightPainter(selectColor);

						//set new draft matching span
						oldDraftHighlightSpan = new Span(h.getStartOffset(), h.getEndOffset());
					}

					//highlight
					try {
						oldHighlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painter);
					} catch (BadLocationException exp) {
						// TODO: handle exception
					}
				}
				
				//a subsentential revision being selected
				if (oldDraftHighlightSpan.length() != 0 || newDraftHighlightSpan.length() != 0) {
					int revision_op = RevisionOp.MODIFY;
					if (oldDraftHighlightSpan.length() != 0)
						revision_op = RevisionOp.ADD;
					if (newDraftHighlightSpan.length() != 0)
						revision_op = RevisionOp.DELETE;
					
					
					//store current selection as un-annotated
					currentUnit = new SubsententialRevisionUnit(oldDraftHighlightSpan, newDraftHighlightSpan, -1, revision_op);
				}
				else {
					currentUnit = null;
				}

			}
		});

		JScrollPane newPane = new JScrollPane(newSentence);
		JScrollPane oldPane = new JScrollPane(oldSentence);

		add(oldPane);
		add(newPane);
		
	}
	
	private void storeSubSententialAnnotation() {
		
		//get annotation selection
		ArrayList<SelectionUnit> sul = parentPanel.annotateBox.getSelectedUnits();
		if (currentUnit == null || sul.isEmpty())
			return;
		
		//annotate current selection
		currentUnit.setRevisionPurpose(sul.get(0).revision_purpose);

		//if current selection is already annotated
		for (SubsententialRevisionUnit sru : subsententialUnits) {
			if (sru.oldDraft.contatins(currentUnit.oldDraft.start)) {
				//remove it
				subsententialUnits.remove(sru);
				break;
			}
		}

		//add current selection to the array
		subsententialUnits.add(currentUnit);

	}

	public void setSentneces(String oldSent, String newSent) {

		this.newStrToHighlight.clear();
		this.oldStrToHighlight.clear();
		this.matchingToHighlight.clear();
		this.matchingToHighlightReverse.clear();
		
		diff_match_patch dmp = new diff_match_patch();

		LinkedList<diff_match_patch.Diff> diff = dmp.diff_WordMode(oldSent, newSent);

		// dmp.diff_cleanupSemantic(diff);

		String newStr = "";
		String oldStr = "";
		Boolean match = false;
		Integer lastIndex = 0;

		for (diff_match_patch.Diff d : diff) {
			if (d.operation == Operation.EQUAL) {
				newStr += d.text;
				oldStr += d.text;
				match = false;
			} else if (d.operation == Operation.DELETE) {
				if (match) {
					this.matchingToHighlight.put(oldStr.length(), lastIndex);
					this.matchingToHighlightReverse.put(lastIndex, oldStr.length());
				}
				
				match = !match;
				lastIndex = oldStr.length();
				
				this.oldStrToHighlight.put(lastIndex, d.text.length() - 1);
				oldStr += d.text;

			} else if (d.operation == Operation.INSERT) {
				if (match) {
					this.matchingToHighlight.put(lastIndex, newStr.length());
					this.matchingToHighlightReverse.put(newStr.length(), lastIndex);
				}
				
				match = !match;
				lastIndex = newStr.length();

				this.newStrToHighlight.put(lastIndex, d.text.length() - 1);
				newStr += d.text;
			}

		}

		this.oldSentence.setText(oldStr);
		Highlighter oldHighlighter = this.oldSentence.getHighlighter();
		HighlightPainter oldPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
		for (Map.Entry<Integer, Integer> ent : this.oldStrToHighlight.entrySet()) {
			try {
				oldHighlighter.addHighlight(ent.getKey(), ent.getKey() + ent.getValue(), oldPainter);
			} catch (BadLocationException e) {
				// TODO: handle exception
			}
		}

		this.newSentence.setText(newStr);
		Highlighter newHighlighter = this.newSentence.getHighlighter();
		HighlightPainter newPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
		for (Map.Entry<Integer, Integer> ent : this.newStrToHighlight.entrySet()) {
			try {
				newHighlighter.addHighlight(ent.getKey(), ent.getKey() + ent.getValue(), newPainter);
			} catch (BadLocationException e) {
				// TODO: handle exception
			}
		}

	}

	/*
	 * public void setNewSentence(String sent) {
	 * this.newSentence.setText("Sentence from NEW version:\n"+sent); }
	 * 
	 * public void setOldSentence(String sent) {
	 * this.oldSentence.setText("Sentence from the OLD version:\n"+sent); }
	 */
}
