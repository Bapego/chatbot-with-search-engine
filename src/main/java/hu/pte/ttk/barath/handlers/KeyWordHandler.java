package hu.pte.ttk.barath.handlers;

import hu.pte.ttk.barath.data.Answer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public abstract class KeyWordHandler{
    private BufferedReader nonKeyWordsFile;
    private String word;
    private ArrayList<String> nonKeyWords;
    private ArrayList<String> combList = new ArrayList<>();

    /**
     * Megnyitjuk a nem kulcsszavakat tartalmazó szótárt és eltároljuk azt a nonKeyWords ArrayList-be
     * @throws IOException
     */
    public KeyWordHandler(){
        try {
            nonKeyWords = new ArrayList<String>();
            //Megpróbáljuk megnyitni a szótárat
            nonKeyWordsFile = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/dictionary/nonKeyWords.txt"), "unicode"));

            //A szótár szavait eltároljuk egy listában
            for( word = nonKeyWordsFile.readLine(); word != null; word = nonKeyWordsFile.readLine() )
            {
                nonKeyWords.add(word.toLowerCase());
            }
            nonKeyWordsFile.close();

        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Hibás szótár megnyitás / KeyWordHandler konstruktor");
        }
    }

    /**
     * Egy stringből kivesz minden felesleges karaktert és nem olyat szót, ami kulcsszó lehet.
     * @param string
     * @return
     * @throws IOException
     */
    public String toKeyString (String string) throws IOException {
        //Minden kisbetű
        string = string.toLowerCase();

        // Megadott karakterek megtartása
        string = unnesCharDel(string);

        //Kulcsszavak megtartása
        string = removeNonKeyWords(string);

        // Felesleges space-ek kitörlése
        string = string.replaceAll("\\s+"," ");

        return string.trim();
    }

    /**
     * unnesCharDelete = unnecessary Characters Delete
     * A felesleges írásjeleket eltűnteti a stringből. Kivéve a @ és a . az e-mai lcímek megtartása miatt
     * @param string
     * @return
     */
    private String unnesCharDel (String string) {
        // Megadott karakterek megtartása
        string = string.replaceAll("[^A-Za-z 0-9öüóúőűáéíÖÜÓÚŐŰÁÉÍ@.]+", "").trim();
        string = string.replace("...", "").trim();
        return string;
    }

    /**
     * Kitörli a nem kulcsszavakat a stringből a nonKeyWords szótár alapján
     * @param string
     * @return
     * @throws IOException
     */
    private String removeNonKeyWords(String string) throws IOException {
        // Egyesével átnézzük, hogy az adott szó volt-e már
        ArrayList stringWords= stringToList(string);
        for (int i = 0; i < stringWords.size(); i++) {
            if(nonKeyWords.contains(stringWords.get(i)))
            {
                string = string.replaceAll("[^A-Za-z0-9öüóőúűáéíÍÖÜÓÚŐŰÁÉ]"+stringWords.get(i).toString() + " ", " ");
                string = string.replaceAll("[^A-Za-z0-9öüóőúűáéíÍÖÜÓÚŐŰÁÉ]"+stringWords.get(i).toString() + "$", " ");
                string = string.replaceAll(" "+stringWords.get(i).toString() + "[^A-Za-z0-9öüóőúűáéíÍÖÜÓÚŐŰÁÉ]", " ");
                string = string.replaceAll("^"+stringWords.get(i).toString() + "[^A-Za-z0-9öüóőúűáéíÍÖÜÓÚŐŰÁÉ]", " ");
            }
        }
        return string;
    }

    /**Egy stringet tartalmazó listát egy stringé alakít a szavakat space-el választja el
     *
     * @param list
     * @return
     */
    public String listToString(ArrayList<String> list) {
        String string = "";

        // Visszaírás string-be
        for (int i = 0; i < list.size(); i++) {
            string = string + list.get(i).trim() + " ";
        }
        return string.trim();
    }

    /**
     * Egy space-el elválasztott szavakból álló stringet a space-ek mentél felvágja és egy listába teszi
     * @param string
     * @return
     */
    public ArrayList<String> stringToList(String string) {
        return new ArrayList(Arrays.asList(string.split(" ")));
    }

    /**
     * Level db szóra vágja fel a stringet. Így akár több szó is kijöhet. De csak egymás melletti szavakkal dolgozik
     * pl.: alma kört répa cékla
     * ha level = 2
     * A lista 0. alma körte
     *         1. körte répa
     *         2. répa cékla
     * @param string
     * @param level
     * @return
     * @throws IOException
     */
    public ArrayList<String> distributor(String string, int level) throws IOException {
        ArrayList<String> disList = new ArrayList<String>();
        string = toKeyString(string);
        int wordCount = string.split("\\s+").length-level;
        ArrayList wordList = stringToList(string);

        int start = 0;
        for (int j = 0; j <= level; j++) {
            string = "";
            for (int i = start; i < wordCount+start; i++) {
                if(i != wordCount+start-1 && level != wordCount) string += wordList.get(i) + " ";
                else string += wordList.get(i);
            }
            start++;
            disList.add(string);
        }
        return disList;
    }

    /**
     * A listából az Answer példányokból álló listából a válasz szerint kiszedjük a duplikált elemeket
     * @param answerList
     * @return
     */
    public ArrayList<Answer> noDuplicateElement(ArrayList<Answer> answerList) {
        ArrayList<Answer> result = new ArrayList<Answer>();
        Set<String> reply = new HashSet<String>();
        for( Answer answer1 : answerList ) {
            if(reply.add(answer1.getAnswer()))
            {
                result.add(answer1);
            }
        }
        return result;
    }

    /**
     * Az Answer típusú listából sorbarendezzük az elemeket a prioritás szerint növekvő sorrenben
     * @param answerList
     */
    public void orderByPriority(ArrayList<Answer> answerList) {
        answerList.sort(Comparator.comparing(Answer::getPriority));
    }

    /**
     * Egy answer lista elemeit írja ki a képernyőre
     * @param answerList
     */
    public void printAnswerList(ArrayList<Answer> answerList) {
        System.out.print("A lista: ");
        for (int i = 0; i < answerList.size(); i++) {
            System.out.println(answerList.get(i).getId() + " - "
                    + answerList.get(i).getAnswer() + " - "
                    + answerList.get(i).getPriority());
        }
    }

    /**
     * Visszaadja, hogy egy strinben mennyi szó szerepel
     * @param string
     * @return
     */
    public int wordCount(String string) {
        String trim = string.trim();
        if(trim.isEmpty()) return 0;
        return trim.split("\\s+").length;
    }

    /**
     * A kombinációs függvény segésfüggvénye
     * @param arr
     * @param data
     * @param start
     * @param end
     * @param index
     * @param r
     * @return
     */
    public String[] combinationUtil(String arr[], String data[], int start, int end, int index, int r) {
        if (index == r)
        {
            String str = "";
            for (int j=0; j<r; j++){

                System.out.print(data[j]+" ");
                if(j+1==r)str+= data[j];
                else str+= data[j] + " ";

            }

            combList.add(str);
            System.out.println();
            return data;
        }

        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r);
        }

        return data;
    }

    /**
     * A bemenő lista szavain végez ismétlés nélküli kombinációt a megadott paraméterek alapján. Az n az össz szó darabszáma, az r pedig, hogy mennyit szeretnénk kiválasztani az összesből. Más szóval n alatt az r darab ismétlés nélküli kombinációt készít a függvény a megadott szavakból.
     * @param arl
     * @param n
     * @param r
     * @return
     */
    public ArrayList<String> combination(ArrayList<String> arl, int n, int r) {
        combList = new ArrayList<>();
        String arr[] = new String[arl.size()];
        for (int i = 0; i < arl.size(); i++) {
            arr[i] = arl.get(i);
        }
        // A temporary array to store all combination one by one
        String data[]=new String[r];

        // Print all combination using temprary array 'data[]'
        combinationUtil(arr, data, 0, n-1, 0, r);
        return combList;
    }

    /**
     * Ezt egy halmazműveletként lehet értelmezni. Az „a” nevű „halmazból” ki lesz vonva a „b” nevű „halmaz”.
     * @param a
     * @param b
     * @return
     */
    public ArrayList<Answer> removeAllSame(ArrayList<Answer> a, ArrayList<Answer> b){

        for (int i = 0; i < b.size(); i++) {
            for (int j = 0; j < a.size(); j++) {
                if (b.get(i).getAnswer().equals(a.get(j).getAnswer())) {
                    a.remove(j);
                }
            }
        }
        return a;
    }
}

