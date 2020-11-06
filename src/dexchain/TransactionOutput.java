package dexchain;

import java.security.PublicKey;

public class TransactionOutput {
	public PublicKey recipient;         // new owner of coins
	public float value;                 // amount of coins own
	public String id;
	public String parentTransactionId;  // transaction with this output to be created in
	
	// constructor
	public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
		this.recipient = recipient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(
				StringUtil.getStringFromKey(recipient) +
				Float.toString(value) +
				parentTransactionId
				);
	}
	
	// check if coin belongs to me
	public boolean belongsToMe(PublicKey publicKey) {
		return publicKey.equals(recipient);
	}
}
