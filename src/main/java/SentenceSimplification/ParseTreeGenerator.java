package SentenceSimplification;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeGenerator {

    private LexicalizedParser loadModel(){

        String model = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        LexicalizedParser lp = LexicalizedParser.loadModel(model);
        return lp;
    }

    private List<Tree> getParseTreesForDocument(String documentName, LexicalizedParser lp){

        List<Tree> parseTrees = new ArrayList<>();

        for (List<HasWord> sentence : new DocumentPreprocessor(documentName)) {
            Tree tree = lp.apply(sentence);
            parseTrees.add(tree);
            /*
            //code to print parse tree
            tree.pennPrint();
            System.out.println();
            */
        }

        return parseTrees;
    }

    public List<Tree> getParseTreesForDocument(String documentName){

        LexicalizedParser lp = loadModel();
        List<Tree> parseTrees = getParseTreesForDocument(documentName, lp);
        return parseTrees;

    }
}
