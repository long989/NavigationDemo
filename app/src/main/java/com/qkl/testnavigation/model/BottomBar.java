package com.qkl.testnavigation.model;

import java.util.List;

/**
 * @author qiukailong
 * @title:
 * @projectName NewDriverSchool
 * @description:
 * @date 2021/11/4
 */
public class BottomBar {
    public int selectTab;
    public List<Tab> tabs;

    public static class Tab {
        /**
         * {
         * "size": 24,
         * "enable": true,
         * "index": 0,
         * "pageUrl": "main/tabs/home",
         * "title": "Home"
         * }
         */
        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
    }
}
