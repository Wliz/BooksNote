package com.jas.guavademo;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Jasmine
 * @date 2018-12-21 13:02
 */
public class BaseTest {
    /**
     * Guava 不可变集合
     */
    @Test
    public void immutableCollection() {
        List<String> list = new ArrayList<>();
        list.add("red");
        list.add("blue");
        list.add("purple");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            if (list.get(i) == "blue") {
                list.set(i, "black");
            }
        }
        System.out.println("------------->");


        ImmutableSet<String> colorNames = ImmutableSet.of("red", "blue");
        colorNames.forEach(System.out::println);
//        colorNames.remove("purple");

        ImmutableSet<User> users = ImmutableSet.of(new User(), new User());
        users.forEach(u ->u.setName(Math.random() + ""));

    }

    /**
     * Guava新集合
     */
    @Test
    public void newCollection() {
        // RangeSet: 一组不相连的、非空的区间
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.closed(1, 10)); // {[1,10]}
        rangeSet.add(Range.closedOpen(11, 15));//不相连区间:{[1,10], [11,15)}
        rangeSet.add(Range.closedOpen(15, 20)); //相连区间; {[1,10], [11,20)}
        rangeSet.add(Range.openClosed(0, 0)); //空区间; {[1,10], [11,20)}
        rangeSet.remove(Range.open(5, 10)); //分割[1, 10]; {[1,5], [10,10], [11,20)}

        System.out.println(rangeSet.toString());
        // 补集
        System.out.println(rangeSet.complement().toString());

        Set<Range<Integer>> ranges = rangeSet.asRanges();
        System.out.println("------------->");
        for (Range<Integer> range : ranges) {
            System.out.println(range.toString());
            System.out.println(range.lowerEndpoint());
        }
        System.out.println("------------->");

        // MultiMap：map嵌套set，list
        Map<String, List<String>> nestMap = new HashMap<>();
        nestMap.put("J", Arrays.asList("J", "a", "s"));
        nestMap.put("w", Arrays.asList("m", "i", "e"));
        for (List<String> value : nestMap.values()) {
            System.out.println("value: " + value);
        }

        // Guava
        Multimap<String, String> multimap = HashMultimap.create();
        multimap.put("J", "J");
        multimap.put("J", "a");
        multimap.put("J", "s");
        multimap.put("J", "m");
        multimap.put("w", "l");
        multimap.put("w", "a");
        multimap.put("w", "d");
        multimap.put("w", "e");
        for (String w : multimap.get("w")) {
            System.out.println("w: " + w);
        }

        for (String l : multimap.get("l")) {
            System.out.println("l: " + l);
        }

        Multiset<String> sets = HashMultiset.create();
        sets.add("1");
        sets.add("1");
        System.out.println(sets.count("1"));

        // Spring
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("J", "J");
        multiValueMap.add("J", "a");
        multiValueMap.add("J", "s");
        multiValueMap.add("J", "m");
        multiValueMap.add("w", "l");
        multiValueMap.add("w", "a");
        multiValueMap.add("w", "d");
        multiValueMap.add("w", "e");
        for (String w : multiValueMap.get("w")) {
            System.out.println("spring w: " + w);
        }
        // 内部无对应key，返回null
        //        for (String l : multiValueMap.get("l")) {
        //            System.out.println("spring l: " + l);
        //        }


        // Table: 嵌套map
        Table<String, Integer, Double> table = HashBasedTable.create();
        table.put("w", 1, 12.4);
        table.put("J", 2, 12.3);
        table.put("J", 1, 12.5);
        System.out.println(table.row("J"));

        // ClassToInstanceMap
        ClassToInstanceMap<Number> instanceMap = MutableClassToInstanceMap.create();
        instanceMap.put(Integer.class, Integer.valueOf(10));
        instanceMap.put(Float.class, 12.3f);

        instanceMap.put(Float.class, 12.33f);

        instanceMap.forEach((k, v) -> {
            if (k.equals(Float.class)) {
                System.out.println(v);
            }
        });
    }

    /**
     * 集合工具类
     */
    @Test
    public void utils() {
        // 静态工厂方法(内部使用Collections.addAll)
        List<Integer> integers = Lists.newArrayList(1, 2, 3, 4);
        System.out.println(integers);
        System.out.println(Lists.partition(integers, 2));
        System.out.println(Iterables.getFirst(ImmutableList.of(1, 2, 4), 10));

        // Iterables(Guava工具类更偏向于接受Iterable而非Collection)
        Iterable<Integer> integerIterable = Iterables.concat(Ints.asList(1, 2, 3), Ints.asList(4, 5, 6));
        System.out.println(integerIterable);
        Integer first = Iterables.getFirst(integerIterable, -1);
        System.out.println(first);
        System.out.println(Iterables.limit(integerIterable, 2));

        // Collection时实际执行size方法
        System.out.println(Iterables.size(integers));
        System.out.println(Iterables.size(integerIterable));
        System.out.println(Iterables.partition(integerIterable, 2));
    }

    /**
     * cache
     */
    @Test
    public void cache() {
        LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
            // 缓存数量
            .maximumSize(10)
            // 缓存有效期5秒
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .removalListener(notification -> System.out.println("key: " + notification.getKey() + " value: " + notification.getValue()))
            .build(CacheLoader.from(key -> -1));

        // 只进行查询，未命中返回null
        System.out.println("key1: " + cache.getIfPresent("key1"));
        // 手动放入数据
        cache.put("key1", 1);
        System.out.println("key1: " + cache.getIfPresent("key1"));
        System.out.println("size: " + cache.size());
        // 手动注销缓存
        cache.invalidate("key1");
        System.out.println("key1: " + cache.getIfPresent("key1"));

        try {
            // 查询缓存，未命中，则加载
            System.out.println("key2: " + cache.get("key2"));
            System.out.println("size: " + cache.size());
            cache.put("key2", 5);
            System.out.println("key2: " + cache.get("key2"));

            // 连续插入10个数据
            for (int i = 3; i < 13; i++) {
                cache.put("key" + i, i);
            }
            System.out.println("size: " + cache.size());
            // 最近最少使用删除缓存
            System.out.println("key2: " + cache.get("key2"));
            // 等待5秒
            Thread.sleep(5000);
            // 超时失效后，size未更新
            System.out.println("size: " + cache.size());
            System.out.println("key3: " + cache.getIfPresent("key3"));
            // 操作的时候进行维护，而非单独启动线程维护，减少消耗
            System.out.println("size: " + cache.size());
            System.out.println("--------->");
            cache.put("key2", 22);
            System.out.println("--------->");
            System.out.println("size: " + cache.size());
            System.out.println("key2: " + cache.get("key2"));
            System.out.println("size: " + cache.size());

            Thread.sleep(5000);
            System.out.println("size: " + cache.size());
            System.out.println("key2: " + cache.getIfPresent("key2"));
            System.out.println("size: " + cache.size());
            // cache.refresh("key2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 字符串处理
     */
    @Test
    public void guavaStrings() {
        // jdk字符串集合可以
        System.out.println(Arrays.asList("1", "2").stream().collect(Collectors.joining(";")));
        // Guava
        System.out.println(Joiner.on(";").join(Arrays.asList(1, 2, 3, 4, 5)));
        System.out.println(Joiner.on(";").join(Ints.asList(1, 2, 3, 4, 5)));
        String str = ",a,,b,";
        // Jdk 最后一个切分元素丢弃
        System.out.println(Arrays.toString(str.split(",")));
        System.out.println(Arrays.asList(str.split(",")));
        // Guava保留
        System.out.println(Splitter.on(",").omitEmptyStrings().split(str));

        // CaseFromat
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "omitOmit"));
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "omitLmit"));
    }
}
