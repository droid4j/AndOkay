package com.dapn.framelibrary.http;

import com.dapn.andokay.baselibrary.utils.MD5Util;
import com.dapn.framelibrary.db.DaoSupportFactory;
import com.dapn.framelibrary.db.IDaoSupport;

import java.util.List;

/**
 * <pre>
 *     author : per4j
 *     e-mail : zhangpanzhao@okay.cn
 *     time   : 2019/04/01
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CacheBeanUtil {

    public static String getCacheDataResult(String finalUrl) {
        final IDaoSupport<CacheBean> dao = DaoSupportFactory.getFactory().getDao(CacheBean.class);
        List<CacheBean> query = dao.querySupport().selection("key = ?").selectionArgs(finalUrl).query();
        if (query.size() != 0) {
            CacheBean cacheBean = query.get(0);
            return cacheBean.getValue();
        }
        return null;
    }

    public static long saveCache(String key, String value) {
        final IDaoSupport<CacheBean> dao = DaoSupportFactory.getFactory().getDao(CacheBean.class);
        dao.delete("key = ?", key);
        long number = dao.insert(new CacheBean(key, value));
        return number;
    }
}
