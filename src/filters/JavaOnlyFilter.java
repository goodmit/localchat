package filters;

import filters.ChatFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JavaOnlyFilter implements ChatFilter {
    List<String> censoredList;

    public JavaOnlyFilter() {
        censoredList = new LinkedList<String>();
        censoredList.add("чай");
        censoredList.add("лимонад");
        censoredList.add("водичка");
    }

    @Override
    public String filter(String message) {
        for (String word: censoredList) {
            message = message.replaceAll(word, "JAVA");
        }

        return message;
    }
}
