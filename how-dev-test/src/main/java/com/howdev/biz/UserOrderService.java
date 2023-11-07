package com.howdev.biz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.howdev.biz.mapper.CategoryMapper;
import com.howdev.biz.mapper.OrderDetailMapper;
import com.howdev.biz.mapper.ProductMapper;
import com.howdev.biz.mapper.UserOrderMapper;
import com.howdev.biz.model.po.Category;
import com.howdev.biz.model.po.Product;

/**
 * UserOrderService class
 *
 * @author haozhifeng
 * @date 2023/11/07
 */
public class UserOrderService {
    static SqlSession sqlSession;

    static UserOrderMapper userOrderMapper;
    static OrderDetailMapper orderDetailMapper;
    static ProductMapper productMapper;
    static CategoryMapper categoryMapper;
    static {
        String resource = "mybatis/mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession();

        userOrderMapper = sqlSession.getMapper(UserOrderMapper.class);
        orderDetailMapper = sqlSession.getMapper(OrderDetailMapper.class);
        productMapper = sqlSession.getMapper(ProductMapper.class);
        categoryMapper = sqlSession.getMapper(CategoryMapper.class);
    }
    public static void main(String[] args) {
        testProduct1();
    }

    static void testCategory1() {
        List<Category> categories = categoryMapper.selectAll();
        categories.forEach(System.out::println);
    }

    static void testProduct1() {
        List<Product> products = productMapper.selectAll();
        products.forEach(System.out::println);
    }

    static void generateAndInsertOrders() {
        List<Long> userIds = Arrays.asList(1001L, 1002L, 1003L);
        List<Product> products = productMapper.selectAll();


    }

    /**
     * generateAndOrders
     *
     * @param userId userId
     * @param allProducts allProducts
     * @param filteredCategoryIds filteredCategoryIds
     * @param filteredCategoryIdQuantitys filteredCategoryIdQuantitys
     * @return:
     * @author: haozhifeng
     */
    static void generateAndOrders(Long userId, List<Product> allProducts, List<Long> filteredCategoryIds,
                                  List<Long> filteredCategoryIdQuantitys) {
        if (null == allProducts || allProducts.isEmpty()) {
            return;
        }

    }


}
