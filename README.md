# openamtypingmodule
All the source code and steps to build an openam instance with a custom authentication module 

The module should be used as a second or third step in an authentication chain, not as a single module.

In the kubernetes directory you can find all the information for displaying the openam instance in docker and kubernetes

The Typing directory contains the source code and compiled .jar code of the typing authentication module.
The setup folder contains all the files related to the module UI and attribute modification. 
Use the scripts included just in case you want to install the module in your own openam instance.

You can find de docker image under the repo:
daniizquie921/openamtypingmodule
