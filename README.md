# NER
Named-entity recognition - extracts and classifies entities in text

http://en.wikipedia.org/wiki/Named-entity_recognition
This code uses tagged text to train a trigram HMM tagger. It identifies persons, organizations, locations, miscellaneous, and other.

Once the maximum likelihood estimates are computed for the bigram probabilities and the emission probabilities, it implements the Viterbi algorithm for finding the most probable tag sequence for a given sentence.
http://en.wikipedia.org/wiki/Viterbi_algorithm
