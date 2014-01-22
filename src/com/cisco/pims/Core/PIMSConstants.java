/*
 * Created on Jan 14, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;
public interface PIMSConstants {
	public static final String ERR_DB_CONNECTION = "Error occurred while connecting to Database !!!";
	public static final String COM_ERR_RPT = "An Error occured  !!!";
	public static final String env = "Environment";
	public static final String envTest = "Test_Environment";
	public static final String envProd = "Production_Environment";
	public static final int testSeq = 1;
	public static final int prodSeq = 10;
	public static final String configErrMsg = "formatError";
	public static final String configErrMsgId = "Error_Msg_Type";
	public static final String configSuccessMsg = "formatsucess";
	public static final String configSuccessMsgId = "Success_Msg_Type";
	public static final String configTrackingMsg = "formatTracking";
	public static final String configTrackingMsgId = "Tracking_Msg_Type";
	public static final String configWarningMsg = "formatWarning";
	public static final String configWarningMsgId = "Warning_Msg_Type";;
	public static final String configPriorityC = "Priority_Critical";
	public static final String configPriorityH = "Priority_High";
	public static final String configPriorityM = "Priority_Medium";
	public static final String configPriorityL = "Priority_Low";
	public static final String configEmmSuccess = "Internal_EMM_Success";
	public static final String configEmmFailure = "Internal_EMM_Failure";
	public static final String STATUS_17 = "17";
	public static final String STATUS_20 = "20";
	public static final String STATUS_30 = "30";
	public static final String STATUS_40 = "40";
	public static final String STATUS_50 = "50";
	public static final String STATUS_70 = "70";
	public static final String PROCESS17_20 = "17_20";
	public static final String PROCESS20_30 = "20_30";
	public static final String PROCESS30_40 = "30_40";
	public static final String PROCESS40_50 = "40_50";
	public static final String PROCESS50_70 = "50_70";
	public static final String INITGLOBAL = "SELECT config_sequence, config_type, default_value FROM dbo.pims_config "
											+ " WHERE config_type IN ('"
											+ PIMSConstants.env
											+ "', '"
											+ PIMSConstants.envTest
											+ "', '"
											+ PIMSConstants.envProd
											+ "', '"
											+ PIMSConstants.configErrMsg
											+ "', '"
											+ PIMSConstants.configSuccessMsg
											+ "', '"
											+ PIMSConstants.configTrackingMsg
											+ "', '"
											+ PIMSConstants.configWarningMsg
											+ PIMSConstants.configErrMsgId
											+ "', '"
											+ PIMSConstants.configSuccessMsgId
											+ "', '"
											+ PIMSConstants.configTrackingMsgId
											+ "', '"
											+ PIMSConstants.configWarningMsgId
											+ "', '"
											+ PIMSConstants.configPriorityC
											+ "', '"
											+ PIMSConstants.configPriorityH
											+ "', '"
											+ PIMSConstants.configPriorityM
											+ "', '"
											+ PIMSConstants.configPriorityL
											+ "', '"
											+ PIMSConstants.configEmmSuccess
											+ "', '"
											+ PIMSConstants.configEmmFailure + "')";
	public static final String QUERYBATCH = "select batch_id from dbo.pims_batch_header where batch_id = ? "
												 + "and batch_status_cd = ?"; 
	public static final String QUERYSTATUS = "select batch_id from dbo.pims_batch_header where batch_status_cd = ?";
	public static final String QUERYBDET_17_20 = "select b.batch_id, b.ship_to_cust_id, d.dhct_sn from dbo.pims_batch_header b "
												+ "join dbo.pims_batch_detail d"
												+ " on b.batch_id = d.batch_id where b.batch_id = ?";
	public static final String INSERTQUERY_17_20 = "Insert into dbo.pims_product (DHCT_SN, SM_SN, MAC_ADDR, "
												 + "MODEL, HW_REV, DHCT_MFG_DT, DATE_CREATE,"
												+ "DHCT_STATUS_CD, CUST_CAA_ID, MFG_ID) "
												+ "values (?,?,?,?,?,?,CURRENT_TIMESTAMP,"+STATUS_20 +",?,?)";
	public static final String QUERYSNGET = "SELECT serial_number, attribute_id, attribute_sequence, attribute_value, transaction_date_time FROM "
											+ "dbo.pims_PRT_SERIAL_ATTRIBUTES"
											+ " WHERE  serial_number = ?"
											+ " AND attribute_id IN (5,7,14,16,20,43,71)"
											+ " ORDER BY serial_number, attribute_id, attribute_sequence, transaction_date_time desc";
	public static final String QUERYCAAGET = "select d.cust_caa_id from dbo.pims_ship_to s join dbo.pims_dncs d on s.dncs_id = d.dncs_id "
											  + "where s.ship_to_cust_id = ?";
	public static final String UPDATEBQUERY = "update dbo.pims_batch_header set batch_status_cd = ? where batch_id = ?";
	
	public static final String QUERYMFGID = "select mfg_id from dbo.pims_type_files where dhct_type = ? and dhct_rev = ?";
	public static final String QUERYPROD_20_30 = "select b.ship_to_cust_id, p.dhct_sn, p.sm_sn, p.mac_addr "
      									       + "from dbo.pims_batch_header b "
										       + "join dbo.pims_batch_detail d on b.batch_id = d.batch_id "
										       + "join dbo.pims_product p on d.dhct_sn = p.dhct_sn "
										       + "where b.batch_id = ?";
	
	public static final String QUERYCERTS_20_30 = "select convert(int, pub_key_cert_length) pub_key_cert_length , "
												+ "pub_key_cert "
												+ "from dbo.pims_certificate "
												+ "where sm_sn = ? and cert_type_cd = 'PK'";

	public static final String QUERYSACERTS_20_30 = "select s.sa_caa_pub_keys, "
												+ "convert(int, s.sa_caa_pub_keys_length) sa_caa_pub_keys_length "
												+ "from dbo.pims_sa_key s "
												+ "join dbo.pims_secure_micro sm on s.sm_ver = sm.sm_ver "
												+ "where sm.sm_sn = ?";

	public static final String QUERYCKCERTS_20_30 = "select ck.cust_caa_pub_key,"
												  + "convert(int, ck.cust_caa_pub_key_length) cust_caa_pub_key_length "
												  + "from dbo.pims_customer_key ck where ck.cust_caa_id = "
												  + "(select dncs.cust_caa_id from dbo.pims_dncs dncs "
												  + "join dbo.pims_ship_to s on dncs.dncs_id = s.dncs_id "
												  + "where s.ship_to_cust_id = ?)";

	public static final String UPDATEQUERY_20_30 = "update dbo.pims_batch_header set nothing_blob = ? where batch_id = ?";
	public static final String QUERYFTPSITEID = "select * from dbo.pims_ftp where site_id = ?";
	public static final String UPDATEQUERYEMM130_40 = "update dbo.pims_batch_header set emm1_blob = ? where batch_id = ?";
	public static final String UPDATEQUERYEMM230_40 = "update dbo.pims_batch_header set emm2_blob = ? where batch_id = ?";
	public static final String UPDATEQUERYSTAT30_40 = "update dbo.pims_batch_header set batch_status_cd = ? where batch_id = ?";
	public static final String QUERYBATCH_40_50 = "select BATCH_ID, "
												+ " emm1_blob, "
												+ "	emm2_blob from dbo.pims_batch_header where batch_id = ?"
												+ " and batch_status_cd = ?";
	
	public static final String QUERY_40_50 = "select BATCH_ID, "
											+ " emm1_blob, "
											+ "	 emm2_blob from dbo.pims_batch_header where batch_status_cd = ?";
	public static final String QUERYPROD40_50 = " SELECT p.sm_sn,"
											+ " p.mac_addr,"
											+ " p.model,"
											+ " p.hw_rev,"
											+ " p.mfg_id,"
											+ " ebs.strategy"
											+ " FROM dbo.pims_product p, dbo.pims_emm_build_strategy ebs"
											+ " WHERE p.dhct_sn = ?"
											+ " AND p.mfg_id = ebs.mfg_id AND p.model = ebs.model"
											+ " AND p.hw_rev = ebs.hw_rev";
	
	public static final String QUERYCERT_40_50 = "SELECT cert_type_cd, pub_key_cert, pub_key_cert_length "
												+ "	FROM dbo.pims_certificate WHERE sm_sn = ?";
	
	public static final String QUERYBATCH_50_70 = "select batch_id, delivery_id, info1 from dbo.pims_batch_header "
													+ "where batch_id = ? and batch_status_cd = ?";

	public static final String QUERY_50_70 = "select batch_id, delivery_id, info1 from dbo.pims_batch_header "
											+ "where batch_status_cd = ?";
	public static final String QUERYPROD50_70 = "SELECT distinct d.dhct_sn, p.cust_caa_id, p.mac_addr, "
												+ "p.emm_file, "
												+ "p.MODEL, "
												+ "p.hw_rev, "
												+ "p.mfg_id, "
												+ "ebs.strategy "
												+ "FROM dbo.pims_BATCH_detail d "
												+ "join dbo.pims_PRODUCT p on p.dhct_sn = d.dhct_sn "
												+ "join dbo.pims_EMM_BUILD_STRATEGY ebs on p.mfg_id = ebs.mfg_id "
												+ "AND p.MODEL = ebs.MODEL AND p.hw_rev = ebs.hw_rev "
												+ "WHERE d.batch_id = ?";										      
	public static final String QUERYCACERTS = "select * from dbo.pims_ca_certificates";
	public static final String QUERYHCTTYPES = "SELECT sq.filename, t.file_data FROM (SELECT distinct filename FROM dbo.PIMS_TYPE_FILES t, dbo.PIMS_EMM_BUILD_STRATEGY ebs, "
			+ "(SELECT DISTINCT s.model, s.hw_rev, m.mfg_id,m.mac_prefix FROM dbo.PIMS_PRODUCT s, dbo.PIMS_MFG_MAC_ADDR_RANGE m,"
			+ " dbo.PIMS_BATCH_DETAIL er WHERE er.batch_id = ?"
			+ " AND er.dhct_sn = s.dhct_sn AND m.mac_prefix=substring(s.mac_addr,1,6)"
			+ " ) psq WHERE psq.model = ebs.model AND psq.hw_rev = ebs.hw_rev AND psq.mfg_id = ebs.mfg_id AND ebs.model=t.dhct_type "
			+ "AND ebs.hw_rev=t.dhct_rev AND ebs.mfg_id=t.mfg_id AND ((SELECT CASE WHEN ebs.strategy = 'MFG_ID' THEN psq.mfg_id ELSE psq.mac_prefix"
			+ " END) = t.mac_ref ) )sq, PIMS_TYPE_FILES t WHERE t.filename = sq.filename";

	public static final String LOGUPDATEQUERY = "INSERT INTO dbo.pims_error_detail ("
											+ "NOTIFICATION_ID, " + "BATCH_ID, " + "DHCT_SN, "
											+ "ITEM_NO, " + "SEQ_NO, " + "TYPE_MSG, "
											+ "CREATED_DATE_TIME, " + "MESSAGE_ID, "
											+ "MESSAGE_DETAIL, " + "ERROR_DETAIL) " + "VALUES("
											+ "NEXT VALUE FOR dbo.SEQ_NOTIFICATION_ID, ?, ?, ?, ?, ?, SYSDATETIME(), ?, ?, ?)";
	public static final String LOGINITQUERY = "SELECT @@SERVERNAME dbHost, DB_NAME() dbName, SCHEMA_NAME() dbSchema, "
			+ "@@SPID dbSessionId, SYSTEM_USER dbSessionUser, HOST_NAME() terminal, PROGRAM_NAME() module";
	
	public static final String UPDATEPRODQUERY = "update dbo.pims_product set emm_file = ? , "
			+ "										  emm_date = ?,"
			+ "										  emm_file_size  = ? " + " where dhct_sn = ?";
	public static final String QUERYMAIL = "select * from dbo.pims_error_detail where batch_id = ? and created_date_time > DateAdd(MINUTE,-5,getdate())";
	public static final String MSG_START_17_20 = "Entered 17_20 Process";
	public static final String MSG_ERRSMSN_17_20 = "Missing Secure Micro SN";
	public static final String MSG_START_20_30 = "Entered 20_30 Process";
	public static final String FILESTATUS = "Successfully";
	public static final String MSG_ERRCERT_20_30 = "Certificate files missing for serial number";
	public static final String MSG_ERRSACERT_20_30 = "SA Certificate files missing for serial number";
	public static final String MSG_ERRCKCERT_20_30 = "Customer Key Certificate files missing for serial number";
	public static final String MAILSSTARTMSG = "<!DOCTYPE HTML PUBLIC \" -//W3C//DTD HTML 4.0 Transitional//EN\"><html><head>"
												+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">"
												+"<title> PIMS Status Mail Messages </title><meta name=\"Generator\"" 
												+"content=\"EditPlus\"><meta name=\"Author\" content=\"Shridhar Pobbala\">"
												+"</head> <body><table border=\"1\" cellpadding=\"3\" rules=\"all\"" 
												+"bgcolor=\"#D8C454\"><tr><td><b><font color=\"#3300FF\">Serial Number</font>"
												+"</b></td>"
												+"<td><b><font color=\"#3300FF\">Message Type</font></b></td>"
												+"<td><b><font color=\"#3300FF\">Date and Time</font></b></td>"
												+"<td><b><font color=\"#3300FF\">Message ID</font></b></td>"
												+"<td><b><font color=\"#3300FF\">Message</font></b></td></tr>";
	public static final String MAILSTARTTAGMSG = "<td><b><font color=\"#3300FF\">"; 
	public static final String MAILENDTAGMSG = "</font></b></td>";
	public static final String MAILENDMSG = "</table><br><br><b>Cisco Systems Inc.,<br>5030 SugarLoaf<br>Lawrenceville GA 30044<br>"
											  + "</b></body></html>";
}
