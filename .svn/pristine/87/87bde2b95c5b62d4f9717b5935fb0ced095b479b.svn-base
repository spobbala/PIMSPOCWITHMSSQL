/*
 * Created on Dec 4, 2013
 * Author Shridhar Pobbala
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.TransferCancelledException;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.LicenseManager;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh1.Ssh1Client;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.ConsoleKnownHostsKeyVerification;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.sftp.SftpClient;

public class CustomSFTP {
	private String strServerNameOrIpAddress;
	private String userName;
	private String sftpPassword;
	private String fromLocation;
	private String toLocation;
	private String fileName;
	private String certKey;
	private int port;
	private String authenticationMethod;
	private String privateKeyFileLoc;
	private byte[] inputBytes;

	public CustomSFTP(String strServerNameOrIpAddress, String userName,
			String sftpPassword, String fromLocation, String toLocation,
			String fileName, int port, String authenticationMethod,
			String privateKeyFileLoc, String certKey) {

		this.strServerNameOrIpAddress = strServerNameOrIpAddress;
		this.userName = userName;
		this.sftpPassword = sftpPassword;
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
		this.fileName = fileName;
		this.port = port;
		this.authenticationMethod = authenticationMethod;
		this.privateKeyFileLoc = privateKeyFileLoc;
		this.certKey = certKey;

	}

	public CustomSFTP(String strServerNameOrIpAddress, String userName,
			String sftpPassword, String toLocation, 
			String fileName, byte[] inputBytes, int port, String authenticationMethod,
			String privateKeyFileLoc, String certKey) {

		this.strServerNameOrIpAddress = strServerNameOrIpAddress;
		this.userName = userName;
		this.sftpPassword = sftpPassword;
		this.toLocation = toLocation;
		this.fileName = fileName;
		this.port = port;
		this.authenticationMethod = authenticationMethod;
		this.privateKeyFileLoc = privateKeyFileLoc;
		this.inputBytes = inputBytes;
		this.certKey = certKey;

	}

	public String sftpSend() {
		String sftpStatus = null;
			if (strServerNameOrIpAddress == null || userName == null
				|| sftpPassword == null || toLocation == null
				|| fileName == null || port <= 0 || certKey == null || authenticationMethod == null) {
			sftpStatus = "Mandatory Parameter missing file not sent:"
					+ fileName;
			return sftpStatus;
		} else if (fromLocation == null && inputBytes == null) {
			sftpStatus = "Mandatory Parameter missing or null, file not sent:"
					+ fileName;
			return sftpStatus;
		}

		if (authenticationMethod.equalsIgnoreCase("public key")
				&& privateKeyFileLoc == null) {
			sftpStatus = "Private Key location is required for public key authentication file not sent:"
					+ fileName;
			return sftpStatus;
		}
		
		SshClient ssh = null;
		PasswordAuthentication pwd = null;
		try {
			LicenseManager.addLicense(certKey);
			SshConnector con = SshConnector.getInstance();
			con.getContext(2).setHostKeyVerification(
					new ConsoleKnownHostsKeyVerification());
			Ssh2Context ssh2Context = (Ssh2Context) con.getContext(2);
			ssh2Context.setPreferredPublicKey("ssh-dss");
			HostKeyVerification hkv = new HostKeyVerification() {

				public boolean verifyHost(String hostname, SshPublicKey key) {
					return true;
				}
			};
			con.setKnownHosts(hkv);
			SocketTransport t = new SocketTransport(strServerNameOrIpAddress,
					port);
			t.setTcpNoDelay(true);
			// if (t.isConnected())
			// System.out.println("connected to sftp server");
			// else
			// System.out.println("error to sftp server");
			ssh = con.connect(t, userName);
			Ssh2Client ssh2 = (Ssh2Client) ssh;
			if (ssh instanceof Ssh1Client) {
				ssh.disconnect();
				sftpStatus = "SSH1 not supported";
				return sftpStatus;
			}
			if (authenticationMethod.equalsIgnoreCase("password")) {
				pwd = new PasswordAuthentication();
				pwd.setPassword(sftpPassword);
				ssh2.authenticate(pwd);
			} else if (authenticationMethod.equalsIgnoreCase("public key")) {
				FileInputStream in = new FileInputStream(privateKeyFileLoc);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int read;
				while ((read = in.read()) > -1)
					out.write(read);
				in.close();
				SshPrivateKeyFile pkfile = SshPrivateKeyFileFactory.parse(out
						.toByteArray());
				SshKeyPair pair;
				if (pkfile.isPassphraseProtected())
					pair = pkfile.toKeyPair(sftpPassword);
				else
					pair = pkfile.toKeyPair(null);
				PublicKeyAuthentication pk = new PublicKeyAuthentication();
				pk.setPrivateKey(pair.getPrivateKey());
				pk.setPublicKey(pair.getPublicKey());
				ssh.authenticate(pk);
			}
			if (ssh.isConnected() && ssh.isAuthenticated()) {
				SftpClient sftp = new SftpClient(ssh2);
				sftp.cd(toLocation);
				if (fromLocation == null) {
					InputStream is = new ByteArrayInputStream(inputBytes);
					sftp.put(is, toLocation+fileName);
					is.close();
				} else {
					sftp.lcd(fromLocation);
					sftp.put(fromLocation + fileName);
				}
			}
			sftpStatus = fileName + ": " + "File Sent Successfully";
			ssh.disconnect();
		} catch (SftpStatusException sso) {
			sftpStatus = "SFTP Error1:" + sso.getMessage();
			sso.printStackTrace();
		} catch (IOException e) {
			sftpStatus = "SFTP Error2:" + e.getMessage();
		} catch (SshException e) {
			sftpStatus = "SFTP Error3:" + e.getMessage();
		} catch (InvalidPassphraseException e) {
			sftpStatus = "SFTP Error4:" + e.getMessage();
		} catch (ChannelOpenException e) {
			sftpStatus = "SFTP Error5:" + e.getMessage();
		} catch (TransferCancelledException eTransExcep) {
			sftpStatus = "SFTP Error6:" + eTransExcep.getMessage();
		} catch (Exception exp) {
			sftpStatus = "SFTP Error7:" + exp.getMessage();
		}
		return sftpStatus;
	}
}
