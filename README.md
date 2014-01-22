## FlickrFaves

FlickrFaves is a small utility for **downloading high-resultion versions of your Flickr favorites to your harddrive**. If you are like me this means hundreds of beautiful new wallpapers at the click of a button! FlickrFaves is a cross-platform utility written in Java.

[**Download now!**](http://upload.magnusvk.com/FlickrFaves-2.0.5.jar) (current version: 2.0.5)

* [executable](http://upload.magnusvk.com/FlickrFaves-2.0.5.jar): JAR-file; simply double-click to run on most systems; requires Java, see below
* [source](http://upload.magnusvk.com/FlickrFaves-2.0.5-src.zip): ZIP-file; cotains only source code

**Warning:** Hundreds of people use FlickrFaves every month without any problem whatsoever. However, this software is released *without any warranty*. Backup the directory you are downloading to and use at your own risk!

**License:** FlickrFaves is released as open-source under the [GNU General Public License](http://www.gnu.org/licenses/gpl.html) version 2, or any later version published by the Free Software Foundation.

**Questions? Problems? Comments? Bugs?** — [email me!](mailto:hello@magnusvk.com)

### Changelog

* 2.0.5: Fix problem where images that were not “large enough” weren’t deleted
* 2.0.4: Fix crash when trying to open the browser
* 2.0.3: Don’t download the image-not-available image
* 2.0.2: Fix another common crash case
* 2.0.1: Now downloads original size again; fixes crashes
* 2.0.0: Fix problems with API key by reducing number of API calls
* 1.1.2: I honestly don’t remember. :)
* 1.1.1: Handle inaccessible favorites gracefully
* 1.1.0: Support for video download; choose whether to download photos, videos or both; download photos that are not public (e.g. for friends only); more gracefully handle network problems
* 1.0.4: Gracefully handle a download directory that does not exist or is not writable
* 1.0.3: Support for proxies; improved error reporting; new versions of BrowserLauncher2 and flickrj
* 1.0.2: Include a new flickrj version to fix problems during authorization
* 1.0.1: If a picture does not have an “original” size, download the largest available one instead
* 1.0: Identical to 1.0-b3; out of beta now as that version is very stable
* 1.0-b3: Fix stalling downloads; add automatic version check; minor improvements
* 1.0-b2: Fix problems with changed Flickr API; improve error handling
* 1.0-b1: Initial release

### Requirements:

* A [Flickr](http://www.flickr.com/) account
* Java Runtime Environment, version 5.0 or later ([available here](http://www.java.com/en/download/index.jsp))

### Features

* Download your Flickr favorites in the highest available resolution
* Optional minimum resolution: only download images that are sufficiently large for your screen
* Optionally limit the number of images downloaded — download only your newest faves
* Choose whether to re-download and overwrite existing images
* Automatically detects incomplete downloads
* Optionally delete stale faves
* Optionally exclude certain images
* Automatically remembers your choices

**Warning:** Always observe photo’s licenses! Do not re-distribute or otherwise utilize downloaded images without permission. You are responsible for this!

This software comes packaged with the following open-source libraries:

* [BrowserLauncher2](http://sf.net/projects/browserlaunch2)
* [flickrj](http://sf.net/projects/flickrj)
