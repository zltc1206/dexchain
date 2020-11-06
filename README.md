# dexchain
A simplistic block chain implemented with Java

contains following classes:

**Block:**<br/>
Block to store transaction information and form DexChain

**Transaction:**<br/>
Information about transaction, including sender, recipient, value, signature, inputs and outputs

**TransactionInput**<br/>

**TransactionOutput**<br/>

**Wallet:**<br/>
Contains private key, public key and UTXOs

**DexChain:**<br/>
Simple block chain

**StringUtil:**<br/>
Some helper functions for string processing, hashcode, signature etc.


*dependencies:*<br/>
need [bouncy castle](https://www.bouncycastle.org/latest_releases.html)
