package com.isprint.cnaac.server.domain.entity;

import java.math.BigDecimal;

public class Products {
    private String productid;

    private String productname;

    private String productpicpath;

    private BigDecimal productprice;

    private String productdescription;

    private Integer productorder;

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid == null ? null : productid.trim();
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname == null ? null : productname.trim();
    }

    public String getProductpicpath() {
        return productpicpath;
    }

    public void setProductpicpath(String productpicpath) {
        this.productpicpath = productpicpath == null ? null : productpicpath.trim();
    }

  

    public BigDecimal getProductprice() {
		return productprice;
	}

	public void setProductprice(BigDecimal productprice) {
		this.productprice = productprice;
	}

	public String getProductdescription() {
        return productdescription;
    }

    public void setProductdescription(String productdescription) {
        this.productdescription = productdescription == null ? null : productdescription.trim();
    }

    public Integer getProductorder() {
        return productorder;
    }

    public void setProductorder(Integer productorder) {
        this.productorder = productorder;
    }
}