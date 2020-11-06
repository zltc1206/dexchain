# dexchain
A simplistic block chain implemented with Java

contains following classes:

**Block**
Block to store transaction information and form DexChain

**Transaction**
Information about transaction, including sender, recipient, value, signature, inputs and outputs

**TransactionInput**

**TransactionOutput**

**Wallet**
Contains private key, public key and UTXOs

**DexChain**
Simple block chain

**StringUtil**
Some helper functions for string processing, hashcode, signature etc.


*dependencies:*
need [bouncy castle](https://www.bouncycastle.org/latest_releases.html)
