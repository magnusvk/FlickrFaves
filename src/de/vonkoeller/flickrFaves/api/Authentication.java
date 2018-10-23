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

import java.net.MalformedURLException;
import java.net.URL;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;

import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;
import de.vonkoeller.flickrFaves.gui.Constants;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

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

		Token token = authI.getRequestToken(Constants.OAUTH_CALLBACK_URL);
		AuthHolder.setRequestToken(token);

		String url = authI.getAuthorizationUrl(token, Permission.READ);

		try {
			return new URL(url);
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

		try {
			Token accessToken = authI.getAccessToken(AuthHolder.getRequestToken(), new Verifier(AuthHolder.getVerifier()));

			Auth auth = authI.checkToken(accessToken);

			// save token to AuthHolder and its preferences
			AuthHolder.setAuth(auth);
			AuthHolder.saveTokenToPrefs();
			return auth;
		} catch (Exception e) {
			throw new FlickrFaveException("Get access token failed.", e);
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
