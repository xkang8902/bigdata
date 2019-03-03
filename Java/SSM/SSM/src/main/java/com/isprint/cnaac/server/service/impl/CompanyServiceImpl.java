package com.isprint.cnaac.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isprint.cnaac.server.dao.CompanyMapper;
import com.isprint.cnaac.server.domain.entity.Company;
import com.isprint.cnaac.server.service.CompanyService;


@Service( value = "companyService")
public class CompanyServiceImpl implements CompanyService {
	
	private final static Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);
	
	@Autowired
	private CompanyMapper companyMapper;

	@Override
	public Company getCompanyByUUID(String uuid) {
		logger.info("according uuid to get company information, uuid = " + uuid);
		return companyMapper.selectByPrimaryKey(uuid);
	}

}
