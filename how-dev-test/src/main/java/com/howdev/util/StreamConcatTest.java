package com.howdev.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamConcatTest {
    public static void main(String[] args) {
        //Map<Long, List<String>> secondToReqIdsMap1 = null;
        Map<Long, List<String>> secondToReqIdsMap1 = new HashMap<>();
        //secondToReqIdsMap1.put(1L, Arrays.asList("1", "2"));


        Map<Long, List<String>> secondToReqIdsMap2 = new HashMap<>();
        secondToReqIdsMap2.put(1L, Arrays.asList("2", "3"));

        mapStreamConcat(secondToReqIdsMap1, secondToReqIdsMap2);
    }

    public static void mapStreamConcat(Map<Long, List<String>> secondToReqIdsMap1, Map<Long, List<String>> secondToReqIdsMap2) {


        // 合并不同秒时间戳的请求ID
        Map<Long, List<String>> mergedSecondToReqIdsMap = Stream.concat(
                        secondToReqIdsMap1.entrySet().stream(),
                        secondToReqIdsMap2.entrySet().stream()
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (list1, list2) -> {
                            List<String> mergedList = new ArrayList<>(list1);
                            mergedList.addAll(list2);
                            return mergedList;
                        }
                ));

        for (Map.Entry<Long, List<String>> entry : mergedSecondToReqIdsMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
