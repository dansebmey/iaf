/*
   Copyright 2018 Nationale-Nederlanden

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.util.DateUtils;
import nl.nn.adapterframework.util.Misc;
import nl.nn.adapterframework.util.XmlBuilder;

/**
 * 
 * 
 * @author alisihab
 *
 */
public class SambaFileSystem implements IFileSystem<SmbFile> {

	private SmbFile smbContext;
	private boolean isForce;
	private boolean listHiddenFiles = true;

	public SambaFileSystem(SmbFile smbContext, boolean isForce) {
		this.smbContext = smbContext;
		this.isForce = isForce;
	}

	@Override
	public void configure() throws ConfigurationException {

	}

	@Override
	public SmbFile toFile(String filename) throws FileSystemException {
		try {
			return new SmbFile(smbContext, filename);
		} catch (IOException e) {
			throw new FileSystemException("unable to get SMB file [" + filename + "]", e);
		}
	}

	@Override
	public Iterator<SmbFile> listFiles() {
		try {
			if (!isListHiddenFiles()) {
				SmbFileFilter filter = new SmbFileFilter() {

					@Override
					public boolean accept(SmbFile file) throws SmbException {
						return !file.isHidden();
					}
				};
				return new SmbFileIterator(smbContext.listFiles(filter));
			}
			return new SmbFileIterator(smbContext.listFiles());
		} catch (SmbException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean exists(SmbFile f) throws FileSystemException {
		try {
			return f.exists();
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public OutputStream createFile(SmbFile f) throws FileSystemException {
		try {
			return new SmbFileOutputStream(f);
		} catch (Exception e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public OutputStream appendFile(SmbFile f) throws FileSystemException {
		try {
			return new SmbFileOutputStream(f, true);
		} catch (Exception e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public InputStream readFile(SmbFile f) throws IOException {
		SmbFileInputStream is = new SmbFileInputStream(f);
		return is;
	}

	@Override
	public void deleteFile(SmbFile f) throws FileSystemException {
		try {
			if (!f.exists()) {
				throw new FileSystemException("file not found");
			}
			if (f.isFile()) {
				f.delete();
			} else {
				throw new FileSystemException("trying to remove [" + f.getName()
						+ "] which is a directory instead of a file");
			}
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public String getInfo(SmbFile f) throws FileSystemException {
		return getFileAsXmlBuilder(f).toXML();
	}

	@Override
	public boolean isFolder(SmbFile f) throws FileSystemException {
		try {
			return f.isDirectory();
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public void createFolder(SmbFile f) throws FileSystemException {
		try {
			if (isForce) {
				f.mkdirs();
			} else {
				f.mkdir();
			}
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public void removeFolder(SmbFile f) throws FileSystemException {
		try {
			if (exists(f)) {
				if (f.isDirectory()) {
					f.delete();
				} else {
					throw new FileSystemException("trying to remove file [" + f.getName()
							+ "] which is a file instead of a directory");
				}
			} else {
				throw new FileSystemException("trying to remove file [" + f.getName()
						+ "] which does not exist");
			}
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}
	}

	@Override
	public void renameTo(SmbFile f, String destination) throws FileSystemException {
		SmbFile dest;
		try {
			dest = new SmbFile(smbContext, destination);
			if (dest.exists()) {
				if (isForce)
					dest.delete();
				else {
					return;
				}
			}
			f.renameTo(dest);
		} catch (SmbException e) {
			throw new FileSystemException(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	@Override
	public XmlBuilder getFileAsXmlBuilder(SmbFile file) throws FileSystemException {
		XmlBuilder fileXml = new XmlBuilder("file");
		fileXml.addAttribute("name", file.getName());
		long fileSize = 0;
		try {
			fileSize = file.length();
			fileXml.addAttribute("size", "" + fileSize);
			fileXml.addAttribute("fSize", "" + Misc.toFileSize(fileSize, true));
			fileXml.addAttribute("directory", "" + file.isDirectory());
			fileXml.addAttribute("canonicalName", file.getCanonicalPath());

			// Get the modification date of the file
			Date modificationDate = null;
			modificationDate = new Date(file.lastModified());
			//add date
			String date = DateUtils.format(modificationDate, DateUtils.FORMAT_DATE);
			fileXml.addAttribute("modificationDate", date);

			// add the time
			String time = DateUtils.format(modificationDate, DateUtils.FORMAT_TIME_HMS);
			fileXml.addAttribute("modificationTime", time);
		} catch (SmbException e) {
			throw new FileSystemException(e);
		}

		return fileXml;
	}

	private class SmbFileIterator implements Iterator<SmbFile> {

		private SmbFile files[];
		private int i = 0;

		public SmbFileIterator(SmbFile files[]) {
			this.files = files;
		}

		@Override
		public boolean hasNext() {
			return files != null && i < files.length;
		}

		@Override
		public SmbFile next() {
			return files[i++];
		}
	}

	@Override
	public void augmentDirectoryInfo(XmlBuilder dirInfo, SmbFile file) {
		dirInfo.addAttribute("name", file.getCanonicalPath());

	}

	public boolean isListHiddenFiles() {
		return listHiddenFiles;
	}

	public void setListHiddenFiles(boolean listHiddenFiles) {
		this.listHiddenFiles = listHiddenFiles;
	}
}