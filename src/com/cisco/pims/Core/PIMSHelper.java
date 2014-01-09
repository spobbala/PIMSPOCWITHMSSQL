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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PIMSHelper {
	public String getByteToStringValues(byte[] emm1, int len,
			byte[] sn1, int i, int j) {
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
	 public void zip(File directory, File zipfile) throws IOException {
		    URI base = directory.toURI();
		    Deque<File> queue = new LinkedList<File>();
		    queue.push(directory);
		    OutputStream out = new FileOutputStream(zipfile);
		    Closeable res = out;
		    try {
		      ZipOutputStream zout = new ZipOutputStream(out);
		      res = zout;
		      while (!queue.isEmpty()) {
		        directory = queue.pop();
		        for (File kid : directory.listFiles()) {
		          String name = base.relativize(kid.toURI()).getPath();
		          if (kid.isDirectory()) {
		            queue.push(kid);
		            name = name.endsWith("/") ? name : name + "/";
		            zout.putNextEntry(new ZipEntry(name));
		          } else {
		            zout.putNextEntry(new ZipEntry(name));
		            this.copy(kid, zout);
		            zout.closeEntry();
		          }
		        }
		      }
		    } finally {
		      res.close();
		    }
		  }
	private void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
	    while (true) {
	      int readCount = in.read(buffer);
	      if (readCount < 0) {
	        break;
	      }
	      out.write(buffer, 0, readCount);
	    }
	}
	private void copy(File file, OutputStream out) throws IOException {
	    InputStream in = new FileInputStream(file);
	    try {
	      this.copy(in, out);
	    } finally {
	      in.close();
	    }
	  }

	  public String randomBatchID(int batchid){
		  long bid = 0;
		  long rval = 0;
		  bid = batchid;
		  rval = (bid * 34872) % 65536;
		  batchid = (int) rval;
		  return this.convertIntToHex(batchid);
		  
	  }
}
