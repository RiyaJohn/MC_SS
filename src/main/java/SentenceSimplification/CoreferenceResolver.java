package main.java.SentenceSimplification;

import arkref.analysis.FindMentions;
import arkref.analysis.RefsToEntities;
import arkref.analysis.Resolve;
import arkref.data.Document;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

public class CoreferenceResolver {

    private Document doc;

    public void resolveCorefence(List<Tree> originalParseTrees){

        List<Tree> trees = new ArrayList<>();

        List<String> entityStrings = new ArrayList<>();

        for (Tree t : originalParseTrees) {


            System.out.println("Original tree");
            t.pennPrint();
            Document.addNPsAbovePossessivePronouns(t);
            System.out.println("After adding noun phrases above possessive pronouns");
            t.pennPrint();
            Document.addInternalNPStructureForRoleAppositives(t);
            System.out.println("after internal np structure for role appositives");
            t.pennPrint();
            trees.add(t);
            /*entityStrings.add(convertSupersensesToEntityString(
                    t,
                    SuperSenseWrapper.getInstance().annotateSentenceWithSupersenses(
                            t)));*/
        }

        doc = new Document(trees, entityStrings);

        FindMentions.go(doc);
        Resolve.go(doc);
        RefsToEntities.go(doc);
    }
}
