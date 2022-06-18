package hu.pte.ttk.barath.managedbeans;

import hu.pte.ttk.barath.chatbot.ChatBot;
import hu.pte.ttk.barath.data.Answer;
import hu.pte.ttk.barath.data.QA;
import hu.pte.ttk.barath.handlers.DBHandler;
import hu.pte.ttk.barath.handlers.KeyWordHandler;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Az chatbot.xhtml oldalunk kezelőosztálya.
 */
@ManagedBean
@ViewScoped
public class QAManagedBean extends KeyWordHandler {

    private ArrayList<QA> qaList = new ArrayList<>();
    private QA selectedQA;
    private long id;
    private int level;
    private Boolean contain;
    private ChatBot bot;
    private ArrayList<Answer> allReply;
    private String question = "";
    private Boolean moreMsg = false;
    private Boolean rive = false;
    private ArrayList<Answer> tooMuch;
    private int maxMsg = 100;

    /**
     * Az osztály konstruktora. Alaphelyzetbe állítja a változókat.
     * @throws IOException
     */
    public QAManagedBean() throws Exception {
        super();
        bot = new ChatBot();
        tooMuch= new ArrayList<>();
        Answer ans = new Answer();
        ans.setAllData(0,"Túl sok válaszlehetőség, kérem pontosítson a kulcsszavakon", 0);
        tooMuch.add(ans);
    }

    /**
     * Példányosítás után lefutó metodus. A kezdő üzenetet írja ki
     */
    @PostConstruct
    public void init() {
        if(qaList.isEmpty()) {
            rive = true;
            selectedQA = new QA();
            id = 0;
            qaList = new ArrayList<>();
            level = 0;
            contain = false;
            allReply = new ArrayList<>();
            String startMsg = "Üdv! Én TTkBot vagyok a PTE TTK weblap kisegítő asszisztese. Minden kérdésedre megpróbálok a legjobban tudásom szerint válaszolni." +
                    " Csak írd be a kulcsszavakat, amelyeket alapján ki tudom neked listázni a megfelelő weboldalakat. A listában" +
                    " legalul vannak a legjobbnak vélt találatok de ha feljebb görgetsz találsz még linkeket. Ha elégedett vagy egy találattal, kérlek" +
                    " szavazz rá!";
            Answer a = new Answer();

            a.setAllData(0, startMsg, 100);
            allReply.add(a);
            selectedQA.setAnswerList(allReply);
            qaList.add(selectedQA);
            selectedQA = new QA();
            allReply = new ArrayList<>();
            moreMsg = false;
        }
    }

    /**
     * A felhasználó a 'Kérdés...' inputba beírt szövegére próbál a metódus választ adni. A Chatbot osztály egy példánya segítségével először a rive fájlban keres, majd az adatbázisban, de ott is először a getReplyList metódus segítségével a szavak elhelyezkedésének figyelembevételével, majd, ha nincs találat, a figyelembevétele nélkül, a getReplyContain metódus segítségével. Majd, ha akkor sincs találat, meghívja a plusLevel metódust. @param qa QA példány
     * Természetesen a legelső, hogy a Rivescript-ben, a bot agyában keres egyezést. Ha nincs, az azt jelenti, hogy a felhasználó valószínűleg keresni szeretne valamit.
     * Az algoritmus egyre magasabb szinteken keres. Minél magasabb szinten van a keresés, annál „engedékenyebb” a program. De ezeken a szinteken belül is két alszintet különböztetek meg. Az első, ahol figyelembe veszi a szavak sorrendjét (getReplyList metódus), a második, ahol nem (getReplyContain metódus). Az adott szint értéke adja meg az első esetben, hogy mennyi szót kell kihagyjunk a keresésből. Más szóval (szószám-szint-1) adja meg, hogy mennyi szónak kell egymás mellett szerepelnie. Míg a második esetben azt jelenti a szint értéke, hogy mennyi szónak kell feltétlenül benne lennie a dokumentumban.
     * Az adott szinten lévő összes kritériumnak megfelelő találatot kiír a felhasználónak. Ekkor a van lehetőség a ’További találatok’ gombbal a további eredményeket keresni. A szintnövelés viszont abban az esetben automatikus, ha az adott szinten nem volt találata a keresésnek. Azokat az eredményeket, amiket megtalált már a program, nem listázza ki újból és nem veszi figyelembe.
     * @throws IOException
     * @throws SQLException
     */
    public void response(QA qa) throws IOException, SQLException {
        level = 0;
        allReply = new ArrayList<>();

        //Adunk neki egy id-t
        qa.setId(idService());

        //Kérdést elküldjük feldolgozásra
        question = selectedQA.getQuestion();
        String keyQuestion = toKeyString(question);
        qa.setQuestion(question);
        //Először megpróbáljuk, hogy sikerül-e az aggyal kommunikálni
        if(question == "" || !bot.getRiveReply(question).equals(bot.getRiveReply("*"))) {
            rive = true;
            allReply = bot.getRiveAnswer(question);
            qa.setAnswerList(allReply);

            //hozzáadjuk a QAlisthez
            qaList.add(qa);
            //új példányt kreálunk a selectedQA-ból, hogy a következő QA-t is fel tudjuk vinni a listába
            selectedQA = new QA();
            moreMsg = false;
        }
        else {
            //Hozzáadjuk a válaszok listáját miután feldolgoztuk azt
            allReply = bot.getReplyList(keyQuestion);
            if(allReply.isEmpty())
            {
                rive = false;
                if(wordCount(keyQuestion)>1) {
                    contain = false;
                    allReply = bot.getReplyContain(level, keyQuestion);
                    if (allReply.size() < maxMsg) {
                        if (!allReply.isEmpty()) {
                            qa.setAnswerList(allReply);
                            qaList.add(qa);
                            selectedQA = new QA();
                            okPlusReplys();
                            level++;

                            System.out.println("1/2: START");
                            printAnswerList(allReply);
                            System.out.println("1/2: END");
                        } else {
                            level++;
                            plusLevel(qa);
                        }
                    } else {
                        qa.setAnswerList(tooMuch);
                        qaList.add(qa);
                        selectedQA = new QA();
                        rive = true;
                    }
                } else {
                    rive = true;
                    allReply = bot.getRiveAnswer("*");
                    qa.setAnswerList(allReply);

                    qaList.add(qa);
                    selectedQA = new QA();
                }
            }
            else {
                contain = true;
                rive = false;
                if(allReply.size() < maxMsg) {

                    allReply = noDuplicateElement(allReply);
                    //Sorba tesszük prioritás szerint
                    orderByPriority(allReply);
                    qa.setAnswerList(allReply);
                    qaList.add(qa);

                    System.out.println("1/1: START");
                    printAnswerList(allReply);
                    System.out.println("1/1: END");

                    selectedQA = new QA();
                    okPlusReplys();
                } else {
                    qa.setAnswerList(tooMuch);
                    qaList.add(qa);
                    selectedQA = new QA();
                    rive = true;
                }
            }
        }

    }

    /**
     * -	A 'További találatok' gomb megnyomásával aktiváljuk ezt a metódust. Minden gombnyomásra egy újabb szinttel mélyebben keres. A már kiadott találatokat nem mutatja meg újra.
     * @param qa QA példány
     * @throws IOException
     * @throws SQLException
     */
    public void plusLevel(QA qa) throws IOException, SQLException {
        //System.out.println("Szint:" + level);
        String keyQuestion = toKeyString(question);
        if(level < wordCount(keyQuestion)) {
            if (contain == false) {

                ArrayList<String> vagdalt = distributor(keyQuestion, level);
                System.out.println("VAGDALT:" + vagdalt);
                ArrayList<Answer> vReplyList = new ArrayList<>();

                for (int i = 0; i < vagdalt.size(); i++) {
                    vReplyList.addAll(bot.getReplyList(vagdalt.get(i)));
                    vReplyList = noDuplicateElement(vReplyList);
                    System.out.println(level + 1 + "BENNE" + i);
                    printAnswerList(vReplyList);
                }
                if (vReplyList.size() < maxMsg) {
                    System.out.println(level + 1 + "START");
                    printAnswerList(allReply);
                    System.out.println(allReply.size());
                    System.out.println(level + 1 + "START");
                    printAnswerList(vReplyList);
                    System.out.println(vReplyList.size());
                    vReplyList = removeAllSame(vReplyList, allReply);
                    System.out.println(level + 1 + "END");
                    printAnswerList(allReply);
                    System.out.println((allReply.size()));
                    System.out.println(level + 1 + "END");
                    printAnswerList(vReplyList);
                    System.out.println(vReplyList.size());
                    contain = true;

                    if (!vReplyList.isEmpty()) {
                        qa.setQuestion("További találatok erre: " + question);
                        allReply.addAll(vReplyList);

                        vReplyList = noDuplicateElement(vReplyList);

                        //Sorba tesszük prioritás szerint
                        orderByPriority(vReplyList);
                        //Ha nincsenek új eredmények
                        qa.setAnswerList(vReplyList);
                        qaList.add(qa);
                        selectedQA = new QA();
                        okPlusReplys();

                        System.out.println(level + 1 + "/1: START");
                        printAnswerList(allReply);
                        System.out.println(level + 1 + "/1: END");
                    } else {
                        plusLevel(qa);
                    }
                } else {
                    qa.setAnswerList(tooMuch);
                    qaList.add(qa);
                    selectedQA = new QA();
                    rive = true;
                }
            } else {
                contain = false;
                ArrayList<Answer> cReplyList = bot.getReplyContain(level, keyQuestion);
                if (cReplyList.size() < maxMsg) {
                    if (!cReplyList.isEmpty()) {
                        cReplyList = removeAllSame(cReplyList, allReply);
                        if (!cReplyList.isEmpty()) {
                            qa.setQuestion("További találatok erre: " + question);
                            allReply.addAll(cReplyList);
                            cReplyList = noDuplicateElement(cReplyList);
                            orderByPriority(cReplyList);
                            qa.setAnswerList(cReplyList);
                            qaList.add(qa);
                            selectedQA = new QA();

                            okPlusReplys();
                            level++;

                            System.out.println(level + 1 + "/2: START");
                            printAnswerList(allReply);
                            System.out.println(level + 1 + "/2: END");
                        } else {
                            level++;
                            plusLevel(qa);
                        }
                    } else {
                        level++;
                        plusLevel(qa);
                    }
                } else {
                    qa.setAnswerList(tooMuch);
                    qaList.add(qa);
                    selectedQA = new QA();
                    rive = true;
                }
            }
        }
        else {
            rive = true;
            allReply = bot.getRiveAnswer("*");
            qa.setAnswerList(allReply);
            qaList.add(qa);
            selectedQA = new QA();
            allReply=new ArrayList<>();
            okPlusReplys();
            level++;
        }
    }

    /**
     * -	Ez a metódus a 'További találatok' gomb megjelenéséért felelős. Ha nincs több szint, lehetőség a további válaszok találatára, nem engedi megjelenni a gombot.
     * @return
     */
    public void okPlusReplys() {
        if (level < wordCount(question) && wordCount(question)>1 && rive == false){
            moreMsg = true;
        }
        else moreMsg = false;
        System.out.println("EZ A GETMORE:"+getMoreMsg());
    }

    /**
     * -	Minden nem Rivescriptből kapott válasz esetén lehetőségünk van egy válasznak növelni a prioritását. Ha megnyomtuk a gombot ez a függvény fut le és a megfelelő id segítségével növeli az adatbázisban a megfelelő rekord priority értékét.
     * @param id
     * @throws IOException
     */
    public void plusPriority(Integer id) throws IOException, SQLException {
        DBHandler db = new DBHandler("qa");
        Integer priority = db.selectPriority("id",id.toString()) +1;
        System.out.println("prioritás és id " + priority + " " + id);
        db.update("priority",priority.toString(),"id",id.toString());
        Answer ans = new Answer();
        ArrayList<Answer> ansList = new ArrayList<>();
        ans.setAllData(0,"Köszönöm a visszajelzést!",100);
        ansList.add(ans);
        QA qa = new QA();
        qa.setAnswerList(ansList);
        qaList.add(qa);
    }

    /**
     * A id-t növelő metódus
     * @return
     */
    private long idService(){
        this.id++;
        return id;
    }

    /**
     * Az 'Üzenetek törlése' gomb megnyomására lefutó metódus. Kiüríti a megjelenítendő qaList-et és alaphelyzetbe állítja a változókat
     */
    public void deleteMsg(){
        qaList = new ArrayList<>();
        init();
    }

    /**
     * A válaszokba link felismeréséért felelős metódus
     * @param string
     * @return
     */
    public Boolean link(String string) {
        if(string.contains("https://")|| string.contains("http://") || string.contains("www.")){
            return true;
        }
        return false;
    }

    //Getterek és setterek

    /**
     * Visszakapjuk a qaList-et
     * @return qaList
     */
    public ArrayList<QA> getQAList() {
        return qaList;
    }

    /**
     * Beállíthatjuk a QAListet
     * @param qaList
     */
    public void setQAList(ArrayList<QA> qaList) {
        this.qaList = qaList;
    }

    /**
     * Visszaadja a selectedQA-t.
     * @return
     */
    public QA getSelectedQA() {
        return selectedQA;
    }

    /**
     * Beállítja a selectedQA-t.
     * @param selectedQA
     */
    public void setSelectedQA(QA selectedQA) {
        this.selectedQA = selectedQA;
    }

    /**
     * Visszaadja a moreMsg értékét, ami 'További találatok' megjelenítéséért felel
     * @return
     */
    public Boolean getMoreMsg() {
        return moreMsg;
    }
}
