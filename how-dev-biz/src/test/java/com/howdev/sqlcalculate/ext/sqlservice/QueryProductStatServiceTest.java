package com.howdev.sqlcalculate.ext.sqlservice;

import java.util.List;
import java.util.Map;

import com.howdev.framework.sqlcalculate.example.dto.ProductDTO;
import com.howdev.common.util.CsvUtil;
import com.howdev.sqlcalculate.QueryProductStatService;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** 
* QueryProductStatService Tester. 
* 
* @author <Authors name> 
* @since <pre>12/13/2023</pre> 
* @version 1.0 
*/ 
public class QueryProductStatServiceTest extends TestCase { 
public QueryProductStatServiceTest(String name) { 
super(name); 
} 

public void setUp() throws Exception { 
super.setUp(); 
} 

public void tearDown() throws Exception { 
super.tearDown(); 
} 


/** 
* 
* Method: process(List<ProductDTO> productDTOList) 
* 
*/ 
public void testProcess() throws Exception {
    List<ProductDTO> productDTOS = CsvUtil.readAndParseToObjectList("product_view.csv", ProductDTO.class);

    QueryProductStatService queryProductStatService = new QueryProductStatService();
    Map<String, Object> processResult = queryProductStatService.process(productDTOS);
    System.out.println(processResult);

} 

/** 
* 
* Method: getSqlServiceKey() 
* 
*/ 
public void testGetSqlServiceKey() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getUsedDbEngine() 
* 
*/ 
public void testGetUsedDbEngine() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getUdfRegistrar() 
* 
*/ 
public void testGetUdfRegistrar() throws Exception { 
//TODO: Test goes here... 
} 



public static Test suite() { 
return new TestSuite(QueryProductStatServiceTest.class); 
} 
} 
