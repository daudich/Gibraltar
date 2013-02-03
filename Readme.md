# Naval Combat System
#### Codename: Gibraltar
#### Team Members: Dhiren Audich, Robert Codd-Downey
***

## What is Naval Combat System?

This is a server which contains and controlls all the logic for a multiplayer distributed naval
warfare game. This was developed as a class project for CIS*3750 at University of Guelph.

It is a top-down naval combat "simulator". It processes and passes messages between all the clients
using the SynchronousQueue data structure in JavaSE API. The protocol for the communications was
conceived as a class to ensure all servers and clients are compatible.

## Lessons learnt and outcome.

We made a lot of mistakes, correct some of them, and ended up with a pretty rad product that we
are quite proud of, considering the tight timeframe that development took place in ~2 weeks for
dev and testing, with some documentation and packaging taking place at the end of the term.

## How to run?

* To build and run:
  'ant'

* To run the client:
  java -jar nelson_client.jar

* Enjoy!

*NOTE: The client was developed by one of the client groups.*