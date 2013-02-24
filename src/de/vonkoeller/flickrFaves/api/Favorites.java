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
package de.vonkoeller.flickrFaves.api;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.favorites.FavoritesInterface;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotosInterface;

import de.vonkoeller.flickrFaves.debug.Tracer;
import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;
import de.vonkoeller.flickrFaves.gui.Constants;
import de.vonkoeller.flickrFaves.gui.FlickrFaves;

/**
 * @author Magnus von Koeller
 * 
 *         Handles favorites API.
 */
public class Favorites {

	/**
	 * Download all faves according to the given parameters.
	 * 
	 * @param dir
	 *            The directory to save images to.
	 * @param downloadPhotos
	 *            Whether to download photos.
	 * @param downloadVideos
	 *            Whether to download videos.
	 * @param overwrite
	 *            Whether to re-download and overwrite existing images.
	 * @param minSize
	 *            The minimum resolution (in at least one dimension) (in px)
	 * @param maxFavesEnabled
	 *            Whether to limit the number of photos downloaded
	 * @param maxFaves
	 *            The maximum number of photos to download
	 * @param flickrFaves
	 *            The parent JPanel
	 * @param totalProgress
	 *            The overall progress bar; needed for updates
	 * @param fileProgress
	 *            The download progress bar; needed for updates
	 * @param progressMsg
	 *            The JLabel containing progress message; needed for updates
	 */
	@SuppressWarnings("unchecked")
	public static void downloadAllFaves(String dir, boolean downloadPhotos,
			boolean downloadVideos, boolean overwrite, int minSize,
			boolean maxFavesEnabled, int maxFaves, boolean deleteStale,
			final FlickrFaves flickrFaves, final JProgressBar totalProgress,
			final JProgressBar fileProgress, final JLabel progressMsg) {
		if (!downloadPhotos && !downloadVideos) {
			throw new IllegalArgumentException(
					"Must enable download of at least one of photos and videos.");
		}

		// get interfaces
		Flickr flickrI = InterfaceHolder.getFlickrI();
		FavoritesInterface favI = flickrI.getFavoritesInterface();
		PhotosInterface photoI = flickrI.getPhotosInterface();

		// for some reason the request context gets lost here, reset it
		AuthHolder.ensureCorrectRequestContext(RequestContext
				.getRequestContext());

		// get list of all favorites, looping over pages of 500 faves as
		// that is the maximum number of photos that Flickr allows to be
		// retrieved per page
		Tracer.trace("Now getting list of faves...");
		List<String> faves = new LinkedList<String>();
		HashMap<String, Photo> favesMap = new HashMap<String, Photo>();
		Collection pl;
		int i = 1;
		try {
			while (true) {
				Tracer.trace("Now getting page " + i + " of list of faves...");
				// get next page of pictures
				Set<String> extras = new HashSet<String>();
				extras.add("media");
				extras.add("originalsecret");
				extras.add("url_o");
				extras.add("url_b");
				extras.add("url_c");
				extras.add("url_z");
				extras.add("url_n");
				extras.add("url_m");

				pl = favI.getList(null, 500, i++, extras);
				// none left? then we're done
				if (pl.isEmpty())
					break;
				for (Object cur : pl) {
					Photo curPhoto = (Photo) cur;
					faves.add(curPhoto.getId());
					favesMap.put(curPhoto.getId(), curPhoto);
				}
			}
		} catch (Exception e) {
			throw new FlickrFaveException("Error while retrieving list of "
					+ "faves.", e);
		}

		// if enabled: delete stale faves
		if (deleteStale) {
			Tracer.trace("Now delete stale faves...");

			// update progress message
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressMsg.setText("Deleting stale faves...");
					progressMsg.revalidate();
				}
			});

			// get list of files in directory; only files with format
			// xxxxxxxx.jpg|.mp4 where x are all digits are of interest
			File[] downloadedFaves = new File(dir)
					.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.matches("\\d{7,}\\.jpg")
									|| name.matches("\\d{7,}\\.mp4");
						}
					});

			// go through all these files and check whether they are still
			// faves
			for (File toCheck : downloadedFaves) {
				// cut off ".jpg" or ".mp4"
				String id = toCheck.getName().substring(0,
						toCheck.getName().length() - 4);
				if (!faves.contains(id)) {
					Tracer.trace("Now deleting stale fave " + toCheck.getName());
					toCheck.delete();
				}
			}
		}

		// did we load too many images? then truncate list
		if (maxFavesEnabled && faves.size() > maxFaves) {
			Tracer.trace("Truncating total list of faves from " + faves.size()
					+ " to " + maxFaves);
			faves = faves.subList(0, maxFaves);
		}

		// now initialize progress bar
		final int numFaves = faves.size();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				totalProgress.setMaximum(numFaves);
				totalProgress.setIndeterminate(false);
				totalProgress.revalidate();
			}
		});

		// iterate over all faves
		i = 0; // overall counter
		int numFailedDownloads = 0; // counter for failed downloads
		try {
			for (String cur : faves) {
				// update progress bar, message and counter
				final int curI = i++;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						totalProgress.setValue(curI);
						progressMsg.setText("" + (curI + 1) + "/" + numFaves
								+ ": Checking size...");
						totalProgress.revalidate();
						progressMsg.revalidate();
					}
				});

				// initialize base filename and check whether to download at all
				String curFilename;
				Photo curPhoto = favesMap.get(cur);
				if ("photo".equals(curPhoto.getMedia())) {
					if (!downloadPhotos) {
						Tracer.trace("Skipping " + cur
								+ " because it is a photo and "
								+ "we are not downloading photos...");
						continue;
					} else {
						curFilename = cur + ".jpg";
					}
				} else if ("video".equals(curPhoto.getMedia())) {
					if (!downloadVideos) {
						Tracer.trace("Skipping " + cur
								+ " because it is a video and "
								+ "we are not downloading videos...");
						continue;
					} else {
						curFilename = cur + ".mp4";
					}
				} else {
					throw new IllegalStateException("Unknown media type: "
							+ curPhoto.getMedia());
				}
				String curFullPath = dir + File.separator + curFilename;

				// check whether this image should be excluded
				File excl = new File(dir + File.separator + "." + curFilename
						+ ".exclude");
				if (excl.exists()) {
					Tracer.trace("Excluding " + curFilename
							+ " because of exlcusion file...");
					continue;
				}

				// check whether this file is already downloaded; in that case
				// and if overwriting is disabled, we can save time by skipping
				// the resolution and file size check
				File out = new File(curFullPath);
				if (out.exists() && !overwrite) {
					Tracer.trace("Skipping file " + curFilename
							+ " -- overwrite disabled...");
					continue;
				}

				// check size by getting all sizes and seeing whether one is
				// big enough
				Tracer.trace("Now downloading largest size for " + cur);

				String originalUrl = null;
				try {
					originalUrl = curPhoto.getOriginalUrl();
				} catch (FlickrException e) {
					// if the original url just isn't available, fine. no need
					// to panic.
				}

				// download the largest available size
				String largestUrl = originalUrl;
				if (largestUrl == null)
					largestUrl = curPhoto.getLargeUrl();
				if (largestUrl == null)
					largestUrl = curPhoto.getMediumUrl();
				if (largestUrl == null)
					largestUrl = curPhoto.getSmallUrl();

				// sufficiently large original available, download it
				if (largestUrl != null) {
					// update progress message
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							progressMsg.setText("" + (curI + 1) + "/"
									+ numFaves + ": Connecting...");
							progressMsg.revalidate();
						}
					});

					// open URL connection
					URL url = new URL(largestUrl);
					URLConnection urlConn = url.openConnection();

					// set timeout
					urlConn.setConnectTimeout(Constants.DOWNLOAD_TIMEOUT * 1000);
					urlConn.setReadTimeout(Constants.DOWNLOAD_TIMEOUT * 1000);

					// get file size
					final int fileSize = urlConn.getContentLength();

					// check if this image is already downloaded
					if (out.exists()) {
						// if overwrite is not enabled, then the file was
						// already skipped earlier (see above) to save time

						// also skip if it is already downloaded and the file
						// size matches (otherwise incomplete download =>
						// delete)
						if (fileSize == out.length()) {
							Tracer.trace("Skipping file " + curFilename
									+ " -- already downloaded...");
							continue;
						}
						// delete file otherwise -- overwrite is enabled and
						// file sizes do not match
						else {
							Tracer.trace("Deleting and re-downloading file "
									+ curFilename + "...");
							out.delete();
						}
					}

					// open URL for download
					try {
						BufferedInputStream inS = new BufferedInputStream(
								urlConn.getInputStream());
						// open file for saving
						BufferedOutputStream outS = new BufferedOutputStream(
								new FileOutputStream(curFullPath));

						// initialize and enable file progress bar
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fileProgress.setIndeterminate(false);
								fileProgress.setEnabled(true);
								fileProgress.setValue(0);
								fileProgress.setMaximum(fileSize);
								fileProgress.revalidate();
								progressMsg.setText("" + (curI + 1) + "/"
										+ numFaves + ": Downloading...");
								progressMsg.revalidate();
							}
						});

						Tracer.trace("Now downloading " + curFilename
								+ " from " + urlConn.getURL());

						// start copying, byte by byte
						try {
							// try to download image; might time out
							downloadImage(inS, outS, fileSize, fileProgress);
						} catch (IOException e) {
							/*
							 * An IOException at this point often means that the
							 * connection has timed out or that there is a
							 * network issue of some sort. This should result in
							 * the more specific SocketTimeoutException;
							 * however, it can also result in other IOExceptions
							 * such as a generic '504 gateway timeout'.
							 * Therefore we retry retry downloading the whole
							 * image once.
							 */
							Tracer.trace("Caught IOException while attempting download... Retrying whole image...");
							inS.close();
							urlConn = url.openConnection();

							// set timeout
							urlConn.setConnectTimeout(Constants.DOWNLOAD_TIMEOUT * 1000);
							urlConn.setReadTimeout(Constants.DOWNLOAD_TIMEOUT * 1000);

							inS = new BufferedInputStream(
									urlConn.getInputStream());
							outS = new BufferedOutputStream(
									new FileOutputStream(curFullPath));
							try {
								downloadImage(inS, outS, fileSize, fileProgress);
							} catch (IOException e2) {
								// connection timed out again -- give up
								Tracer.trace("Caught IOException again... Giving up...");
								numFailedDownloads++;
								continue;
							}
						}

						// close streams
						inS.close();
						outS.close();
					} catch (IOException e) {
						/*
						 * Getting an IOException here really shouldn't happen;
						 * however, we might as well handle it graciously and
						 * try downloading the other photos.
						 */
						Tracer.trace("Caught IOException outside downloadImage() "
								+ "while attemption to download "
								+ curFilename
								+ " -- failing gracefully");
						numFailedDownloads++;
						continue;
					} finally {
						// reset and disable file progress bar -- download done
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fileProgress.setIndeterminate(true);
								fileProgress.setEnabled(false);
								fileProgress.revalidate();
							}
						});
					}

					Tracer.trace("Checking dimensions of the downloaded file...");
					File downloadedFile = new File(curFullPath);
					BufferedImage image = null;
					try {
						image = getBufferedImage(downloadedFile);
					} catch (Exception e) {
						Tracer.trace("Exception "
								+ e
								+ " occured when trying to get file dimensions. Ignoring...");
					}
					if (!downloadedFile.exists()
							|| (image != null && image.getWidth() < minSize && image
									.getHeight() < minSize)) {
						Tracer.trace("Image not large enough! Deleting and excluding.");

						// image is too small
						if (downloadedFile != null)
							downloadedFile.delete();

						// make sure that we don't try this again
						excl.createNewFile();
					}

					Tracer.trace("Done downloading " + curFilename + "...");
				}

			}
		} catch (Exception e) {
			throw new FlickrFaveException("Error while downloading faves.", e);
		}

		if (numFailedDownloads > 0)
			JOptionPane
					.showMessageDialog(
							flickrFaves,
							"<html>"
									+ numFailedDownloads
									+ " of your favorites could not be downloaded because"
									+ "<br>they could not be accessed or because of a network error."
									+ "<br>This should not happen -- but downloading the other"
									+ "<br>"
									+ (faves.size() - numFailedDownloads)
									+ " photos was successful.",
							"Download Failed", JOptionPane.ERROR_MESSAGE);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				flickrFaves.displayDone();
			}
		});
	}

	private static void downloadImage(BufferedInputStream inS,
			BufferedOutputStream outS, int fileSize,
			final JProgressBar fileProgress) throws IOException {
		int numBytesRead = 0;
		int bytesCopied = 0;
		byte[] bytesRead = new byte[512];
		while (true) {
			// read bytes; retry three times in case of timeout
			boolean success = false;
			int timeOutCount = 0;
			while (!success) {
				try {
					numBytesRead = inS.read(bytesRead, 0, 512);
					success = true;
				} catch (IOException e) {
					/*
					 * An IOException at this point often means that the
					 * connection has timed out or that there is a network issue
					 * of some sort. This should result in the more specific
					 * SocketTimeoutException; however, it can also result in
					 * other IOExceptions such as a generic '504 gateway
					 * timeout'. Therefore we retry retry downloading the whole
					 * image once.
					 */
					timeOutCount++;
					if (timeOutCount > 2)
						throw e;
					// try again
					success = false;
					Tracer.trace("Connection error (IOException) #"
							+ timeOutCount + ", retrying last block...");
				}
			}
			// end of file? -- then stop loop
			if (numBytesRead < 1)
				break;
			// write bytes and update count
			outS.write(bytesRead, 0, numBytesRead);
			bytesCopied += numBytesRead;

			// update label with progress if file size is
			// available
			final int curBytesCopied = bytesCopied;
			if (fileSize > 0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fileProgress.setValue(curBytesCopied);
						fileProgress.revalidate();
					}
				});
			}
		}
	}

	private static BufferedImage getBufferedImage(File stream) {
		BufferedImage bufferedImage = null;

		Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);

		Exception lastException = null;
		while (iter.hasNext()) {
			ImageReader reader = null;
			try {
				reader = (ImageReader) iter.next();
				ImageReadParam param = reader.getDefaultReadParam();
				reader.setInput(stream, true, true);
				Iterator<ImageTypeSpecifier> imageTypes = reader
						.getImageTypes(0);
				while (imageTypes.hasNext()) {
					ImageTypeSpecifier imageTypeSpecifier = imageTypes.next();
					int bufferedImageType = imageTypeSpecifier
							.getBufferedImageType();
					if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) {
						param.setDestinationType(imageTypeSpecifier);
						break;
					}
				}
				bufferedImage = reader.read(0, param);
				if (null != bufferedImage)
					break;
			} catch (Exception e) {
				lastException = e;
			} finally {
				if (null != reader)
					reader.dispose();
			}
		}
		// If you don't have an image at the end of all readers
		if (null == bufferedImage) {
			if (null != lastException) {
				throw new RuntimeException(lastException);
			}
		}
		return bufferedImage;
	}
}
