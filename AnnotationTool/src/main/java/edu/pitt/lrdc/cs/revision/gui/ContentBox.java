package edu.pitt.lrdc.cs.revision.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Utilities;

import org.apache.poi.poifs.property.Parent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.Color;
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

	public ContentBox(int axis) {
		super(axis);

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

				Integer matchingIndex = 0;
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

				Integer matchingIndex = 0;
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
