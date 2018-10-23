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

/**
 * @author Magnus von Koeller
 * 
 *         Contains constants used in FlickrFaves.
 */
public class Constants {

	/** The human-readble version string. Also update build.properties! */
	public static final String VERSION = "2.0.7";

	/** The build number, useful for automatic comparison of versions. */
	public static final int BUILD_NO = 22;

	/** The URL of the file containing the latest version, for version check. */
	public static final String LAST_VERSION_URL = "http://upload.magnusvk.com/software_versions/FlickrFaves.txt";

	/** The homepage of the FlickrFaves project. */
	public static final String FLICKR_FAVES_URL = "https://github.com/magnusvk/FlickrFaves";

	/** The user-agent string to use when performing the version check. */
	public static final String USER_AGENT = "FlickrFaves/" + VERSION;

	/** The maximum length of the dir name when displayed in the GUI. */
	public static final int DIR_NAME_MAX_LENGTH = 30;

	/** The timeout in seconds before a fave download is interrupted. */
	public static final int DOWNLOAD_TIMEOUT = 20;

	/** The timeout in seconds before the version check is aborted. */
	public static final int VERSION_CHECK_TIMEOUT = 2;

	/** The callback url to get oauth verifier after user has granted permission */
	public static final int OAUTH_CALLBACK_PORT = 8801;
	public static final String OAUTH_CALLBACK_URL = "http://localhost:" + OAUTH_CALLBACK_PORT;

	/** Print debug messages? */
	public static final boolean DEBUG = true;

}
