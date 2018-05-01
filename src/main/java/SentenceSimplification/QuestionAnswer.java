package SentenceSimplification;

import java.util.Objects;

public class QuestionAnswer {

    private String question;
    private String answer;
    private String sentence;

    public QuestionAnswer(String question, String answer){
        this.question = question;
        this.answer = answer;
    }
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSentence() {
        return sentence;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof QuestionAnswer)) {
            return false;
        }

        QuestionAnswer c = (QuestionAnswer) o;

        // Compare the data members and return accordingly
        return question.equals(c.getQuestion()) && answer.equals(c.getAnswer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answer);
    }
}
