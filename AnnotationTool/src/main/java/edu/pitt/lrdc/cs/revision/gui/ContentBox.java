package edu.pitt.lrdc.cs.revision.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Utilities;

import org.apache.poi.poifs.property.Parent;

import edu.pitt.lrdc.cs.revision.model.RevisionOp;
import edu.pitt.lrdc.cs.revision.model.RevisionPurpose;
import edu.pitt.lrdc.cs.revision.model.RevisionUnit;
import edu.pitt.lrdc.cs.revision.model.Span;
import edu.pitt.lrdc.cs.revision.model.SubsententialRevisionUnit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
	JTextArea pairSentence1;
	JTextArea pairSentence2;
	JScrollPane pairSentence1SB;
	JScrollPane pairSentence2SB;
	Box sentenceBox;
	Map<Integer, Integer> matchingToHighlight;
	Map<Integer, Integer> matchingToHighlightReverse;
	
	Boolean pairPanelClicked;

	private ArrayList<SubsententialRevisionUnit> subsententialUnits;
	SubsententialRevisionUnit currentUnit = null;
	int sentenceLevelRevisionPurpose;
	
	private AdvBaseLevelPanelV4 parentPanel;

	// Javadoc comment follows
    /**
     * @deprecated
     * Now with subsentential annotation most of the work is done in this class
     * so we need to have access to other classes from parent
     * I kept this for backward compatibility and keep the code and older interfaces still working
     * Omid Kashefi
     */
    @Deprecated
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
		
		this.pairPanelClicked = false;
		sentenceLevelRevisionPurpose = RevisionPurpose.UNANNOTATED;
		
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
		
	
		matchingToHighlight = new HashMap<Integer, Integer>();
		matchingToHighlightReverse = new HashMap<Integer, Integer>();

		//mouse event on old draft box
		oldSentence.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
				oldSentence.setFont(pairSentence1.getFont().deriveFont(Font.BOLD));
				newSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
				pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
				pairSentence2.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));

				//store previous selection's annotation
				registerSubSententialAnnotation();
				
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
						if (sru.oldDraft().contatins(h.getStartOffset())) {
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
						if (sru.newDraft().contatins(h.getStartOffset())) {
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
				
				oldSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
				newSentence.setFont(pairSentence1.getFont().deriveFont(Font.BOLD));
				pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
				pairSentence2.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));

				//store previous selection's annotation
				registerSubSententialAnnotation();
				
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
						if (sru.newDraft().contatins(h.getStartOffset())) {
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
						if (sru.oldDraft().contatins(h.getStartOffset())) {
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

		pairSentence1 = new JTextArea();
		pairSentence2 = new JTextArea();
		pairSentence1.setRows(1);
		pairSentence2.setRows(1);
		pairSentence1.setEditable(false);
		pairSentence2.setEditable(false);
		pairSentence1SB = new JScrollPane(pairSentence1, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pairSentence2SB = new JScrollPane(pairSentence2);
		Box sentenceBox = new Box(BoxLayout.X_AXIS);
		sentenceBox.add(pairSentence1SB);
		sentenceBox.add(pairSentence2SB);
		

		add(oldPane);
		add(newPane);
		add(sentenceBox);
		
		//annotate sentence level
		pairSentence1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pairPanelClicked = true;
//				if (sentenceLevelRevisionPurpose != RevisionPurpose.UNANNOTATED) {
					//make selected subsentnetial unit, unselected
					currentUnit = null;

					pairSentence1.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
					pairSentence2.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
					pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.BOLD));
					pairSentence2.setFont(pairSentence2.getFont().deriveFont(Font.BOLD));
					
					oldSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
					newSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));

					parentPanel.annotateBox.reload(sentenceLevelRevisionPurpose);

	//			}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				//register subsentnetial unit first
				registerSubSententialAnnotation();
				//if trying to annotate sentence level
				if (pairPanelClicked) { 
					if (sentenceLevelRevisionPurpose != RevisionPurpose.UNANNOTATED)
						pairPanelClicked = false;
					//get annotation selection
					ArrayList<SelectionUnit> sul = parentPanel.annotateBox.getSelectedUnits();
					if (!sul.isEmpty()) {
						sentenceLevelRevisionPurpose = sul.get(0).revision_purpose;
						pairSentence1.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
						pairSentence2.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
						pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
						pairSentence2.setFont(pairSentence2.getFont().deriveFont(Font.PLAIN));
						return;
					}
				}
			}
		});
		pairSentence2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pairPanelClicked = true;
				//make selected subsentnetial unit, unselected
				currentUnit = null;

				pairSentence1.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
				pairSentence2.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
				pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.BOLD));
				pairSentence2.setFont(pairSentence2.getFont().deriveFont(Font.BOLD));

				oldSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
				newSentence.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));

				parentPanel.annotateBox.reload(sentenceLevelRevisionPurpose);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				//register subsentnetial unit first
				registerSubSententialAnnotation();
				//if trying to annotate sentence level
				if (pairPanelClicked) { 
					if (sentenceLevelRevisionPurpose != RevisionPurpose.UNANNOTATED)
						pairPanelClicked = false;
					//get annotation selection
					ArrayList<SelectionUnit> sul = parentPanel.annotateBox.getSelectedUnits();
					if (!sul.isEmpty()) {
						sentenceLevelRevisionPurpose = sul.get(0).revision_purpose;
						pairSentence1.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
						pairSentence2.setBackground(ColorConstants.getColor(sentenceLevelRevisionPurpose));
						pairSentence1.setFont(pairSentence1.getFont().deriveFont(Font.PLAIN));
						pairSentence2.setFont(pairSentence2.getFont().deriveFont(Font.PLAIN));
						return;
					}
				}
			}
		});
	}
	
	private void registerSubSententialAnnotation() {
		
		//get annotation selection
		ArrayList<SelectionUnit> sul = parentPanel.annotateBox.getSelectedUnits();
		if (this.currentUnit == null || sul.isEmpty())
			return;
		
		//annotate current selection
		this.currentUnit.setRevisionPurpose(sul.get(0).revision_purpose);

		//if current selection is already annotated
		for (SubsententialRevisionUnit sru : subsententialUnits) {
			if (sru.oldDraft().contatins(this.currentUnit.oldDraft().start())) {
				//remove it
				subsententialUnits.remove(sru);
				break;
			}
		}

		//add current selection to the array
		subsententialUnits.add(this.currentUnit);
	}
	
	private void findDiffnHighlight(String oldSent, String newSent) {
		Map<Integer, Integer> newStrToHighlight = new HashMap<Integer, Integer>();
		Map<Integer, Integer> oldStrToHighlight = new HashMap<Integer, Integer>();
		
		diff_match_patch dmp = new diff_match_patch();

		LinkedList<diff_match_patch.Diff> diff = dmp.diff_WordMode(oldSent, newSent);

		// dmp.diff_cleanupSemantic(diff);

		String newStr = "";
		String oldStr = "";
		Boolean match = false;
		Integer lastIndex = 0;
		Span lastSpan = new Span(-1, -1);
		//find the diff
		for (diff_match_patch.Diff d : diff) {
			if (d.operation == Operation.EQUAL) {
				newStr += d.text;
				oldStr += d.text;
				match = false;
			} else if (d.operation == Operation.DELETE) {
				Span nd = new Span(-1, -1);
				if (match) {
					this.matchingToHighlight.put(oldStr.length(), lastIndex);
					this.matchingToHighlightReverse.put(lastIndex, oldStr.length());
					nd = lastSpan;
				}
				
				match = !match;
				lastIndex = oldStr.length();
				
				oldStrToHighlight.put(lastIndex, d.text.length() - 1);
				oldStr += d.text;
				
				Span od = new Span(lastIndex, lastIndex+d.text.length()-1);
				lastSpan = new Span(lastIndex, lastIndex+d.text.length()-1);
				
				Boolean addUnit = true;
				SubsententialRevisionUnit su = new SubsententialRevisionUnit(od, nd, RevisionPurpose.UNANNOTATED, RevisionOp.DELETE);
				for (SubsententialRevisionUnit sru : subsententialUnits) {
					if (sru.newDraft().contatins(su.newDraft().start())) {
						if (su.oldDraft().start() == -1) {
							addUnit = false;
							break;
						}
						if (sru.oldDraft().start() == -1) {
							//remove it
							subsententialUnits.remove(sru);
							subsententialUnits.add(su);
							break;
						}
					}
				}
				if (addUnit)
					subsententialUnits.add(su);

			} else if (d.operation == Operation.INSERT) {
				Span od = new Span(-1, -1);
				if (match) {
					this.matchingToHighlight.put(lastIndex, newStr.length());
					this.matchingToHighlightReverse.put(newStr.length(), lastIndex);
					od = lastSpan;
				}
				
				match = !match;
				lastIndex = newStr.length();

				newStrToHighlight.put(lastIndex, d.text.length() - 1);
				newStr += d.text;

				Span nd = new Span(lastIndex, lastIndex+d.text.length()-1);
				lastSpan = new Span(lastIndex, lastIndex+d.text.length()-1);

				Boolean addUnit = true;
				SubsententialRevisionUnit su = new SubsententialRevisionUnit(od, nd, RevisionPurpose.UNANNOTATED, RevisionOp.ADD);
				for (SubsententialRevisionUnit sru : subsententialUnits) {
					if (sru.oldDraft().contatins(su.oldDraft().start())) {
						if (su.newDraft().start() == -1) {
							addUnit = false;
							break;
						}
						else if (sru.newDraft().start() == -1) {
							//remove it
							subsententialUnits.remove(sru);
							break;
						}
					}
				}
				if (addUnit)
					subsententialUnits.add(su);
			}

		}

		this.oldSentence.setText(oldStr);
		Highlighter oldHighlighter = this.oldSentence.getHighlighter();
		HighlightPainter oldPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
		for (Map.Entry<Integer, Integer> ent : oldStrToHighlight.entrySet()) {
			try {
				oldHighlighter.addHighlight(ent.getKey(), ent.getKey() + ent.getValue(), oldPainter);
			} catch (BadLocationException e) {
				// TODO: handle exception
			}
		}

		this.newSentence.setText(newStr);
		Highlighter newHighlighter = this.newSentence.getHighlighter();
		HighlightPainter newPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
		for (Map.Entry<Integer, Integer> ent : newStrToHighlight.entrySet()) {
			try {
				newHighlighter.addHighlight(ent.getKey(), ent.getKey() + ent.getValue(), newPainter);
			} catch (BadLocationException e) {
				// TODO: handle exception
			}
		}
	}
	
	// Javadoc comment follows
    /**
     * @deprecated
     * I kept this for backward compatibility and keep the code and older interfaces still working
     * I need subsentential revisions to show proper annotations
     * Omid Kashefi
     */
	public void setSentneces(String oldSent, String newSent) {

		this.matchingToHighlight.clear();
		this.matchingToHighlightReverse.clear();

		findDiffnHighlight(oldSent, newSent);
	}

	public void setSentneces(String oldSent, String newSent, RevisionUnit ru) {
	
		this.matchingToHighlight.clear();
		this.matchingToHighlightReverse.clear();
		this.subsententialUnits.clear();
		this.subsententialUnits.addAll(ru.getSubsententialUnits());
		this.currentUnit = null;
		this.sentenceLevelRevisionPurpose = ru.getRevision_purpose();

		
		this.pairPanelClicked = false;
		
		this.pairSentence1.setText(oldSent);
		this.pairSentence2.setText(newSent);

		if (ru.getSubsententialUnits().isEmpty()) {
			//find subsentential revisions automatically and highlight accordingly
			findDiffnHighlight(oldSent, newSent);
		}
		else {
			//highlight based on annotation
			
			this.oldSentence.setText(oldSent);
			Highlighter oldHighlighter = this.oldSentence.getHighlighter();

			this.newSentence.setText(newSent);
			Highlighter newHighlighter = this.newSentence.getHighlighter();

			for (SubsententialRevisionUnit sru : ru.getSubsententialUnits()) {

				//keep track of old-new matching pairs
				if (sru.oldDraft().start() != -1 && sru.newDraft().start() != -1) {
					this.matchingToHighlight.put(sru.oldDraft().start(), sru.newDraft().start());
					this.matchingToHighlightReverse.put(sru.newDraft().start(), sru.oldDraft().start());
				}

				//get revision color
				HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(ColorConstants.getColor(sru.RevisionPurpose()));

				try {
					//highlight oldDraft
					if (sru.oldDraft().start() != -1)
						oldHighlighter.addHighlight(sru.oldDraft().start(), sru.oldDraft().end(), painter);
					
					//highlight oldDraft
					if (sru.newDraft().start() != -1)
						newHighlighter.addHighlight(sru.newDraft().start(), sru.newDraft().end(), painter);
				} catch (BadLocationException e) {
					// TODO: handle exception
				}
			}
			
		
		}
		
		if (ru.getRevision_purpose() != RevisionPurpose.UNANNOTATED) {
			pairSentence1.setBackground(ColorConstants.getColor(ru.getRevision_purpose()));
			pairSentence2.setBackground(ColorConstants.getColor(ru.getRevision_purpose()));
			sentenceLevelRevisionPurpose = ru.getRevision_purpose();
		}
		else {
			pairSentence1.setBackground(Color.white);
			pairSentence2.setBackground(Color.white);
			sentenceLevelRevisionPurpose = RevisionPurpose.UNANNOTATED;
		}
			

		
		pairSentence1SB.setMaximumSize(new Dimension(newSentence.getWidth()/2, newSentence.getMinimumSize().height*2));
		pairSentence2SB.setMaximumSize(new Dimension(newSentence.getWidth()/2, newSentence.getMinimumSize().height*2));
		pairSentence1SB.setMinimumSize(new Dimension(newSentence.getWidth()/2, newSentence.getMinimumSize().height*2));
		pairSentence2SB.setMinimumSize(new Dimension(newSentence.getWidth()/2, newSentence.getMinimumSize().height*2));
		//oldSentence.setCaretPosition(0);
		//pane2.getHorizontalScrollBar().setValue(10);


	}

	public RevisionUnit getAnnotations() {
		RevisionUnit ru = new RevisionUnit(parentPanel.doc.getRoot());
		
		ru.setSubsententialUnits(this.subsententialUnits);
		
		ru.setRevision_purpose(this.sentenceLevelRevisionPurpose);

		return ru;
	}
}
