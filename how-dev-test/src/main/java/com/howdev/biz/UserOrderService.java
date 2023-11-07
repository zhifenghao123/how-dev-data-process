package com.howdev.biz;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.howdev.biz.mapper.CategoryMapper;
import com.howdev.biz.mapper.ProductMapper;
import com.howdev.biz.model.Category;
import com.howdev.biz.model.Product;

/**
 * UserOrderService class
 *
 * @author haozhifeng
 * @date 2023/11/07
 */
public class UserOrderService {
    static SqlSession sqlSession;
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
    }
    public static void main(String[] args) {
        //testProduct1();
    }

    static void testCategory1() {
        CategoryMapper categoryMapper = sqlSession.getMapper(CategoryMapper.class);
        List<Category> categories = categoryMapper.selectAll();
        categories.forEach(System.out::println);
    }

    static void testProduct1() {
        ProductMapper productMapper = sqlSession.getMapper(ProductMapper.class);
        List<Product> products = productMapper.selectAll();
        products.forEach(System.out::println);
    }


}
