package hu.pte.ttk.barath.data;
import java.util.ArrayList;

// Question-Answers
public class QA {

    private long id;
    private String question;
    private ArrayList<Answer> answerList = new ArrayList<>();

    /**
     * Q - Question
     * A - Answers
     * Üres konstruktor
     */
    public QA(){}

    /**
     * Visszaadja az adott példányhoz tartozó kérdést
     * @return A kérdés
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Beállíthatjuk az adott példányhoz tartozó kérdést
     * @param question A kérdés
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Megkapjuk az adott példányhoz tartozó id-t
     * @return id azonosító
     */
    public long getId() {
        return id;
    }

    /**
     * Beállítjuk az adott példányho tartozó id-t
     * @param id azonosító
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Az Answer tíousú answer listát adja vissza.
     * @return válasz
     */
    public ArrayList<Answer> getAnswerList() {
        return answerList;
    }

    /**
     * Beállíthatjuk az Answer típusú answer listáts
     * @param answerList válaszlista
     */
    public void setAnswerList(ArrayList<Answer> answerList) {
        this.answerList = answerList;
    }
}
