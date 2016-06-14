package com.opentools.collection;

import java.util.*;

/**
 * 增加集合工具类，此处的一些方法可能都是自己项目中需要又没有现成的轮子时造出来的
 *
 * @author aaron
 */
public class CollectionUtil {

    /**
     * 将map中的key处理成一个list,这部分代码不止一次用到，而其他轮子中好像又没有提供
     *
     * @param map
     * @return
     */
    public static List<Object> keyToList(Map<Object, Object> map) {

        Set<Object> keySet = map.keySet();
        List<Object> list = new ArrayList<>(keySet);
        return list;
    }

    /**
     * 获取一个listmap中含有指定key值的map
     *
     * @param list
     * @param key
     * @return
     */
    public static Map<Object, Object> get(List<Map<Object, Object>> list, Object key) {

        for (Map<Object, Object> map : list) {
            if (map.containsKey(key)) {

                return map;
            }
        }

        return null;
    }

    /**
     * 更新一个listmap列表中的数据，
     *
     * @param destList
     * @param map      更新的数据
     * @param add      没有时是否新增
     * @return
     */
    public static boolean updateListMap(List<Map<Object, Object>> destList, Map<Object, Object> map, boolean add) {

        for (Map<Object, Object> destMap : destList) {

            Set<Object> keySet = map.keySet();

            for (Object key : keySet) {

                Object object = destMap.get(key);
                if (null == object) {

                    if (add) destMap.put(key, map.get(key));

                } else {

                    destMap.replace(key, map.get(key));
                }
            }
        }

        return true;
    }

    /**
     * 过滤掉含有重复的指定key值的数据
     *
     * @param list 该方法危险，未经测试
     */
    @Deprecated
    public static void filterList(List<Map<String, Object>> list, String key) {

        List<Object> keys = new ArrayList<>();
        for (Map<String, Object> map : list) {

            Object object = map.get(key);

            if (!keys.contains(object)) {

                keys.add(object);
            } else {

                list.remove(map);
            }
        }
    }

    /**
     * 检查集合大小是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection<?> collection) {

        if (null == collection || collection.isEmpty() || collection.size() <= 0) return true;
        return false;
    }

    /**
     * 检查集合是否不为空
     *
     * @param collection
     * @return
     */
    public static boolean isNotEmpty(Collection<?> collection) {

        return !isEmpty(collection);
    }

    /**
     * 将一个集合输出为指定的分隔符的字符串
     *
     * @param list
     * @param operator
     * @return
     */
    public static String listToString(List<Object> list, String operator) {

        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(list)) {

            for (Object object : list) {

                if (null != object) {

                    stringBuilder.append(object);
                }

                if (!object.equals(list.get(list.size() - 1))) {

                    stringBuilder.append(operator);
                }
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 过滤掉src中在dest中已经存在的值
     * @param src
     * @param dest
     * @return
     */
    public static Collection<Object> filterCollection(Collection<Object> src, Collection<Object> dest) {

        if (isEmpty(dest)) return src;
        Iterator<Object> iterator = src.iterator();
        while (iterator.hasNext()) {

            Object next = iterator.next();
            if (dest.contains(next)) {

                iterator.remove();
            }
        }

        return src;
    }

    private CollectionUtil() {}

}