package hu.pte.ttk.barath.chatbot;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.rivescript.Config;
import com.rivescript.RiveScript;
import com.rivescript.lang.Perl;
import hu.pte.ttk.barath.data.Answer;
import hu.pte.ttk.barath.handlers.DBHandler;
import hu.pte.ttk.barath.handlers.KeyWordHandler;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;


public class ChatBot extends KeyWordHandler {
    private RiveScript bot;
    private DBHandler dbh;

    /**
     * Az osztály konstruktora, megnyitj a arive file-t és a perl fájlt, a makrók használatához
     * @throws Exception Ha nem sikerül megnyitni a file-t
     */
    public ChatBot() throws IOException {
        super();
        dbh = new DBHandler("qa");
        String string = "";
        OutputStream outputStream = null;
        try {

            bot = new RiveScript(Config.utf8());
            InputStream stream=this.getClass().getResourceAsStream("/rivescript/ttkBot.rive");
            ByteSource byteSource = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return stream;
                }
            };
            String text = byteSource.asCharSource(Charsets.UTF_8).read();
            bot.stream(text);

            // Interpreter
            InputStream stream2=this.getClass().getResourceAsStream("/lang/rsp4j.pl");
            System.out.println(stream2.read());
            byte[] buffer2=new byte[stream2.available()];
            File file2=new File("src/main/resources/rsp4j.pl");
            OutputStream outputStream2=new FileOutputStream(file2);
            outputStream2.write(buffer2);
            bot.setHandler("perl", new Perl(file2.getAbsolutePath()));
            // Java-ban definiálunk egy objektum makrót
            bot.setSubroutine("javatest", new JavaMacros());

            //Válasz adása
            bot.sortReplies();

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Egy stringre, string választ adó függvény
     * @param msg - üzenet
     * @return válasz
     */
    public String getRiveReply(String msg) {
        return bot.reply("user", msg);
    }

    /**
     * Adott üzenetre ad választ.
     * @param msg üzenet
     * @return Egy elemű válaszlista
     */
    public ArrayList<Answer> getRiveAnswer(String msg) {
        String riveReply = bot.reply("user", msg);
        ArrayList<Answer> answerList = new ArrayList<>();
        Answer answer = new Answer();

        answer.setAllData(0,riveReply,1);
        answerList.add(answer);
        return answerList;
    }

    /**
     * Válasz listát azon adatbázisban llévő aatokról, amelyek tartalmazzás a msg-t
     * @param msg Ezt kell tartalmaznia a dokumentumnak.
     * @return Válaszlista.
     * @throws IOException Ha nem sikerül megnyitni az adatbázist
     */
    public ArrayList<Answer> getReplyList(String msg) throws IOException, SQLException {
        ArrayList<Answer> answerList;

        answerList = dbh.allReply(msg);
        return answerList;
    }

    /**
     * A megadott szint-szavakszama(msg)-hez képest veszi a msg-ben lévő szavak kombinációját, majd
     * lekérdezés parancsot készít belőlük és lekérdezi őket.
     * @param level Milyen szinten van a QAManagedBeans-ben a keresés
     * @param msg Az üzenet
     * @return A válaszok listája.
     * @throws SQLException Lkérdezés hiba.
     * @throws IOException Adatbázis megnyitás hiba.
     */
    public ArrayList<Answer> getReplyContain(int level, String msg) throws SQLException, IOException {
        ArrayList<Answer> answerList;
        msg=msg.toLowerCase();
        ArrayList<String> msgWords= stringToList(msg);

        answerList = dbh.allContainReply(level,"question",msgWords);

        return answerList;
    }
}
