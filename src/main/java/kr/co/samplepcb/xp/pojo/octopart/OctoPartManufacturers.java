package kr.co.samplepcb.xp.pojo.octopart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OctoPartManufacturers {
    private String id;
    private String name;
    private List<String> aliases;
    @JsonProperty(value = "display_flag")
    private String displayFlag;
    @JsonProperty(value = "homepage_url")
    private String homepageUrl;
    private String slug;
    @JsonProperty(value = "is_verified")
    private Boolean isVerified;
    @JsonProperty(value = "is_distributorapi")
    private Boolean isDistributorapi;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getDisplayFlag() {
        return displayFlag;
    }

    public void setDisplayFlag(String displayFlag) {
        this.displayFlag = displayFlag;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public Boolean getDistributorapi() {
        return isDistributorapi;
    }

    public void setDistributorapi(Boolean distributorapi) {
        isDistributorapi = distributorapi;
    }
}
