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
package de.vonkoeller.flickrFaves.exceptions;

/**
 * @author Magnus von Koeller
 * 
 *         Any irrecoverable error in FlickrFaves.
 */
public class FlickrFaveException extends RuntimeException {

	/**
	 * For Serializable.
	 */
	private static final long serialVersionUID = -752680170286184935L;

	public FlickrFaveException() {
		super();
	}

	public FlickrFaveException(String msg) {
		super(msg);
	}

	public FlickrFaveException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
