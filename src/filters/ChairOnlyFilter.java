package filters;

import filters.ChatFilter;

import java.util.ArrayList;
import java.util.List;

public class ChairOnlyFilter implements ChatFilter {

    @Override
    public String filter(String message) {
        List<String> censoredList = new ArrayList<String>();
        censoredList.add("стол");
        censoredList.add("диван");
        censoredList.add("шкаф");

        for (String word: censoredList) {
            message = message.replaceAll(word, "СТУЛ");
        }

        return message;
    }


}
