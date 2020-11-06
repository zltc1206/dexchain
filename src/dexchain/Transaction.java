package dexchain;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId; // hash of transaction
	public PublicKey sender;     // sender address
	public PublicKey recipient;  // recipient address
	public float value;          // amount of funds transferred
	public byte[] signature;
	
	// inputs: reference to previous transactions; outputs: amount received in the transaction
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0;
	
	// constructor
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	// create and process transaction, return true if transaction can be created
	public boolean processTransaction() {
		if (!verifySignature()) {
			System.out.println("Fail to verify transaction signature");
			return false;
		}
		
		// take input, store unspent transactions
		for (TransactionInput i : inputs) {
			i.UTXO = DexChain.UTXOs.get(i.transactionOutputId);
		}
		
		// check input value
		if (inputsTotal() < DexChain.minimumTransaction) {
			System.out.println("Inputs too small: " + inputsTotal());
			return false;
		}
		
		// generate output
		float remaining = inputsTotal() - value;
		transactionId = calculateId();
		outputs.add(new TransactionOutput(this.recipient, value, transactionId));  // send to recipient
		outputs.add(new TransactionOutput(this.sender, remaining, transactionId)); // remaining back to sender
		
		// store unspent
		for (TransactionOutput o : outputs) {
			DexChain.UTXOs.put(o.id, o);
		}
		
		// remove inputs from unspent storage since they are spent
		// because a transaction output can only be used once as an input
		for (TransactionInput i : inputs) {
			if (i.UTXO == null) continue;
			DexChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	// return value sum from inputs
	public float inputsTotal() {
		float total = 0;
		for (TransactionInput i : inputs) {
			if (i.UTXO == null) continue;
			total += i.UTXO.value;
		}
		return total;
	}
	
	// return value sum from outputs
	public float outputsTotal() {
		float total = 0;
		for (TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	// calculate transactionID
	private String calculateId() {
		sequence++; // increment sequence to avoid 2 identical transactions having same ID
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(recipient) +
				Float.toString(value) +
				sequence
				);
	}
	
	// generate signature by signing all data needs to be protected: 
	// sender and recipient address; fund value
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + 
					  StringUtil.getStringFromKey(recipient) +
					  Float.toString(value);
		
		signature = StringUtil.applyECDSASignature(privateKey, data);
	}
	
	// verify signature to see if data signed is tempered
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + 
				  	  StringUtil.getStringFromKey(recipient) +
				  	  Float.toString(value);
		
		return StringUtil.ECDSASigVerified(sender, data, signature);
	}
}
