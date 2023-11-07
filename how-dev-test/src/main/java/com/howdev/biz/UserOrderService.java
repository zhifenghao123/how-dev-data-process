package com.howdev.biz;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.howdev.biz.mapper.CategoryMapper;
import com.howdev.biz.model.Category;

/**
 * UserOrderService class
 *
 * @author haozhifeng
 * @date 2023/11/07
 */
public class UserOrderService {
    public static void main(String[] args) throws IOException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        CategoryMapper categoryMapper = sqlSession.getMapper(CategoryMapper.class);
        Category category = categoryMapper.selectByPrimaryKey(1L);
        System.out.println(category);

    }
}
