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

import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;

import de.vonkoeller.flickrFaves.exceptions.FlickrFaveException;

/**
 * @author Magnus von Koeller
 * 
 *         Singleton holding the current instance of the Flickr API.
 */
public class InterfaceHolder {

	/** This application's Flickr API key. */
	static final String API_KEY = "995070972bd64558e2f6a3dec03b8e75";

	/** This applications' Flickr shared secret. */
	static final String SHARED_SECRET = "7540c8541cdb5db6";

	/** The current flickr instance. */
	private static Flickr flickr = null;

	/**
	 * This class is a singleton and therefore not instantiable.
	 */
	private InterfaceHolder() {
		super();
	}

	/**
	 * Gets the current Flickr interface. If not yet instantiated, instantiates
	 * it.
	 * 
	 * @return The current Flickr interface.
	 */
	public static Flickr getFlickrI() {
		if (flickr == null) {
			try {
				flickr = new Flickr(API_KEY, SHARED_SECRET, new REST());
			} catch (ParserConfigurationException e) {
				throw new FlickrFaveException(
						"Could not instantiate Flickr object.", e);
			}
			RequestContext rc = RequestContext.getRequestContext();
			rc.setAuth(AuthHolder.getAuth());
		}
		return flickr;
	}
}
