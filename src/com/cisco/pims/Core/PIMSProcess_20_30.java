/*
 * Created on Jan 21, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.cisco.pims.Utilities.CustomFTP;
import com.cisco.pims.Utilities.CustomSFTP;

public class PIMSProcess_20_30 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	private PIMSHelper helper;
	
	public PIMSProcess_20_30(Connection lclCon, Properties propFile) {
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
				this.pimsCon, PIMSConstants.QUERYBATCH, batchid,
				PIMSConstants.STATUS_20) : DBConnectionFactory
				.prepareStatement(this.pimsCon,
						PIMSConstants.QUERYSTATUS,
						PIMSConstants.STATUS_20);
		rSet = pstmt.executeQuery();
		SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
		this.helper = new PIMSHelper(); 
		while (rSet.next()) {
			this.generateNothingBlob_20_30(rSet
					.getInt("BATCH_ID"));
			mailProc.generateEmail(rSet.getInt("BATCH_ID"),
					PIMSConstants.PROCESS20_30);
		}
		}catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 20_30 Process, Error Details:"
							+ sql.getMessage());
		} finally {
			DBConnectionFactory.close(this.pimsCon, pstmt, rSet);
		}
	}
	private void generateNothingBlob_20_30(int batchid) {
		String serialNumber = null;
		String smsn = null;
		String tString = "0";
		String tString1 = null;

		int finalLength = 0;
		int totLen = 0;

		Set<byte[]> blobData = new LinkedHashSet<byte[]>();

		byte[] byteValues = null;
		byte[] nothingBlob = null;
		byte[] tempValue = new byte[1024];

		Map<String, String> cValues = new HashMap<String, String>();

		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
				pimsLogging.getPriorityLow(), pimsLogging.getTrackingMsgId(),
				PIMSConstants.MSG_START_20_30);
		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYPROD_20_30, batchid);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				serialNumber = rSet.getString("DHCT_SN");
				smsn = rSet.getString("SM_SN");
				cValues = this.populateCertificates(smsn,
						rSet.getString("SHIP_TO_CUST_ID"), batchid,
						serialNumber);
				if (cValues.size() != 6)
					continue;
				tString = "0";
				int certLen = Integer.parseInt(cValues.get("PKLEN"));
				int tlen = 1024 - certLen;
				tString = String.format(String.format("%%0%dd", tlen * 2), 0)
						.replace("0", tString);
				totLen = serialNumber.getBytes().length // dhct_sn length
						+ 2 // PK Certificate Length
						+ 1024 // PK Certificate length
						+ 128 // CUS_CAA
						+ 384 // SACAA
						+ 6 // SMSN length
						+ 6; // MAC Address
				byte[] finalValue = new byte[totLen];

				System.arraycopy(cValues.get("PKCERT").getBytes(), 0,
						tempValue, 0, certLen);
				byteValues = helper.hexStringToByteArray(tString);
				System.arraycopy(byteValues, 0, tempValue, certLen,
						byteValues.length);

				// Start building blob here
				System.arraycopy(serialNumber.getBytes(), 0, finalValue, 0,
						serialNumber.getBytes().length); // dhct_sn

				totLen = serialNumber.getBytes().length;
				tString1 = helper.convertIntToHex(certLen);
				byteValues = helper.hexStringToByteArray(tString1);
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length);// pk cert length

				totLen = totLen + byteValues.length;
				System.arraycopy(tempValue, 0, finalValue, totLen, 1024); // PK
																			// cert

				totLen = totLen + 1024;
				System.arraycopy(cValues.get("CKCERT").getBytes(), 0,
						finalValue, totLen, 128); // cust_caa

				totLen = totLen + 128;
				System.arraycopy(cValues.get("SACERT").getBytes(), 0,
						finalValue, totLen, 384); // sa_caa

				totLen = totLen + 384;
				byteValues = helper.hexStringToByteArray(smsn);
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length); // sm_sn

				totLen = totLen + byteValues.length;
				byteValues = helper.hexStringToByteArray(rSet
						.getString("MAC_ADDR"));
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length); // mac address

				totLen = totLen + byteValues.length;
				finalLength = finalLength + totLen;
				blobData.add(finalValue);
			}
			DBConnectionFactory.close(pstmt, rSet);
			if (blobData.size() > 0) {
				nothingBlob = new byte[finalLength];
				int i = 0;
				for (byte[] bytes : blobData) {
					System.arraycopy(bytes, 0, nothingBlob, i, bytes.length);
					i = i + bytes.length;
				}
				this.loadNothingBlob(PIMSConstants.UPDATEQUERY_20_30,
						nothingBlob, batchid);
				this.pimsCon.commit();
				this.sendNothingBlob("70", batchid, nothingBlob);
				pstmt = DBConnectionFactory.prepareStatement(pimsCon,
						PIMSConstants.UPDATEBQUERY, PIMSConstants.STATUS_30,
						batchid);
				pstmt.executeUpdate();
				pimsCon.commit();
				pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
						pimsLogging.getPriorityLow(),
						pimsLogging.getSuccessMsgId(),
						"Nothing Blob updated for the batch id:" + batchid);
			}
		} catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 20_30 Process, Error Details:"
							+ sql.getMessage());
		} catch (Exception e) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Exception in 20_30 Process, Error Details:"
							+ e.getMessage());
			e.printStackTrace();

		} finally {
			DBConnectionFactory.close(pstmt, rSet);
		}
	}
	private Map<String, String> populateCertificates(String smsn,
			String shipCust, int batchid, String serialNumber) {
		int cnt = 0;
		Map<String, String> cValues = new HashMap<String, String>();

		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYCERTS_20_30, smsn);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				cnt++;
				cValues.put("PKLEN", rSet.getString("pub_key_cert_length"));
				cValues.put("PKCERT", rSet.getString("pub_key_cert"));
			}
			DBConnectionFactory.close(pstmt, rSet);
			if (cnt == 0) {
				pimsLogging.logMessage(batchid, serialNumber, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						PIMSConstants.MSG_ERRCERT_20_30);
			}
			cnt = 0;
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYSACERTS_20_30, smsn);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				cValues.put("SALEN", rSet.getString("SA_CAA_PUB_KEYS_LENGTH"));
				cValues.put("SACERT", rSet.getString("SA_CAA_PUB_KEYS"));
				cnt++;
			}
			DBConnectionFactory.close(pstmt, rSet);
			if (cnt == 0)
				pimsLogging.logMessage(batchid, serialNumber, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						PIMSConstants.MSG_ERRSACERT_20_30);

			cnt = 0;
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYCKCERTS_20_30, shipCust);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				cValues.put("CKLEN", rSet.getString("CUST_CAA_PUB_KEY_LENGTH"));
				cValues.put("CKCERT", rSet.getString("CUST_CAA_PUB_KEY"));
				cnt++;
			}
			DBConnectionFactory.close(pstmt, rSet);
			if (cnt == 0)
				pimsLogging.logMessage(batchid, serialNumber, null,
						pimsLogging.getSequence(),
						pimsLogging.getPriorityHigh(),
						pimsLogging.getErrMsgId(),
						PIMSConstants.MSG_ERRCKCERT_20_30);

		} catch (SQLException sql) {
			sql.printStackTrace();
			pimsLogging.logMessage(batchid, serialNumber, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 20_30 Process while reading certificates data, Error Details:"
							+ sql.getMessage());
		}
		return cValues;

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
	public void sendNothingBlob(String siteID, int batchID, byte[] nothingBlob) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		String priKeyLoc = null;
		String status = null;

		CustomFTP ftpClient = null;
		CustomSFTP sftpClient = null;

		int port = 0;

		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYFTPSITEID, siteID);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				String tserverName = rSet.getString("SITE_ADDRESS");
				String serverName = null;
				String userName = rSet.getString("USER_ID");
				String ftpPwd = rSet.getString("PASSWORD");
				String toLocation = rSet.getString("HOME_DIRECTORY");
				String fileName = String.format("%08d", batchID);
				priKeyLoc = propFile.getProperty("PubKeyLoc");
				// Get Port from IP Address
				if (tserverName.contains(":")) {
					serverName = tserverName.substring(0,
							tserverName.indexOf(":") + 1).trim();
					port = Integer.parseInt(tserverName.substring(tserverName
							.indexOf(":") + 1));
				} else if (tserverName.contains(" ")) {
					serverName = tserverName.substring(0,
							tserverName.indexOf(" ") + 1).trim();
					port = Integer.parseInt(tserverName.substring(tserverName
							.indexOf(" ") + 1));
				}

				if (port < 1)
					port = 21;
				if (port <= 21) {
					ftpClient = new CustomFTP(serverName, userName, ftpPwd,
							toLocation, fileName, nothingBlob);
					status = ftpClient.ftpSendFile();
				} else if (port >= 22) {
					sftpClient = new CustomSFTP(serverName, userName, ftpPwd,
							toLocation, fileName, nothingBlob, port,
							"public key", priKeyLoc,
							propFile.getProperty("EULA"));
					status = sftpClient.sftpSend();
				}

				if (status.contains(PIMSConstants.FILESTATUS)) {
					pimsLogging.logMessage(batchID, null, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityLow(),
							pimsLogging.getSuccessMsgId(),
							"Nothing Blob sent successfully with fileName: "
									+ fileName + " to Location " + toLocation
									+ " on server:" + serverName);
				}
			}
		} catch (SQLException sql) {
			pimsLogging.logMessage(batchID, null, null, pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(), pimsLogging.getErrMsgId(),
					"DB Error in 20_30 Process while FTPing Nothing Blob to Site ID"
							+ siteID + ", Error Details:" + sql.getMessage());
		} finally {
			DBConnectionFactory.close(pstmt, rSet);
		}

	}
}
