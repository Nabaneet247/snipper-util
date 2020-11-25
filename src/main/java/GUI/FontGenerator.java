package GUI;

import java.awt.*;
import java.util.Calendar;

class FontGenerator {

    private static String[] supportedFonts = new String[] {
            "Arial",
            "Calibri",
            "Comic Sans MS",
            "Georgia",
            "Lucida Console",
            "Serif",
            "Verdana"
    };


    static Font getRandomFont() {
        return getRandomFont(Constants.DEFAULT_FONT_SIZE);
    }

    static Font getRandomFont(int fontSize) {
        try {
            int index = getDayOfWeek() % supportedFonts.length;
            return new Font(supportedFonts[index], Font.PLAIN, fontSize);
        } catch (Exception e) {
            return getDefaultFont(fontSize);
        }
    }

    static Font getDefaultFont(int fontSize) {
        return new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
    }

    private static int getDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

}
