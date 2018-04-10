package SentenceSimplification;

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

            t.pennPrint();
            Document.addNPsAbovePossessivePronouns(t);
            t.pennPrint();
            Document.addInternalNPStructureForRoleAppositives(t);
            t.pennPrint();
            trees.add(t);
            entityStrings.add(convertSupersensesToEntityString(
                    t,
                    SuperSenseWrapper.getInstance().annotateSentenceWithSupersenses(
                            t)));
        }

        doc = new Document(trees, entityStrings);

        FindMentions.go(doc);
        Resolve.go(doc);
        RefsToEntities.go(doc);
    }

    private String convertSupersensesToEntityString(Tree t,
                                                    List<String> supersenses) {
        String res = "";

        List<String> converted = new ArrayList<String>();
        for (int i = 0; i < supersenses.size(); i++) {
            if (supersenses.get(i).endsWith("noun.person")) {
                converted.add("PERSON");
            } else {
                converted.add(supersenses.get(i));
            }
        }

        List<Tree> leaves = t.getLeaves();
        while (leaves.size() > converted.size())
            converted.add("0");
        for (int i = 0; i < leaves.size(); i++) {
            if (i > 0) res += " ";
            res += leaves.get(i) + "/" + converted.get(i);
        }

        return res;
    }
}
