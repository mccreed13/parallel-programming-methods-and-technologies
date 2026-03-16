package org.example.patterns;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.patterns.HtmlTagCounter.FORK_THRESHOLD;

public class HtmlTagCounter {
    private static final List<String> TAGS = Arrays.asList("div", "p", "span", "h1", "a", "section", "ul", "li", "img");
    private static final int TAGS_COUNT_FROM = 1000;
    private static final int TAGS_COUNT_TO = 1500;
    private static final int DOCUMENT_COUNT = 1000;
    public static final int FORK_THRESHOLD = 10;

    public static void main(String[] args) {
        List<String> documents = generateHtmlDocs(DOCUMENT_COUNT);

        //Sequential
        long start = System.currentTimeMillis();
        Map<String, Long> res1 = sequentialCountTags(documents);
        System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");

        //Map-Reduce
        start = System.currentTimeMillis();
        Map<String, Long> res2 = mapReduceCountTags(documents);
        System.out.println("Map-Reduce: " + (System.currentTimeMillis() - start) + "ms");
        Map<String, Long> res3;

        //Fork-Join
        start = System.currentTimeMillis();
        try (ForkJoinPool pool = new ForkJoinPool()){
            res3 = pool.invoke(new ForkJoinTagCounter(documents));
            System.out.println("Fork-Join: " + (System.currentTimeMillis() - start) + "ms");
        }

        assert res1.equals(res2);
        assert res2.equals(res3);
        System.out.println("Result: " + res1);
    }

    public static List<String> generateHtmlDocs(int count) {
        Random random = new Random();
        return IntStream.range(0, count).mapToObj(_ -> {
            int tagsInDoc = random.nextInt(TAGS_COUNT_TO-TAGS_COUNT_FROM) + TAGS_COUNT_FROM;
            return IntStream.range(0, tagsInDoc)
                    .mapToObj(_ -> "<" + TAGS.get(random.nextInt(TAGS.size())) + "> content </" +
                            TAGS.get(random.nextInt(TAGS.size())) + ">")
                    .collect(Collectors.joining("\n", "<html><body>", "</body></html>"));
        }).toList();
    }

    public static Map<String, Long> sequentialCountTags(List<String> docs) {
        Map<String, Long> globalCounts = new HashMap<>();
        for (String doc : docs) {
            Map<String, Long> docCounts = parse(doc);
            docCounts.forEach((tag, count) ->
                    globalCounts.merge(tag, count, Long::sum));
        }
        return globalCounts;
    }

    public static Map<String, Long> mapReduceCountTags(List<String> docs) {
        return docs.parallelStream()
                .map(HtmlTagCounter::parse)
                .reduce(new ConcurrentHashMap<>(), (acc, map) -> {
                    map.forEach((tag, count) -> acc.merge(tag, count, Long::sum));
                    return acc;
                });
    }

    public static ConcurrentHashMap<String, Long> parse(String html) {
        ConcurrentHashMap<String, Long> counts = new ConcurrentHashMap<>();
        Pattern pattern = Pattern.compile("<([a-zA-Z0-9]+)[^>]*>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            counts.put(tag, counts.getOrDefault(tag, 0L) + 1);
        }
        return counts;
    }
}

class ForkJoinTagCounter extends RecursiveTask<Map<String, Long>> {
    private final List<String> docs;
    public static int THRESHOLD = FORK_THRESHOLD;

    public ForkJoinTagCounter(List<String> docs) {
        this.docs = docs;
    }

    @Override
    protected Map<String, Long> compute() {
        if (docs.size() <= THRESHOLD) {
            return HtmlTagCounter.sequentialCountTags(docs);
        } else {
            int mid = docs.size() / 2;
            ForkJoinTagCounter leftTask = new ForkJoinTagCounter(docs.subList(0, mid));
            ForkJoinTagCounter rightTask = new ForkJoinTagCounter(docs.subList(mid, docs.size()));

            leftTask.fork(); // Відгалуження (Fork)
            Map<String, Long> rightResult = rightTask.compute();
            Map<String, Long> leftResult = leftTask.join(); // Очікування та злиття (Join)

            leftResult.forEach((tag, count) -> rightResult.merge(tag, count, Long::sum));
            return rightResult;
        }
    }
}
