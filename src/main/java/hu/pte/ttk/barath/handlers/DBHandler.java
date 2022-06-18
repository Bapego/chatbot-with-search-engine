package hu.pte.ttk.barath.handlers;

import hu.pte.ttk.barath.data.Answer;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class DBHandler extends KeyWordHandler{
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String databaseUrl;
    private String user = "oxlwkvspetpobl";
    private String password = "a6865ee415778c81ec358f8e24253e23853dd74bcefc902f1e69880e6ce252eb";
    private String tableName;
    private Statement statement;

    /**
     * Csatlakozik a paraméterekben megadott jelszóval és felhasználónévvel az adatbázishoz és beállítja a tábla nevét, amin ezek után dolgozni fog.
     * @param tableName táblanév
     * @throws IOException sikertelen csatlakozás esetén
     */
    public DBHandler(String tableName) throws IOException {
        super();
        this.tableName = tableName;
        try {
                databaseUrl = "jdbc:postgresql://ec2-54-197-234-117.compute-1.amazonaws.com:5432/dcqsm7seie5dbt?ssl=true&sslmode=require&sslfactory=org.postgresql.ssl.NonValidatingFactory";
                connection = DriverManager.getConnection(databaseUrl, user, password);
                statement = connection.createStatement();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ez a metódus egy Select * from tableName parancsnak felel meg. Az eredményeket kiírja a képernyőre.
     */
    public void selectAll() throws SQLException {
        try {

            preparedStatement = connection.prepareStatement("select * from "+tableName);

            resultSet = preparedStatement.executeQuery();

            printResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ez a metódus egy Select * from tableName where columName = param parancsnak felel meg. Az eredményeket kiírja a képernyőre.
     * @param columnName oszlopnév
     * @param param amit keresünk
     */
    public void select(String columnName, String param) throws SQLException {
        try {

            preparedStatement = connection.prepareStatement("select * from "+tableName+" where "+columnName+"=?");
            preparedStatement.setString(1,param);

            resultSet = preparedStatement.executeQuery();
            printResultSet(resultSet);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ezen függvény egy megadott rekord priority értékét adja vissza.
     * @param columnName oszlopnév
     * @param param keresett paraméter
     * @return prioritás
     */
    public int selectPriority(String columnName, String param) throws SQLException {
        int priority = 0;
        try {

            preparedStatement = connection.prepareStatement("select * from "+tableName+" where "+columnName+"="+param);

            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            priority = resultSet.getInt("priority");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return priority;
    }

    /**
     * A metódus egy insert parancsnak felel meg, ahol minden adatot megadunk.
     * @param question kérdés
     * @param answer válasz
     * @param priority prioritás
     */
    public void insert(String question, String answer, int priority) throws SQLException {
        try {
            statement.executeUpdate(String.format("insert into %s(question,answer, priority) values ('%s', '%s', %d)",tableName,question, answer, priority));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A metódussal a bemenő lista szavainak bizonyos kombinációja alapján készítek egy lekérést. A szavak száma mínusz a level értéke számmal lefuttatom a KeyWordHandler combination metódusát. Majd a visszakapott értékből készítem a lekérdezést.
     * Pl.: Az input: „alma körte banán, citrom”. Ha level=1, meghívom a combination(„alma, körte, banán, citrom”, 4, 3) függvényt a megadott paraméterekkel. Majd hozzáfűzök az eredményhez bizonyos karaktereket, hogy egybefűzve egy lekérdezés parancsot kapjak.
     * 	SELECT * FROM tableName WHERE REGEXP_LIKE (columnName,alma) and REGEXP_LIKE (columnName,körte) and REGEXP_LIKE (columnName,banán) or REGEXP_LIKE(columnName,alma) and REGEXP_LIKE (columnName,körte) and REGEXP_LIKE (columnName,citrom) or REGEXP_LIKE (columnName,alma) and REGEXP_LIKE (columnName,banán) and REGEXP_LIKE (columnName,citrom) or REGEXP_LIKE (columnName,körte) and REGEXP_LIKE (columnName,banán) and REGEXP_LIKE (columnName,citrom)
     * Majd a parancsot végrehajtja a függvény.
     * @param level szint
     * @param columnName oszlopnév
     * @param args keresett argumentum
     * @return a válasz lista
     * @throws SQLException ha nem sikerül a kiolvasás az ab-ból
     */
    public ArrayList<Answer> allContainReply(int level, String columnName, ArrayList<String> args) throws SQLException {
        ArrayList<Answer> answerList = new ArrayList<>();
        ArrayList<String> tmp;
        ArrayList<String> replyList = new ArrayList<>();
        ArrayList<String> combList;

        Answer answer = new Answer();
        System.out.println("szint " + level);
        int argSize = args.size();
        combList = combination(args,argSize,argSize-level);
        int combSize = combList.size();
        System.out.println("combSize:" + combSize);
        System.out.println("combList:" + combList);
        String string = "SELECT * FROM "+tableName+" WHERE "+columnName+" LIKE ";
        String or = " or "+columnName+" LIKE ";
        String and = " and "+columnName+" LIKE ";
        for (int i = 0; i < combSize; i++) {
            tmp = stringToList(combList.get(i));
            for (int j = 0; j < tmp.size(); j++) {
                if(i != combSize-1) {
                    if (j + 1 != tmp.size()) tmp.set(j, "'%" + tmp.get(j) + "%'" + and);
                    else tmp.set(j, "'%" + tmp.get(j) + "%'" + or);
                }
                else{
                    if (j + 1 != tmp.size()) tmp.set(j, "'%" + tmp.get(j) + "%'" + and);
                    else tmp.set(j, "'%" + tmp.get(j) + "%'");
                }
            }
            replyList.add(listToString(tmp));
            System.out.println("lista:" + replyList);
        }
        string += listToString(replyList);
        System.out.println("string"+string);

        preparedStatement = connection.prepareStatement(string);

        resultSet = preparedStatement.executeQuery();

        //Ebben az esetben kiszűrjük azokat a találatokat, amikben benne van a hírarcívum
        while (resultSet.next()) {
            if(!resultSet.getString("answer").contains("hirarchivum")) {
                answer.setAllData(resultSet.getInt("id"), resultSet.getString("answer"), resultSet.getInt("priority"));
                answerList.add(answer);
                answer = new Answer();
            }
        }


        //A duplikált elemeket kivesszük
        answerList = noDuplicateElement(answerList);

        /*for (int i = 0; i < answerList.size(); i++) {
            System.out.println(answerList.get(i).getAnswer());
        }*/
        return answerList;
    }

    /**
     * A keresési eredményeket menti el egy Answer típusú listában.
     * @param answerList válaszlista
     * @param answer válasz
     * @return az adott answer
     * @throws SQLException
     */
    private Answer setAnswer(ArrayList<Answer> answerList, Answer answer) throws SQLException {
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            answer.setAllData(resultSet.getInt("id"), resultSet.getString("answer"), resultSet.getInt("priority"));
            answerList.add(answer);
            answer = new Answer();
        }
        return answer;
    }

    /**
     * A metódus egy delete from tableName where columnName = param parancsnak felel meg.
     * @param columnName
     * @param param
     */
    public void delete(String columnName, String param) throws SQLException {
        try {
            statement.executeUpdate(String.format("delete from %s where %s='%s'",tableName,columnName, param));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A metódus egy update tableName set upColumnName = upParam where weColumnName = weparam parancsnak felel meg.
     * @param upColumnName
     * @param upParam
     * @param weColumnName
     * @param weparam
     */
    public void update(String upColumnName, String upParam, String weColumnName, String weparam) throws SQLException {
        try {
            statement.executeUpdate(String.format("update %s set %s = '%s' where %s='%s'",tableName,upColumnName,upParam,weColumnName, weparam));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Az összes rekordot kiszelektálja amiben megtalálható a param
     * @param question milyen szót vagy stringet keresunk
     * @return ArrayList minden lehetséges válasszal
     */
    public ArrayList<Answer> allReply(String question) throws SQLException {
        ArrayList<Answer> answerList = new ArrayList<>();
        Answer answer= new Answer();
        try {
            //minden olyan találat, ahol vagy ugyanaz vagy benne van a szövegben
            preparedStatement = connection.prepareStatement(
                    String.format("SELECT * FROM %s WHERE question LIKE '%%%s%%' ORDER BY priority DESC",tableName, question));
            //Itt vol egy változtatás
            setAnswer(answerList, answer);
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
        return answerList;
    }

    /**
     * A metódus azon sorokat szedi ki az adattáblából, amelyek tartalmazzák a contain nevű stringet.
     * @param columnName
     * @param contain
     */
    public void deleteContain(String columnName, String contain) throws SQLException {
        try {
            statement.executeUpdate(String.format("DELETE FROM %s WHERE %s LIKE '%%%s%%'",tableName,columnName, contain));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Minden adatot töröl az adattáblából.
     */
    public void deleteAllData() throws SQLException {
        try {
            statement.executeUpdate(String.format("delete from %s", tableName));
            statement.executeUpdate(String.format("ALTER SEQUENCE qa_id_seq RESTART WITH 1", tableName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A függvény azt figyeli, hogy a megadott question és answer pár szerepel-e már a táblázatban.
     * @param question
     * @param answer
     * @return
     * @throws SQLException
     */
    public Boolean existInTable(String question, String answer) throws SQLException {
        preparedStatement = connection.prepareStatement(
                String.format("select * from %s where question = '%s' AND answer = '%s'",tableName,question,answer),
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        resultSet = preparedStatement.executeQuery();
        resultSet.first();
        //System.out.println("Ennyiszer van benne a táblázatban egy az egyben: " + resultSet.getRow());

        if (resultSet.getRow()>0) {
            return true;
        }
        preparedStatement = connection.prepareStatement(
                String.format("select * from %s where answer = '%s'",tableName,answer),
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String quest = resultSet.getString("question");
            String id = resultSet.getString("id");
            if(quest.length() > 200){
                update("question",question,"answer",answer);
                System.out.println("Id " + id);
                return true;
            }
        }
        return false;
    }

    /**
     * A megadott resultSet, vagyis egy adott parancs után kinyert adatokat írj a képernyőre.
     * @param rs
     * @throws SQLException
     */
    private void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            String id = resultSet.getString("id");
            String question = resultSet.getString("question");
            String answer = resultSet.getString("answer");
            String priority = resultSet.getString("priority");

            System.out.println(id + " - " + question + " - " + answer + " - " + priority);
        }
    }

    @PreDestroy
    private void closeConnect() throws SQLException {
        connection.close();
    }
}
