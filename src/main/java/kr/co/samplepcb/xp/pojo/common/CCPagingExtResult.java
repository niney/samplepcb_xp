package kr.co.samplepcb.xp.pojo.common;

public class CCPagingExtResult<T, D> extends coolib.common.CCPagingResult<T> {
    D extraData;

    public D getExtraData() {
        return extraData;
    }

    public void setExtraData(D extraData) {
        this.extraData = extraData;
    }
}
