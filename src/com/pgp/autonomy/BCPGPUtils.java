package com.pgp.autonomy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

public class BCPGPUtils {

	public static PGPPublicKey readPublicKey(String publicKeyFilePath) throws IOException, PGPException {

		InputStream in = new FileInputStream(new File(publicKeyFilePath));

		in = PGPUtil.getDecoderStream(in);
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new BcKeyFingerprintCalculator());
		PGPPublicKey key = null;

		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
		while (key == null && rIt.hasNext()) {
			PGPPublicKeyRing kRing = rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			//boolean encryptionKeyFound = false;

			while (key == null && kIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) kIt.next();
				if (k.isEncryptionKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"Can't find encryption key in key ring.");
		}

		return key;
	}

	public static PGPPublicKey readPublicKey(String publicKeyFilePath, long keyId) throws IOException, PGPException {

		InputStream in = new FileInputStream(new File(publicKeyFilePath));

		in = PGPUtil.getDecoderStream(in);
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new BcKeyFingerprintCalculator());
		PGPPublicKey key = null;

		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
		while (rIt.hasNext()) {
			PGPPublicKeyRing kRing = rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			//boolean encryptionKeyFound = false;

			while (kIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) kIt.next();
				long keyid = k.getKeyID();
				if (keyid == keyId) {
					key = k;
				}
				//if (k.isEncryptionKey()) {
				//	key = k;
				//}
			}
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"Can't find encryption key in key ring.");
		}

		return key;
	}
	
	public static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID,
			char[] pass) throws IOException, PGPException,
			NoSuchProviderException {
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
				PGPUtil.getDecoderStream(keyIn), new BcKeyFingerprintCalculator());

		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

		if (pgpSecKey == null) {
			return null;
		}
		

		//return pgpSecKey.extractPrivateKey(pass, "BC");
		
		return pgpSecKey.extractPrivateKey(new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass));
	}
	
	public static PGPSecretKey findSecretKey(InputStream in) throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in, new BcKeyFingerprintCalculator());

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPSecretKey key = null;

        //
        // iterate through the key rings.
        //
        Iterator<PGPSecretKeyRing> rIt = pgpSec.getKeyRings();

        while (key == null && rIt.hasNext()) {
            PGPSecretKeyRing kRing = rIt.next();
            Iterator<PGPSecretKey> kIt = kRing.getSecretKeys();

            while (key == null && kIt.hasNext()) {
                PGPSecretKey k = (PGPSecretKey) kIt.next();

                if (k.isSigningKey()) {
                    key = k;
                }
            }
        }

        if (key == null) {
            throw new IllegalArgumentException(
                    "Can't find signing key in key ring.");
        }
        return key;
    }
}
