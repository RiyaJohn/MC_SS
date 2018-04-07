package SentenceSimplification.java;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

class ParseTreeGenerator {

    private Annotation getAnnotation(String document){

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // create an empty Annotation just with the given text
        Annotation annotation = new Annotation(document);

        // run all Annotators on this text
        pipeline.annotate(annotation);

        return annotation;
    }

    public List<Tree> getParseTreesForDocument(String document){

        List<Tree> parseTrees = null;

        Annotation annotatedDocument = getAnnotation(document);

        List<CoreMap> sentences = annotatedDocument.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences){
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            try {
                parseTrees.add(tree);
            }
            catch (Exception e)
            {}
        }

        return parseTrees;

    }
}
