package dexchain;

import java.security.Security;
import java.util.*;

public class DexChain {
	public static ArrayList<Block> dexChain = new ArrayList<Block>();     // chain
	public static Map<String, TransactionOutput> UTXOs = new HashMap<>(); // unspent transactions
	
	public static int difficulty = 5;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;
	
	public static void main(String[] args) {
		// set up Bouncy Castle as security provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();
		
		// create genesis transaction to send 100 coins to walletA
		// then manually sign the transaction, set id and add transaction output
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new TransactionOutput(
															genesisTransaction.recipient,
															genesisTransaction.value,
															genesisTransaction.transactionId
															));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // store first transaction into UTXOs
		
		System.out.println("Creating and mining Genesis block...");
		
		// add genesis block to chain
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		// add new transactions and blocks
		Block block1 = new Block(genesis.hash);
		System.out.println("\nwallet A balance: " + walletA.getBalance());
		System.out.println("Attempting to send 40 from walletA to walletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("walletA balance now: " + walletA.getBalance());
		System.out.println("walletB balance now: " + walletB.getBalance());
		
		Block block2 = new Block(block1.hash);
		System.out.println("\nwallet A balance: " + walletA.getBalance());
		System.out.println("Attempting to send 1000 from walletA to walletB...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("walletA balance now: " + walletA.getBalance());
		System.out.println("walletB balance now: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nwallet A balance: " + walletA.getBalance());
		System.out.println("Attempting to send 20 from walletB to walletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
		addBlock(block3);
		System.out.println("walletA balance now: " + walletA.getBalance());
		System.out.println("walletB balance now: " + walletB.getBalance());
		
		System.out.println("\nIs the chain valid?");
		isValidChain();
	}
	
	// check if a chain is valid
	public static Boolean isValidChain() {
		Block currBlock;
		Block prevBlock;
		String hashTarget = StringUtil.difficultyString(difficulty);
		Map<String, TransactionOutput> tempUTXOs = new HashMap<>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		for (int i = 1; i < dexChain.size(); i++) {
			currBlock = dexChain.get(i);
			prevBlock = dexChain.get(i-1);
			
			// compare recorded and calculated hash
			if (!currBlock.hash.equals(currBlock.calculateHash())) {
				System.out.println("Current hashes not match: " + i);
				return false;
			}
			// compare previous hash and recorded previous hash
			if (!prevBlock.hash.equals(currBlock.prevHash)) {
				System.out.println("Previous hashes not match: " + i);
				return false;
			}
			// check is hash is solved
			if (!currBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("The block has not been mined yet: " + i);
				return false;
			}
			
			// iterate through chain transactions
			TransactionOutput tempOutput;
			for (int j = 0; j < currBlock.transactions.size(); j++) {
				Transaction currTransaction = currBlock.transactions.get(j);
				
				// check transaction signature
				if (!currTransaction.verifySignature()) {
					System.out.println("Signature verification on transaction(" + j + ") invalid");
					return false;
				}
				
				// check inputs outputs equality
				if (currTransaction.inputsTotal() != currTransaction.outputsTotal()) {
					System.out.println("Inputs not matching outputs on transaction(" + j + ")");
					return false;
				}
				
				// check inputs
				for (TransactionInput input : currTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if (tempOutput == null) {
						System.out.println("Reference input missing on transaction(" + j + ")");
						return false;
					}
					
					if (input.UTXO.value != tempOutput.value) {
						System.out.println("Reference input invalid on transaction(" + j + ")");
						return false;
					}
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				// add outputs
				for (TransactionOutput output : currTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				// check recipient
				if (currTransaction.outputs.get(0).recipient != currTransaction.recipient) {
					System.out.println("Recipient not match on transaction(" + j +")");
					return false;
				}
				// check sender
				if (currTransaction.outputs.get(1).recipient != currTransaction.sender) {
					System.out.println("Transaction(" + j +") change not match sender");
					return false;
				}
			}
		}
		System.out.println("Valid Chain!");
		return true;
	}
	
	// add block to chain after mining
	public static void addBlock(Block block) {
		block.mineBlock(difficulty);
		dexChain.add(block);
	}

}
