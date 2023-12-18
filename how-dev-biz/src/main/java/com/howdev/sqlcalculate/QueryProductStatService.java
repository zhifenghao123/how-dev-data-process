package com.howdev.sqlcalculate;

import java.util.List;
import java.util.Map;

import com.howdev.framework.sqlcalculate.example.entity.Product;
import com.howdev.framework.sqlcalculate.example.dto.ProductDTO;
import com.howdev.framework.sqlcalculate.example.udf.sqlite.SqliteNumToStrFunction;
import com.howdev.framework.sqlcalculate.jdbc.core.TableMetaDataContainer;
import com.howdev.framework.sqlcalculate.jdbc.enumeration.DbEngineEnum;
import com.howdev.framework.sqlcalculate.jdbc.sqlservice.SqlCalculate;
import com.howdev.framework.sqlcalculate.jdbc.udf.UdfRegistrar;
import com.howdev.util.bean.BeanConvertUtils;

/**
 * QueryProductStatService class
 *
 * @author haozhifeng
 * @date 2023/12/13
 */
public class QueryProductStatService extends SqlCalculate {
    private static final String SQL_SERVICE_KEY = "QueryProductStatService";

    public Map<String, Object> process(List<ProductDTO> productDTOList) {
        List<Product> products = BeanConvertUtils.batchConvert(Product.class, productDTOList);

        TableMetaDataContainer tableMetaDataContainer = new TableMetaDataContainer();
        tableMetaDataContainer.addTableMetaData(Product.class, products);
        Map<String, Object> map = calculateSql(tableMetaDataContainer, null);
        return map;
    }

    @Override
    public String getSqlServiceKey() {
        return SQL_SERVICE_KEY;
    }

    @Override
    public DbEngineEnum getUsedDbEngine() {
        return DbEngineEnum.SQLITE;
    }

    @Override
    public UdfRegistrar getUdfRegistrar() {
        UdfRegistrar udfRegistrar = new UdfRegistrar();
        udfRegistrar.registerUdf(new SqliteNumToStrFunction());
        return udfRegistrar;
    }
}
