package forpdateam.ru.forpda.ui.views.control;

/**
 * Created by fedor on 21.03.2017.
 */

public class MathUtils {

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

}