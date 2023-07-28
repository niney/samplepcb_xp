package kr.co.samplepcb.xp.pojo;

import java.util.List;

public class Alimtalk {

    public String templateCode;
    public String reSend;
    public String resendCallback;
    public String resendType;
    public String resendTitle;
    public String resendContent;
    public List<Receiver> list;

    public static class Receiver {
        public String phone;
        public List<String> templateParam;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getReSend() {
        return reSend;
    }

    public void setReSend(String reSend) {
        this.reSend = reSend;
    }

    public String getResendCallback() {
        return resendCallback;
    }

    public void setResendCallback(String resendCallback) {
        this.resendCallback = resendCallback;
    }

    public String getResendType() {
        return resendType;
    }

    public void setResendType(String resendType) {
        this.resendType = resendType;
    }

    public String getResendTitle() {
        return resendTitle;
    }

    public void setResendTitle(String resendTitle) {
        this.resendTitle = resendTitle;
    }

    public String getResendContent() {
        return resendContent;
    }

    public void setResendContent(String resendContent) {
        this.resendContent = resendContent;
    }

    public List<Receiver> getList() {
        return list;
    }

    public void setList(List<Receiver> list) {
        this.list = list;
    }
}
