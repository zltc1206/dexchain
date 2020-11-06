package dexchain;

public class TransactionInput {
	public String transactionOutputId; // reference to transaction output id
	public TransactionOutput UTXO;     // contains un-spent transaction output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
