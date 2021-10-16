# java-spread-assignment
IN5020 Assignment 2, using the Spread toolkit

## Table Of Contents
> * [Usage](#Usage)
> * [Specifications](#Specifications)
> * [Work Distribution](#Work Distribution)

## Usage
* Info
* Info
* Info

## Specifications
### getSyncedBalance() 
The naive implementation of getSyncedBalance waits until the entire outstandingCollections log is emptied before it reports the
client's balance. Since we completely empty the outstandingCollections log every 10 seconds, this means that at the end of this process
of emptying the collection, the (naively) synced balance will be reported. This might provide wrong results since it doesn't account
for when the getSyncedBalance call was made, but will rather wait until all the outstanding transactions are processed before 
reporting the balance.
The correct implementation however, will wait until the transaction for the getSyncedBalance is found in the outstandingCollection 
processing, and then use this to report the current (correctly) synced balance in the same client that made the call (and not the others).
This ensures that the getSyncedBalance call reports the correct balance, synced to the time when the call was made, rather than after
processing the entire batch of commands added in the last 10 seconds.

### Work Distribution
Consistent cooperation using intelliJ's "Code With Me" in planned development sessions online. With everyone participating in solving the task at hand. 

## Team
[![Jorgenwh](https://avatars.githubusercontent.com/u/56941036?v=4&s=144)](https://github.com/jorgenwh)	 |  [![Mamag](https://avatars.githubusercontent.com/u/18614750?v=4&s=144)](https://github.com/OniuUI) | [![Vegardoa](https://avatars.githubusercontent.com/u/40339509?v=4&s=144)](https://github.com/VitriolicTurtle)
---|---|---
[Jorgenwh](https://github.com/jorgenwh) | [Mamag](https://github.com/OniuUI) | [Vegardoa](https://github.com/VitriolicTurtle)