package dexchain;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Wallet {
	public PrivateKey privateKey; // for signature
	public PublicKey publicKey;   // for address
	
	public Map<String, TransactionOutput> myUTXOs = new HashMap<>(); // unspent transaction owned by this wallet
	
	// constructor
	public Wallet() {
		generateKeyPair();
	}
	
	// generate key pair
	public void generateKeyPair() {
		try {
			// use Elliptic Curve Digital Signature Algorithm
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			// Initialize key generator to generate key pair
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			// set public and private keys from key pair
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// returns balance and stores UTXOs owned by this wallet to myUTXOs
	public float getBalance() {
		float total = 0;
		
		for (Map.Entry<String, TransactionOutput> item : DexChain.UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			
			if (UTXO.belongsToMe(publicKey)) {
				myUTXOs.put(UTXO.id, UTXO);
				total += UTXO.value;
			}
		}
		return total;
	}
	
	// generate transaction from this wallet
	public Transaction sendFunds(PublicKey _recipient, float value) {
		// check balance
		if (getBalance() < value) {
			System.out.println("Not enough funds remaining, transaction aborted");
			return null;
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<>();
		float total = 0;
		
		// iterate through wallet UTXO and add to total till target value reached
		for (Map.Entry<String, TransactionOutput> item : myUTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if (total > value) break;
		}
		
		// initiate transaction
		Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		// update myUTXO
		for (TransactionInput i : inputs) {
			myUTXOs.remove(i.transactionOutputId);
		}
		return newTransaction;
	}
}
