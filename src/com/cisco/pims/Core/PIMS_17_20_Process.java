/*
 * Created on Dec 11, 2013
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.cisco.pims.Utilities.CustomFTP;
import com.cisco.pims.Utilities.CustomSFTP;
import com.cisco.pims.Utilities.SendMail;

public class PIMS_17_20_Process {
	public static String mailMessage = "";
	static DBConnectionDAO DAOClient = null;
	static PIMSHelper helper = null;
	private static final String STATUS_17 = "17";
	private static final String STATUS_20 = "20";
	private static final String STATUS_30 = "30";
	private static final String STATUS_40 = "40";
	private static final String STATUS_50 = "50";
	// private static final String STATUS_70 = "70";
	private static final String FILEPATH = "c:/testin/30_40/";

	public static void main(String args[]) {
		DAOClient = new DBConnectionDAO();
		helper = new PIMSHelper();
		ResultSet rSet = null;
		String readQuery = null;
		String processName = null;
		processName = "50_70";
		DAOClient
				.connect(
						"mssql",
						"jdbc:sqlserver://ALLN01-ATS-QMXSQL1\\QMXSQL1:51005;databaseName=PIMS",
						"pimsdb", "Pimsdb!");
		if (DAOClient.errorStatus) {
			mailMessage = DAOClient.errorMessage;
			if (mailMessage != null)
				System.out.println(mailMessage);
		} else {
			if (processName.equals("17_20")) {
				readQuery = "select b.batch_id, b.ship_to_cust_id, d.dhct_sn from dbo.pims_batch_header b join dbo.pims_batch_detail d"
						+ " on b.batch_id = d.batch_id where d.batch_id in "
						+ "(select batch_id from dbo.pims_batch_header where batch_status_cd = '"
						+ STATUS_17 + "')";
				rSet = DAOClient.readDataFromTable(readQuery);
				prepareProductTable_17_20(rSet);
			} else if (processName.equals("20_30")) {
				readQuery = "select b.batch_id, p.dhct_sn, p.sm_sn, p.mac_addr, convert(int, c.pub_key_cert_length),"
						+ " c.pub_key_cert, s.sa_caa_pub_keys, convert(int,s.sa_caa_pub_keys_length),"
						+ " ck.cust_caa_pub_key, convert(int,ck.cust_caa_pub_key_length), ftp.site_id"
						+ " from dbo.pims_batch_header b"
						+ " join dbo.pims_batch_detail d on b.batch_id = d.batch_id"
						+ " join dbo.pims_ship_to ship on b.ship_to_cust_id = ship.ship_to_cust_id"
						+ " join dbo.pims_dncs dncs on ship.dncs_id = dncs.dncs_id"
						+ " join dbo.pims_ftp ftp on dncs.ftp_site_id = ftp.site_id"
						+ " join dbo.pims_customer_key ck on dncs.cust_caa_id = ck.cust_caa_id"
						+ " join dbo.pims_product p on d.dhct_sn = p.dhct_sn"
						+ " join dbo.pims_certificate c on p.sm_sn = c.sm_sn"
						+ " join dbo.pims_secure_micro sm on p.sm_sn = sm.sm_sn"
						+ " join dbo.pims_sa_key s on sm.sm_ver = s.sm_ver"
						+ " where d.batch_id in (select batch_id from dbo.pims_batch_header where batch_status_cd = '"
						+ STATUS_20 + "')" + " and c.cert_type_cd = 'PK'";
				rSet = DAOClient.readDataFromTable(readQuery);
				generateNothingBlob_20_30(rSet);
			} else if (processName.equals("30_40")) {
				readFilesFromLocal();
			} else if (processName.equals("40_50")) {
				readQuery = "select BATCH_ID, "
						+ "			emm1_blob, "
						+ "			emm2_blob from dbo.pims_batch_header where batch_status_cd = '"
						+ STATUS_40 + "'";
				rSet = DAOClient.readDataFromTable(readQuery);
				process_40_50(rSet);

			}else if (processName.equals("50_70")) {
				readQuery = "select batch_id, delivery_id, info1 from dbo.pims_batch_header where batch_status_cd = '" + STATUS_50 +"' order by batch_id";
				rSet = DAOClient.readDataFromTable(readQuery);
				process_50_70(rSet);
				
			}
			if (DAOClient != null)
				DAOClient.close();
			System.out.println(DAOClient.errorMessage);
			// SendMail sendMail = new SendMail();
			// String mailBody = sendMail.formatMessage(DAOClient.errorMessage);
			// sendMail.sendMail(processName+" Process update",
			// mailBody,"pshridharbabu@gmail.com", "spobbala@cisco.com");
		}
	}

	private static void process_50_70(ResultSet rSet) {
		int batchid = 0;
		String delID = null;
		String info1 = null;
		String baseDir = "c:/testout/TarFiles/"; 
		String tStr = null;
		String tStr1 = null;
		String readQuery = null;
		Map<String, byte[]> mBlobs = new HashMap<String, byte[]>();
		File dir = null;
		FileOutputStream fos = null;
		ResultSet rSet1 = null;
		try {
			while(rSet.next()){
				batchid = rSet.getInt("BATCH_ID");
				delID = rSet.getString("DELIVERY_ID");
				info1 = rSet.getString("INFO1");
				mBlobs = generateBlobs(batchid);
				if(info1!=null)
					tStr1 = info1+"-"+delID+"-COMPLETE";
				else
					tStr1 = delID+"-COMPLETE";
				
				dir = new File(baseDir+tStr1);
				if(dir.exists()){
					dir.delete();
					dir.mkdir();
				} else
					dir.mkdir();
				tStr = tStr1+"/inventry";
				dir = new File(baseDir+tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				fos.write(mBlobs.get("INV"));
				fos.close();
				mBlobs.remove("INV");
				
				tStr = tStr1+"/toc";
				dir = new File(baseDir+tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				fos.write(mBlobs.get("TOC"));
				fos.close();
				mBlobs.remove("TOC");

				tStr = tStr1+"/dncs";
				dir = new File(baseDir+tStr);
				dir.mkdir();

				tStr = tStr1+"/dncs/revlist";
				dir = new File(baseDir+tStr);
				dir.createNewFile();
				
				tStr = tStr1+"/dncs"+"/bootpgs";
				dir = new File(baseDir+tStr);
				dir.mkdir();
				tStr = tStr1+"/dncs"+"/cauth";
				dir = new File(baseDir+tStr);
				dir.mkdir();
				readQuery = "select * from dbo.pims_ca_certificates";
				rSet1 = DAOClient.readDataFromTable(readQuery);
				while(rSet1.next()){
					tStr = tStr1+"/dncs"+"/cauth/" + rSet1.getString("CERTIFICATE_NAME");
					dir = new File(baseDir+tStr);
					dir.createNewFile();
					fos = new FileOutputStream(dir);
					String hexValue = rSet1.getString("CERTIFICATE_BLOB");
					byte[] byteValue = helper.hexStringToByteArray(hexValue);
					fos.write(byteValue);
					fos.close();
				}
				rSet1.close();
				tStr = tStr1+"/dncs"+"/dhcts";
				dir = new File(baseDir+tStr);
				dir.mkdir();
					for(String mac: mBlobs.keySet()){
					tStr = tStr1+"/dncs"+"/dhcts/" + mac; 
					dir = new File(baseDir+tStr);
					dir.createNewFile();
					fos = new FileOutputStream(dir);
					String hexValue = new String(mBlobs.get(mac));//rSet1.getString("EMM_FILE");
					byte[] byteValue = helper.hexStringToByteArray(hexValue);
					fos.write(byteValue);
					fos.close();
				}

				tStr = tStr1+"/dncs"+"/hcttypes";
				dir = new File(baseDir+tStr);
				dir.mkdir();
				readQuery = "SELECT sq.filename, t.file_data FROM (SELECT distinct filename FROM dbo.PIMS_TYPE_FILES t, dbo.PIMS_EMM_BUILD_STRATEGY ebs, "
						+ "(SELECT DISTINCT s.model, s.hw_rev, m.mfg_id,m.mac_prefix FROM dbo.PIMS_PRODUCT s, dbo.PIMS_MFG_MAC_ADDR_RANGE m,"
						+ " dbo.PIMS_BATCH_DETAIL er WHERE er.batch_id=" +batchid +" AND er.dhct_sn = s.dhct_sn AND m.mac_prefix=substring(s.mac_addr,1,6)"
						+ " ) psq WHERE psq.model = ebs.model AND psq.hw_rev = ebs.hw_rev AND psq.mfg_id = ebs.mfg_id AND ebs.model=t.dhct_type "
						+ "AND ebs.hw_rev=t.dhct_rev AND ebs.mfg_id=t.mfg_id AND ((SELECT CASE WHEN ebs.strategy = 'MFG_ID' THEN psq.mfg_id ELSE psq.mac_prefix"
						+ " END) = t.mac_ref ) )sq, PIMS_TYPE_FILES t WHERE t.filename = sq.filename";
				rSet1 = DAOClient.readDataFromTable(readQuery);
				while(rSet1.next()){
				tStr = tStr1+"/dncs"+"/hcttypes/"+rSet1.getString("FILENAME");
				System.out.println(tStr);
				dir = new File(baseDir+tStr);
				dir.createNewFile();
				fos = new FileOutputStream(dir);
				fos.write(rSet1.getBytes("FILE_DATA"));
				fos.close();
				}
				rSet1.close();
				dir = new File("C:/testout/"+delID+"-"+helper.randomBatchID(batchid)+".tar");
				File dir1 = new File(baseDir);
				helper.zip(dir1, dir);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	private static Map<String, byte[]> generateBlobs(int batchid) throws SQLException{
		ResultSet rSet = null;
		byte[] byteValue = null;
		String tStr = null;
		int i = 0;
		int tInt = 0;
		Set<byte[]> tocblobData = new LinkedHashSet<byte[]>();
		Set<byte[]> invblobData = new LinkedHashSet<byte[]>();
		Map<String, byte[]> mBlobs = new LinkedHashMap<String, byte[]>();
		String readQuery = "SELECT distinct "
							    + "d.dhct_sn, "
							    + "p.cust_caa_id, "
							    + "p.mac_addr, "
							    + "p.emm_file, "
							    + "p.MODEL, "
							    + "p.hw_rev, "
							    + "p.mfg_id, "
							    + "ebs.strategy "
							    + "FROM dbo.pims_BATCH_detail d "
							    + "join dbo.pims_PRODUCT p on p.dhct_sn = d.dhct_sn "
								+ "join dbo.pims_EMM_BUILD_STRATEGY ebs on p.mfg_id = ebs.mfg_id "
								+ "AND p.MODEL = ebs.MODEL AND p.hw_rev = ebs.hw_rev "
								+ "WHERE d.batch_id = " +batchid;
		rSet = DAOClient.readDataFromTable(readQuery);
			while(rSet.next()){
				mBlobs.put(rSet.getString("MAC_ADDR"), rSet.getBytes("EMM_FILE"));
				
				////Build TOC BLOB/////
				byte[] tocBlob = new byte[36];
				byte[] invBlob = new byte[49];
				byte[] invfinalBlob = new byte[67+49];
				tStr = rSet.getString("DHCT_SN");
				tStr = String.format("%-9s", tStr);
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, tocBlob, 0, byteValue.length);

				i = byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length);
				
				i = i+ byteValue.length;
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
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length); // mfg_id/MAC FIRST 6 CHARS

				i = i + byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, tocBlob, i, byteValue.length);
				tocblobData.add(tocBlob);
				
				////Build INV BLOB///
				i = 0;
				tStr = rSet.getString("DHCT_SN");
				tStr = String.format("%-9s", tStr);
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, invBlob, 0, byteValue.length);
				
				i = i+ byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);
				
				i = i+ byteValue.length;
				tStr = rSet.getString("MAC_ADDR");
				tStr = tStr.toUpperCase();
				tStr = tStr.replaceAll("..", "$0:").substring(0, 17);
//				tStr = tStr + Character.toString('\0');
				byteValue = tStr.getBytes();
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // MAC
				
				i = i+ byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);
				
				i = i+ byteValue.length;
				tStr = "00000000000000000000000000";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i+ byteValue.length;
				tStr = "00";
				byteValue = helper.hexStringToByteArray(tStr);
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length);

				i = i+ byteValue.length;
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
				System.arraycopy(byteValue, 0, invBlob, i, byteValue.length); // mfg_id/MAC FIRST 6 CHARS
				
				byte[] emptyBlob = createEmptyBlob();
				i = i + byteValue.length;
				System.arraycopy(emptyBlob, 0, invfinalBlob, i, emptyBlob.length); // Add empty Blob
				
				i = emptyBlob.length;
				System.arraycopy(invBlob, 0, invfinalBlob, i, invBlob.length); 
				invblobData.add(invfinalBlob);
			}
			int len = tocblobData.size();
			byte[] tocFinal_Blob = new byte[36*len];
			len = invblobData.size();
			byte[] invfinalBlob = new byte[116*len];
			i = 0;
			for (byte[] bytes: tocblobData){
				System.arraycopy(bytes, 0, tocFinal_Blob, i, bytes.length);
				i = i + bytes.length;
			}
			i = 0;
			if(tocFinal_Blob!=null)
				mBlobs.put("TOC", tocFinal_Blob);
			for (byte[] bytes: invblobData){
				System.arraycopy(bytes, 0, invfinalBlob, i, bytes.length);
				i = i + bytes.length;
			}
			if(invfinalBlob!=null)
				mBlobs.put("INV", invfinalBlob);
				
		
		return mBlobs;
	}

	private static byte[] createEmptyBlob() {
		byte[] emptyBlob = new byte[67];
		byte[] byteValue = null;
		String tStr = null;
		int i = 0;

		tStr = "000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "0000000000000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "0000000000000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "00000000000000000000000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "00";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "0000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "0000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);

		i = i+ byteValue.length;
		tStr = "000000";
		byteValue = helper.hexStringToByteArray(tStr);
		System.arraycopy(byteValue, 0, emptyBlob, i, byteValue.length);
        return emptyBlob;
	}

	private static void process_40_50(ResultSet rSet) {
		byte[] emm1 = null;
		byte[] emm2 = null;
		byte[] sn1 = new byte[9];
		byte[] sn2 = new byte[9];
		byte[] mfgByte = new byte[14];
		int i = 0;
		int len = 0;
		int len1 = 0;
		int len2 = 0;
		int tVal = 0;
		int tInt = 0;
		String snum1 = null;
		String tStr = null;
		Map<String, String> mValues = new HashMap<String, String>();
		Set<Integer> mBatch = new HashSet<Integer>();
		byte[] emm1_Blob = new byte[308];
		byte[] emm2_Blob = new byte[308];
		byte[] raw1 = new byte[44];
		byte[] byteValue = null;
		try {
			while (rSet.next()) {
				mBatch.add(rSet.getInt("BATCH_ID"));
				emm1 = rSet.getBytes("EMM1_BLOB");
				emm2 = rSet.getBytes("EMM2_BLOB");
				while ((i * 339) < emm1.length) {
					len = i * 339;
					len1 = (i * 339) + 323;
					snum1 = helper.getByteToStringValues(emm1, len, sn1, 0, 9);
					tStr = helper.getByteToStringValues(emm2, len, sn2, 0, 9);
					len = i * 339;

					mValues = readVariablesFromRS(snum1);
					mValues.put("MFGDATE",
							helper.getByteToStringValues(emm1, len1, mfgByte, 0, 14));
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
					System.arraycopy(byteValue, 0, 
									 raw1, tVal, 
									 byteValue.length); // MAC FIRST 6 CHARS
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
					tStr = String.format("%1$-" + 2048 + "s", tStr).replaceAll(
							" ", "0");
					byteValue = helper.hexStringToByteArray(tStr);
					System.arraycopy(byteValue, 0, raw3, tVal, byteValue.length);

					tVal = tVal + byteValue.length;
					tStr = mValues.get("US");
					tStr = String.format("%1$-" + 2048 + "s", tStr).replaceAll(
							" ", "0");
					byteValue = helper.hexStringToByteArray(tStr);
					System.arraycopy(byteValue, 0, raw3, tVal, byteValue.length);

					tVal = tVal + byteValue.length;
					System.arraycopy(emm1_Blob, 0, raw3, tVal, emm1_Blob.length); // copy
																					// emm1

					tVal = tVal + emm1_Blob.length;
					System.arraycopy(emm2_Blob, 0, raw3, tVal, emm2_Blob.length); // copy
																					// emm2
					if (raw3 != null && snum1 != null)
						updateProductTable(raw3, snum1, mValues.get("MFGDATE"),
								raw3.length);
					tVal = 0;
					mValues.clear();
				}
			}
			updateBatchTable(mBatch, STATUS_50);
			rSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updateProductTable(byte[] emm_Blob, String snum1,
			String mfgdate, int len) {
		String updateQuery = "update dbo.pims_product set emm_file = ? , "
				+ "										  emm_date = ?,"
				+ "										  emm_file_size  = ? " + " where dhct_sn = ?";
		DAOClient
				.updateProductTable(updateQuery, emm_Blob, mfgdate, len, snum1);
	}

	private static Map<String, String> readVariablesFromRS(String dhctSN)
			throws SQLException {
		String selectQuery = " SELECT p.sm_sn,"
				+ " p.mac_addr,"
				+ " p.model,"
				+ " p.hw_rev,"
				+ " p.mfg_id,"
				+ " ebs.strategy"
				+ " FROM dbo.pims_product p, dbo.pims_emm_build_strategy ebs WHERE p.dhct_sn = ?"
				+ " AND p.mfg_id = ebs.mfg_id AND p.model = ebs.model AND p.hw_rev = ebs.hw_rev";
		Map<String, String> mValues = new HashMap<String, String>();
		ResultSet rSet = null;
		rSet = DAOClient.readDataFromTable(selectQuery, dhctSN);
		while (rSet.next()) {
			mValues.put("SMSN", rSet.getString("SM_SN"));
			mValues.put("MAC", rSet.getString("MAC_ADDR"));
			mValues.put("MODEL", rSet.getString("MODEL"));
			mValues.put("HWREV", rSet.getString("HW_REV"));
			mValues.put("MFGID", rSet.getString("MFG_ID"));
			mValues.put("STRGY", rSet.getString("STRATEGY"));
		}
		selectQuery = "SELECT cert_type_cd, pub_key_cert, pub_key_cert_length FROM dbo.pims_certificate WHERE sm_sn = ?";
		rSet = DAOClient.readDataFromTable(selectQuery, mValues.get("SMSN"));
		while (rSet.next()) {
			mValues.put(rSet.getString("CERT_TYPE_CD"),
					rSet.getString("PUB_KEY_CERT"));
			mValues.put(rSet.getString("CERT_TYPE_CD") + "LEN",
					rSet.getString("PUB_KEY_CERT_LENGTH"));
		}
		return mValues;
	}

	private static void prepareProductTable_17_20(ResultSet rSet) {
		Map<String, String> mValues = new HashMap<String, String>();
		Map<String, String> mapID = new HashMap<String, String>();
		Set<Integer> mBatch = new HashSet<Integer>();
		String serialNumber = null;
		boolean uStatus = true;
		int cnt = 0;
		int count = 0;
		String insertQuery = null;
		insertQuery = "Insert into dbo.pims_product (DHCT_SN, SM_SN, MAC_ADDR, MODEL, HW_REV, DHCT_MFG_DT, DATE_CREATE,"
				+ "DHCT_STATUS_CD, CUST_CAA_ID, MFG_ID) "
				+ "values (?,?,?,?,?,?,CURRENT_TIMESTAMP,'20',?,?)";

		try {
			while (rSet.next()) {
				serialNumber = rSet.getString("DHCT_SN");
				mValues = getSNAttribs(serialNumber);
				if (mValues.get("SMSN") == null) {
					DAOClient
							.setStatus(
									"No SMSN " + " for Serial Number:"
											+ serialNumber + " in Batch ID:"
											+ rSet.getInt("BATCH_ID"), true);
					uStatus = false;
				}
				String mfgID = null;
				String tempStr = null;
				String custCAAID = null;
				tempStr = mValues.get("MODEL") + mValues.get("HWVER");
				mfgID = mapID.get(tempStr);
				if (mfgID == null) {
					cnt++;
					mfgID = getMfgID(mValues.get("MODEL"), mValues.get("HWVER"));
					mapID.put("HWVER" + cnt, mValues.get("HWVER"));
					if (mfgID == null) {
						mfgID = getMfgID(mValues.get("MODEL"),
								mValues.get("MATREVLEVEL"));
						mapID.put("HWVER" + cnt, mValues.get("MATREVLEVEL"));
					}
					mapID.put(tempStr, mfgID);
				}
				custCAAID = mapID.get(rSet.getString("SHIP_TO_CUST_ID"));
				if (custCAAID == null) {
					custCAAID = getCustID(rSet.getString("SHIP_TO_CUST_ID"));
					mapID.put(rSet.getString("SHIP_TO_CUST_ID"), custCAAID);
				}
				if (custCAAID == null) {
					DAOClient.setStatus("No CUSTCAAID setup for SHIP_TO:"
							+ rSet.getString("SHIP_TO_CUST_ID")
							+ " for Serial Number:" + serialNumber
							+ " in Batch ID:" + rSet.getInt("BATCH_ID"), true);
					uStatus = false;
				}
				if (mfgID == null) {
					DAOClient
							.setStatus(
									"No MFGID setup for model:"
											+ mValues.get("MODEL")
											+ " and HWVER/MATREVLEVEL "
											+ mValues.get("HWVER") + "/"
											+ mValues.get("MATREVLEVEL")
											+ " for Serial Number:"
											+ serialNumber + " in Batch ID:"
											+ rSet.getInt("BATCH_ID"), true);
					uStatus = false;
				}
				mValues.put("SN", serialNumber);
				mValues.put("MFGID", mfgID);
				mValues.put("CAAID", custCAAID);
				mValues.put("HWVER", mapID.get("HWVER" + cnt));
				if (uStatus) {
					count++;
					DAOClient.insertProductTable(mValues, insertQuery);
					mBatch.add(rSet.getInt("BATCH_ID"));
				}
				uStatus = true;
				mValues.clear();
			}
			DAOClient.setStatus("no of records updated:" + count);
			if (rSet != null)
				rSet.close();
			if (!mBatch.isEmpty())
				updateBatchTable(mBatch, STATUS_20);
			else
				DAOClient.setStatus("No Batch Data Read");
			mBatch.clear();
			mapID.clear();

		} catch (SQLException e) {
			DAOClient.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}

	}

	private static void generateNothingBlob_20_30(ResultSet rSet) {
		String tString = "0";
		String tString1 = null;
		byte[] tempValue = new byte[1024];
		int finalLength = 0;
		Set<byte[]> blobData = new LinkedHashSet<byte[]>();
		Set<Integer> batchIDs = new LinkedHashSet<Integer>();
		Set<Integer> tbatchIDs = new LinkedHashSet<Integer>();
		Map<Integer, byte[]> nBlobs = new HashMap<Integer, byte[]>();
		int batchid = 0;
		byte[] byteValues = null;
		byte[] nothingBlob = null;
		int totLen = 0;
		try {
			while (rSet.next()) {
				batchid = rSet.getInt(1);
				batchIDs.add(batchid); // + "#" + rSet.getString(11));
				tbatchIDs.add(batchid);// + "#" + rSet.getString(11));
				if (batchIDs.size() > 1) {
					nothingBlob = new byte[finalLength];
					int i = 0;
					for (byte[] bytes : blobData) {
						System.arraycopy(bytes, 0, nothingBlob, i, bytes.length);
						i = i + bytes.length;
					}
					int j = 0;
					for (int bID : batchIDs) {
						if (j == 0)
							batchIDs.remove(bID);
						j++;
					}
					nBlobs.put(batchid, nothingBlob);
					nothingBlob = null;
					blobData.clear();
					totLen = 0;
					finalLength = 0;
				}
				tString = "0";
				int certLen = rSet.getInt(5);
				int tlen = 1024 - certLen;
				tString = String.format(String.format("%%0%dd", tlen * 2), 0)
						.replace("0", tString);
				totLen = rSet.getBytes(2).length // dhct_sn length
						+ 2 // PK Certificate Length
						+ 1024 // PK Certificate length
						+ 128 // CUS_CAA
						+ 384 // SACAA
						+ 6 // SMSN length
						+ 6; // MAC Address
				byte[] finalValue = new byte[totLen];
				System.arraycopy(rSet.getBytes(6), 0, tempValue, 0, certLen);
				byteValues = helper.hexStringToByteArray(tString);
				System.arraycopy(byteValues, 0, tempValue, certLen,
						byteValues.length);

				// Start building blob here
				System.arraycopy(rSet.getBytes(2), 0, finalValue, 0,
						rSet.getBytes(2).length); // dhct_sn
				totLen = rSet.getBytes(2).length;
				tString1 = helper.convertIntToHex(certLen);
				byteValues = helper.hexStringToByteArray(tString1);
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length);// pk cert length
				totLen = totLen + byteValues.length;
				System.arraycopy(tempValue, 0, finalValue, totLen, 1024); // PK
																			// cert
				totLen = totLen + 1024;
				System.arraycopy(rSet.getBytes(9), 0, finalValue, totLen, 128); // cust_caa
				totLen = totLen + 128;
				System.arraycopy(rSet.getBytes(7), 0, finalValue, totLen, 384); // sa_caa
				totLen = totLen + 384;
				byteValues = helper.hexStringToByteArray(rSet.getString(3));
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length); // sm_sn
				totLen = totLen + byteValues.length;
				byteValues = helper.hexStringToByteArray(rSet.getString(4));
				System.arraycopy(byteValues, 0, finalValue, totLen,
						byteValues.length); // mac address
				totLen = totLen + byteValues.length;
				finalLength = finalLength + totLen;
				blobData.add(finalValue);
			}
			if (nBlobs.size() == 0 && blobData.size() > 0) {
				nothingBlob = new byte[finalLength];
				int i = 0;
				for (byte[] bytes : blobData) {
					System.arraycopy(bytes, 0, nothingBlob, i, bytes.length);
					i = i + bytes.length;
				}
				nBlobs.put(batchid, nothingBlob);
				nothingBlob = null;

				String updateQuery = "update dbo.pims_batch_header set nothing_blob = ? where batch_id = ?";

				for (int bID : tbatchIDs) {
					prepareToSendFile("70", batchid, nBlobs.get(batchid));
					batchid = bID;
					DAOClient.loadNothingBlob(updateQuery, nBlobs.get(batchid),
							batchid);
					updateQuery = "update dbo.pims_batch_header set batch_status_cd = ? where batch_id = ?";
					DAOClient.updateBatchTable(updateQuery, batchid, STATUS_30);
				}
			} else
				DAOClient.setStatus("No batches with status " + STATUS_30,
						false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void prepareToSendFile(String siteID, int batchID,
			byte[] nothingBlob) throws SQLException {
		ResultSet rSet = null;
		String readQuery = "select * from dbo.pims_ftp where site_id = '"
				+ siteID + "'";
		rSet = DAOClient.readDataFromTable(readQuery);
		CustomFTP ftpClient = null;
		CustomSFTP sftpClient = null;
		while (rSet.next()) {
			String serverName = rSet.getString("SITE_ADDRESS");
			String userName = rSet.getString("USER_ID");
			String ftpPwd = rSet.getString("PASSWORD");
			String toLocation = rSet.getString("HOME_DIRECTORY");
			String fileName = String.format("%08d", batchID);
			if (serverName.contains(":21")) {
				ftpClient = new CustomFTP(serverName, userName, ftpPwd,
						toLocation, fileName, nothingBlob);
				ftpClient.ftpSendFile();
			} else if (serverName.contains(":22")) {
				int i = serverName.indexOf(":");
				int port = Integer.parseInt(serverName.substring(i + 1));
				serverName = serverName.substring(0, i);
				sftpClient = new CustomSFTP(
						serverName,
						userName,
						ftpPwd,
						toLocation,
						fileName,
						nothingBlob,
						port,
						"public key",
						"C:/Users/spobbala/AppData/Roaming/SSH/UserKeys/XI_SFTP_Test",
						"C:/Users/spobbala/Desktop/Cassandra/lib/eula.properties");
				String status = sftpClient.sftpSend();
				System.out.println(status);
			}
		}

	}

	public static String getCustID(String shipToID) throws SQLException {
		String readQuery = "select d.cust_caa_id from dbo.pims_ship_to s join dbo.pims_dncs d on s.dncs_id = d.dncs_id "
				+ "where s.ship_to_cust_id = '" + shipToID + "'";
		String custID = null;
		ResultSet rSet = DAOClient.readDataFromTable(readQuery);
		while (rSet.next())
			custID = rSet.getString(1);
		if (rSet != null)
			rSet.close();
		return custID;
	}

	public static String getMfgID(String model, String hwverOrMatRev)
			throws SQLException {
		String mfgID = null;
		String readQuery = "select mfg_id from dbo.pims_type_files where dhct_type = '"
				+ model + "' and dhct_rev = '" + hwverOrMatRev + "'";
		ResultSet rSet = DAOClient.readDataFromTable(readQuery);

		while (rSet.next())
			mfgID = rSet.getString(1);

		if (rSet != null)
			rSet.close();

		return mfgID;
	}

	public static void updateBatchTable(Set<Integer> sBatches, String bStatus) {
		String updateQuery = "update dbo.pims_batch_header set batch_status_cd = ? where batch_id = ?";
		for (Integer batchId : sBatches) {
			DAOClient.updateBatchTable(updateQuery, batchId, bStatus);
		}

	}

	public static Map<String, String> getSNAttribs(String serialNumber)
			throws SQLException {
		ResultSet rSet1 = null;
		String readQuery = null;
		String formatString = null;
		readQuery = "SELECT serial_number, attribute_id, attribute_sequence, attribute_value, transaction_date_time FROM "
				+ "dbo.pims_PRT_SERIAL_ATTRIBUTES"
				+ " WHERE  serial_number = ?"
				+ " AND attribute_id IN (5,7,14,16,20,43,71)"
				+ " ORDER BY serial_number, attribute_id, attribute_sequence, transaction_date_time desc";

		Map<String, String> mValues = new HashMap<String, String>();
		rSet1 = DAOClient.readDataFromTable(readQuery, serialNumber);

		while (rSet1.next()) {
			switch (rSet1.getInt("ATTRIBUTE_ID")) {
			case 5: // MFGDATE
				mValues.put("MFGDATE", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 71: // SMSN
				mValues.put("SMSN", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 7: // MACADDR
				if (rSet1.getString("ATTRIBUTE_SEQUENCE").equals("1"))
					mValues.put("MACADDR", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 20: // MODEL
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("[^0-9]", "");
				mValues.put("MODEL", formatString);
				break;
			case 14: // HWVER
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("\\.", "");
				if (formatString.contains("0x"))
					formatString = "01";
				mValues.put("HWVER", formatString);
				break;
			case 16: // MATERIALREVLEVEL
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("\\.", "");
				mValues.put("MATREVLEVEL", formatString);
				break;

			default:
				// do nothing
				break;
			}
		}
		if (rSet1 != null)
			rSet1.close();
		return mValues;
	}

	private static void readFilesFromLocal() {
		String fileTemp = null;
		String file1 = null;
		String fileType = null;
		int batchid = 0;
		String updateQuery = null;
		byte[] result = null;
		boolean fileOne = false;
		boolean fileTwo = false;
		try {
			File fileFolder = new File(FILEPATH);
			if (fileFolder != null) {
				for (final File fileEntry : fileFolder.listFiles()) {
					if (!fileEntry.isDirectory()) {
						fileTemp = fileEntry.getName();
						File currentFile = new File(FILEPATH + fileTemp);
						InputStream input = new BufferedInputStream(
								new FileInputStream(currentFile));
						result = helper.readAndClose(input);
						int i = fileTemp.indexOf(".");
						file1 = fileTemp.substring(0, i);
						fileType = fileTemp.substring(i + 1, fileTemp.length());
						batchid = Integer.parseInt(file1.replaceFirst(
								"^0+(?!$)", ""));
						if (fileType.equals("1")) {
							updateQuery = "update dbo.pims_batch_header set emm1_blob = ? where batch_id = ?";
							fileOne = true;
						} else if (fileType.equals("2")) {
							updateQuery = "update dbo.pims_batch_header set emm2_blob = ? where batch_id = ?";
							fileTwo = true;
						}
						if (updateQuery != null && result != null)
							DAOClient.loadNothingBlob(updateQuery, result,
									batchid);
						currentFile.delete();
					}
				}
				if (!fileOne)
					DAOClient.setStatus(
							"File 1 is missing in 30_40 process for batchid:"
									+ batchid, true);
				if (!fileTwo)
					DAOClient.setStatus(
							"File 2 is missing in 30_40 process for batchid:"
									+ batchid, true);
				if (fileOne && fileTwo) {
					updateQuery = "update dbo.pims_batch_header set batch_status_cd = ? where batch_id = ?";
					DAOClient.updateBatchTable(updateQuery, batchid, STATUS_40);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
}
