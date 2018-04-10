package edu.pitt.lrdc.cs.revision.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.*;

import edu.pitt.cs.revision.reviewLinking.ReviewItem;
import edu.pitt.lrdc.cs.revision.model.ReviewDocument;
import edu.pitt.lrdc.cs.revision.model.ReviewRevisionDocument;
import edu.pitt.lrdc.cs.revision.model.RevisionDocument;
import edu.pitt.lrdc.cs.revision.model.RevisionOp;
import edu.pitt.lrdc.cs.revision.model.RevisionUnit;

public class AdvBaseLevelPanelV4 extends JPanel implements LevelPanel {
	JList sentenceList; // old
	JList newSentenceList; // new
	JSplitPane splitPane;

	RevisionDocument doc; // Data model
	ReviewRevisionDocument reviewDoc;
	Hashtable<String, List<ReviewItem>> reviewTable;

	JButton changeAlignmentButton; // Change alignment
	JButton reviewAlignmentButton;
	JPanel buttonPanel;
	Box sentenceBox;
	AnnotateBox annotateBox;// For annotate the purposes and operations
	ContentBox annotateContentDetail;
	ColoredListWrapperV2 wrapper;
	// LevelDemoPanel levelPanel;

	ArrayList<RevisionUnit> currentRU = null;

	DraftDisplayPanel ddp;

	public void setDisplay(DraftDisplayPanel ddp) {
		this.ddp = ddp;
	}

	String highlightOld = "";
	String highlightNew = "";

	public void highlight() {

		ddp.highLight(true, highlightOld);
		ddp.highLight(false, highlightNew);
	}

	// ArrayList<Integer> currentOldSentenceIndex;
	// ArrayList<Integer> currentNewSentenceIndex;
	@Override
	public void registerRevision() {
		//ArrayList<SelectionUnit> sus = annotateBox.getSelectedUnits();
		if (currentRU == null || currentRU.size() == 0) {
			// do nothing
			System.err.println("Do nothing");
		} else {
			ArrayList<Integer> oldSentenceIndex = currentRU.get(0)
					.getOldSentenceIndex();
			ArrayList<Integer> newSentenceIndex = currentRU.get(0)
					.getNewSentenceIndex();

			// the same units will not be processed
			// the new units will be registered and the old units will be
			// removed

			RevisionUnit annotatedRU = annotateContentDetail.getAnnotations();

			// First remove the unexisting old
			for (RevisionUnit ru : currentRU) {
				boolean isExist = false;
				if (annotatedRU.getRevision_purpose() == ru.getRevision_purpose() &&
					annotatedRU.getSubsententialUnits().equals(ru.getSubsententialUnits())) {
					isExist = true;
					break;
				}
				if (!isExist) {
					ru.setAbandoned();
				}
			}

			// Now add the new stuff
			boolean isExist = false;
			for (RevisionUnit ru : currentRU) {
				if (annotatedRU.getRevision_purpose() == ru.getRevision_purpose() &&
					annotatedRU.getSubsententialUnits().equals(ru.getSubsententialUnits())) {
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				RevisionUnit newUnit = new RevisionUnit(doc.getRoot());
				newUnit.setOldSentenceIndex(oldSentenceIndex);
				newUnit.setRevision_op(RevisionOp.MODIFY);
				if (oldSentenceIndex != null
						&& oldSentenceIndex.size() != 0) {
					String oldSentence = "";
					for (Integer oldIndex : oldSentenceIndex) {
						if (oldIndex != -1)
							oldSentence += doc.getOldSentence(oldIndex)
									+ "\n";
					}
					newUnit.setOldSentence(oldSentence);
				} else {
					newUnit.setRevision_op(RevisionOp.ADD);
				}
				newUnit.setNewSentenceIndex(newSentenceIndex);
				if (newSentenceIndex != null
						&& newSentenceIndex.size() != 0) {
					String newSentence = "";
					for (Integer newIndex : newSentenceIndex) {
						if (newIndex != -1)
							newSentence += doc.getNewSentence(newIndex)
									+ "\n";
					}
					newUnit.setNewSentence(newSentence);
				} else {
					newUnit.setRevision_op(RevisionOp.DELETE);
				}
				// newUnit.setRevision_op(su.revision_op);
				newUnit.setRevision_purpose(annotatedRU.getRevision_purpose());
				//add subsentential units
				newUnit.setSubsententialUnits(annotatedRU.getSubsententialUnits());

				newUnit.setRevision_level(0);
				newUnit.setRevision_index(doc.getRoot().getNextIndexAtLevel(0));
				doc.getRoot().addUnit(newUnit);
				wrapper.changePurpose(newUnit);
			}

		}
		doc.check();
		doc.getRoot().clear();
		ddp.reload();
	}

	boolean changeAlignment = false;

	boolean compareArr(int[] a1, int[] a2) {
		// Debug info
		/*
		 * for (Integer i : a1) System.out.print(i + "\t");
		 * System.out.println(); for (Integer i : a2) System.out.print(i +
		 * "\t"); System.out.println();
		 */
		HashSet<Integer> set = new HashSet<Integer>();
		for (Integer i : a1) {
			if (i != -1)
				set.add(i);
		}

		HashSet<Integer> set2 = new HashSet<Integer>();
		for (Integer i : a2) {
			if (i != -1)
				set2.add(i);
		}

		if (set.size() != set2.size())
			return false;
		for (Integer i : set) {
			if (!set2.contains(i))
				return false;
		}
		return true;
	}

	/*
	 * private void restoreDefaults() { SwingUtilities.invokeLater(new
	 * Runnable() {
	 * 
	 * @Override public void run() {
	 * splitPane.setDividerLocation(splitPane.getSize().width /2);
	 * //mainSplittedPane.setDividerLocation(mainSplittedPane.getSize().width
	 * /2); } }); }
	 */

	public AdvBaseLevelPanelV4(RevisionDocument doc,
			ReviewRevisionDocument reviewDoc,
			Hashtable<String, List<ReviewItem>> reviewTable) {
		this.doc = doc;
		this.reviewDoc = reviewDoc;
		this.reviewTable = reviewTable;
		wrapper = new ColoredListWrapperV2(this);
		// ListSelectionHandler listHandler = new ListSelectionHandler();
		// sentenceList.getSelectionModel().addListSelectionListener(listHandler);
		// newSentenceList.getSelectionModel().addListSelectionListener(
		// listHandler);
		sentenceList = wrapper.getOldSentenceList();
		newSentenceList = wrapper.getNewSentenceList();
		wrapper.paint();
		JScrollPane pane = new JScrollPane(sentenceList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		JScrollPane newPane = new JScrollPane(newSentenceList);
		JScrollBar sBar2 = pane.getVerticalScrollBar();
		sBar2.setModel(newPane.getVerticalScrollBar().getModel());

		annotateBox = new AnnotateBox();
		// sentenceList.setSize(this.getWidth()/2, this.getHeight()/2);
		/*
		 * JPanel sentenceBoxPanel = new JPanel();
		 * sentenceBoxPanel.add(sentenceList);
		 * 
		 * JPanel newSentenceBoxPanel = new JPanel();
		 * newSentenceBoxPanel.add(newSentenceList);
		 */
		// JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// sentenceBoxPanel, newSentenceBoxPanel);
		// splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane,
		// newPane);

		// splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// sentenceBoxPanel, newSentenceBoxPanel);
		// splitPane.setDividerLocation(0.5);
		// splitPane.setResizeWeight(.5d);

		// JScrollPane splitScroll = new JScrollPane(splitPane);
		sentenceBox = new Box(BoxLayout.X_AXIS);
		sentenceBox.add(pane);
		sentenceBox.add(newPane);
		// sentenceBox.add(splitPane);
		// sentenceBox.add(splitScroll);
		annotateContentDetail = new ContentBox(BoxLayout.Y_AXIS, this);
		// levelPanel = new LevelDemoPanel(doc, 0);
		// levelPanel.boundPanel(this);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// GridBagConstraints c = new GridBagConstraints();
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.gridx = 0;
		// c.gridy = 0;
		// c.gridheight = 4;
		changeAlignmentButton = new JButton("Change alignment");
		changeAlignmentButton.setToolTipText("Change the sentence alignment");
		changeAlignmentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				/*
				 * JFrame frame = new JFrame("Change alignment");
				 * frame.setSize(800, 600); int[] oldIndices =
				 * sentenceList.getSelectedIndices(); int[] newIndices =
				 * newSentenceList.getSelectedIndices(); ArrayList<Integer>
				 * oldIndiceArr = new ArrayList<Integer>(); ArrayList<Integer>
				 * newIndiceArr = new ArrayList<Integer>(); for(Integer
				 * oldIndex: oldIndices) oldIndiceArr.add(oldIndex+1);
				 * for(Integer newIndex: newIndices)
				 * newIndiceArr.add(newIndex+1);
				 * 
				 * frame.setContentPane(new AlignmentChangePanel(doc,
				 * oldIndiceArr, newIndiceArr)); frame.show();
				 */
				showAlign();
			}
		});

		reviewAlignmentButton = new JButton("Review alignment");
		reviewAlignmentButton.setToolTipText("Change the review alignment");
		reviewAlignmentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				/*
				 * JFrame frame = new JFrame("Change alignment");
				 * frame.setSize(800, 600); int[] oldIndices =
				 * sentenceList.getSelectedIndices(); int[] newIndices =
				 * newSentenceList.getSelectedIndices(); ArrayList<Integer>
				 * oldIndiceArr = new ArrayList<Integer>(); ArrayList<Integer>
				 * newIndiceArr = new ArrayList<Integer>(); for(Integer
				 * oldIndex: oldIndices) oldIndiceArr.add(oldIndex+1);
				 * for(Integer newIndex: newIndices)
				 * newIndiceArr.add(newIndex+1);
				 * 
				 * frame.setContentPane(new AlignmentChangePanel(doc,
				 * oldIndiceArr, newIndiceArr)); frame.show();
				 */
				showReviewAlign();
			}
		});

		add(sentenceBox);
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(changeAlignmentButton);
		buttonPanel.add(reviewAlignmentButton);
		add(buttonPanel);
		add(annotateContentDetail);
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.gridx = 0;
		// c.gridy = 4;
		// c.gridheight = 4;
		add(annotateBox);

		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.gridx = 0;
		// c.gridy = 8;
		// c.gridheight = 2;

		// c.gridheight = 1;
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.gridx = 0;
		// c.gridy = 10;
		// add(levelPanel);
		// restoreDefaults();
	}

	public void showAlign() {
		JFrame frame = new JFrame("Change alignment");
		frame.setSize(1200, 800);
		/*
		 * int[] oldIndices = sentenceList.getSelectedIndices(); int[]
		 * newIndices = newSentenceList.getSelectedIndices(); ArrayList<Integer>
		 * oldIndiceArr = new ArrayList<Integer>(); ArrayList<Integer>
		 * newIndiceArr = new ArrayList<Integer>(); for(Integer oldIndex:
		 * oldIndices) oldIndiceArr.add(oldIndex+1); for(Integer newIndex:
		 * newIndices) newIndiceArr.add(newIndex+1);
		 */
		// ArrayList<Integer> oldIndiceArr = wrapper.getOldSelectedIndexes();
		// ArrayList<Integer> newIndiceArr = wrapper.getNewSelectedIndexes();
		// AlignmentChangePanel acp = new AlignmentChangePanel(doc,
		// oldIndiceArr, newIndiceArr);
		AlignmentChangePanelV2P acp = new AlignmentChangePanelV2P(doc);
		acp.setListWrapper(wrapper);
		frame.setContentPane(acp);
		frame.show();
	}

	public void showReviews() {
		if (reviewDoc != null) {
			String reviewStr = reviewDoc.getRelatedReviewStr(
					wrapper.getOldSelectedIndexes(),
					wrapper.getNewSelectedIndexes());
			if (reviewStr == null || reviewStr.length() == 0) {
				annotateBox.displayReviews("No reviews aligned");
			} else {
				annotateBox.displayReviews(reviewStr);
			}
		}
	}

	public void showReviewAlign() {
		JFrame frame = new JFrame("Review alignment");
		frame.setSize(1200, 800);
		/*
		 * int[] oldIndices = sentenceList.getSelectedIndices(); int[]
		 * newIndices = newSentenceList.getSelectedIndices(); ArrayList<Integer>
		 * oldIndiceArr = new ArrayList<Integer>(); ArrayList<Integer>
		 * newIndiceArr = new ArrayList<Integer>(); for(Integer oldIndex:
		 * oldIndices) oldIndiceArr.add(oldIndex+1); for(Integer newIndex:
		 * newIndices) newIndiceArr.add(newIndex+1);
		 */
		// ArrayList<Integer> oldIndiceArr = wrapper.getOldSelectedIndexes();
		// ArrayList<Integer> newIndiceArr = wrapper.getNewSelectedIndexes();
		// AlignmentChangePanel acp = new AlignmentChangePanel(doc,
		// oldIndiceArr, newIndiceArr);
		if (reviewTable != null) {
			ReviewChangePanel acp = new ReviewChangePanel(doc, reviewDoc,
					wrapper.getOldSelectedIndexes(),
					wrapper.getNewSelectedIndexes(),reviewTable);
			frame.setContentPane(acp);
			frame.show();
		} else {
			JOptionPane.showMessageDialog(this,
					"Does not have reviews assigned", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
