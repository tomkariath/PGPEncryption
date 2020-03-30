package com.pgp;

import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class Util {

	public static PGPPublicKey readPublicKey(InputStream in) throws IOException {
		try {
			PGPPublicKeyRing pgpPub = new PGPPublicKeyRing(in, new BcKeyFingerprintCalculator());
			return pgpPub.getPublicKey();
		} catch (IOException io) {
			System.out.println("readPublicKey() threw an IOException");
			System.out.println(io.toString());
			throw io;
		}

	}
	
}
