/*
 * Created on Jan 21, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.File;
import java.io.FileNotFoundException;
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

	public void process(int batchid) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		try {
			pstmt = batchid > 0 ? DBConnectionFactory.prepareStatement(
					this.pimsCon, PIMSConstants.QUERYBATCH_50_70, batchid,
					PIMSConstants.STATUS_50) : DBConnectionFactory
					.prepareStatement(this.pimsCon, PIMSConstants.QUERY_50_70,
							PIMSConstants.STATUS_50);
			rSet = pstmt.executeQuery();
			SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
			this.helper = new PIMSHelper();
			while (rSet.next()) {
				this.process_50_70(rSet.getInt("BATCH_ID"),
						rSet.getString("DELIVERY_ID"), rSet.getString("INFO1"));
				mailProc.generateEmail(batchid, pimsLogging.getNotificationIDSet(),
						PIMSConstants.PROCESS50_70);
				pimsLogging.clearNotificationID();
			}
		} catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 50_70 Process, Error Details:"
							+ sql.getMessage());
			
		} catch(Exception e){
			e.printStackTrace();
		}
			finally {
			DBConnectionFactory.close(this.pimsCon, pstmt, rSet);
		}
	}

	private void process_50_70(int batchid, String delID, String info1) {
		String tempDir = null;
		String tStr = null;
		String parentFolder = null;
		String tarFileLoc = null;
		String palFileLoc = null;

		Map<String, Map<String, byte[]>> allBlobs = new HashMap<String, Map<String, byte[]>>();
		Map<String, byte[]> compBlobs = new HashMap<String, byte[]>();
		Map<String, byte[]> macBlobs = new HashMap<String, byte[]>();
		Map<String, byte[]> ipBlobs = new HashMap<String, byte[]>();
		Map<String, byte[]> tocBlobs = new HashMap<String, byte[]>();

		PreparedStatement pstmt = null;

		pimsLogging.logSuccessMessage(batchid, null,
				"Entered 50_70 Process batchid:" + batchid);
		tempDir = propFile.getProperty("TempFileLoc");
		tarFileLoc = propFile.getProperty("TarFileLocation");
		palFileLoc = propFile.getProperty("PalletFileLocation");
		
		if (tempDir == null || tarFileLoc == null || palFileLoc==null) {
			pimsLogging.logErrorMessage(batchid, null,
					"Tar File Location or temp Directory or Pallet Directory missing in properties file"
							+ "Tar File Location=" + tarFileLoc + "Temp Dir="
							+ tempDir);
			
		}

		try {
			File dir = new File(tarFileLoc);
			this.createDir(dir);
			dir = new File(palFileLoc);
			this.createDir(dir);
			
			allBlobs = this.generateBlobs(batchid);
			compBlobs = allBlobs.get("IANDT");
			macBlobs = allBlobs.get("MACS");
			ipBlobs = allBlobs.get("INVPALLET");
			tocBlobs = allBlobs.get("TOCPALLET");
			if (info1 != null)
				parentFolder = info1 + "-" + delID + "-COMPLETE";
			else
				parentFolder = delID + "-COMPLETE";
			
			if(ipBlobs!=null){
				this.generateFiles(batchid, parentFolder, tempDir, 
						compBlobs.get("INV"),compBlobs.get("TOC"), macBlobs);
				tStr = delID + "-" + "COMPLETE" + ".tar";
				this.createTar(tempDir, parentFolder, palFileLoc+tStr);
				for(String pal: ipBlobs.keySet()){
					parentFolder = delID + "-"+ pal;
					macBlobs = allBlobs.get(pal);
					this.generateFiles(batchid, parentFolder, tempDir, 
							ipBlobs.get(pal), tocBlobs.get(pal), macBlobs);
					tStr = delID + "-" + pal + ".tar";
					this.createTar(tempDir, parentFolder, palFileLoc+tStr);
				}
				tStr = delID + "-" + helper.randomBatchID(batchid) + ".tar";
				parentFolder = "";
				this.createTar(palFileLoc, parentFolder, tarFileLoc+tStr);
			}else{
				if (info1 != null)
					parentFolder = info1 + "-" + delID + "-COMPLETE";
				else
					parentFolder = delID + "-COMPLETE";
				this.generateFiles(batchid, parentFolder, tempDir, 
						compBlobs.get("INV"),compBlobs.get("TOC"), macBlobs);
				tStr = delID + "-" + helper.randomBatchID(batchid) + ".tar";
				this.createTar(tempDir, parentFolder, tarFileLoc+tStr);
			}
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.UPDATEBQUERY, PIMSConstants.STATUS_70,
					batchid);
			pstmt.executeUpdate();
			pimsCon.commit();

			pimsLogging.logSuccessMessage(batchid, null,
					"Tar File Generated in Location:" + tarFileLoc + tStr);
			
		} catch (SQLException sql) {
			pimsLogging.logErrorMessage(batchid, null,
					"DB Error in 50_70 Process while generating tar file, Error Details:"
							+ sql.getMessage() + ":" + sql.getCause());
			
		} catch (IOException e) {
			pimsLogging.logErrorMessage(batchid, null,
					"Error in 50_70 Process while generating tar file, Error Details:"
							+ e.getMessage() + ":" + e.getCause());
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void createTar(String tempDir, String parentFolder, String fileLoc) throws IOException {
		File dir = null;
		dir = new File(fileLoc);
		File dir1 = new File(tempDir);
		PIMSHelper.tar(dir1, dir);
		File delFolder = new File(tempDir + parentFolder);
		PIMSHelper.fileDelete(delFolder);
	}

	private void generateFiles(int batchid, String parentFolder, String tempDir,
			byte[] invBlobs, byte[] tocBlobs, Map<String, byte[]> macBlobs)
			throws FileNotFoundException, IOException, SQLException {
		File dir = null;
		String tStr = null;
		dir = new File(tempDir + parentFolder);
		this.createDir(dir);

		tStr = parentFolder + "/inventry";
		dir = new File(tempDir + tStr);
		this.createInvtry(dir, invBlobs);

		tStr = parentFolder + "/toc";
		dir = new File(tempDir + tStr);
		this.createInvtry(dir, tocBlobs);

		tStr = parentFolder + "/dncs";
		dir = new File(tempDir + tStr);
		this.createDir(dir);

		tStr = parentFolder + "/dncs/revlist";
		dir = new File(tempDir + tStr);
		dir.createNewFile();

		tStr = parentFolder + "/dncs" + "/bootpgs";
		dir = new File(tempDir + tStr);
		this.createDir(dir);

		tStr = parentFolder + "/dncs" + "/cauth";
		dir = new File(tempDir + tStr);
		this.createDir(dir);

		this.writeCertFiles(parentFolder, tempDir);

		this.writeDhctBlobs(parentFolder, tempDir, macBlobs);

		this.writeHctTypes(parentFolder, tempDir, batchid);

	}

	private void writeHctTypes(String parentFolder, String tempDir, int batchid)
			throws SQLException, IOException {
		String tStr = null;
		File dir = null;
		PreparedStatement pstmt = null;
		ResultSet rSet1 = null;
		FileOutputStream fos = null;

		tStr = parentFolder + "/dncs" + "/hcttypes";
		dir = new File(tempDir + tStr);
		dir.mkdir();
		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYHCTTYPES, batchid);
		rSet1 = pstmt.executeQuery();
		while (rSet1.next()) {
			tStr = parentFolder + "/dncs" + "/hcttypes/"
					+ rSet1.getString("FILENAME");
			dir = new File(tempDir + tStr);
			dir.createNewFile();
			fos = new FileOutputStream(dir);
			fos.write(rSet1.getBytes("FILE_DATA"));
			fos.close();
		}
		DBConnectionFactory.close(pstmt, rSet1);

	}

	private void writeDhctBlobs(String parentFolder, String tempDir,
			Map<String, byte[]> macBlobs) throws FileNotFoundException,
			IOException {
		String tStr = null;
		File dir = null;
		FileOutputStream fos = null;

		tStr = parentFolder + "/dncs" + "/dhcts";
		dir = new File(tempDir + tStr);
		this.createDir(dir);

		for (String mac : macBlobs.keySet()) {
			tStr = parentFolder + "/dncs" + "/dhcts/" + mac;
			dir = new File(tempDir + tStr);
			dir.createNewFile();
			fos = new FileOutputStream(dir);
			byte[] byteValue = macBlobs.get(mac);
			fos.write(byteValue);
			fos.close();
		}

	}

	private void writeCertFiles(String pDir, String tempDir)
			throws SQLException, IOException {
		PreparedStatement pstmt = null;
		ResultSet rSet1 = null;
		File dir = null;
		FileOutputStream fos = null;

		String tStr = null;

		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYCACERTS);
		rSet1 = pstmt.executeQuery();

		while (rSet1.next()) {
			tStr = pDir + "/dncs" + "/cauth/"
					+ rSet1.getString("CERTIFICATE_NAME");
			dir = new File(tempDir + tStr);
			dir.createNewFile();
			fos = new FileOutputStream(dir);
			String hexValue = rSet1.getString("CERTIFICATE_BLOB");
			byte[] byteValue = helper.hexStringToByteArray(hexValue);
			fos.write(byteValue);
			fos.close();
		}
		DBConnectionFactory.close(pstmt, rSet1);

	}

	private void createInvtry(File dir, byte[] blobFile) throws IOException {
		FileOutputStream fos = null;
		dir.createNewFile();
		fos = new FileOutputStream(dir);
		fos.write(blobFile);
		fos.close();
	}

	private void createDir(File dir) throws FileNotFoundException, IOException {
		if (dir.exists()) {
			PIMSHelper.fileDelete(dir);
			dir.mkdir();
		} else
			dir.mkdir();

	}

	private Map<String, Map<String, byte[]>> generateBlobs(int batchid) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		byte[] byteValue = null;
		String tStr = null;
		String serialNumber = null;
		String prevPallet = null;
		String curPallet = null;
		int i = 0;
		int tInt = 0;
		int cnt = 0;
		Set<byte[]> tocblobData = new LinkedHashSet<byte[]>();
		Map<String, byte[]> tocPalletBlob = new LinkedHashMap<String, byte[]>();
		Set<byte[]> invblobData = new LinkedHashSet<byte[]>();
		Map<String, byte[]> invPalletBlob = new LinkedHashMap<String, byte[]>();
		Map<String, byte[]> mBlobs = new LinkedHashMap<String, byte[]>();
		Map<String, byte[]> macBlobs = new LinkedHashMap<String, byte[]>();
		Map<String, byte[]> tmacBlobs = new LinkedHashMap<String, byte[]>();
		Map<String, byte[]> ttmacBlobs = null;
		Map<String, Map<String, byte[]>> finalBlobs = new LinkedHashMap<String, Map<String, byte[]>>();
		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYPROD50_70, batchid);
			rSet = pstmt.executeQuery();

			while (rSet.next()) {
				if(rSet.getString("PALLET_ID")!=null && rSet.getString("PALLET_ID").length() > 0 && cnt==0)
					prevPallet = rSet.getString("PALLET_ID");
				cnt++;
				macBlobs.put(rSet.getString("MAC_ADDR"),
						helper.hexStringToByteArray(rSet.getString("EMM_FILE")));

				// //Build TOC BLOB//////////////////////////////////////////
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
				if (rSet.getString("PALLET_ID") != null && rSet.getString("PALLET_ID").length() > 0) {
						curPallet = rSet.getString("PALLET_ID");
					if(cnt>1 && !(curPallet.equals(prevPallet))){
						ttmacBlobs = new LinkedHashMap<String, byte[]>(tmacBlobs);
						finalBlobs.put(prevPallet, ttmacBlobs);
					tmacBlobs.clear();
					}
					prevPallet = curPallet;
					if (tocPalletBlob.get(rSet.getString("PALLET_ID")) == null)
						tocPalletBlob.put(rSet.getString("PALLET_ID"), tocBlob);
					else {
						int len = tocBlob.length
								+ tocPalletBlob
										.get(rSet.getString("PALLET_ID")).length;
						byte[] newBlob = new byte[len];
						System.arraycopy(
								tocPalletBlob.get(rSet.getString("PALLET_ID")),
								0,
								newBlob,
								0,
								tocPalletBlob.get(rSet.getString("PALLET_ID")).length);
						System.arraycopy(
								tocBlob,
								0,
								newBlob,
								tocPalletBlob.get(rSet.getString("PALLET_ID")).length,
								tocBlob.length);
						tocPalletBlob.put(rSet.getString("PALLET_ID"), newBlob);
					}
				}
				tmacBlobs.put(rSet.getString("MAC_ADDR"),
						helper.hexStringToByteArray(rSet.getString("EMM_FILE")));

				// //Build INV BLOB//////////////////////////////////////////
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
				if (rSet.getString("PALLET_ID") != null && rSet.getString("PALLET_ID").length() > 0) {
					if (invPalletBlob.get(rSet.getString("PALLET_ID")) == null)
						invPalletBlob.put(rSet.getString("PALLET_ID"),
								invfinalBlob);
					else {
						int len = invfinalBlob.length
								+ invPalletBlob
										.get(rSet.getString("PALLET_ID")).length;
						byte[] newBlob = new byte[len];
						System.arraycopy(
								invPalletBlob.get(rSet.getString("PALLET_ID")),
								0,
								newBlob,
								0,
								invPalletBlob.get(rSet.getString("PALLET_ID")).length);
						System.arraycopy(
								invBlob,
								0,
								newBlob,
								invPalletBlob.get(rSet.getString("PALLET_ID")).length,
								invBlob.length);
						invPalletBlob.put(rSet.getString("PALLET_ID"), newBlob);
					}
				}

			}
			finalBlobs.put(curPallet, tmacBlobs);
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
			finalBlobs.put("MACS", macBlobs);
			finalBlobs.put("IANDT", mBlobs);
			finalBlobs.put("INVPALLET", invPalletBlob);
			finalBlobs.put("TOCPALLET", tocPalletBlob);
		} catch (SQLException sql) {
			pimsLogging.logErrorMessage(
					batchid,
					serialNumber,
					"DB Error in 50_70 Process batchid:" + batchid
							+ ", Error Details:" + sql.getMessage() + ":"
							+ sql.getCause());
			

		} catch (Exception e) {
			pimsLogging.logErrorMessage(
					batchid,
					serialNumber,
					"Error in 50_70 Process batchid:" + batchid
							+ ", Error Details:" + e.getMessage() + ":"
							+ e.getCause());
			
		}
		return finalBlobs;
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
