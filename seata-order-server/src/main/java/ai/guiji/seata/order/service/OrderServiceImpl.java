package ai.guiji.seata.order.service;

import ai.guiji.seata.order.dao.OrderDao;
import ai.guiji.seata.order.entity.Order;
import ai.guiji.seata.order.feign.AccountApi;
import ai.guiji.seata.order.feign.StorageApi;
import com.alibaba.fastjson.JSON;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author IT云清
 */
@Service("orderServiceImpl")
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private StorageApi storageApi;
    @Autowired
    private AccountApi accountApi;

    /**
     * 创建订单
     *
     * @param order
     * @return 测试结果：
     * 1.添加本地事务：仅仅扣减库存
     * 2.不添加本地事务：创建订单，扣减库存
     */
    @Override
    @GlobalTransactional(name = "fsp-create-order", rollbackFor = Exception.class)
    public void create(Order order) {
        LOGGER.info("------->交易开始, order:{}", JSON.toJSONString(order));
        //本地方法
        orderDao.create(order);

        //远程方法 扣减库存
        storageApi.decrease(order.getProductId(), order.getCount());

        //远程方法 扣减账户余额

        LOGGER.info("------->扣减账户开始order中");
        accountApi.decrease(order.getUserId(), order.getMoney());
        LOGGER.info("------->扣减账户结束order中");

        LOGGER.info("------->交易结束");
    }

    /**
     * 修改订单状态
     */
    @Override
    public void update(Long userId, BigDecimal money, Integer status) {
        LOGGER.info("修改订单状态，入参为：userId={},money={},status={}", userId, money, status);
        orderDao.update(userId, money, status);
    }
}
