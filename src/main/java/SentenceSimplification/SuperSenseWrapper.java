package SentenceSimplification;

import edu.cmu.ark.DiscriminativeTagger;
import edu.cmu.ark.LabeledSentence;
import edu.stanford.nlp.trees.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.MorphologicalProcessor;

public class SuperSenseWrapper {

    private DiscriminativeTagger discriminativeTagger;
    private static SuperSenseWrapper superSenseWrapperInstance;

    private SuperSenseWrapper(){
        String propertiesFilePath = ClassLoader.getSystemResource("config/QuestionTransducer.properties").getFile().replaceAll("%20"," ");
        DiscriminativeTagger.loadProperties(propertiesFilePath);
        String modelPath = ClassLoader.getSystemResource("config/superSenseModelAllSemcor.ser.gz").toExternalForm().substring(6).replaceAll("%20"," ");
        discriminativeTagger = DiscriminativeTagger.loadModel(modelPath);
        try {
            JWNL.initialize(new FileInputStream("C:/Users/Sharanya R C/IdeaProjects/MachineComprehension/src/main/resources/config/file_properties.xml"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static SuperSenseWrapper getInstance(){
        //ensure that only one instance of the DiscriminativeTagger is running
        if(superSenseWrapperInstance == null){
            superSenseWrapperInstance = new SuperSenseWrapper();
        }
        return superSenseWrapperInstance;
    }

    public List<String> annotateSentenceWithSupersenses(Tree parseTree){

        List<String> result = new ArrayList<>();
        int numberOfLeaves = parseTree.getLeaves().size();
        if(numberOfLeaves <= 1){
            return result;
        }

		//Get a list that contains for each word in the sentence: word, lemma and pos tag
        LabeledSentence labeled = generateSupersenseTaggingInput(parseTree);

        //find the most likely super sense for each word of the sentence
        discriminativeTagger.findBestLabelSequenceViterbi(labeled, discriminativeTagger.getWeights());

        for(String pred: labeled.getPredictions()){
            result.add(pred);
        }
        while(result.size() < numberOfLeaves) result.add("0");

        return result;
    }

    private LabeledSentence generateSupersenseTaggingInput(Tree parseTree){
        LabeledSentence result = new LabeledSentence();
        List<Tree> leaves = parseTree.getLeaves();

        for(int i=0;i<leaves.size();i++){
            //get the word
            String word = leaves.get(i).label().toString();
            Tree parentOfWord = leaves.get(i).parent(parseTree);
            //the parent node will contain the POS tag of the word
            String pos = parentOfWord.label().toString();
            //use WordNet to get the Lemma of the word
            String stem = getLemma(word, pos);
            result.addToken(word, stem, pos, "0");
        }

        return result;
    }

    public String getLemma(String word, String pos) {
		/*
			CC - Coordinating conjunction - for, and, nor , but, or, yet, so
			CD - Cardinal number - one, two, three...
			DT - Determiner - a, an, the
			EX - Existential there - there is a God. There are boys in the yard.
			FW - Foreign word
			IN - Preposition or subordinating conjunction - across, among, until, before, after...
			LS - List item marker
			MD - Modal - can, could, may, might, must, will, would, shall, should
			PDT - Predeterminer - both, a lot of (occurs before a determiner)
			POS - Possessive ending - 's , s'
			PRP - Personal pronoun - I, he, she, it...
			PRP$ - Possessive pronoun (prolog version PRP-S) his, her, their...
			SYM - Symbol
			TO - to
			UH - Interjection - ah, oh, dear me, yikes...
			WDT - Wh-determiner
			WP - Wh-pronoun
			WP$ - Possessive wh-pronoun (prolog version WP-S)
			WRB - Wh-adverb
		 */
        //if the word belongs to any of the above categories, convert it to lower case and return
        if (!(pos.startsWith("N") || pos.startsWith("V") || pos.startsWith("J") || pos.startsWith("R"))
                || pos.startsWith("NNP")) {
            return word.toLowerCase();
        }

        String res = word.toLowerCase();

        //convert is, are, were, was -> be
        if (res.equals("is") || res.equals("are") || res.equals("were")
                || res.equals("was")) {
            res = "be";
        } else {
            try {
                //this uses wordnet to get the base form of the word
                IndexWord iw;
                char firstLetterOfPosTag = pos.charAt(0);

                switch(firstLetterOfPosTag){
                    /*
                        Possible tags for Verb:
                        VB - Verb, base form
                        VBD - Verb, past tense
                        VBG - Verb, gerund or present participle
                        VBN - Verb, past participle
                        VBP - Verb, non-3rd person singular present
                        VBZ - Verb, 3rd person singular present
                     */
                    case 'V':   iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(
                            POS.VERB, res);
                                break;
                    /*
                        Possible tags for Noun:
                        NN - Noun, singular or mass
                        NNS - Noun, plural
                        NNP - Proper noun, singular
                        NNPS - Proper noun, plural
                     */
                    case 'N':
                                Dictionary dict = Dictionary.getInstance();
                                MorphologicalProcessor mp = dict.getMorphologicalProcessor();
                                iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(
                            POS.NOUN, res);
                                break;
                    /*
                        Possible tags for adjective:
                        JJ - Adjective
                        JJR - Adjective, comparative
                        JJS - Adjective, superlative
                     */
                    case 'J':   iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(
                            POS.ADJECTIVE, res);
                                break;
                    /*
                        Possible tags for Adverb:
                            RB - Adverb
                            RBR - Adverb, comparative
                            RBS - Adverb, superlative
                            RP - Particle
                     */
                    case 'R':    iw = Dictionary.getInstance().getMorphologicalProcessor().lookupBaseForm(
                            POS.ADVERB, res);
                                    break;
                    default: iw=null;
                }
                if (iw == null) return res;
                res = iw.getLemma();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res;
    }

}
