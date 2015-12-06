package edu.pitt.lrdc.cs.revision.gui;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

public class MainFrameApplet extends JApplet {
	   @Override
	   public void init() {
	      try {
	         SwingUtilities.invokeAndWait(new Runnable() {
	            @Override
				public void run() {
	               MainFrameV3 myFrame = new MainFrameV3();
	               Container contentPane = myFrame.getContentPane();
	               setContentPane(contentPane);
	            }
	         });
	      } catch (InterruptedException e) {
	         e.printStackTrace();
	      } catch (InvocationTargetException e) {
	         e.printStackTrace();
	      }
	   }
}
