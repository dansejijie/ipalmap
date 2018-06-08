package com.backustech.ipalmap.utils;

import com.palmaplus.nagrand.data.Param;

/**
 * Created by lchad on 2016/11/1.
 * Github: https://github.com/lchad
 */
public class Constant {
    /**
     * lua地址
     */
    public static final String LUR_NAME = "Nagrand/lua";
    /**
     * 正大广场ID
     */
    public static final int SINGLE_BUILDING_ID = 3329;
    /**
     * 上海证券大厦ID
     */
    public static final int MULTI_BUILDING_ID = 1473;
    /**
     * 地图数据服务器地址
     */
    public static final String SERVER_MAP_URL = "https://api.ipalmap.com/";

    public static final String SERVER_BACK_URL="http://demo.huitu.hz.backustech.com";

    public static final String BOOK_URL="/Bookshelf/Bookshelf/findBookIndex";

    public static final String BOOK_REPORT_MISS_URL="/Bookshelf/Bookshelf/reportMissing";
    /**
     * 楼层下拉菜单中显示字段
     */
    public static final Param<String> FLOOR_SHOW_FIELD = new Param<>("address", String.class);
    /**
     * AppKey，可以从图聚的开发者平台上获取
     */
    public static final String APP_KEY = "c9f2e55a6f124fb2956dc9cc8ab26ddd";

}
