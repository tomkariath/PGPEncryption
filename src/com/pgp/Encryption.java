package com.pgp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.examples.KeyBasedFileProcessor;

public class Encryption {
	public static void encrpt() {
		Security.addProvider(new BouncyCastleProvider());
		
		try {
			FileInputStream key = new FileInputStream("keys/Public.gpg");
			PGPPublicKey publicKey = Util.readPublicKey(key);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
