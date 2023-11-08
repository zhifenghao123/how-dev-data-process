package com.howdev.biz;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.howdev.biz.mapper.CategoryMapper;
import com.howdev.biz.mapper.OrderDetailMapper;
import com.howdev.biz.mapper.ProductMapper;
import com.howdev.biz.mapper.UserOrderMapper;
import com.howdev.biz.model.bo.UserOrderBO;
import com.howdev.biz.model.po.Category;
import com.howdev.biz.model.po.OrderDetail;
import com.howdev.biz.model.po.Product;
import com.howdev.biz.model.po.UserOrder;
import com.howdev.util.DateTimeUtil;

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

        // 自动提交事务
        sqlSession = sqlSessionFactory.openSession(true);

        userOrderMapper = sqlSession.getMapper(UserOrderMapper.class);
        orderDetailMapper = sqlSession.getMapper(OrderDetailMapper.class);
        productMapper = sqlSession.getMapper(ProductMapper.class);
        categoryMapper = sqlSession.getMapper(CategoryMapper.class);
    }
    public static void main(String[] args) {
        generateAndInsertOrders();
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

        UserOrderBO userOrderBO1 =
                generateAndOrders(1001L, 1, products, Arrays.asList(1L, 3L), Arrays.asList(2, 2));
        UserOrderBO userOrderBO2 =
                generateAndOrders(1002L, 2, products, Arrays.asList(2L, 6L, 7L), Arrays.asList(1, 1, 2));
        UserOrderBO userOrderBO3 =
                generateAndOrders(1003L, 1, products, Arrays.asList(2L, 3L), Arrays.asList(1, 2));
        System.out.println(userOrderBO1);
        System.out.println(userOrderBO2);
        System.out.println(userOrderBO3);
        System.out.println("save userOrderBO1, result=" + saveGeneratedOrders(userOrderBO1));
        System.out.println("save userOrderBO2, result=" + saveGeneratedOrders(userOrderBO2));
        System.out.println("save userOrderBO3, result=" + saveGeneratedOrders(userOrderBO3));


    }

    /**
     * generateAndOrders
     *
     * @param userId userId
     * @param allProducts allProducts
     * @param filteredCategoryIds filteredCategoryIds
     * @param filteredCategoryIdQuantities filteredCategoryIdQuantities
     * @return:
     * @author: haozhifeng
     */
    static UserOrderBO generateAndOrders(Long userId, Integer orderStatus, List<Product> allProducts,
                                         List<Long> filteredCategoryIds,
                                         List<Integer> filteredCategoryIdQuantities) {
        try {
            // sleep 100毫秒，避免两个订单太快，达到毫秒级别相同
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (null == allProducts || allProducts.isEmpty()) {
            return null;
        }

        Map<Long, List<Product>> categoryIdToProductsMap =
                allProducts.stream().
                        filter(product -> filteredCategoryIds.contains(product.getCategoryId())).
                        collect(Collectors.groupingBy(Product::getCategoryId));

        Long orderId = Long.valueOf(DateTimeUtil.formatNow(DateTimeUtil.yyyyMMddHHmmssSSS_1_FORMAT) + "00");

        UserOrderBO userOrderBO = new UserOrderBO();
        userOrderBO.setUserId(userId);
        userOrderBO.setOrderId(orderId);
        userOrderBO.setOrderNumber(DateTimeUtil.formatNow(DateTimeUtil.yyyyMMddHHmmssSSS_1_FORMAT));
        userOrderBO.setStatus(orderStatus);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (int i = 0; i < filteredCategoryIds.size(); i++) {
            Long categoryId = filteredCategoryIds.get(i);
            Integer quantities = filteredCategoryIdQuantities.get(i);

            List<Product> products = categoryIdToProductsMap.get(categoryId);
            if (quantities.equals(0L)) {
                continue;
            }
            // 如果设置的要分配该类产品的数量 大于 该类产品的数量
            if (quantities > products.size()) {
                for (int j = 1; j <= products.size(); j++) {
                    Product product = products.get(j - 1);
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderId(userOrderBO.getOrderId());
                    orderDetail.setOrderDetailId(orderId + (i * products.size() + j));
                    orderDetail.setProductId(product.getProductId());
                    orderDetail.setPrice(product.getPrice());
                    orderDetail.setQuantity(j == products.size()?  quantities - (j -1) : 1);
                    orderDetails.add(orderDetail);
                }
            } else {
                for (int j = 1; j <= quantities; j++) {
                    Product product = products.get(j -1);
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderId(userOrderBO.getOrderId());
                    orderDetail.setOrderDetailId(orderId + (i * quantities + j));
                    orderDetail.setProductId(product.getProductId());
                    orderDetail.setPrice(product.getPrice());
                    orderDetail.setQuantity(1);
                    orderDetails.add(orderDetail);
                }
            }
        }
        userOrderBO.setOrderDetails(orderDetails);

        BigDecimal totalPrice = orderDetails.stream()
                .map(orderDetail -> orderDetail.getPrice().multiply(new BigDecimal(orderDetail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        userOrderBO.setTotalPrice(totalPrice);
        return userOrderBO;
    }

    private static boolean saveGeneratedOrders(UserOrderBO userOrderBO) {
        Date currentTime = new Date();

        UserOrder userOrder = new UserOrder();
        userOrder.setOrderId(userOrderBO.getOrderId());
        userOrder.setUserId(userOrderBO.getUserId());
        userOrder.setOrderNumber(userOrderBO.getOrderNumber());
        userOrder.setTotalPrice(userOrderBO.getTotalPrice());
        userOrder.setStatus(userOrderBO.getStatus());
        userOrder.setCreated(currentTime);
        userOrder.setUpdated(currentTime);

        List<OrderDetail> orderDetailBOs = userOrderBO.getOrderDetails();
        List<OrderDetail> orderDetails = orderDetailBOs.stream().map(orderDetailBo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderDetailId(orderDetailBo.getOrderDetailId());
            orderDetail.setOrderId(orderDetailBo.getOrderId());
            orderDetail.setProductId(orderDetailBo.getProductId());
            orderDetail.setPrice(orderDetailBo.getPrice());
            orderDetail.setQuantity(orderDetailBo.getQuantity());
            orderDetail.setCreated(currentTime);
            orderDetail.setUpdated(currentTime);
            return orderDetail;
        }).collect(Collectors.toList());

        int insertSuccCnt = userOrderMapper.insert(userOrder);
        int batchInsertSuccCnt = orderDetailMapper.batchInsert(orderDetails);
        return true;
    }


}
