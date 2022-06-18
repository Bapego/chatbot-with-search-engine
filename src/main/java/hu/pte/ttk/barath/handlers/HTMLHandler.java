package hu.pte.ttk.barath.handlers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HTMLHandler extends KeyWordHandler{
    private ArrayList<String> allValidLinkList = new ArrayList<String>();
    private String url;
    private DBHandler dbh;
    private String validLinkText;
    private String link;
    private int rec = 0;

    /**
     * Már az objektum létrehozásával lefut az összes szükséges metódus. A weboldalról kiolvassa az összes kért adatot és frissíti a táblát, ha esetleg új adat keletkezett volna.
     * @param url
     * @param tableName
     * @throws Exception
     */
    public HTMLHandler(String url, String tableName) throws Exception {
        dbh = new DBHandler(tableName);
        this.url = url;
        allValidLinkList.add(url);
        //dbh.deleteAllData();
        readData(url);
    }

    /**
     * Ezen metódus segítségével olvassa ki a program az adott url-ből a megadott adatokat. Az adatokat a következőképpen tárolom:
     * Ha egy szöveghez tartozik link, vagyis a HTML kódban az <a> tagen belül van a szöveg, a href utáni szöveget az answer oszlopba mentem, míg a hozzá tartozó, a honlapon megjelenő szöveget a question oszlopba. Ennek a prioritása 2. Minél nagyobb egy válasz prioritása, annál előrébb sorolom, mikor kilistázásra kerül a felhasználónak.
     * A weboldalon található szimpla szöveg a question oszlopba, míg a szöveget tartalmazó weboldal linkje az answer oszlopba kerül. Előtte viszont a szövegből kitörlöm azon szövegrészleteket, amikhez tartozik link.
     * Ezen metódus rekurzívan lefut, azon linkekre, amik megfelelnek a validLink függvény által állőállított feltételeknek. Ha nem felel meg, például egy kép esetében, a kép címét és a kép url-jét elmenti, de nem próbálja lefuttatni ezt a metódust a kép url-jére.
     * @param url
     */
    private void readData(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            //Kiszedjükazon sorokat a doc-ból, ami tartalmaz linket
            doc.select("a[href]").remove();
            String docText = toKeyString(doc.body().text());
            if(!dbh.existInTable(docText, url)){
                if(url.contains("hirek") || url.contains("hirarchivum")|| url.contains("galeria") || url.contains("archivum")) dbh.insert(docText,url, 1);
                else dbh.insert(docText,url,2);
                    System.out.println(docText + " - " + url);
                    System.out.println(docText.length());
            }

            for (Element eLink : links) {
                validLinkText = toKeyString(eLink.text());
                link = eLink.attr("abs:href");
                //Kiszűrjük azokat a linkeket, amikhez nem tartozik szöveg
                if(!validLinkText.equals(""))
                {
                    //Ha nem létezik már ilyen QA páros az AB-ban
                    if(!dbh.existInTable(validLinkText, link)){
                            dbh.insert(validLinkText,link,1);
                            System.out.println(validLinkText +" - " + link);
                    }
                }
                if(validLink(link))
                {
                    if(!allValidLinkList.contains(link))
                    {
                        allValidLinkList.add(link);

                        //Rekurzió
                        System.out.println(rec++);
                        readData(link);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * //Megvizsgálja, hogy az adott link valid-e vagyis a pte.ttk-hoz tartozik-e és, hogy nem file-e vagy nincs-e benne anchor link
     * Ezen ívül kiszűrjük egyelőre az angol linkeket is
     * @param link
     * @return valid link
     */
    private Boolean validLink (String link) {
        //2019.04.27.-én egy joido.ttk.pte.hu olalt csináltak amit kiszűrök
        //Ezen kívül kiszűröm a képeket és egyéb - elmondom hogy sok mellékoldalt nem vettem bele
        if (link.contains("www.ttk.pte.hu") && !link.contains(".png") && !link.contains(".jpg") && !link.contains(".jpeg") &&
        !link.contains("/files/") && !link.contains("#") || link.contains("mii.ttk.pte.hu") )
            // Kiszűröm az TTk angol oldalait
            if (!link.contains("/en/")){
                return true;
            }
        return false;
    }
}