package cn.zzwtsy.score;

import cn.zzwtsy.score.utils.ConsoleProgressBar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;

/**
 * 获取B站动漫、电影等真实评分
 *
 * @author zzwtsy
 * @since 2023/01/04
 */
public class Main {
    public static final Main INSTANCE = new Main();
    public static int oneScoreTotal = 0;
    public static int twoScoreTotal = 0;
    public static int threeScoreTotal = 0;
    public static int fourScoreTotal = 0;
    public static int fiveScoreTotal = 0;
    public static int zeroScoreTotal = 0;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入media_id: ");
        int mid = scanner.nextInt();
        List<Integer> longScore = Main.INSTANCE.getLongScore(mid);
        List<Integer> shortScore = Main.INSTANCE.getShortScore(mid);
        //计算评分人数
        double totalCount = longScore.size() + shortScore.size();
        long tempScore = 0;
        //计算评分
        for (Integer integer : longScore) {
            tempScore += integer;
        }
        for (Integer integer : shortScore) {
            tempScore += integer;
        }
        //获取总评分
        double totalScore = tempScore;
        //计算真实评分（总评分/总评分人数）
        double realScore = totalScore / totalCount;
        long end = System.currentTimeMillis();
        //计算程序运行时间
        long timeElapsed = (end - start) / 1000;
        String date;
        if (timeElapsed < 60) {
            date = timeElapsed + "秒";
        } else {
            date = (timeElapsed / 60) + "分钟";
        }
        System.out.println("\n短评论个数:\t" + shortScore.size() + " 条"
                + "\n长评论个数:\t" + longScore.size() + " 条"
                + "\n评分人数:\t" + String.format("%.0f 人", totalCount)
                + "\n真实评分:\t" + String.format("%.1f 分", realScore)
                + "\n耗时:\t\t" + date
                + "\n====================="
                + "\n0分个数：" + zeroScoreTotal
                + "\n1分个数：" + oneScoreTotal
                + "\n2分个数：" + twoScoreTotal
                + "\n3分个数：" + threeScoreTotal
                + "\n4分个数：" + fourScoreTotal
                + "\n5分个数：" + fiveScoreTotal
        );
    }

    /**
     * 获取长评论
     *
     * @param mid 视频 media_id
     * @return {@link List}<{@link Integer}>
     */
    private List<Integer> getLongScore(int mid) {
        return getScore(true, mid);
    }

    /**
     * 获取短评论
     *
     * @param mid 视频 media_id
     * @return {@link List}<{@link Integer}>
     */
    private List<Integer> getShortScore(int mid) {
        return getScore(false, mid);
    }

    /**
     * 获取评分
     *
     * @param scoreType 评分类型 true：长评论 false：短评论
     * @param mid       视频 media_id
     * @return {@link List}<{@link Integer}>
     */
    private List<Integer> getScore(boolean scoreType, int mid) {
        List<Integer> score = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Response response = null;
        String responseBody;
        long count = 0;
        int i = 0;
        long next = 0;
        for (; ; ) {
            String url;
            OkHttpClient okHttpClient = new OkHttpClient();
            if (i == 0) {
                if (scoreType) {
                    url = "https://api.bilibili.com/pgc/review/long/list?media_id=" + mid + "&ps=20&sort=0";
                } else {
                    url = "https://api.bilibili.com/pgc/review/short/list?media_id=" + mid + "&ps=20&sort=0";
                }
                i = 1;
            } else {
                if (scoreType) {
                    url = "https://api.bilibili.com/pgc/review/long/list?media_id=" + mid + "&ps=20&sort=0&cursor=" + next;
                } else {
                    url = "https://api.bilibili.com/pgc/review/short/list?media_id=" + mid + "&ps=20&sort=0&cursor=" + next;
                }
            }
            Request request = new okhttp3.Request
                    .Builder()
                    .get()
                    .url(url)
                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54")
                    .build();
            try {
                response = okHttpClient.newCall(request).execute();
                responseBody = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = jsonNode.get("data");
                int total = dataNode.get("total").asInt();
                String title;
                if (scoreType) {
                    title = "获取长评论进度";
                } else {
                    title = "获取短评论进度";
                }
                ConsoleProgressBar progress = new ConsoleProgressBar(0, total, 30, '█', title);
                progress.show(count);
                //获取 json 文件中的 list 内容
                JsonNode listContent = dataNode.get("list");
                for (JsonNode element : listContent) {
                    int tempScore = element.get("score").asInt();
                    //获取评分
                    score.add(tempScore);
                    switch (tempScore) {
                        case 2 -> oneScoreTotal += 1;
                        case 4 -> twoScoreTotal += 1;
                        case 6 -> threeScoreTotal += 1;
                        case 8 -> fourScoreTotal += 1;
                        case 10 -> fiveScoreTotal += 1;
                        case 0 -> zeroScoreTotal += 1;
                        default -> {
                        }
                    }
                    count++;
                }
                next = dataNode.get("next").asLong();
                if (next == (0)) {
                    return score;
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
