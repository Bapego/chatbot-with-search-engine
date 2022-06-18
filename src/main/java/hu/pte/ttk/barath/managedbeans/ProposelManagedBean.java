package hu.pte.ttk.barath.managedbeans;

import hu.pte.ttk.barath.handlers.DBHandler;
import hu.pte.ttk.barath.handlers.KeyWordHandler;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@ManagedBean
@ApplicationScoped
public class ProposelManagedBean extends KeyWordHandler {

    private String link;
    private ArrayList<String> keywordList;
    private String keywords;
    private Boolean ok = false;
    private DBHandler db;

    /**
     * Konstruktor. Egy DBHandler példányt hoz létre.
     * @throws IOException
     */
    public ProposelManagedBean() throws IOException {
        super();
        db = new DBHandler("qa");
    }

    /**
     * Visszaadja a beírt kulcsszavak listáját.
     * @return
     */
    public ArrayList<String> getKeywords() {
        return keywordList;
    }

    /**
     * Beállíthatja a kulcsszólistát.
     * @param keywordList
     */
    public void setKeywords(ArrayList<String> keywordList) {
        //System.out.println(keywordList);
        this.keywordList = keywordList;
        keywords = listToString(keywordList);
    }

    /**
     * Visszaadja a beírt linket.
     * @return
     */
    public String getLink() {
        return link;
    }

    /**
     * Beállítja a linket.
     * @param link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Az ok változót adja vissza. Az ok változó a hibaüzenet megjelenéséért felel.
     * @return
     */
    public Boolean getOk() {
        return ok;
    }

    /**
     * Az ok változót állítja be.
     * @param ok
     */
    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    /**
     * A ’Mentés’ gomb megnyomása után lefutó metódus. Elmenti a beírt értékeket az adatbázisba.
     */
    public void saveToDB() throws SQLException {
        db.insert(keywords, link, 0);

        keywordList = null;
        link = null;
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Sikeres mentés!"));
        setOk(true);
    }
}
