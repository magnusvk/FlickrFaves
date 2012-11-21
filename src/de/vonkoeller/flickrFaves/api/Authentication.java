/*
 * Copyright (C) 2006 Magnus von Koeller 
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

import java.net.MalformedURLException;
import java.net.URL;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;

import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;

/**
 * @author Magnus von Koeller
 * 
 *         Handles Flickr authentication.
 */
public class Authentication {

	/**
	 * Builds the URL to open in the user's browser for authentication.
	 * 
	 * @return The URL to open.
	 */
	public static URL buildAuthURL() {
		// get interfaces
		Flickr flickrI = InterfaceHolder.getFlickrI();
		AuthInterface authI = flickrI.getAuthInterface();

		// get frob
		String frob;
		try {
			frob = authI.getFrob();
		} catch (Exception e) {
			throw new FlickrFaveException("Retrieving frob failed.", e);
		}

		// store frob for later use
		AuthHolder.setFrob(frob);

		// build authentication URL
		try {
			return authI.buildAuthenticationUrl(Permission.READ, frob);
		} catch (MalformedURLException e) {
			throw new FlickrFaveException("flickrj could not build "
					+ "authentication URL.", e);
		}
	}

	/**
	 * Converts a frob to a token. The frob must already be saved in AuthHolder!
	 * 
	 * @return The flickrj Auth object containing the token.
	 */
	public static Auth getAuth() {
		// get interfaces
		Flickr flickrI = InterfaceHolder.getFlickrI();
		AuthInterface authI = flickrI.getAuthInterface();

		// shared secret already initialized

		try {
			// get token
			Auth auth = authI.getToken(AuthHolder.getFrob());
			// save token to AuthHolder and its preferences
			AuthHolder.setAuth(auth);
			AuthHolder.saveTokenToPrefs();
			return auth;
		} catch (Exception e) {
			throw new FlickrFaveException("Converting frob to token failed.", e);
		}
	}

	/**
	 * Checks whether there is a valid Auth flickrj authentication object
	 * available, either already loaded or through a saved token in the
	 * preferences.
	 * 
	 * @return true if valid authentication token is available, false if not
	 */
	public static boolean validTokenAvailable() {
		// maybe Auth object is already available?
		if (AuthHolder.getAuth() != null)
			return true;
		// if not, try loading token and Auth from preferences
		AuthHolder.loadTokenFromPrefs();
		// now try again whether Auth object is available
		return AuthHolder.getAuth() != null;
	}

}
