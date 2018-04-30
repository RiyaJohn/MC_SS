package SentenceSimplification;

import arkref.parsestuff.TregexPatternFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionGenerator {

    public List<QuestionAnswer> generateQuestions(List<Question> sentenceParseTrees){

        List<QuestionAnswer> questionAnswers = new ArrayList<>();

        Map<String, String> tregexAndWhWordList = AnalysisUtilities.getTregexFromFile("C:/Users/Sharanya R C/IdeaProjects/MachineComprehension/src/main/resources/tregex.txt");
        for(Question q : sentenceParseTrees){
            questionAnswers.addAll(generateHowQuestions(q.getSourceTree()));
            questionAnswers.addAll(generateHowQuestions(q.getIntermediateTree()));
            questionAnswers.addAll(generateWhyQuestions(q.getSourceTree()));
            questionAnswers.addAll(generateWhyQuestions(q.getIntermediateTree()));
            Tree superSenseTags = getSuperSenseTagTree(q.getIntermediateTree());
            for(Map.Entry<String,String> tregexAndWhWord : tregexAndWhWordList.entrySet()) {
                questionAnswers.addAll(generateWhereWhoQuestions(q.getIntermediateTree(), superSenseTags, tregexAndWhWord.getKey(), tregexAndWhWord.getValue()));
            }
        }
        return questionAnswers;
    }

    Tree getSuperSenseTagTree(Tree sentenceTree){
        List<String> tags = SuperSenseWrapper.getInstance().annotateSentenceWithSupersenses(
                sentenceTree);

        Tree ssTree = sentenceTree.deepCopy();
        List<Tree> leaves = ssTree.getLeaves();

        for (int i = 0; i < leaves.size(); i++) {
            leaves.get(i).setValue(tags.get(i));
        }
        return ssTree;
    }

    private List<QuestionAnswer> generateHowQuestions(Tree parseTree){

        List<QuestionAnswer> questionAnswers = new ArrayList<>();

        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree parentNode;

        tregexOpStr = "/^RB.*/ = adverb [$+ /^RB.*/ >> (ADVP  $ /^VB.*/ ) >> VP | >: (ADVP  $ /^VB.*/ ) >> VP]| ADVP = adverbphrase ($ /^VB.*/| $ VP)";
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(parseTree);

        while(matcher.find()){
            oldNode = matcher.getNode("adverb");
            if(oldNode == null){
                oldNode = matcher.getNode("adverbphrase");
            }
            answerNode = oldNode.deepCopy();
            List<Tree> answerList = answerNode.getLeaves();
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

    private List<QuestionAnswer> generateWhyQuestions(Tree parseTree){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        questionAnswers.addAll(replaceBecauseClause(parseTree));
        questionAnswers.addAll(replaceInOrderTo(parseTree));
        questionAnswers.addAll(replaceLest(parseTree));
        questionAnswers.addAll(replaceSoThat(parseTree));
        return questionAnswers;
    }

    /*
        Replace the clause containing because by why.
        But do not replace the clause if it contains "just because"
        "John studied because he wanted a good grade." --> "Jogn studied why"
        "Just because he's rich, doesn't mean he is happy." ---> X
     */
    private List<QuestionAnswer> replaceBecauseClause(Tree parseTree){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree parentNode;

        tregexOpStr = "/^*/=replace ( < (/^*/ < /[b|B]ecause/) & !< ((/^*/ < /[j|J]ust/) $+ (/^*/ < because)))";
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(parseTree);


        while(matcher.find()){
            oldNode = matcher.getNode("replace");
            answerNode = oldNode.deepCopy();
            List<Tree> answerList = answerNode.getLeaves();
            StringBuilder answer = new StringBuilder();
            for(Tree t: answerList){
                answer.append(t.toString()+" ");
            }
            int numChildren = answerNode.numChildren();
            while(numChildren > 0)
                answerNode.removeChild(--numChildren);
            answerNode.setValue("why");
            parentNode = oldNode.parent(parseTree);
            int nodeNumber = parentNode.objectIndexOf(oldNode);
            parentNode.setChild(nodeNumber,answerNode);
            String questionString = AnalysisUtilities.getQuestionString(parseTree);
            questionAnswers.add(new QuestionAnswer(questionString, answer.toString()));
            parentNode.setChild(nodeNumber,oldNode);
        }
        return questionAnswers;
    }

    /*
            checks for in order to and in order that in the middle of a sentence.
            This may not always form a subordinate clause.
            Hence, we use a regular expression to extract the part of the sentence after "in order to" or "in order that"
            Since both of them may appear at the beginning of the sentence,
            we extract the clause till a comma or period is reached.
            E.g. "He left his job in order to study." --> "He left his job why" "in order to study."
                 "In order to be early, he woke up at 5." --> "why he woke up at 5" "In order to be early,"
         */
    private List<QuestionAnswer> replaceInOrderTo(Tree parseTree){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        String sentence = AnalysisUtilities.getQuestionString(parseTree);
        Pattern pattern = Pattern.compile("[i|I]n\\sorder\\s[to|that][^,|.]*[,|\\.]");
        Matcher pmatcher = pattern.matcher(sentence);
        while(pmatcher.find()) {
            int matchStart = pmatcher.start();
            int matchEnd = pmatcher.end();
            String answer = sentence.substring(matchStart,matchEnd);
            int start = (matchStart > 0)?matchStart-1:0;
            int end = (matchEnd+1)<sentence.length()?matchEnd+1:sentence.length();
            String question = sentence.substring(0,start) + " why " + sentence.substring(end, sentence.length());
            questionAnswers.add(new QuestionAnswer(question,answer));
        }
        return questionAnswers;
    }
    private List<QuestionAnswer> replaceLest(Tree parseTree){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree parentNode;

        tregexOpStr = "/^*/=replace < (/^*/ < /[l|L]est/)";
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(parseTree);

        while(matcher.find()){
            oldNode = matcher.getNode("replace");
            answerNode = oldNode.deepCopy();
            List<Tree> answerList = answerNode.getLeaves();
            StringBuilder answer = new StringBuilder();
            for(Tree t: answerList){
                answer.append(t.toString()+" ");
            }
            int numChildren = answerNode.numChildren();
            while(numChildren > 0)
                answerNode.removeChild(--numChildren);
            answerNode.setValue("why");
            parentNode = oldNode.parent(parseTree);
            int nodeNumber = parentNode.objectIndexOf(oldNode);
            parentNode.setChild(nodeNumber,answerNode);
            String questionString = AnalysisUtilities.getQuestionString(parseTree);
            questionAnswers.add(new QuestionAnswer(questionString, answer.toString()));
            parentNode.setChild(nodeNumber,oldNode);
        }
        return questionAnswers;
    }

    private List<QuestionAnswer> replaceSoThat(Tree parseTree){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        String tregexOpStr;
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree parentNode;

        tregexOpStr = "SBAR=replace < ((RB < /[s|S]o/)  $+ (IN < that))";
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(parseTree);

        while(matcher.find()){
            oldNode = matcher.getNode("replace");
            answerNode = oldNode.deepCopy();
            List<Tree> answerList = answerNode.getLeaves();
            StringBuilder answer = new StringBuilder();
            for(Tree t: answerList){
                answer.append(t.toString()+" ");
            }
            int numChildren = answerNode.numChildren();
            while(numChildren > 0)
                answerNode.removeChild(--numChildren);
            answerNode.setValue("why");
            parentNode = oldNode.parent(parseTree);
            int nodeNumber = parentNode.objectIndexOf(oldNode);
            parentNode.setChild(nodeNumber,answerNode);
            String questionString = AnalysisUtilities.getQuestionString(parseTree);
            questionAnswers.add(new QuestionAnswer(questionString, answer.toString()));
            parentNode.setChild(nodeNumber,oldNode);
        }
        return questionAnswers;
    }

    private List<QuestionAnswer> generateWhereWhoQuestions(Tree parseTree, Tree superSenseTags, String tregexOpStr, String whQuestionPhrase){
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        TregexPattern matchPattern;
        TregexMatcher matcher;
        Tree answerNode;
        Tree oldNode;
        Tree oldNodeInSST;
        Tree parentNode;
        matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
        matcher = matchPattern.matcher(superSenseTags);
        System.out.println(parseTree.getLeaves());
        System.out.println(superSenseTags.getLeaves());
        while(matcher.find()){
            oldNodeInSST = matcher.getNode("replace");
            if(oldNodeInSST!=null) {
                int nodeNumber = oldNodeInSST.nodeNumber(superSenseTags);
                oldNode = parseTree.getNodeNumber(nodeNumber);
                answerNode = oldNode.deepCopy();
                List<Tree> answerList = answerNode.getLeaves();
                StringBuilder answer = new StringBuilder();
                for (Tree t : answerList) {
                    answer.append(t.toString() + " ");
                }
                int numChildren = answerNode.numChildren();
                while (numChildren > 0)
                    answerNode.removeChild(--numChildren);
                answerNode.setValue(whQuestionPhrase);
                parentNode = oldNode.parent(parseTree);
                int nodeNumber2 = parentNode.objectIndexOf(oldNode);
                parentNode.setChild(nodeNumber2, answerNode);
                String questionString = AnalysisUtilities.getQuestionString(parseTree);
                questionAnswers.add(new QuestionAnswer(questionString, answer.toString()));
                parentNode.setChild(nodeNumber2, oldNode);
            }
        }
        return questionAnswers;
    }
}