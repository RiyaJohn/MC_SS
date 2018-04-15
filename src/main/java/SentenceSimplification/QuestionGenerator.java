package SentenceSimplification;

import arkref.parsestuff.TregexPatternFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.util.ArrayList;
import java.util.List;

public class QuestionGenerator {

    public List<QuestionAnswer> generateQuestions(List<Question> sentenceParseTrees){

        List<QuestionAnswer> questionAnswers = new ArrayList<>();

        for(Question q : sentenceParseTrees){
            questionAnswers.addAll(generateHowQuestions(q.getSourceTree()));
            //questionAnswers.addAll(generateHowQuestions(q.getIntermediateTree()));
        }
        return questionAnswers;
    }

    public List<QuestionAnswer> generateHowQuestions(Tree parseTree){

        List<QuestionAnswer> questionAnswers = new ArrayList<>();

        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree parentNode;

        tregexOpStr = "/^RB.*/ = adverb [$+ /^RB.*/ >> (ADVP  $ /^VB.*/ ) >> VP | >: (ADVP  $ /^VB.*/ ) >> VP]";
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(parseTree);


        while(matcher.find()){
            oldNode = matcher.getNode("adverb");
            answerNode = oldNode.deepCopy();
            List<Tree> answerList = answerNode.getChildrenAsList();
            StringBuilder answer = new StringBuilder();
            for(Tree t: answerList){
                answer.append(t.toString()+" ");
            }
            int numChildren = answerNode.numChildren();
            while(numChildren > 0)
                answerNode.removeChild(--numChildren);
            answerNode.setValue("how");
            parentNode = oldNode.parent(parseTree);
            int nodeNumber = parentNode.objectIndexOf(oldNode);
            parentNode.setChild(nodeNumber,answerNode);
            String questionString = AnalysisUtilities.getQuestionString(parseTree);
            questionAnswers.add(new QuestionAnswer(questionString, answer.toString()));
            parentNode.setChild(nodeNumber,oldNode);
        }
        return questionAnswers;
    }
}
