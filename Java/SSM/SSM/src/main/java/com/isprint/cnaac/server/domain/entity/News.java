package com.isprint.cnaac.server.domain.entity;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class News implements Serializable {
	
	private static final long serialVersionUID = 6197091110872564822L;

	private String uuid;

    private String title;

    private String summary;

    private String author;

    private String provenance;

    private Date pubtime;

    private String path;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

    private String type;

    private Integer clickCount;

    private String summaryPic;

    private byte[] contents;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    @NotNull
    @Size(min = 1, max = 200)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    @NotNull
    @Size(min = 1, max = 1000)
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary == null ? null : summary.trim();
    }

    @Size(min = 0, max = 100)
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    @Size(min = 0, max = 500)
    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance == null ? null : provenance.trim();
    }

    public Date getPubtime() {
        return pubtime;
    }

    public void setPubtime(Date pubtime) {
        this.pubtime = pubtime;
    }

    @Size(min = 0, max = 100)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Size(min = 0, max = 100)
    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    @Size(min = 0, max = 100)
    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }

    @Size(min = 0, max = 10)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    @Size(min = 0, max = 500)
    public String getSummaryPic() {
        return summaryPic;
    }

    public void setSummaryPic(String summaryPic) {
        this.summaryPic = summaryPic == null ? null : summaryPic.trim();
    }

    @NotNull   
    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }
}