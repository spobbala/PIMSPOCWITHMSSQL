/*
 * Created on Dec 4, 2013
 * Author Shridhar Pobbala
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class CustomFTP {
	private String strServerNameOrIpAddress;
	private String userName;
	private String ftpPassword;
	private String fromLocation;
	private String toLocation;
	private String fileName;
	private byte[] inputBytes;

	public CustomFTP(String strServerNameOrIpAddress, String userName,
			String ftpPassword, String fromLocation, String toLocation,
			String fileName) {
		this.strServerNameOrIpAddress = strServerNameOrIpAddress;
		this.userName = userName;
		this.ftpPassword = ftpPassword;
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
		this.fileName = fileName;
	}

	public CustomFTP(String strServerNameOrIpAddress, String userName,
			String ftpPassword, String toLocation, String fileName,
			byte[] inputBytes) {
		this.strServerNameOrIpAddress = strServerNameOrIpAddress;
		this.userName = userName;
		this.ftpPassword = ftpPassword;
		this.toLocation = toLocation;
		this.fileName = fileName;
		this.inputBytes = inputBytes;
	}

	public boolean removeFile(String path, String fileName12)
			throws IOException {
		String fileName = path + fileName12;
		File f = new File(fileName);
		if (!f.exists())
			throw new IllegalArgumentException(
					"Delete: no such file or	directory : " + fileName);

		if (!f.canWrite())
			throw new IllegalArgumentException("Delete: write protected: "
					+ fileName);
		if (f.isDirectory()) {
			String files[] = f.list();
			if (files.length > 0)
				throw new IllegalArgumentException(
						"Delete: directory not empty:" + fileName);

		}
		boolean success = f.delete();
		if (!success)
			throw new IllegalArgumentException("Delete: deletion failed");
		else
			return success;
	}

	public long getFileSize(FTPClient ftpClient, String fileName)
			throws IOException {
		ftpClient.sendCommand("size " + fileName);
		long size = 0;
		String response = ftpClient.getReplyString().substring(4).trim();
		try {
			size = Long.parseLong(response);
		} catch (Exception e) {
			return -1L;
		}
		return size;
	}

	public String ftpSendFile() {
		String ftpStatus = null;
		boolean ftpStat = false;
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(strServerNameOrIpAddress);
			if (ftpClient.isConnected()) {
				ftpClient.login(userName, ftpPassword);
				ftpClient.changeWorkingDirectory(toLocation);
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				ftpClient.enterLocalPassiveMode();
				if(fromLocation!=null){
					FileInputStream fis =  new FileInputStream(fromLocation+ fileName);
					ftpStat = ftpClient.storeFile(fileName, fis);
					fis.close();
				}
				else{
					InputStream is = new ByteArrayInputStream(inputBytes);
					ftpStat = ftpClient.storeFile(fileName, is);
					is.close();
				}
				
				if (ftpStat)
					ftpStatus = "The following file FTP'ed Successfully:"
							+ fileName;
				else
					ftpStatus = "FTP Failed, file not sent: " + fileName;
				ftpClient.disconnect();
			}

		} catch (IOException e) {
			ftpStatus = e.getMessage();
		} catch (Exception e) {
			ftpStatus = e.getMessage();
		}
		return ftpStatus;

	}

}