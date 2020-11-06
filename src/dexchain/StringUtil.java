package dexchain;

import java.security.*;
import java.util.Base64;
import java.util.ArrayList;

// helper class to convert string to hash code
public class StringUtil {
	
	// applies SHA256 algorithm and returns result as hexString
	public static String applySha256(String input) {
		try {
			// SHA256 algorithm generates fixed size 256-bit hash
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			// apply SHA256 to input
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			// hash as hexidecimal
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString(); // return signature
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// takes private key, applies ECDSA signature to sign and returns as byte array
	public static byte[] applyECDSASignature(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSignature = dsa.sign();
			output = realSignature;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	// verify if signature is valid
	public static boolean ECDSASigVerified(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify =  Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// return difficulty string target to compare to hash: '0' * difficulty
	public static String difficultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
	// return encoded string from keys
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	// convert array of transactions into a merkle tree, returen tree root
	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();
		ArrayList<String> prevTreeLayer = new ArrayList<>();
		
		for (Transaction t : transactions) {
			prevTreeLayer.add(t.transactionId);
		}
		
		ArrayList<String> treeLayer = prevTreeLayer;
		while (count > 1) {
			treeLayer = new ArrayList<>();
			for (int i = 1; i < prevTreeLayer.size(); i++) {
				treeLayer.add(applySha256(prevTreeLayer.get(i-1) + prevTreeLayer.get(i)));
			}
			count = treeLayer.size();
			prevTreeLayer = treeLayer;
		}
		
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}
}
