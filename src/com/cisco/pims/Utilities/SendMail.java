/*
 * Created on Nov 21, 2013
 * Author Shridhar Pobbala
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class SendMail {
	Properties propFile = null;
	BodyPart messageBodyPart = null;
	Properties propMail = null;
	MimeMessage message = null;
	private static final String propFilePath = "C:/testin/pimspocproperties.properties";

	public void connectToMailServer() {
		propFile = new Properties();
		try {
			propFile.load(new FileInputStream(propFilePath));
		} catch (FileNotFoundException fn) {
			fn.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String username = propFile.getProperty("UserName");
		final String password = propFile.getProperty("password");
		propMail = new Properties();
		propMail.put("mail.smtp.auth", "true");
		propMail.put("mail.smtp.starttls.enable", "true");
		propMail.put("mail.smtp.host", propFile.getProperty("MailServer"));
		propMail.put("mail.smtp.port", propFile.getProperty("MailPort"));
		Session session = Session.getDefaultInstance(propMail,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		message = new MimeMessage(session);
	}

	public boolean sendMail(String mailSubject, String mailMessage,
			String mailFrom, String mailTo) {
		this.connectToMailServer();
		try {
			message.setFrom(new InternetAddress(mailFrom));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(mailTo));
			message.setSubject(mailSubject);
			message.setContent(mailMessage, "text/html; charset=ISO-8859-1");
//			message.setText(mailMessage);
			Transport.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean sendMailWithAttachment(String mailSubject,
			String mailMessage, String mailFrom, String mailTo,
			byte[] attachmentData, String attachmentName) {
		this.connectToMailServer();
		try {
			message.setFrom(new InternetAddress(mailFrom));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(mailTo));
			message.setSubject(mailSubject);
			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			messageBodyPart.setText(mailMessage);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			ByteArrayDataSource source = new ByteArrayDataSource(
					attachmentData, "application/octet-stream");
			// DataSource source = new FileDataSource(filePath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(attachmentName);
			multipart.addBodyPart(messageBodyPart);
			
			// Put parts in message
			message.setContent(multipart);
			Transport.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return true;
	}
	public String formatMessage(String inMessage) {
		String outMessage = null;
		outMessage = inMessage + "<br><br><b>Regards,<br>PIMS Support Team.";
		return outMessage;
	}
	
}
