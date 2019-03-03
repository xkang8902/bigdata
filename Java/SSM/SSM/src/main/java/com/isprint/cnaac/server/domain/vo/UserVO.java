package com.isprint.cnaac.server.domain.vo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserVO {
	
	private String email;
	private String password;
	
	@NotNull
	@Size(min = 1, max = 1000)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@NotNull
	@Size(min = 1, max = 1000)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
