package edu.pitt.lrdc.cs.revision.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import edu.pitt.cs.revision.reviewLinking.ReviewItem;
import edu.pitt.lrdc.cs.revision.model.ReviewDocument;
import edu.pitt.lrdc.cs.revision.model.ReviewItemRevision;
import edu.pitt.lrdc.cs.revision.model.ReviewRevisionDocument;
import edu.pitt.lrdc.cs.revision.model.RevisionDocument;
import edu.pitt.lrdc.cs.revision.model.RevisionPurpose;
import edu.pitt.lrdc.cs.revision.model.RevisionUnit;
/**
 * Review linking panel
 * @author zhangfan
 *
 */

//code borrowed from http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm
class CheckBoxNodeRenderer implements TreeCellRenderer {
	  private JCheckBox leafRenderer = new JCheckBox();

	  private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();

	  Color selectionBorderColor, selectionForeground, selectionBackground,
	      textForeground, textBackground;

	  ReviewChangePanel rcp;
	  public void setReviewChangePanel(ReviewChangePanel rcp) {
		  this.rcp = rcp;
	  }
	  
	  protected JCheckBox getLeafRenderer() {
	    return leafRenderer;
	  }

	  public CheckBoxNodeRenderer() {
	    Font fontValue;
	    fontValue = UIManager.getFont("Tree.font");
	    if (fontValue != null) {
	      leafRenderer.setFont(fontValue);
	    }
	    Boolean booleanValue = (Boolean) UIManager
	        .get("Tree.drawsFocusBorderAroundIcon");
	    leafRenderer.setFocusPainted((booleanValue != null)
	        && (booleanValue.booleanValue()));

	    selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
	    selectionForeground = UIManager.getColor("Tree.selectionForeground");
	    selectionBackground = UIManager.getColor("Tree.selectionBackground");
	    textForeground = UIManager.getColor("Tree.textForeground");
	    textBackground = UIManager.getColor("Tree.textBackground");
	  }

	  public Component getTreeCellRendererComponent(JTree tree, Object value,
	      boolean selected, boolean expanded, boolean leaf, int row,
	      boolean hasFocus) {

	    Component returnValue;
	    if (leaf) {

	      String stringValue = tree.convertValueToText(value, selected,
	          expanded, leaf, row, false);
	      leafRenderer.setText(stringValue);
	      leafRenderer.setSelected(false);

	      leafRenderer.setEnabled(tree.isEnabled());

	      if (selected) {
	        leafRenderer.setForeground(selectionForeground);
	        leafRenderer.setBackground(selectionBackground);
	      } else {
	        leafRenderer.setForeground(textForeground);
	        leafRenderer.setBackground(textBackground);
	      }

	      if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
	        Object userObject = ((DefaultMutableTreeNode) value)
	            .getUserObject();
	        if (userObject instanceof CheckBoxNode) {
	          CheckBoxNode node = (CheckBoxNode) userObject;
	          leafRenderer.setText(node.getText());
	          leafRenderer.setSelected(node.isSelected());
	          if(rcp!=null)
	          rcp.showDetail(node.getText());
	        }
	      }
	      returnValue = leafRenderer;
	    } else {
	      returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree,
	          value, selected, expanded, leaf, row, hasFocus);
	    }
	    return returnValue;
	  }
	}

	class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	  CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

	  ChangeEvent changeEvent = null;

	  JTree tree;

	  public CheckBoxNodeEditor(JTree tree) {
	    this.tree = tree;
	  }
	  
	  ReviewChangePanel rcp;
	  public void setReviewChangePanel(ReviewChangePanel rcp) {
		  this.rcp = rcp;
	  }

	  public Object getCellEditorValue() {
	    JCheckBox checkbox = renderer.getLeafRenderer();
	    CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(),
	        checkbox.isSelected());
	    return checkBoxNode;
	  }

	  public boolean isCellEditable(EventObject event) {
	    boolean returnValue = false;
	    if (event instanceof MouseEvent) {
	      MouseEvent mouseEvent = (MouseEvent) event;
	      TreePath path = tree.getPathForLocation(mouseEvent.getX(),
	          mouseEvent.getY());
	      if (path != null) {
	        Object node = path.getLastPathComponent();
	        if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
	          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
	          Object userObject = treeNode.getUserObject();
	          returnValue = ((treeNode.isLeaf()) && (userObject instanceof CheckBoxNode));
	        }
	      }
	    }
	    return returnValue;
	  }

	  public Component getTreeCellEditorComponent(JTree tree, Object value,
	      boolean selected, boolean expanded, boolean leaf, int row) {

	    Component editor = renderer.getTreeCellRendererComponent(tree, value,
	        true, expanded, leaf, row, true);

	    // editor always selected / focused
	    ItemListener itemListener = new ItemListener() {
	      public void itemStateChanged(ItemEvent itemEvent) {
	        if (stopCellEditing()) {
	          fireEditingStopped();
	         
	        }
	        
	      }
	    };
	    if (editor instanceof JCheckBox) {
	      ((JCheckBox) editor).addItemListener(itemListener);
	    }

	    return editor;
	  }
	}

	class CheckBoxNode {
	  String text;

	  boolean selected;

	  public CheckBoxNode(String text, boolean selected) {
	    this.text = text;
	    this.selected = selected;
	  }

	  public boolean isSelected() {
	    return selected;
	  }

	  public void setSelected(boolean newValue) {
	    selected = newValue;
	  }

	  public String getText() {
	    return text;
	  }

	  public void setText(String newValue) {
	    text = newValue;
	  }

	  public String toString() {
	    return getClass().getName() + "[" + text + "/" + selected + "]";
	  }
	}

	class NamedVector extends Vector {
	  String name;

	  public NamedVector(String name) {
	    this.name = name;
	  }

	  public NamedVector(String name, Object elements[]) {
	    this.name = name;
	    for (int i = 0, n = elements.length; i < n; i++) {
	      add(elements[i]);
	    }
	  }

	  public String toString() {
	    return "[" + name + "]";
	  }
	}


public class ReviewChangePanel extends JPanel {
	private ReviewRevisionDocument rd;
	private RevisionDocument doc;

	private JTree tree;

	private JTextArea revisionDetailPane;
	private JTextArea detailPane;

	private JButton confirmButton;
	private JButton cancelButton;

	private ArrayList<Integer> oldIndices;
	private ArrayList<Integer> newIndices;

	private Hashtable<String, ReviewItem> nodeMap = new Hashtable<String, ReviewItem>();

	public ReviewChangePanel(RevisionDocument doc, ReviewRevisionDocument rd,
			ArrayList<Integer> oldIndices, ArrayList<Integer> newIndices,
			Hashtable<String, List<ReviewItem>> reviewTable) {
		this.rd = rd;
		this.doc = doc;
		this.oldIndices = oldIndices;
		this.newIndices = newIndices;

		Object[] rootNodes = new Object[reviewTable.size()];
		Iterator<String> it = reviewTable.keySet().iterator();

		int index = 0;
		while (it.hasNext()) {
			String key = it.next();
			List<ReviewItem> reviewItems = reviewTable.get(key);
			CheckBoxNode options[] = new CheckBoxNode[reviewItems.size()];
			for (int i = 0; i < reviewItems.size(); i++) {
				CheckBoxNode node = new CheckBoxNode(reviewItems.get(i)
						.getContent(), false);
				options[i] = node;
				nodeMap.put(reviewItems.get(i).getContent(), reviewItems.get(i));
			}
			Vector vector = new NamedVector(key, options);
			rootNodes[index] = vector;
			index++;
		}

		tree = new JTree(rootNodes);
		tree.setEnabled(true);
		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		tree.setCellRenderer(renderer);
		renderer.setReviewChangePanel(this);
		
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
	    tree.setEditable(true);

	    JPanel revisionDetailPanel = new JPanel();
		revisionDetailPane = new JTextArea(5,100);
		
		revisionDetailPane.setLineWrap(true);
		revisionDetailPane.setWrapStyleWord(true);
		JPanel detailPanel = new JPanel();
		detailPane = new JTextArea(5,100);
		detailPane.setLineWrap(true);
		detailPane.setWrapStyleWord(true);
		
		String revisionDetail = "Revision:\n";
		String oldSentence = doc.getOldSentences(oldIndices);
		String newSentence = doc.getNewSentences(newIndices);
		revisionDetail += "OLD: " + oldSentence + "\n";
		revisionDetail += "NEW: " + newSentence + "\n";
		revisionDetailPane.setText(revisionDetail);
		revisionDetailPanel.add(revisionDetailPane);
		revisionDetailPanel.setBorder(BorderFactory.createTitledBorder("Revision Details"));
		detailPanel.add(detailPane);
		detailPanel.setBorder(BorderFactory.createTitledBorder("Review Details"));
		
		loadReviews();

		JPanel confirmPanel = new JPanel();
		confirmButton = new JButton("Confirm");
		cancelButton = new JButton("Cancel");
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				alignReviews();
				close();

			}
		});
		/*
		 * cancelButton.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent arg0) { // TODO
		 * Auto-generated method stub cancel(); } });
		 */
		confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));
		confirmPanel.add(confirmButton);

		setAllNodes();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(tree);
		this.add(revisionDetailPanel);
		this.add(detailPanel);
		this.add(confirmPanel);
	}

	public void showDetail(String str) {
		detailPane.setText(str);
	}
	
	

	public void close() {
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		topFrame.dispatchEvent(new WindowEvent(topFrame,
				WindowEvent.WINDOW_CLOSING));
	}

	/*
	 * public void cancel() { DefaultListModel<String> values =
	 * (DefaultListModel<String>) reviewList .getModel();
	 * DefaultListModel<String> valuesToRemove = (DefaultListModel<String>)
	 * candidateList .getModel(); for (int i = 0; i < valuesToRemove.size();
	 * i++) { values.addElement(valuesToRemove.getElementAt(i)); }
	 * valuesToRemove.clear(); }
	 */

	public void alignReviews() {
		rd.removeReviews(oldIndices, newIndices);
		ArrayList<RevisionUnit> units = doc.getRoot().getRevisionUnitAtLevel(0);
		RevisionUnit unit = null;
		String oldStandard = getIndiceStr(oldIndices);
		String newStandard = getIndiceStr(newIndices);
		for(int i = 0;i<units.size();i++) {
			RevisionUnit temp = units.get(i);
			String oldIndiceStr = getIndiceStr(temp.getOldSentenceIndex());
			String newIndiceStr = getIndiceStr(temp.getNewSentenceIndex());
			if(oldStandard.equals(oldIndiceStr)&&newStandard.equals(newIndiceStr)) {
				unit = temp;
				break;
			}
		}
		String type = "";
		if(unit!=null) type = RevisionPurpose.getPurposeName(unit.getRevision_purpose());
		List<ReviewItem> reviews = getAllNodes();
		for(ReviewItem item: reviews) {
			ReviewItemRevision rr = new ReviewItemRevision();
			rr.setItem(item);
			rr.setNewIndices(newIndices);
			rr.setOldIndices(oldIndices);
			rr.setRevisionType(type);
			rd.addReviewItemRevision(rr);
		}
	}
	
	public String getIndiceStr(ArrayList<Integer> indices) {
		
		String indexStr = "";
		if(indices!=null) {
			Collections.sort(indices);
		for(Integer index: indices) {
			if(index!=-1)
			indexStr += index + "_";
		}
		}
		return indexStr;
	}
	
	public List<ReviewItem> getAllNodes() {
		List<ReviewItem> reviews = new ArrayList<ReviewItem>();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
	    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		scanNode(reviews,root);
		return reviews;
	}
	
	public void setAllNodes() {
		List<ReviewItemRevision> reviews = rd.getRelatedReviews(oldIndices, newIndices);
		HashSet<String> selectedSet = new HashSet<String>();
		for(ReviewItemRevision rr: reviews) {
			selectedSet.add(rr.getItem().getContent());
		}
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
	    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		setNode(root, selectedSet);
	}
	
	public void setNode(DefaultMutableTreeNode node, HashSet<String> selected) {
		int childCount = node.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
	        if (childNode.getChildCount() > 0) {
	            setNode(childNode,selected);
	        } else {
	           CheckBoxNode box = (CheckBoxNode)(childNode.getUserObject());
	           if(selected.contains(box.getText())) {
	        	  box.setSelected(true);
	           }
	        }
	    }
	}
	
	public void scanNode(List<ReviewItem> list, DefaultMutableTreeNode node) {
		int childCount = node.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
	        if (childNode.getChildCount() > 0) {
	            scanNode(list,childNode);
	        } else {
	           CheckBoxNode box = (CheckBoxNode)(childNode.getUserObject());
	           if(box.isSelected()) {
	        	   list.add(nodeMap.get(box.getText()));
	           }
	        }

	    }
	}

	public void loadReviews() {
		List<ReviewItemRevision> reviews = rd.getRelatedReviews(oldIndices,
				newIndices);
		for (ReviewItemRevision review : reviews) {

		}
	}
}
