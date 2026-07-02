package com.codder.ultimate;

import java.util.ArrayList;
import java.util.List;

public class SvgaEntryManager {

    private static final List<String> SVGA_URLS = new ArrayList<>();

    public static void setSvgaList(List<String> list) {
        SVGA_URLS.clear();
        SVGA_URLS.addAll(list);
    }

    public static List<String> getSvgaList() {
        return SVGA_URLS;
    }
}