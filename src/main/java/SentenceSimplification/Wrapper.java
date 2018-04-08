package main.java.SentenceSimplification;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class Wrapper {

    public static void main(String[] args)
    {
        String document = "passage.txt";

        //generate parse trees for each sentence in the document
        ParseTreeGenerator parseTreeGenerator = new ParseTreeGenerator();
        List<Tree> parseTrees = parseTreeGenerator.getParseTreesForDocument(document);

        //perform pronoun noun phrase coreference resolution using arkref
        CoreferenceResolver coreferenceResolver = new CoreferenceResolver();
        coreferenceResolver.resolveCorefence(parseTrees);

        //perform sentence simplification
    }
}
