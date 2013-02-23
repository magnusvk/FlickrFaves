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

import java.util.prefs.Preferences;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;

import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;

/**
 * @author Magnus von Koeller
 * 
 *         This class holds all authentication information in static variables.
 */
public class AuthHolder {

	/**
	 * Not instantiable.
	 */
	private AuthHolder() {
		super();
	}

	/** The Flickr API frob. */
	private static String frob = null;

	/** The Flickr API token. */
	private static String token = null;

	/** The flickrj Auth object corresponding to token. */
	private static Auth auth = null;

	/**
	 * Ensure that the passed RequestContext contains the current Auth object
	 * and the correct shared secret.
	 * 
	 * @param rc
	 *            The RequestContext to check.
	 */
	public static void ensureCorrectRequestContext(RequestContext rc) {
		rc.setAuth(AuthHolder.getAuth());
	}

	/**
	 * Forget current authorization token and delete it from preferences.
	 */
	public static void deauthorize() {
		// first set frob, token and auth to null
		frob = null;
		token = null;
		auth = null;
		// delete token from preferences
		Preferences prefs = Preferences.userNodeForPackage(AuthHolder.class);
		prefs.remove("token");
	}

	/**
	 * Save the current token to this class' preferences.
	 */
	public static void saveTokenToPrefs() {
		// load this class' preferences
		Preferences prefs = Preferences.userNodeForPackage(AuthHolder.class);
		// save token
		prefs.put("token", getToken());
	}

	/**
	 * Loads the token saved in this class' preferences, if any.
	 */
	public static void loadTokenFromPrefs() {
		// load this class' preferences
		Preferences prefs = Preferences.userNodeForPackage(AuthHolder.class);
		// load token
		setToken(prefs.get("token", null));
		// if loaded token is text 'null', set to actual null
		if ("null".equals(getToken()))
			setToken(null);
		// attempt loading Auth from token
		buildAuthFromToken();
	}

	/**
	 * Saves the flickrj auth object and the token it contains.
	 * 
	 * @param auth
	 *            The flickrj auth object to save.
	 */
	public static void setAuth(Auth auth) {
		// save auth
		AuthHolder.auth = auth;
		// also save the token itself
		AuthHolder.token = auth.getToken();
	}

	/**
	 * Returns the saved flickrj Auth object. If there is no Auth object saved
	 * but a token is available, the corresponding Auth object is obtained
	 * automatically.
	 * 
	 * @return The flickrj Auth object.
	 */
	public static Auth getAuth() {
		// maybe we have to build the auth from an available token?
		buildAuthFromToken();
		return auth;
	}

	/**
	 * If the flickrj Auth object is not yet available but a token is, loads the
	 * Auth object. Otherwise, does nothing.
	 */
	private static void buildAuthFromToken() {
		// if flickrj Auth object is not yet loaded but token is avaiblable,
		// then check token and save resultant Auth object
		if (auth == null && token != null) {
			Flickr flickrI = InterfaceHolder.getFlickrI();
			AuthInterface authI = flickrI.getAuthInterface();
			try {
				auth = authI.checkToken(token);
			} catch (FlickrException e) {
				// is the token invalid? then fail silently
				if (e.getErrorCode() == "98")
					auth = null;
			} catch (Exception e) {
				throw new FlickrFaveException("Obtaining Auth object from "
						+ "known token failed in AuthHolder.", e);
			}
		}
	}

	/**
	 * @return the frob
	 */
	public static String getFrob() {
		return frob;
	}

	/**
	 * @param frob
	 *            the frob to set
	 */
	public static void setFrob(String frob) {
		AuthHolder.frob = frob;
	}

	/**
	 * @return the token
	 */
	public static String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public static void setToken(String token) {
		AuthHolder.token = token;
	}
}
