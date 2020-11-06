package dexchain;

import java.util.Date;
import java.util.ArrayList;

public class Block {
	
	public String hash;
	public String prevHash;
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<>();
	public long timeStamp;  // number of ms since 1/1/1970
	public int nonce;
	
	// constructor
	public Block(String prevHash) {
		this.prevHash = prevHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash();
	}
	
	// calculate the hash
	public String calculateHash() {
		String calculatedHash = StringUtil.applySha256(
						prevHash + 
						Long.toString(timeStamp) + 
						Integer.toString(nonce) +
						merkleRoot
				);
		return calculatedHash;
	}
	
	// mining block: increase nonce until target reached
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.difficultyString(difficulty);
		
		while (!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined: " + hash);
	}
	
	// add transaction to block
	public boolean addTransaction(Transaction transaction) {
		if (transaction == null) return false;
		if (prevHash != "0") { // not genesis block
			if (!transaction.processTransaction()) { // process transaction
				System.out.println("Transaction failed to process. Aborted.");
				return false;
			}
		}
		transactions.add(transaction);
		System.out.println("Transaction added to Block.");
		return true;
	}
}
