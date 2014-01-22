/*
 * Created on Jan 6, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarOutputStream;

public class PIMSHelper {
	public String getByteToStringValues(byte[] emm1, int len, byte[] sn1,
			int i, int j) {
		String value = null;
		System.arraycopy(emm1, len, sn1, i, j);
		value = new String(sn1);
		return value;
	}

	public String convertIntToHex(int iValue) {
		return Integer.toHexString(0x10000 | iValue).substring(1).toUpperCase();
	}

	public byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public byte[] readAndClose(InputStream aInput) {
		byte[] blobFile = new byte[32 * 1024];
		ByteArrayOutputStream result = null;
		try {
			try {
				result = new ByteArrayOutputStream(blobFile.length);
				int bytesRead = 0;
				while (bytesRead != -1) {
					// aInput.read() returns -1, 0, or more :
					bytesRead = aInput.read(blobFile);
					if (bytesRead > 0) {
						result.write(blobFile, 0, bytesRead);
					}
				}
			} finally {
				aInput.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result.toByteArray();
	}

	public static void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		fileDelete(zipfile);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		try {
			TarOutputStream zout = new TarOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new TarEntry(kid, name));
					} else {
						zout.putNextEntry(new TarEntry(kid, name));
						copy(kid, zout);
					}
				}
			}
		} finally {
			res.close();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	public String randomBatchID(int batchid) {
		long bid = 0;
		long rval = 0;
		bid = batchid;
		rval = (bid * 34872) % 65536;
		batchid = (int) rval;
		return this.convertIntToHex(batchid);

	}

	public static void fileDelete(File srcFile) throws FileNotFoundException,
			IOException {
		// Checks if file is a directory
		if (srcFile.isDirectory()) {
			// Gathers files in directory
			File[] b = srcFile.listFiles();
			for (int i = 0; i < b.length; i++) {
				// Recursively deletes all files and sub-directories
				fileDelete(b[i]);
			}
			// Deletes original sub-directory file
			srcFile.delete();
		} else {
			srcFile.delete();
		}
	}

}
