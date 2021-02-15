package kr.co.samplepcb.xp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MouserParam {

    @JsonProperty("SearchByPartRequest")
    private CSearchByPartRequest searchByPartRequest;

    public CSearchByPartRequest getSearchByPartRequest() {
        return searchByPartRequest;
    }

    public void setSearchByPartRequest(CSearchByPartRequest searchByPartRequest) {
        this.searchByPartRequest = searchByPartRequest;
    }

    public static class CSearchByPartRequest {
        private String mouserPartNumber;
        private String partSearchOptions;

        public String getMouserPartNumber() {
            return mouserPartNumber;
        }

        public void setMouserPartNumber(String mouserPartNumber) {
            this.mouserPartNumber = mouserPartNumber;
        }

        public String getPartSearchOptions() {
            return partSearchOptions;
        }

        public void setPartSearchOptions(String partSearchOptions) {
            this.partSearchOptions = partSearchOptions;
        }
    }

}
