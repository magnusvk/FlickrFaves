/*
 * Copyright (C) 2006-2013 Magnus von Koeller 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package de.vonkoeller.flickrFaves.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.aetrion.flickr.FlickrException;

import de.vonkoeller.flickrFaves.api.AuthHolder;
import de.vonkoeller.flickrFaves.api.Authentication;
import de.vonkoeller.flickrFaves.api.Favorites;
import de.vonkoeller.flickrFaves.debug.Tracer;
import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * @author Magnus von Koeller
 * 
 *         The GUI for the FlickrDownloader.
 */
public class FlickrFaves extends JPanel implements ActionListener {

	/**
	 * For Serializable.
	 */
	private static final long serialVersionUID = -1492303108378378714L;

	/** The currently selected download directory. */
	private String downloadDir = null;

	/** The label displaying the currently selected download directory. */
	private JLabel downloadDirLabel = null;

	/** The checkbox for enabling the download of photos. */
	private JCheckBox mediaTypePhoto;

	/** The checkbox for enabling the download of videos. */
	private JCheckBox mediaTypeVideo;

	/** The JSpinner for selecting minimum image size. */
	private JSpinner minSizeSpinner = null;

	/** The JCheckBox for choosing whether to overwrite old images. */
	private JCheckBox overwriteCheck = null;

	/**
	 * The JCheckBox for choosing whether to limit the no. of images downloaded.
	 */
	private JCheckBox maxFavesCheck = null;

	/** The JSpinner for selecting how many images to download at most. */
	private JSpinner maxFavesSpinner = null;

	/** The JCheckBox for choosing whether to delete stale faves. */
	private JCheckBox deleteStaleCheck = null;

	/** The JButton for starting the download. */
	private JButton startDownload = null;

	/**
	 * Constructor. Initializes GUI with "connecting..." message.
	 */
	public FlickrFaves() {
		// first show "connecting..." message
		Tracer.trace("Checking authorization...");

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("<html><font size='+1'>Connecting to Flickr..."));
		revalidate();
	}

	/**
	 * Actually initializes the panel and checks the Flickr auth token.
	 */
	public void initialize() {
		try {
			if (Authentication.validTokenAvailable()) {
				Tracer.trace("Authorization ok...");
				showDownloadOptions();
			} else {
				Tracer.trace("No auth token available, get one now...");
				authorize();
			}
		} catch (FlickrFaveException e) {
			showExceptionAndQuit(e.getCause());
		}
	}

	/**
	 * Initializes the window to show an explanation about authorization and
	 * shows a button to open the browser.
	 */
	private void authorize() {
		// clear pane
		removeAll();
		/*
		 * Set layout manager. I will use a number of FlowLayouts within a
		 * one-column GridLayout.
		 */
		setLayout(new GridLayout(3, 1));

		// first row: big explanatory label
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel authMsg = new JLabel("<html><font size='+1'>This program "
				+ "requires your authorization<br> before it can read your "
				+ "favorites on Flickr</font>");
		p1.add(authMsg);
		add(p1);

		// second row: detailed explanation
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel detailMsg = new JLabel(
				"<html>Authorizing is a simple process "
						+ " which takes place in <br>your web browser. When you are "
						+ "finished, return to this <br>window to complete authorization "
						+ "and begin using <br><i>FlickrFaves</i>.");
		p2.add(detailMsg);
		add(p2);

		// third row: button to start authorization
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton authButton = new JButton("Authorize...");
		authButton.addActionListener(this);
		authButton.setActionCommand("authWeb");
		p3.add(authButton);
		add(p3);

		// repaint
		validate();
	}

	/**
	 * Opens the web browser for Flickr authorization.
	 */
	private void authWeb() {
		try {
			BrowserLauncher bLaunch = new BrowserLauncher(null);
			bLaunch.openURLinBrowser(Authentication.buildAuthURL().toString());
		} catch (Exception e) {
			showExceptionAndQuit(e);
		}
		authorizeNext();
	}

	/**
	 * Shows the window where the user must click once he completed
	 * authorization in his browser.
	 */
	private void authorizeNext() {
		// clear pane
		removeAll();
		/*
		 * Set layout manager. I will use a number of FlowLayouts within a
		 * one-column GridLayout.
		 */
		setLayout(new GridLayout(3, 1));

		// first row: big explanatory label
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel authMsg = new JLabel("<html><font size='+1'>Return to this "
				+ "window after <br>you have finished the authorization "
				+ "<br>process on Flickr.com</font>");
		p1.add(authMsg);
		add(p1);

		// second row: detailed explanation
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel detailMsg = new JLabel(
				"<html>Once you are done, click the 'Complete Authorization' "
						+ "button <br>below and you can begin using <i>FlickrFaves</i>.");
		p2.add(detailMsg);
		add(p2);

		// third row: button to complete authorization or start over
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton authButton = new JButton("Complete Authorization");
		authButton.addActionListener(this);
		authButton.setActionCommand("completeAuth");
		p3.add(authButton);
		JButton restartButton = new JButton("Cancel");
		restartButton.addActionListener(this);
		restartButton.setActionCommand("deauthorize");
		p3.add(restartButton);
		add(p3);

		// repaint
		revalidate();
	}

	/**
	 * Completes authentication by converting the frob to a token. Then calls
	 * method for displaying download options.
	 */
	private void completeAuth() {
		try {
			Authentication.getAuth();
		} catch (FlickrFaveException e) {
			showExceptionAndQuit(e.getCause());
		}
		showDownloadOptions();
	}

	/**
	 * Shows the downloading options.
	 */
	public void showDownloadOptions() {
		// clear pane
		removeAll();
		/*
		 * Set layout manager. I will use a number of FlowLayouts within a
		 * one-column GridLayout.
		 */
		setLayout(new GridLayout(0, 1));

		// first row: big explanatory label
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel msg = new JLabel("<html><font size='+1'>Download Your "
				+ "Favorites</font>");
		p1.add(msg);
		add(p1);

		// second row: Select directory to save to
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel downloadTo = new JLabel("Download to:");
		p2.add(downloadTo);
		downloadDirLabel = new JLabel();
		p2.add(downloadDirLabel);
		JButton chooseDDir = new JButton("Select");
		chooseDDir.addActionListener(this);
		chooseDDir.setActionCommand("openDownloadDirChoose");
		p2.add(chooseDDir);
		add(p2);

		// third row: Media type selection
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.LEFT));
		p3.add(new JLabel("Media type: "));
		mediaTypePhoto = new JCheckBox();
		p3.add(mediaTypePhoto);
		mediaTypePhoto.setText("photos");
		mediaTypeVideo = new JCheckBox();
		p3.add(mediaTypeVideo);
		mediaTypeVideo.setText("videos");
		add(p3);

		ItemListener mediaItemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateDownloadNowEnabledState();
			}
		};
		mediaTypePhoto.addItemListener(mediaItemListener);
		mediaTypeVideo.addItemListener(mediaItemListener);

		// fourth row: Select minimum size
		JPanel p4 = new JPanel();
		p4.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel minSize = new JLabel(
				"Minimum resolution in at least one dimension (in px):");
		p4.add(minSize);
		minSizeSpinner = new JSpinner(new SpinnerNumberModel(1024, 0, null, 1));
		// adjust spinner width
		Dimension d = minSizeSpinner.getPreferredSize();
		d.width = 75;
		minSizeSpinner.setPreferredSize(d);
		minSizeSpinner.setMaximumSize(d);
		p4.add(minSizeSpinner);
		add(p4);

		// fifth row: Download only new images?
		JPanel p5 = new JPanel();
		p5.setLayout(new FlowLayout(FlowLayout.LEFT));
		maxFavesCheck = new JCheckBox("Load at most this many images: ");
		maxFavesCheck.addActionListener(this);
		maxFavesCheck.setActionCommand("maxFavesCheckChanged");
		p5.add(maxFavesCheck);
		maxFavesSpinner = new JSpinner(new SpinnerNumberModel(50, 1, null, 1));
		// adjust spinner width
		d = maxFavesSpinner.getPreferredSize();
		d.width = 75;
		maxFavesSpinner.setPreferredSize(d);
		maxFavesSpinner.setMaximumSize(d);
		maxFavesSpinner.setEnabled(false);
		p5.add(maxFavesSpinner);
		add(p5);

		// sixth row: Overwrite existing images?
		JPanel p6 = new JPanel();
		p6.setLayout(new FlowLayout(FlowLayout.LEFT));
		overwriteCheck = new JCheckBox("Overwrite existing images?", false);
		p6.add(overwriteCheck);
		add(p6);

		// seventh row: Delete stale faves?
		JPanel p7 = new JPanel();
		p7.setLayout(new FlowLayout(FlowLayout.LEFT));
		deleteStaleCheck = new JCheckBox("Delete stale faves?", false);
		p7.add(deleteStaleCheck);
		add(p7);

		// eighth row: Download now! button
		JPanel p8 = new JPanel();
		p8.setLayout(new FlowLayout(FlowLayout.LEFT));
		startDownload = new JButton("Download now!");
		startDownload.addActionListener(this);
		startDownload.setActionCommand("downloadNow");
		if (downloadDir == null)
			startDownload.setEnabled(false);
		p8.add(startDownload);
		add(p8);

		// load default settings; these are automatically applied
		getSavedSettingsFromPrefs();

		// repaint
		revalidate();
	}

	/**
	 * If the given string is null, set text "<please choose>" instead. If str
	 * is longer than length characters, trim and adding trailing dots.
	 * 
	 * @param str
	 *            The string to check.
	 * @param length
	 *            The maximum length of the string.
	 * @return The checked and (maybe) changed string.
	 */
	private String trimDownloadDir(String str, int length) {
		if (str == null)
			str = "<please choose>";
		else if (str.length() > length)
			str = "..." + str.substring(str.length() - length, str.length());
		return str;
	}

	/**
	 * Opens the download directory selection window and updates the local
	 * variables downloadDir and downloadDirLabel with the new selection.
	 */
	private void openDownloadDirChooser() {
		// set up file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Choose download directory...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// show file chooser; if user didn't cancel, read selected directory
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			downloadDir = chooser.getSelectedFile().getAbsolutePath();
			downloadDirLabel.setText(trimDownloadDir(downloadDir,
					Constants.DIR_NAME_MAX_LENGTH));
			downloadDirLabel.setToolTipText(downloadDir);
			startDownload.setEnabled(true);
		}
	}

	/**
	 * Remembers following current settings in the preferences: download
	 * directory, media type, minimum resolution, maxFaves enabled, maximum
	 * faves to download, overwrite exisiting files and delete stale
	 */
	private void saveSettingsInPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(FlickrFaves.class);
		prefs.put("defaultDownloadDir", downloadDir);
		prefs.put("defaultMinSize", minSizeSpinner.getValue().toString());
		prefs.put("defaultMaxFavesEnabled",
				Boolean.toString(maxFavesCheck.isSelected()));
		prefs.put("defaultMaxFaves", maxFavesSpinner.getValue().toString());
		prefs.put("defaultOverwrite",
				Boolean.toString(overwriteCheck.isSelected()));
		prefs.put("defaultDeleteStale",
				Boolean.toString(deleteStaleCheck.isSelected()));
		prefs.put("defaultDownloadPhotos",
				Boolean.toString(mediaTypePhoto.isSelected()));
		prefs.put("defaultDownloadVideos",
				Boolean.toString(mediaTypeVideo.isSelected()));
	}

	/**
	 * Loads the saved settings from the preferences and sets local variables
	 * accordingly. Retrieves the following preferences: download directory,
	 * minimum resolution, maxFaves enabled, maximum faves to download,
	 * overwrite exisiting files and delete stale
	 * 
	 * Assumes the following local variables are already instantiated:
	 */
	private void getSavedSettingsFromPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(FlickrFaves.class);
		downloadDir = prefs.get("defaultDownloadDir", null);
		if (downloadDir == null) {
			// no default download dir available; user must first choose
			// before allowing start
			downloadDirLabel.setText("<please choose>");
		} else {
			// default download dir available, enable download button
			downloadDirLabel.setText(trimDownloadDir(downloadDir,
					Constants.DIR_NAME_MAX_LENGTH));
			downloadDirLabel.setToolTipText(downloadDir);
		}
		mediaTypePhoto.setSelected(prefs.getBoolean("defaultDownloadPhotos",
				true));
		mediaTypeVideo.setSelected(prefs.getBoolean("defaultDownloadVideos",
				false));
		minSizeSpinner.setValue(prefs.getInt("defaultMinSize", 1024));
		maxFavesCheck.setSelected(prefs.getBoolean("defaultMaxFavesEnabled",
				false));
		maxFavesSpinner.setValue(prefs.getInt("defaultMaxFaves", 50));
		maxFavesSpinner.setEnabled(maxFavesCheck.isSelected());
		overwriteCheck.setSelected(prefs.getBoolean("defaultOverwrite", false));
		deleteStaleCheck.setSelected(prefs.getBoolean("defaultDeleteStale",
				false));

		updateDownloadNowEnabledState();
	}

	/**
	 * Sets up the display for download and then starts it. When finished,
	 * switches to "Done" view.
	 */
	private void downloadNow() {
		// check whether the download directory exists and is writable
		File downloadDirFile = new File(downloadDir);
		if (!downloadDirFile.exists() || !downloadDirFile.canWrite()
				|| !downloadDirFile.isDirectory()) {
			JOptionPane
					.showMessageDialog(
							this,
							"<html>The download directory that you have chosen<br>"
									+ "does not exist or may not be written to.<br><br>"
									+ "Please choose a different directory.<br>",
							"Download Directory Invalid",
							JOptionPane.ERROR_MESSAGE);
			return;
		}

		// save current settings as default in preferences
		saveSettingsInPrefs();

		// clear pane
		removeAll();
		/*
		 * Set layout manager. I will use a number of FlowLayouts within a
		 * one-column GridLayout.
		 */
		setLayout(new GridLayout(0, 1));

		// first row: big explanatory label
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel msg = new JLabel(
				"<html><font size='+1'>Now Downloading...</font>");
		p1.add(msg);
		add(p1);

		// second row: total progress bar
		Box p2 = Box.createHorizontalBox();
		p2.add(Box.createHorizontalStrut(5));
		JLabel totalPrograssLabel = new JLabel("Total progress:");
		p2.add(totalPrograssLabel);
		p2.add(Box.createHorizontalGlue());
		final JProgressBar totalProgress = new JProgressBar(0, 0);
		totalProgress.setIndeterminate(true);
		Dimension d = totalProgress.getPreferredSize();
		d.width = 240;
		totalProgress.setPreferredSize(d);
		totalProgress.setMaximumSize(d);
		p2.add(totalProgress);
		p2.add(Box.createHorizontalStrut(30));
		add(p2);

		// third row: file progress bar
		Box p3 = Box.createHorizontalBox();
		p3.add(Box.createHorizontalStrut(5));
		JLabel filePrograssLabel = new JLabel("Download progress:");
		p3.add(filePrograssLabel);
		p3.add(Box.createHorizontalGlue());
		final JProgressBar fileProgress = new JProgressBar(0, 0);
		fileProgress.setIndeterminate(true);
		fileProgress.setEnabled(false);
		d = fileProgress.getPreferredSize();
		d.width = 240;
		fileProgress.setPreferredSize(d);
		fileProgress.setMaximumSize(d);
		p3.add(fileProgress);
		p3.add(Box.createHorizontalStrut(30));
		add(p3);

		// fourth row: current progress message
		JPanel p4 = new JPanel();
		p4.setLayout(new FlowLayout(FlowLayout.LEFT));
		final JLabel progressMsg = new JLabel("Getting list of faves...");
		p4.add(progressMsg);
		add(p4);

		// repaint
		revalidate();

		// actually start downloading pictures but in a seperate thread
		// to make sure that components actually repaint
		final FlickrFaves flickrFaves = this;
		Runnable dlFaves = new Runnable() {
			public void run() {
				try {
					Favorites.downloadAllFaves(downloadDir,
							mediaTypePhoto.isSelected(),
							mediaTypeVideo.isSelected(),
							overwriteCheck.isSelected(),
							((Integer) minSizeSpinner.getValue()),
							maxFavesCheck.isSelected(),
							((Integer) maxFavesSpinner.getValue()),
							deleteStaleCheck.isSelected(), flickrFaves,
							totalProgress, fileProgress, progressMsg);
				} catch (FlickrFaveException e) {
					flickrFaves.showExceptionAndQuit(e);
				}
			}
		};
		Thread dlFavesThread = new Thread(dlFaves);
		dlFavesThread.start();
	}

	/**
	 * Show message 'completed' and offer option to re-start or quit.
	 */
	public void displayDone() {
		// clear pane
		removeAll();
		/*
		 * Set layout manager. I will use a number of FlowLayouts within a
		 * one-column GridLayout.
		 */
		setLayout(new GridLayout(3, 1));

		// first row: big explanatory label
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel bigMsg = new JLabel(
				"<html><font size='+1'>We're done! All files downloaded!");
		p1.add(bigMsg);
		add(p1);

		// second row: detailed explanation
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel detailMsg = new JLabel(
				"<html><i>FlickrFaves</i> has downloaded all the favorites "
						+ "you requested <br>it to download. You can now "
						+ "either quit or download again.");
		p2.add(detailMsg);
		add(p2);

		// third row: button to start over or quit
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton authButton = new JButton("Quit");
		authButton.addActionListener(this);
		authButton.setActionCommand("quit");
		p3.add(authButton);
		JButton restartButton = new JButton("Start over...");
		restartButton.addActionListener(this);
		restartButton.setActionCommand("showDownloadOptions");
		p3.add(restartButton);
		add(p3);

		// repaint
		revalidate();
	}

	/**
	 * Shows the about box with information about this software and its version.
	 */
	public void showAboutBox() {
		// show dialog with error message
		JOptionPane
				.showMessageDialog(
						this,
						"<html>This is FlickrFaves version "
								+ Constants.VERSION
								+ ", build #"
								+ Constants.BUILD_NO
								+ ". <br><br>This software is made "
								+ "available under the <br>GNU General Public "
								+ "License which should have <br>been included with "
								+ "this software.<br><br>For more information, go to <br>"
								+ "<a href='" + Constants.FLICKR_FAVES_URL
								+ "http://www.vonkoeller.de/flickrfaves'>"
								+ Constants.FLICKR_FAVES_URL + "</a>",
						"About FlickrFaves " + Constants.VERSION,
						JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Handles the program's events.
	 * 
	 * @param action
	 *            The action performed.
	 */
	public void actionPerformed(ActionEvent action) {
		String cmd = action.getActionCommand();
		if ("quit".equals(cmd))
			System.exit(0);
		else if ("authWeb".equals(cmd))
			authWeb();
		else if ("completeAuth".equals(cmd))
			completeAuth();
		else if ("deauthorize".equals(cmd)) {
			AuthHolder.deauthorize();
			authorize();
		} else if ("showDownloadOptions".equals(cmd))
			showDownloadOptions();
		else if ("openDownloadDirChoose".equals(cmd))
			openDownloadDirChooser();
		else if ("downloadNow".equals(cmd))
			downloadNow();
		else if ("about".equals(cmd))
			showAboutBox();
		else if ("maxFavesCheckChanged".equals(cmd)) {
			// toggle enabled of spinner according to checkbox
			maxFavesSpinner.setEnabled(maxFavesCheck.isSelected());
		} else
			System.out.println("Unrecognized event!");
	}

	/**
	 * Shows a dialog box appropriate for the given exception, prints a stack
	 * trace and then quits abnormally.
	 * 
	 * @param e
	 *            The caught exception
	 */
	public void showExceptionAndQuit(Throwable e) {
		// print stack trace
		if (Constants.DEBUG && e != null)
			e.printStackTrace();

		// retrieve wrapped exception
		if (e instanceof FlickrFaveException)
			e = e.getCause();

		// build error message
		boolean encourageErrorReport = false;
		String errorMsg = "<html>An error has occurred and this program must be "
				+ "terminated! <br><br>This seems to be the reason:<br>";
		if (e instanceof UnsupportedOperatingSystemException)
			errorMsg += "<i>Your operating system is not supported.</i>";
		else if (e instanceof BrowserLaunchingExecutionException
				|| e instanceof BrowserLaunchingInitializingException)
			errorMsg += "<i>Failed to open your browser for authentication.</i>";
		else if (e instanceof FlickrException) {
			FlickrException flickrE = (FlickrException) e;
			if ("105".equals(flickrE.getErrorCode())
					|| "0".equals(flickrE.getErrorCode())) {
				errorMsg += "<i>The Flickr API is temporarily unavailable!</i><br>"
						+ "Please try again later!";
			} else {
				errorMsg += "<i>A Flickr API call failed with the following "
						+ "message: " + flickrE.getErrorCode() + ": "
						+ flickrE.getErrorMessage() + "</i>";
				encourageErrorReport = true;
			}
		} else if (e instanceof UnknownHostException
				|| e instanceof NoRouteToHostException
				|| e instanceof SocketTimeoutException
				|| e instanceof ConnectException) {
			errorMsg += "<i>Could not connect to Flickr!</i><br>"
					+ "Check your proxy settings and Internet connection.";
		} else {
			encourageErrorReport = true;
			errorMsg += "<i>I don't know!</i>";
		}

		// indicate that stacktrace will be copied to the clipboard
		errorMsg += "<br><br>Press OK to copy an error report to the clipboard.<br>";
		if (encourageErrorReport) {
			errorMsg += "Please send this error report to "
					+ "<a href='mailto:flickrFaves@vonkoeller.de'>flickrFaves@vonkoeller.de</a>";
		}

		// show dialog with error message
		if (JOptionPane.showConfirmDialog(this, errorMsg, "Error Occured",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.OK_OPTION) {
			// build error report
			String errorReport = "An error has occured within FlickrFaves "
					+ Constants.VERSION + " (build #" + Constants.BUILD_NO
					+ ").\n\n";

			// append stacktrace
			errorReport += "STACKTRACE:\n";
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			errorReport += stackTrace.toString();

			// append debug log
			errorReport += "\nDEBUG LOG:\n";
			errorReport += Tracer.getTrace();

			// copy stacktrace to clipboard
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			clipboard.setContents(new StringSelection(errorReport), null);

			if (encourageErrorReport) {
				JOptionPane
						.showMessageDialog(
								this,
								"<html>An error report has been copied to the clipboard.<br>"
										+ "To help make FlickrFaves better, please send it to<br>"
										+ "<a href='mailto:flickrFaves@vonkoeller.de'>flickrFaves@vonkoeller.de</a> "
										+ "now!<br><br>Thank you!",
								"Error Report", JOptionPane.INFORMATION_MESSAGE);
			}
		}

		// quit
		System.exit(-1);
	}

	/**
	 * Check whether this version is the newest currently available. If not,
	 * show information message.
	 */
	private void versionCheck() {
		try {
			Tracer.trace("Now doing the version check...");

			// open connection to file containing the latest version
			URL url = new URL(Constants.LAST_VERSION_URL);
			URLConnection urlConn = url.openConnection();
			urlConn.addRequestProperty("User-Agent", Constants.USER_AGENT);

			// set a small timeout, so that this doesn't block start-up for
			// too long
			urlConn.setConnectTimeout(Constants.VERSION_CHECK_TIMEOUT * 1000);
			urlConn.setReadTimeout(Constants.VERSION_CHECK_TIMEOUT * 1000);

			// get the latest version
			LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(urlConn.getInputStream()));
			int buildNo = new Integer(reader.readLine());
			String versionString = reader.readLine();

			Tracer.trace("Latest version is " + versionString + ", build #"
					+ buildNo + "...");

			// check whether current build is the newest available
			if (Constants.BUILD_NO < buildNo) {
				// newer version available
				Tracer.trace("Newer version available!");

				// open a dialog box asking whether to download the newest
				// version now
				int openFlickrFavesHp = JOptionPane.showConfirmDialog(this,
						"<html>This is FlickrFaves version "
								+ Constants.VERSION + ", build #"
								+ Constants.BUILD_NO
								+ ". <br>The newest available version is "
								+ versionString + ", build #" + buildNo
								+ "<br><br>Download new version now?",
						"New Version Available", JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
				if (openFlickrFavesHp == JOptionPane.YES_OPTION) {
					// go to FlickrFaves' homepage
					BrowserLauncher bLaunch = new BrowserLauncher(null);
					bLaunch.openURLinBrowser(Constants.FLICKR_FAVES_URL);
				}
			}
		} catch (Exception e) {
			/*
			 * The version check is not essential, therefore any errors are
			 * simply ignored. If in debug mode, however, write a message and a
			 * stack trace.
			 */
			Tracer.trace("Caught an exception while doing"
					+ " the version check...");
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			Tracer.trace(stackTrace.toString());
		}
	}

	private void updateDownloadNowEnabledState() {
		startDownload.setEnabled((mediaTypePhoto.isSelected() || mediaTypeVideo
				.isSelected()) && downloadDir != null);
	}

	/**
	 * Main function. Builds menu bar and starts GUI.
	 * 
	 * @param args
	 *            Command line arguments (ignored)
	 */
	public static void main(String[] args) {
		Tracer.trace("Starting up...");

		// use system-wide proxy configuration
		System.setProperty("java.net.useSystemProxies", "true");

		// initialize window
		JFrame frame = new JFrame("FlickrFaves " + Constants.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(450, 280);

		// initialize gui pane
		FlickrFaves flickrFaves = new FlickrFaves();

		// build menu
		JMenu file = new JMenu("File");
		JMenuItem deauthorize = new JMenuItem("Deauthorize");
		deauthorize.addActionListener(flickrFaves);
		deauthorize.setActionCommand("deauthorize");
		file.add(deauthorize);
		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(flickrFaves);
		quit.setActionCommand("quit");
		file.add(quit);

		JMenu about = new JMenu("About");
		JMenuItem aboutItm = new JMenuItem("About FlickrFaves");
		aboutItm.addActionListener(flickrFaves);
		aboutItm.setActionCommand("about");
		about.add(aboutItm);

		// add menu to frame
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(about);
		frame.setJMenuBar(menuBar);

		// show content, but disable menu while connecting
		frame.setContentPane(flickrFaves);
		frame.setVisible(true);
		file.setEnabled(false);
		about.setEnabled(false);

		// check version
		flickrFaves.versionCheck();

		// connect to Flickr
		flickrFaves.initialize();

		// done connecting; reactivate menubar
		file.setEnabled(true);
		about.setEnabled(true);
	}

}
