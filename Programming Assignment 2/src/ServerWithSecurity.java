import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class ServerWithSecurity {
	private static final int PORT = 4321;
	
	private static final long MAX_KEY_LENGTH = 8192L;
	private static final String WELCOME_MESSAGE = "Hello, this is SecStore!";
	private static final String SHA1_WITH_RSA = "SHA1withRSA";
	private static final String SUN_JSSE = "SunJSSE";
	private static final String RSA = "RSA";
	private static final char HEX_DIGIT [] = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	private static File keyFile;
	private static byte [] keyFileBytes;
	private static Signature dsa;
	private static KeyFactory keyFactory;
	private static PrivateKey privateKey;
	
	public static void init(String filename) {
		try {
			System.out.println("Loading key pair from " + filename);
			keyFile = new File(filename);
			dsa = Signature.getInstance(SHA1_WITH_RSA, SUN_JSSE);
			keyFactory = KeyFactory.getInstance(RSA, SUN_JSSE);

			if (!keyFile.exists()) {
				System.err.println("Key file not found!");
				System.exit(-1);
			}
			
			if(keyFile.length() > MAX_KEY_LENGTH) {
				System.err.println("Key file is too big!");
				System.exit(-1);
			}
			
			FileInputStream is = new FileInputStream(keyFile);

			int offset = 0;
			int read = 0;
			keyFileBytes = new byte[(int) keyFile.length()];
			
			while (offset < keyFileBytes.length && (read = is.read(keyFileBytes, offset, keyFileBytes.length - offset)) >= 0 ) {
				offset += read;
			}
			
			is.close();
			
			System.out.println("Key file loaded");
			
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(keyFileBytes);
			privateKey = keyFactory.generatePrivate(privKeySpec);
			dsa.initSign(privateKey);
			
			System.out.println("Initialization complete");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] sign(byte[] message) throws Exception {
		dsa.update(message);
		return dsa.sign();
	}
	
	public static void main(String[] args) {
		init("../privateServer.der");
		
		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		DataOutputStream toClient = null;
		DataInputStream fromClient = null;

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedFileOutputStream = null;
		boolean established = false;
		
		try {
			System.out.println("Server starting on port " + PORT);
			welcomeSocket = new ServerSocket(PORT);
			System.out.println("Server started! Listening...");
			
			byte[] signedWelcome = sign(WELCOME_MESSAGE.getBytes("UTF-8"));
			
			connectionSocket = welcomeSocket.accept();
			fromClient = new DataInputStream(connectionSocket.getInputStream());
			toClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();

				// If the packet is for transferring the filename
				if (established && packetType == 0) {

					System.out.println("Receiving file...");

					int numBytes = fromClient.readInt();
					byte [] filename = new byte[numBytes];
					fromClient.read(filename);

					fileOutputStream = new FileOutputStream("../recv/" + new String(filename, 0, numBytes));
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

				// If the packet is for transferring a chunk of the file
				} else if (established && packetType == 1) {

					int numBytes = fromClient.readInt();
					byte [] block = new byte[numBytes];
					fromClient.read(block);

					if (numBytes > 0)
						bufferedFileOutputStream.write(block, 0, numBytes);

				} else if (established && packetType == 2) {

					System.out.println("Closing connection...");

					if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					if (bufferedFileOutputStream != null) fileOutputStream.close();
					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}
				else if(packetType == 3) {
					System.out.println("Receiving hello");
					int numBytes = fromClient.readInt();
					byte[] helo = new byte[numBytes];
					fromClient.read(helo);
					
					String heloString = new String(helo, 0, numBytes);
					if(!heloString.equals("HELO")) {
						System.err.println("Invalid HELO received: " + heloString);
					}
					else {
						established = true;
						System.out.println("HELO");
						System.out.println("Sending welcome message...");
						toClient.writeInt(signedWelcome.length);
						toClient.write(signedWelcome);
						toClient.flush();
						System.out.println("Sent welcome message");
					}
				}
				else if(established && packetType == 4) {
					System.out.println("Sending server certificate...");
					
					File cert = new File("../server.crt");
					FileInputStream fileInputStream = new FileInputStream(cert);
					BufferedInputStream bis = new BufferedInputStream(fileInputStream);
					byte [] fromFileBuffer = new byte[(int) cert.length()];
					
					toClient.writeInt((int) cert.length());
					toClient.flush();
					
					int numBytes = 0;
					
					for (boolean fileEnded = false; !fileEnded;) {
						numBytes = bis.read(fromFileBuffer);
						fileEnded = numBytes < fromFileBuffer.length;
						
						toClient.write(fromFileBuffer);
						toClient.flush();
					}
					
					System.out.println("Sent server certificate");
				}
			}
		} catch (Exception e) {e.printStackTrace();}

	}

}
