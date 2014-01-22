/*
 * Created on Jan 21, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class PIMSProcess_30_40 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	private PIMSHelper helper;
	
	public PIMSProcess_30_40(Connection lclCon, Properties propFile) {
		this.pimsCon = lclCon;
		this.propFile = propFile;
		this.pimsLogging = new PIMSLogging(pimsCon,
				propFile.getProperty("Environment"));
	}
	public void process() {
		PreparedStatement pstmt = null;
		String fileTemp = null;
		String file1 = null;
		String fileType = null;
		String filePath_30_40 = null;
		int batchid = 0;
		int whichBlob = 0;
		byte[] result = null;
		boolean fileOne = false;
		boolean fileTwo = false;
		filePath_30_40 = propFile.getProperty("FileLoc_30_40");
		helper = new PIMSHelper();
		try {
			File fileFolder = new File(filePath_30_40);
			if (fileFolder != null) {
				for (final File fileEntry : fileFolder.listFiles()) {
					if (!fileEntry.isDirectory()) {
						fileTemp = fileEntry.getName();
						File currentFile = new File(filePath_30_40 + fileTemp);
						InputStream input = new BufferedInputStream(
								new FileInputStream(currentFile));
						result = helper.readAndClose(input);
						int i = fileTemp.indexOf(".");
						file1 = fileTemp.substring(0, i);
						fileType = fileTemp.substring(i + 1, fileTemp.length());
						batchid = Integer.parseInt(file1.replaceFirst(
								"^0+(?!$)", ""));
						pimsLogging.logMessage(batchid, null, null,
								pimsLogging.getSequence(),
								pimsLogging.getPriorityLow(),
								pimsLogging.getTrackingMsgId(),
								"Entered 30_40 Process");
						if (fileType.equals("1")) {
							whichBlob = 1;
							fileOne = true;
						} else if (fileType.equals("2")) {
							whichBlob = 2;
							fileTwo = true;
						}
						if (whichBlob != 0 && result != null) {
							if (whichBlob == 1) {
								this.loadNothingBlob(
										PIMSConstants.UPDATEQUERYEMM130_40,
										result, batchid);
								pimsLogging.logMessage(batchid, null, null,
										pimsLogging.getSequence(),
										pimsLogging.getPriorityLow(),
										pimsLogging.getSuccessMsgId(),
										"Loaded EMM1 Blob into table");
							} else if (whichBlob == 2) {
								this.loadNothingBlob(
										PIMSConstants.UPDATEQUERYEMM230_40,
										result, batchid);
								pimsLogging.logMessage(batchid, null, null,
										pimsLogging.getSequence(),
										pimsLogging.getPriorityLow(),
										pimsLogging.getSuccessMsgId(),
										"Loaded EMM2 Blob into table");
							}
							currentFile.delete();
						}
					}
				}
				if (!fileOne)
					pimsLogging.logMessage(batchid, null, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"File 1 is missing in 30_40 process for batchid:"
									+ batchid);
				if (!fileTwo)
					pimsLogging.logMessage(batchid, null, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"File 2 is missing in 30_40 process for batchid:"
									+ batchid);
				if (fileOne && fileTwo) {
					pstmt = DBConnectionFactory.prepareStatement(pimsCon,
							PIMSConstants.UPDATEQUERYSTAT30_40,
							PIMSConstants.STATUS_40, batchid);
					pimsCon.commit();
					pstmt.executeUpdate();
				}
			}
		} catch (SQLException sql) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"Error in 30_40 Process while upload blobs, Error Details:"
							+ sql.getMessage());
		} catch (FileNotFoundException e) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"Error in 30_40 Process while upload blobs, Error Details:"
							+ e.getMessage());
		} finally {
			DBConnectionFactory.close(pstmt);
		}
	}
	public void loadNothingBlob(String updateQuery, byte[] nothingBlob,
			int batchid) {
		PreparedStatement updateBatch = null;
		try {
			updateBatch = DBConnectionFactory.prepareStatement(pimsCon,
					updateQuery, nothingBlob, batchid);
			updateBatch.executeUpdate();
			pimsCon.commit();
		} catch (SQLException sql) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"Error while updating Nothing Blob for the batch id:"
							+ batchid + ", Error Details:" + sql.getMessage());
		} finally {
			DBConnectionFactory.close(updateBatch);
		}
	}
}
