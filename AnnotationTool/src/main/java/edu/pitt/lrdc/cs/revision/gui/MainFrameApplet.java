package edu.pitt.lrdc.cs.revision.gui;

import java.applet.Applet;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import edu.pitt.lrdc.cs.revision.io.RestServiceUploader;
import edu.pitt.lrdc.cs.revision.io.RevisionDocumentReader;
import edu.pitt.lrdc.cs.revision.io.RevisionDocumentWriter;
import edu.pitt.lrdc.cs.revision.model.RevisionDocument;

public class MainFrameApplet extends JApplet {
	   @Override
	   public void init() {
	      try {
	         SwingUtilities.invokeAndWait(new Runnable() {
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
