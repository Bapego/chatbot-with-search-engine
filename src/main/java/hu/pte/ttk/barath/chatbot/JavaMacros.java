package hu.pte.ttk.barath.chatbot;
import com.rivescript.macro.Subroutine;
import com.rivescript.util.StringUtils;

import java.lang.String;
import java.lang.StringBuilder;

public class JavaMacros implements Subroutine {
	/**
	 * Felhasznált kód. Java-ban megírt makró. A bevitt szó fordítottját adja vissza.
	 * @param rs RiveScript.
	 * @param args A szöveg
	 * @return A fordított szöveg.
	 */
	public String call (com.rivescript.RiveScript rs, String[] args) {
		String message = StringUtils.join(args, " ");

		String user = rs.currentUser();
		rs.setUservar(user, "java", "This variable was set by Java when you said 'reverse " + message + "'");

		return new StringBuilder(message).reverse().toString();
	}
}
