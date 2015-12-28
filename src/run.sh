#!/bin/bash

#Compile code
javac *.java

#Run Viterbi, which outputs a prediction file
java Viterbi

#Create output folder and move prediction file inside output
mkdir ../output
mv prediction_file ../output

#Evaluate
python ../eval_ne_tagger.py ../data/ner_dev.key ../output/prediction_file

#Clean up
rm *.class