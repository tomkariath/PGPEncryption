package com.pgp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.asn1.cmc.DecryptedPOP;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;


public class BouncyCastlePGPTest {

	public static void main(String[] args) {
		
		// the keyring that holds the public key we're encrypting with
		String publicKeyFilePath = "D:\\Downloads\\WorkDocs\\Public.gpg";
		
		// init the security provider
		Security.addProvider(new BouncyCastleProvider());
		
		try {
			
			System.out.println("Creating a temp file...");
			
			// create a file and write the string to it
			File outputfile = File.createTempFile("pgp", null);
			FileWriter writer = new FileWriter(outputfile);
			writer.write("the message I want to encrypt".toCharArray());
			writer.close();
			
			System.out.println("Temp file created at ");
			System.out.println(outputfile.getAbsolutePath());
			System.out.println("Reading the temp file to make sure that the bits were written\n----------------------------");
			
			BufferedReader isr = new BufferedReader(new FileReader(outputfile));
			String line = "";
			while ((line = isr.readLine()) != null) {
				System.out.println(line + "\n");
			}
			isr.close();
			
			// read the key 
			FileInputStream	in = new FileInputStream(publicKeyFilePath);
			System.out.println("blah");
			PGPPublicKey key = readPublicKey(in);
			
			// find out a little about the keys in the public key ring
			System.out.println("Key Strength = " + key.getBitStrength());
			System.out.println("Algorithm = " + key.getAlgorithm());
			
			int count = 0;
			for (Iterator<String> iterator = key.getUserIDs(); iterator.hasNext();) {
				count++;
				System.out.println((String)iterator.next());	
			}
			System.out.println("Key Count = " + count);
			// create an armored ascii file
			FileOutputStream out = new FileOutputStream(outputfile.getAbsolutePath() + ".asc");
		
			// encrypt the file
			encryptFile(outputfile.getAbsolutePath(), out, key);
			
			System.out.println("Reading the encrypted file\n----------------------------");
			BufferedReader isr2 = new BufferedReader(new FileReader(new File(outputfile.getAbsolutePath() + ".asc")));
			String line2 = "";
			while ((line2 = isr2.readLine()) != null) {
				System.out.println(line2);
			}
			isr2.close();
				
				
		} catch (PGPException e) {
			System.out.println(e.toString());
			System.out.println(e.getUnderlyingException().toString());
			
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
	}
	
	private static PGPPublicKey readPublicKey(InputStream in) throws IOException {
		try {
			PGPPublicKeyRing pgpPub = new PGPPublicKeyRing(in, new BcKeyFingerprintCalculator());
			return pgpPub.getPublicKey();
		} catch (IOException io) {
			System.out.println("readPublicKey() threw an IOException");
			System.out.println(io.toString());
			throw io;
		}

	}
	
	private static void encryptFile(String fileName, OutputStream out, PGPPublicKey encKey)
	throws IOException, NoSuchProviderException, PGPException  
	{
			
		out = new ArmoredOutputStream(out);
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		
		System.out.println("creating comData...");
		
		// get the data from the original file 
		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
		PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
		comData.close();
		
		System.out.println("comData created...");
		
		System.out.println("using PGPEncryptedDataGenerator...");
		
		// object that encrypts the data
		PGPDataEncryptorBuilder encryptorBuilder = new BcPGPDataEncryptorBuilder(PGPEncryptedDataGenerator.CAST5);
		PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(encryptorBuilder);
		BcPublicKeyKeyEncryptionMethodGenerator keyEncryptionMethodGenerator = new BcPublicKeyKeyEncryptionMethodGenerator(encKey);
		cPk.addMethod(keyEncryptionMethodGenerator);
		
		System.out.println("used PGPEncryptedDataGenerator...");
		
		// take the outputstream of the original file and turn it into a byte array
		byte[] bytes = bOut.toByteArray();
		
		System.out.println("wrote bOut to byte array...");
		
		// write the plain text bytes to the armored outputstream
		OutputStream cOut = cPk.open(out, bytes.length);
		cOut.write(bytes);
		
		
		// cOut.close();
		cPk.close();
		out.close();
	}
}