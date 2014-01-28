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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class PIMSProcess_30_40 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	private PIMSHelper helper;
	private Set<Integer> nIDs = new LinkedHashSet<Integer>();
	private int notifID = 0;

	public PIMSProcess_30_40(Connection lclCon, Properties propFile) {
		this.pimsCon = lclCon;
		this.propFile = propFile;
		this.pimsLogging = new PIMSLogging(pimsCon,
				propFile.getProperty("Environment"));
	}

	public void process(int batchid) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		int tbatchid = 0;

		SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
		try {
			pstmt = batchid > 0 ? DBConnectionFactory.prepareStatement(
					this.pimsCon, PIMSConstants.QUERYBATCH, batchid,
					PIMSConstants.STATUS_30) : DBConnectionFactory
					.prepareStatement(this.pimsCon, PIMSConstants.QUERYSTATUS,
							PIMSConstants.STATUS_30);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				tbatchid = rSet.getInt("BATCH_ID");
				this.process_30_40(tbatchid);
				mailProc.generateEmail(rSet.getInt("BATCH_ID"),nIDs, 
						PIMSConstants.PROCESS40_50);
			}
		} catch (SQLException sql) {
			notifID = pimsLogging.logMessage(tbatchid, null, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Error in 30_40 Process while upload blobs, Error Details:"
							+ sql.getMessage());
			nIDs.add(notifID);
		} finally {
			DBConnectionFactory.close(pstmt, rSet);
		}
	}

	private void process_30_40(int batchid) {
		PreparedStatement pstmt = null;
		String fileTemp = null;
		String filePath_30_40 = null;

		byte[] result = null;

		boolean fileOne = false;
		boolean fileTwo = false;

		File currentFile = null;

		helper = new PIMSHelper();

		try {
			notifID = pimsLogging.logMessage(batchid, null, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityLow(),
					pimsLogging.getTrackingMsgId(), "Entered 30_40 Process");
			nIDs.add(notifID);

			filePath_30_40 = propFile.getProperty("FileLoc_30_40");
			if (filePath_30_40 == null) {
				notifID = pimsLogging.logMessage(batchid, null, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						"Error in 30_40 Process, EMM in folder not configured");
				nIDs.add(notifID);
				return;
			}

			// Load emm1 blob file
			fileTemp = String.format("%08d", batchid) + ".1";
			currentFile = new File(filePath_30_40 + fileTemp);
			if (currentFile != null) {
				try {
					InputStream input = new BufferedInputStream(
							new FileInputStream(currentFile));
					result = helper.readAndClose(input);
				} catch (FileNotFoundException e) {
					notifID = pimsLogging.logMessage(batchid, null, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"Error in 30_40 Process, EMM1 file not found, Error Details:"
									+ e.getMessage());
					nIDs.add(notifID);
				}
			}
			if (result != null) {
				this.loadNothingBlob(PIMSConstants.UPDATEQUERYEMM130_40,
						result, batchid);
				notifID = pimsLogging.logMessage(batchid, null, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityLow(),
						pimsLogging.getSuccessMsgId(),
						"Loaded EMM1 Blob into table");
				nIDs.add(notifID);
				currentFile.delete();
				fileOne = true;
			}
			fileTemp = String.format("%08d", batchid) + ".2";
			currentFile = new File(filePath_30_40 + fileTemp);
			if (currentFile != null) {
				try {
					InputStream input = new BufferedInputStream(
							new FileInputStream(currentFile));
					result = helper.readAndClose(input);
				} catch (FileNotFoundException e) {
					notifID = pimsLogging.logMessage(batchid, null, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"Error in 30_40 Process, EMM2 file not found, Error Details:"
									+ e.getMessage());
					nIDs.add(notifID);
				}
			}
			if (result != null) {
				this.loadNothingBlob(PIMSConstants.UPDATEQUERYEMM230_40,
						result, batchid);
				notifID = pimsLogging.logMessage(batchid, null, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityLow(),
						pimsLogging.getSuccessMsgId(),
						"Loaded EMM2 Blob into table");
				nIDs.add(notifID);
				currentFile.delete();
				fileTwo = true;
			}
			if (!fileOne){
				notifID = pimsLogging.logMessage(batchid, null, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						"File 1 is missing in 30_40 process for batchid:"
								+ batchid);
				nIDs.add(notifID);
			}
			
			if (!fileTwo){
				notifID = pimsLogging.logMessage(batchid, null, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						"File 2 is missing in 30_40 process for batchid:"
								+ batchid);
				nIDs.add(notifID);
			}
			if (fileOne && fileTwo) {
				pstmt = DBConnectionFactory.prepareStatement(pimsCon,
						PIMSConstants.UPDATEQUERYSTAT30_40,
						PIMSConstants.STATUS_40, batchid);
				pimsCon.commit();
				pstmt.executeUpdate();
			}
			fileOne = false;
			fileTwo = false;

		} catch (SQLException sql) {
			notifID = pimsLogging.logMessage(batchid, null, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Error in 30_40 Process while upload blobs, Error Details:"
							+ sql.getMessage());
			nIDs.add(notifID);
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
			notifID = pimsLogging.logMessage(batchid, null, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Error while updating Nothing Blob for the batch id:"
							+ batchid + ", Error Details:" + sql.getMessage());
			nIDs.add(notifID);
		} finally {
			DBConnectionFactory.close(updateBatch);
		}
	}
}
