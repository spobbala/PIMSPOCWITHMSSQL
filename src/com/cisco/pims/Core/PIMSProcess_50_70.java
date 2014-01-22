/*
 * Created on Jan 21, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PIMSProcess_50_70 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	private PIMSHelper helper;
	
	public PIMSProcess_50_70(Connection lclCon, Properties propFile) {
		this.pimsCon = lclCon;
		this.propFile = propFile;
		this.pimsLogging = new PIMSLogging(pimsCon,
				propFile.getProperty("Environment"));
	}
	public void process(int batchid){
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		try {
			pstmt = batchid > 0 ? DBConnectionFactory.prepareStatement(
					this.pimsCon, PIMSConstants.QUERYBATCH_50_70,
					batchid, PIMSConstants.STATUS_50) : DBConnectionFactory
					.prepareStatement(this.pimsCon,
							PIMSConstants.QUERY_50_70,
							PIMSConstants.STATUS_50);
			rSet = pstmt.executeQuery();
			SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
			this.helper = new PIMSHelper(); 
			while (rSet.next()) {
				this.process_50_70(rSet.getInt("BATCH_ID"),
						rSet.getString("DELIVERY_ID"),
						rSet.getString("INFO1"));
				mailProc.generateEmail(rSet.getInt("BATCH_ID"),
						PIMSConstants.PROCESS50_70);
			}
		}
		catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 50_70 Process, Error Details:"
							+ sql.getMessage());
		} finally {
			DBConnectionFactory.close(this.pimsCon, pstmt, rSet);
		}
}
	private void process_50_70(int batchid, String delID, String info1) {
		String baseDir = null;
		String tStr = null;
		String parentFolder = null;
		String tarFileLoc = null;
		Map<String, byte[]> mBlobs = new HashMap<String, byte[]>();
		File dir = null;
		FileOutputStream fos = null;
		PreparedStatement pstmt = null;
		ResultSet rSet1 = null;
		pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
				pimsLogging.getPriorityHigh(), pimsLogging.getTrackingMsgId(),
				"Entered 50_70 Process batchid:" + batchid);
			baseDir = propFile.getProperty("TempFileLoc");
			tarFileLoc = propFile.getProperty("TarFileLocation");
		if (baseDir == null || tarFileLoc == null) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"Tar File Location or temp Directory missing in properties file"
							+ "Tar File Location=" + tarFileLoc + "Temp Dir="
							+ baseDir);
		}

		try {
			mBlobs = this.generateBlobs(batchid);
			if (info1 != null)
				parentFolder = info1 + "-" + delID + "-COMPLETE";
			else
				parentFolder = delID + "-COMPLETE";

			dir = new File(baseDir + parentFolder);
			if (dir.exists()) {
				PIMSHelper.fileDelete(dir);
			} else
				dir.mkdir();
			tStr = parentFolder + "/inventry";
			dir = new File(baseDir + tStr);
			dir.createNewFile();
			fos = new FileOutputStream(dir);
			fos.write(mBlobs.get("INV"));
			fos.close();
			mBlobs.remove("INV");

			tStr = parentFolder + "/toc";
			dir = new File(baseDir + tStr);
			dir.createNewFile();
			fos = new FileOutputStream(dir);
			fos.write(mBlobs.get("TOC"));
			fos.close();
			mBlobs.remove("TOC");

			tStr = parentFolder + "/dncs";
			dir = new File(baseDir + tStr);
			dir.mkdir();

			tStr = parentFolder + "/dncs/revlist";
			dir = new File(baseDir + tStr);
			dir.createNewFile();

			tStr = parentFolder + "/dncs" + "/bootpgs";
			dir = new File(baseDir + tStr);
			dir.mkdir();
			tStr = parentFolder + "/dncs" + "/cauth";
			dir = new File(baseDir + tStr);
			dir.mkdir();
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYCACERTS);
			rSet1 = pstmt.executeQuery();
			while (rSet1.next()) {
				tStr = parentFolder + "/dncs" + "/cauth/"
						+ rSet1.getString("CERTIFICATE_NAME");
				dir = new File(baseDir + tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				String hexValue = rSet1.getString("CERTIFICATE_BLOB");
				byte[] byteValue = helper.hexStringToByteArray(hexValue);
				fos.write(byteValue);
				fos.close();
			}
			DBConnectionFactory.close(pstmt, rSet1);
			tStr = parentFolder + "/dncs" + "/dhcts";
			dir = new File(baseDir + tStr);
			dir.mkdir();
			for (String mac : mBlobs.keySet()) {
				tStr = parentFolder + "/dncs" + "/dhcts/" + mac;
				dir = new File(baseDir + tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				byte[] byteValue = mBlobs.get(mac);
				fos.write(byteValue);
				fos.close();
			}

			tStr = parentFolder + "/dncs" + "/hcttypes";
			dir = new File(baseDir + tStr);
			dir.mkdir();
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYHCTTYPES, batchid);
			rSet1 = pstmt.executeQuery();
			while (rSet1.next()) {
				tStr = parentFolder + "/dncs" + "/hcttypes/"
						+ rSet1.getString("FILENAME");
				dir = new File(baseDir + tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				fos.write(rSet1.getBytes("FILE_DATA"));
				fos.close();
			}
			DBConnectionFactory.close(pstmt, rSet1);
			tStr = delID + "-" + helper.randomBatchID(batchid) + ".tar";
			dir = new File(tarFileLoc + tStr);
			File dir1 = new File(baseDir);
			PIMSHelper.zip(dir1, dir);
			File delFolder = new File(baseDir + parentFolder);
			PIMSHelper.fileDelete(delFolder);
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.UPDATEBQUERY, PIMSConstants.STATUS_70,
					batchid);
			pstmt.executeUpdate();
			pimsCon.commit();
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getSuccessMsgId(),
					"Tar File Generated in Location:" + tarFileLoc + tStr);
		} catch (SQLException sql) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"DB Error in 50_70 Process while generating tar file, Error Details:"
							+ sql.getMessage() + ":" + sql.getCause());
		} catch (IOException e) {
			pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"Error in 50_70 Process while generating tar file, Error Details:"
							+ e.getMessage() + ":" + e.getCause());
		}

	}

	private Map<String, byte[]> generateBlobs(int batchid) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		byte[] byteValue = null;
		String tStr = null;
		String serialNumber = null;
		int i = 0;
		int tInt = 0;
		Set<byte[]> tocblobData = new LinkedHashSet<byte[]>();
		Set<byte[]> invblobData = new LinkedHashSet<byte[]>();
		Map<String, byte[]> mBlobs = new LinkedHashMap<String, byte[]>();
		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYPROD50_70, batchid);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				mBlobs.put(rSet.getString("MAC_ADDR"),
						helper.hexStringToByteArray(rSet.getString("EMM_FILE")));

				// //Build TOC BLOB/////
				byte[] tocBlob = new byte[36];
				byte[] invBlob = new byte[49];
				byte[] invfinalBlob = new byte[67 + 49];
				tStr = rSet.getString("DHCT_SN");
				serialNumber = rSet.getString("DHCT_SN");
				tStr = String.format("%-9s", tStr);
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, tocBlob, 0, byteValue.length);

				i = byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length);

				i = i + byteValue.length;
				tStr = rSet.getString("MAC_ADDR");
				tStr = tStr.toUpperCase();
				tStr = tStr.replaceAll("..", "$0:").substring(0, 17);
				tStr = tStr + Character.toString('\0');
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length); // MAC

				i = i + byteValue.length;
				tStr = rSet.getString("MODEL");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length); // MODEL

				i = i + byteValue.length;
				tStr = rSet.getString("HW_REV");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length); // HWREV

				// EBS Strategy
				i = i + byteValue.length;
				if (rSet.getString("STRATEGY").equals("MFG_ID"))
					tStr = rSet.getString("MFG_ID");
				else
					tStr = rSet.getString("MAC_ADDR").substring(0, 6);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length); // mfg_id/MAC
																				// FIRST
																				// 6
																				// CHARS

				i = i + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length);
				tocblobData.add(tocBlob);

				// //Build INV BLOB///
				i = 0;
				tStr = rSet.getString("DHCT_SN");
				tStr = String.format("%-9s", tStr);
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, invBlob, 0, byteValue.length);

				i = i + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i + byteValue.length;
				tStr = rSet.getString("MAC_ADDR");
				tStr = tStr.toUpperCase();
				tStr = tStr.replaceAll("..", "$0:").substring(0, 17);
				// tStr = tStr + Character.toString('\0');
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // MAC

				i = i + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i + byteValue.length;
				tStr = "00000000000000000000000000";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i + byteValue.length;
				tStr = rSet.getString("MODEL");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // MODEL

				i = i + byteValue.length;
				tStr = rSet.getString("HW_REV");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // HWREV

				// EBS Strategy
				i = i + byteValue.length;
				if (rSet.getString("STRATEGY").equals("MFG_ID"))
					tStr = rSet.getString("MFG_ID");
				else
					tStr = rSet.getString("MAC_ADDR").substring(0, 6);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // mfg_id/MAC
																				// FIRST
																				// 6
																				// CHARS

				byte[] emptyBlob = this.createEmptyBlob();
				i = i + byteValue.length;
				System.arraycopy(emptyBlob, 0, invfinalBlob, i,
						emptyBlob.length); // Add
											// empty
											// Blob

				i = emptyBlob.length;
				System.arraycopy(invBlob, 0, invfinalBlob, i, invBlob.length);
				invblobData.add(invfinalBlob);
			}

			int len = tocblobData.size();
			byte[] tocFinal_Blob = new byte[36 * len];
			len = invblobData.size();
			byte[] invfinalBlob = new byte[116 * len];
			i = 0;
			for (byte[] bytes : tocblobData) {
				System.arraycopy(bytes, 0, tocFinal_Blob, i, bytes.length);
				i = i + bytes.length;
			}
			i = 0;
			if (tocFinal_Blob != null)
				mBlobs.put("TOC", tocFinal_Blob);
			for (byte[] bytes : invblobData) {
				System.arraycopy(bytes, 0, invfinalBlob, i, bytes.length);
				i = i + bytes.length;
			}
			if (invfinalBlob != null)
				mBlobs.put("INV", invfinalBlob);
		} catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 50_70 Process batchid:" + batchid
							+ ", Error Details:" + sql.getMessage() + ":"
							+ sql.getCause());

		} catch (Exception e) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Error in 50_70 Process batchid:" + batchid
							+ ", Error Details:" + e.getMessage() + ":"
							+ e.getCause());
		}
		return mBlobs;
	}
	private byte[] createEmptyBlob() {
		byte[] emptyBlob = new byte[67];
		byte[] byteValue = null;
		String tStr = null;
		int i = 0;

		tStr = "000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "0000000000000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "0000000000000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "00000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "0000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "0000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i + byteValue.length;
		tStr = "000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);
		return emptyBlob;
	}

}
