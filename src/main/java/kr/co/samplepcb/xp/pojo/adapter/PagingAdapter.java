package kr.co.samplepcb.xp.pojo.adapter;

import coolib.common.CCPagingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PagingAdapter {

    public static <T> CCPagingResult<T> toCCPagingResult(String query, Pageable pageable, Page<T> data) {
        CCPagingResult<T> ccPagingResult = new CCPagingResult<>();
        ccPagingResult.setResult(true);
        ccPagingResult.setQ(query);
        settingPaging(pageable, data, ccPagingResult);
        return ccPagingResult;
    }

    public static <T> CCPagingResult<T> toCCPagingResult(Pageable pageable, Page<T> data) {
        CCPagingResult<T> ccPagingResult = new CCPagingResult<>();
        ccPagingResult.setResult(true);
        settingPaging(pageable, data, ccPagingResult);
        return ccPagingResult;
    }

    public static <T> CCPagingResult<T> toCCPagingResult(Pageable pageable, List<T> data, long totalCount) {
        CCPagingResult<T> ccPagingResult = new CCPagingResult<>();
        ccPagingResult.setResult(true);
        settingPaging(pageable, data, totalCount, ccPagingResult);
        return ccPagingResult;
    }

    private static <T> void settingPaging(Pageable pageable, Page<T> data, CCPagingResult<T> ccPagingResult) {
        ccPagingResult.setCurrentPage(pageable.getPageNumber() + 1);
        ccPagingResult.setOffset((int) pageable.getOffset());
        ccPagingResult.setSize(pageable.getPageSize());
        ccPagingResult.setData(data.getContent());
        ccPagingResult.setTotalCount(Math.toIntExact(data.getTotalElements()));
    }


    public static <T> CCPagingResult<T> toCCPagingResult(String query, Pageable pageable, List<T> data, long totalCount) {
        CCPagingResult<T> ccPagingResult = new CCPagingResult<>();
        ccPagingResult.setResult(true);
        ccPagingResult.setQ(query);
        settingPaging(pageable, data, totalCount, ccPagingResult);
        return ccPagingResult;

    }

    private static <T> void settingPaging(Pageable pageable, List<T> data, long totalCount, CCPagingResult<T> ccPagingResult) {
        ccPagingResult.setCurrentPage(pageable.getPageNumber() + 1);
        ccPagingResult.setOffset((int) pageable.getOffset());
        ccPagingResult.setSize(pageable.getPageSize());
        ccPagingResult.setData(data);
        ccPagingResult.setTotalCount(Math.toIntExact(totalCount));
    }
}
