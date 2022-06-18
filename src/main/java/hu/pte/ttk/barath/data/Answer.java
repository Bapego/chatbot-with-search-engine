package hu.pte.ttk.barath.data;

public class Answer {
    private int id;
    private String answer;
    private int priority;

    /**
     * Egyszerű konstruktor.
     */
    public Answer(){}

    /**
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     *
     * @param answer válasz
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     *
     * @return priority: prioritás
     */
    public int getPriority() {
        return priority;
    }

    /**
     *
     * @param priority prioritás
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Set, minden adathoz.
     * @param id azonosító
     * @param answer válasz szövege
     * @param priority prioritási szint
     */
    public void setAllData(int id, String answer, int priority) {
        this.priority = priority;
        this.answer = answer;
        this.id = id;
    }
}
