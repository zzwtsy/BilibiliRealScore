package cn.zzwtsy.score.utils;

import java.text.DecimalFormat;

/**
 * 控制台进度条
 *
 * @author <a href="https://blog.csdn.net/tjuyanming/article/details/80176171">Thinking in Java<a/>
 * @since 2023/01/06
 */
public class ConsoleProgressBar {

    /**
     * 进度条起始值
     */
    private long minimum = 0;

    /**
     * 进度条最大值
     */
    private long maximum = 100;
    /**
     * 进度条长度
     */
    private long barLen = 100;
    /**
     * 用于进度条显示的字符
     */
    private char showChar = '=';

    /**
     * 进度条标题
     */
    private String title;

    private final DecimalFormat formatter = new DecimalFormat("#.##%");

    /**
     * 使用系统标准输出，显示字符进度条及其百分比。
     */
    public ConsoleProgressBar() {
    }

    /**
     * 使用系统标准输出，显示字符进度条及其百分比。
     *
     * @param minimum  进度条起始值
     * @param maximum  进度条最大值
     * @param barLen   进度条长度
     * @param showChar 用于进度条显示的字符
     * @param title    进度条标题
     */
    public ConsoleProgressBar(long minimum, long maximum,
                              long barLen, char showChar, String title) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.barLen = barLen;
        this.showChar = showChar;
        this.title = title;
    }

    /**
     * 显示进度条。
     *
     * @param value 当前进度。进度必须大于或等于起始点且小于等于结束点（start <= current <= end）。
     */
    public void show(long value) {
        if (value < minimum || value > maximum) {
            return;
        }

        reset();
        minimum = value;
        float rate = (float) (minimum * 1.0 / maximum);
        long len = (long) (rate * barLen);
        draw(len, rate);
        if (minimum == maximum) {
            afterComplete();
        }
    }

    private void draw(long len, float rate) {
        System.out.print(title + ": ");
        for (int i = 0; i < len; i++) {
            System.out.print(showChar);
        }
        System.out.print(' ');
        System.out.print(format(rate));
    }


    private void reset() {
        //光标移动到行首
        System.out.print('\r');
    }

    private void afterComplete() {
        System.out.print("\n\n");
    }

    private String format(float num) {
        return formatter.format(num);
    }
}