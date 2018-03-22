import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.net.Socket;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.Signature;
import java.security.PublicKey;
import java.util.Arrays;

public class ClientWithSecurity {
	private static final String CA_CERT_PATH = "../CA.crt";
	private static final String HELO = "HELO";
	private static final String WELCOME_MESSAGE = "Hello, this is SecStore!";
	private static final String SHA1_WITH_RSA = "SHA1withRSA";
	private static final String SUN_JSSE = "SunJSSE";
	private static final String RSA = "RSA";
	
	public static void main(String[] args) {
	    	String filename = "rr.txt";
		
		int numBytes = 0;

		Socket clientSocket = null;

		DataOutputStream toServer = null;
		DataInputStream fromServer = null;

		long timeStarted = System.nanoTime();

		try {

			System.out.println("Establishing connection to server...");

			// Connect to server and get the input and output streams
			clientSocket = new Socket("localhost", 4321);
			toServer = new DataOutputStream(clientSocket.getOutputStream());
			fromServer = new DataInputStream(clientSocket.getInputStream());
			System.out.println("Sending HELO...");
			toServer.writeInt(3);
			toServer.writeInt(HELO.getBytes().length);
			toServer.write(HELO.getBytes());
			toServer.flush();
			
			int welcomeLength = fromServer.readInt();
			byte[] welcome = new byte[welcomeLength];
			fromServer.read(welcome);
			
			System.out.println("Received welcome!");
			System.out.println("Requesting server certificate...");
			
			toServer.writeInt(4);
			toServer.flush();
			
			int certLength = fromServer.readInt();
			byte[] cert = new byte[certLength];
			
			int read = fromServer.readInt();
			int offset = 0;
			
			while(read == 1) {
				int length = fromServer.readInt();
				if(length == -1) break;
				byte[] block = new byte[length];
				fromServer.read(block);
				for(int i = 0; i < length; i++) {
					cert[offset + i] = block[i];
				}
				offset += length;
				
				read = fromServer.readInt();
			}
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate serverCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert));

			System.out.println("Received server certificate!");
			System.out.println("Verifying server certificate with " + CA_CERT_PATH + "...");
			X509Certificate CAcert = (X509Certificate) cf.generateCertificate(new FileInputStream(CA_CERT_PATH));
			PublicKey key = CAcert.getPublicKey();
			
			serverCert.checkValidity();
			serverCert.verify(key);
			
			System.out.println("Verified server certificate!");
			System.out.println("Verifying welcome message...");
			
			Signature dsa = Signature.getInstance(SHA1_WITH_RSA, SUN_JSSE);
			dsa.initVerify(serverCert.getPublicKey());
			dsa.update(WELCOME_MESSAGE.getBytes("UTF-8"));
			if(!dsa.verify(welcome)) {
				System.err.println("Verification failed! Terminating file transfer");
				System.exit(-1);
			}
			
			System.out.println("Verified welcome message!");
			System.out.println("Sending file...");
			
			sendWithCP1(filename, toServer, fromServer);
			
			System.out.println("Closing connection...");
			toServer.writeInt(2);
			toServer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
	}
	
	public static void sendWithCP1(String filename, DataOutputStream toServer, DataInputStream fromServer) {
		try {
			// Send the filename
			toServer.writeInt(5);
			toServer.writeInt(filename.getBytes().length);
			toServer.write(filename.getBytes());
			toServer.flush();

			// Open the file
			File file = new File(filename);
			if(!file.exists()) {
				System.err.println("File has problem");
				System.exit(-1);
			}
			if(file.length() == 0) {
				System.err.println("Empty file");
				System.exit(-1);
			}
		    	FileInputStream fileInputStream = new FileInputStream(file);
			BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream);
			byte [] fromFileBuffer = new byte[117];
			int numBytes = 0;
			
			// Send the file
			int count = 0;
			for (boolean fileEnded = false; !fileEnded;) {
				numBytes = bufferedFileInputStream.read(fromFileBuffer);
				fileEnded = numBytes < fromFileBuffer.length;

				toServer.writeInt(1);
				toServer.writeInt(numBytes);
				toServer.write(fromFileBuffer, 0, numBytes);
				toServer.flush();
				count++;
			}
			System.out.println("Sent " + count + " blocks");
			bufferedFileInputStream.close();
			fileInputStream.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
