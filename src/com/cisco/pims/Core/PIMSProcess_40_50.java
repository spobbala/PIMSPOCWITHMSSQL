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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PIMSProcess_40_50 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	private PIMSHelper helper;

	public PIMSProcess_40_50(Connection lclCon, Properties propFile) {
		this.pimsCon = lclCon;
		this.propFile = propFile;
		this.pimsLogging = new PIMSLogging(pimsCon,
				propFile.getProperty("Environment"));
	}

	public void process(int batchid) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		try {
			pstmt = batchid > 0 ? DBConnectionFactory.prepareStatement(
					this.pimsCon, PIMSConstants.QUERYBATCH_40_50, batchid,
					PIMSConstants.STATUS_40) : DBConnectionFactory
					.prepareStatement(this.pimsCon, PIMSConstants.QUERY_40_50,
							PIMSConstants.STATUS_40);
			rSet = pstmt.executeQuery();
			SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
			this.helper = new PIMSHelper();
			while (rSet.next()) {
				this.process_40_50(rSet.getInt("BATCH_ID"),
						rSet.getBytes("EMM1_BLOB"), rSet.getBytes("EMM2_BLOB"));
				mailProc.generateEmail(rSet.getInt("BATCH_ID"),
						PIMSConstants.PROCESS40_50);
			}

		} catch (SQLException sql) {
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

	private void process_40_50(int batchid, byte[] emm1, byte[] emm2) {
		PreparedStatement pstmt = null;

		byte[] sn1 = new byte[9];
		byte[] sn2 = new byte[9];
		byte[] mfgByte = new byte[14];

		int i = 0;
		int len = 0;
		int len1 = 0;
		int len2 = 0;
		int tVal = 0;
		int tInt = 0;

		String serialNumber = null;
		String tStr = null;

		Map<String, String> mValues = new HashMap<String, String>();

		byte[] emm1_Blob = new byte[308];
		byte[] emm2_Blob = new byte[308];
		byte[] raw1 = new byte[44];
		byte[] byteValue = null;
		try {
			pimsLogging.logMessage(batchid, serialNumber, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getTrackingMsgId(),
					"Entered 40_50 Process batchid:" + batchid);

			while ((i * 339) < emm1.length) {
				len = i * 339;
				len1 = (i * 339) + 323;
				serialNumber = helper.getByteToStringValues(emm1, len, sn1, 0,
						9);
				tStr = helper.getByteToStringValues(emm2, len, sn2, 0, 9);
				len = i * 339;

				mValues = this.readValuesForSN(serialNumber);
				mValues.put("MFGDATE", helper.getByteToStringValues(emm1, len1,
						mfgByte, 0, 14));
				len2 = (i * 339) + 15;
				System.arraycopy(emm1, len2, emm1_Blob, 0, 308);
				System.arraycopy(emm2, len2, emm2_Blob, 0, 308);
				i++;

				// /////////////////Build RAW 1 data///////////////////
				tStr = mValues.get("MAC");
				tStr = tStr.toUpperCase();
				tStr = tStr.replaceAll("..", "$0:").substring(0, 17);
				tStr = tStr + Character.toString('\0');
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, raw1, 0, byteValue.length); // MAC

				tStr = mValues.get("SMSN");
				tStr = tStr.toUpperCase();
				tStr = tStr.replaceAll("..", "$0:").substring(0, 17);
				tStr = tStr + Character.toString('\0');
				byteValue = tStr.getBytes();
				tVal = byteValue.length;
				System.arraycopy(byteValue, 0, raw1, tVal, byteValue.length); // SMSN

				tVal = tVal + byteValue.length;
				tStr = mValues.get("MODEL");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw1, tVal, byteValue.length); // MODEL

				tVal = tVal + byteValue.length;
				tStr = mValues.get("HWREV");
				tInt = Integer.parseInt(tStr);
				tStr = helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw1, tVal, byteValue.length); // HWREV

				// EBS Strategy
				tVal = tVal + byteValue.length;
				if (mValues.get("STRGY").equals("MFG_ID"))
					tStr = mValues.get("MFGID");
				else
					tStr = mValues.get("MAC").substring(0, 6);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw1, tVal, byteValue.length); // MAC
																				// FIRST
																				// 6
																				// CHARS
				tVal = tVal + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw1, tVal, byteValue.length); // RPAD
																				// 00
				tVal = 0;

				// ////////////////////Build RAW 2 data///////////
				byte[] raw2 = new byte[32 + raw1.length];
				int pk_cert_offset = raw1.length + 8 * 4;
				int us_cert_offset = pk_cert_offset + 1024;
				int emm_1_offset = us_cert_offset + 1024;
				int emm_2_offset = emm_1_offset + 308;

				System.arraycopy(raw1, 0, raw2, tVal, raw1.length); // RAW1
																	// copy

				tVal = raw1.length;
				tStr = "0000" + helper.convertIntToHex(pk_cert_offset);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tInt = Integer.parseInt(mValues.get("PKLEN"));
				tStr = "0000" + helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = "0000" + helper.convertIntToHex(us_cert_offset);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tInt = Integer.parseInt(mValues.get("USLEN"));
				tStr = "0000" + helper.convertIntToHex(tInt);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = "0000" + helper.convertIntToHex(emm_1_offset);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = "0000" + helper.convertIntToHex(308);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = "0000" + helper.convertIntToHex(emm_2_offset);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = "0000" + helper.convertIntToHex(308);
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw2, tVal, byteValue.length);

				tVal = 0;

				// ////////////////Build RAW 3
				// data/////////////////////////////////////////
				byte[] raw3 = new byte[emm1_Blob.length + emm2_Blob.length
						+ raw2.length + 2048];
				System.arraycopy(raw2, 0, raw3, tVal, raw2.length); // copy
																	// raw2

				tVal = raw2.length;
				tStr = mValues.get("PK");
				tStr = String.format("%1$-" + 2048 + "s", tStr).replaceAll(" ",
						"0");
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw3, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				tStr = mValues.get("US");
				tStr = String.format("%1$-" + 2048 + "s", tStr).replaceAll(" ",
						"0");
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, raw3, tVal, byteValue.length);

				tVal = tVal + byteValue.length;
				System.arraycopy(emm1_Blob, 0, raw3, tVal, emm1_Blob.length); // copy
																				// emm1

				tVal = tVal + emm1_Blob.length;
				System.arraycopy(emm2_Blob, 0, raw3, tVal, emm2_Blob.length); // copy
																				// emm2
				if (raw3 != null && serialNumber != null) {
					SimpleDateFormat format = new SimpleDateFormat(
							"MMddyyyyhhmmss");
					Date parsed = format.parse(mValues.get("MFGDATE"));
					java.sql.Timestamp tDate = new java.sql.Timestamp(
							parsed.getTime());
					pstmt = DBConnectionFactory.prepareStatement(pimsCon,
							PIMSConstants.UPDATEPRODQUERY, raw3, tDate,
							raw3.length, serialNumber);
					pstmt.executeUpdate();
					pimsCon.commit();
				}
				tVal = 0;
				mValues.clear();
			}
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.UPDATEBQUERY, PIMSConstants.STATUS_50,
					batchid);
			pstmt.executeUpdate();
			pimsCon.commit();
			pimsLogging.logMessage(batchid, serialNumber, null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getSuccessMsgId(),
					"Process 40_50 Completed, Batchid:" + batchid);

		} catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Exception in 40_50 Process, Error Details:"
							+ sql.getMessage());
		} catch (Exception e) {
			pimsLogging.logMessage(
					batchid,
					serialNumber,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Exception in 40_50 Process, Error Details:"
							+ e.getMessage());
		} finally {
			DBConnectionFactory.close(pstmt);
		}

	}

	private Map<String, String> readValuesForSN(String dhctSN)
			throws SQLException {

		Map<String, String> mValues = new HashMap<String, String>();
		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYPROD40_50, dhctSN);
		rSet = pstmt.executeQuery();
		while (rSet.next()) {
			mValues.put("SMSN", rSet.getString("SM_SN"));
			mValues.put("MAC", rSet.getString("MAC_ADDR"));
			mValues.put("MODEL", rSet.getString("MODEL"));
			mValues.put("HWREV", rSet.getString("HW_REV"));
			mValues.put("MFGID", rSet.getString("MFG_ID"));
			mValues.put("STRGY", rSet.getString("STRATEGY"));
		}
		DBConnectionFactory.close(pstmt, rSet);
		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYCERT_40_50, mValues.get("SMSN"));
		rSet = pstmt.executeQuery();
		while (rSet.next()) {
			mValues.put(rSet.getString("CERT_TYPE_CD"),
					rSet.getString("PUB_KEY_CERT"));
			mValues.put(rSet.getString("CERT_TYPE_CD") + "LEN",
					rSet.getString("PUB_KEY_CERT_LENGTH"));
		}
		DBConnectionFactory.close(pstmt, rSet);
		return mValues;
	}
}
