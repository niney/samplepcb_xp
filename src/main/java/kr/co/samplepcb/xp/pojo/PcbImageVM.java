package kr.co.samplepcb.xp.pojo;

public class PcbImageVM {
    private String uploadFileName;
    private String originFileName;
    private String pathToken;
    private String size;

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getOriginFileName() {
        return originFileName;
    }

    public void setOriginFileName(String originFileName) {
        this.originFileName = originFileName;
    }

    public String getPathToken() {
        return pathToken;
    }

    public void setPathToken(String pathToken) {
        this.pathToken = pathToken;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
