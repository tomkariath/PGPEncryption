package pgp.autonomy;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;

public class KeyPairGenerator {
	
	public static PGPKeyRingGenerator generateKeyRingGenerator (String emailId, char[] passKey) {
		return generateKeyRingGenerator(emailId, passKey, 0xc0);
	}

	private static PGPKeyRingGenerator generateKeyRingGenerator (String emailId, char[] passKey, int s2kcount) {
		RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
		
		final BigInteger FERMAT_NUMBER = BigInteger.valueOf(0x10001);
		final int KEY_SIZE = 2048;
		final int NUMBER_OF_CERTAINITY_TESTS = 12;
		
		keyPairGenerator.init(new RSAKeyGenerationParameters(FERMAT_NUMBER, new SecureRandom(), KEY_SIZE, NUMBER_OF_CERTAINITY_TESTS));
		
		try {
			PGPKeyPair masterSigningKey = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, keyPairGenerator.generateKeyPair(), new Date());
			PGPKeyPair encryptionSubKey = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, keyPairGenerator.generateKeyPair(), new Date());
			
			PGPSignatureSubpacketGenerator signHashGen = new PGPSignatureSubpacketGenerator();
			signHashGen.setKeyFlags(false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
			signHashGen.setPreferredSymmetricAlgorithms(false, new int[] {SymmetricKeyAlgorithmTags.AES_128, SymmetricKeyAlgorithmTags.AES_192, 
					SymmetricKeyAlgorithmTags.AES_256});
			signHashGen.setPreferredHashAlgorithms(false, new int[] {HashAlgorithmTags.SHA1, HashAlgorithmTags.SHA224, HashAlgorithmTags.SHA256, 
					HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA512});
			
			signHashGen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);
			
			PGPSignatureSubpacketGenerator encHashGen = new PGPSignatureSubpacketGenerator();
			encHashGen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);
			
			PGPDigestCalculator sha1Calculator = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
			PGPDigestCalculator sha256Calculator = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);
			
			PBESecretKeyEncryptor secretKeyEncryptor = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calculator, s2kcount))
					.build(passKey);
			
			PGPKeyRingGenerator keyRingGenerator = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, masterSigningKey, emailId, 
					sha1Calculator, signHashGen.generate(), null, 
					new BcPGPContentSignerBuilder(masterSigningKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), secretKeyEncryptor);
			
			keyRingGenerator.addSubKey(encryptionSubKey, encHashGen.generate(), null);
			
			return keyRingGenerator;
			
		} catch (PGPException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		char pass[] = "test123@".toCharArray();
		
		PGPKeyRingGenerator keyRingGenerator = generateKeyRingGenerator("test@gmail.com", pass);
		
		try {
			PGPPublicKeyRing publicKeyRing = keyRingGenerator.generatePublicKeyRing();
			BufferedOutputStream publicOutputStream = new BufferedOutputStream(new FileOutputStream("PublicKeyRing.pkr"));
			publicKeyRing.encode(publicOutputStream);
			publicOutputStream.close();
			
			PGPSecretKeyRing privaKeyRing = keyRingGenerator.generateSecretKeyRing();
			BufferedOutputStream privateOutputStream = new BufferedOutputStream(new FileOutputStream("PrivateKeyRing.pkr"));
			privaKeyRing.encode(privateOutputStream);
			privateOutputStream.close();			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
