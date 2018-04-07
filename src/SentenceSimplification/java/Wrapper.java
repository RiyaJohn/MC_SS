package SentenceSimplification.java;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class Wrapper {

    public static void main(String[] args)
    {
        String document = "However, Thomas Jefferson, the third US President"
                +"did not believe that the Embargo Act, which restricted trade with Europe"
                +"would impact the American economy.";

        ParseTreeGenerator parseTreeGenerator = new ParseTreeGenerator();
        List<Tree> parseTrees = parseTreeGenerator.getParseTreesForDocument(document);

        for(Tree tree: parseTrees){
            System.out.println(tree.yield());
        }
    }
}
