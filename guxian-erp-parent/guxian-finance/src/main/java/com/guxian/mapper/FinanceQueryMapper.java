package com.guxian.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 财务模块跨表查询（成品应收 / 客户往来 / 收款明细 / 订单财务联动）
 */
@Mapper
public interface FinanceQueryMapper {

    /** 成品应收：订单财务列表（分页） */
    @Select("""
            <script>
            SELECT order_id AS orderId, order_no AS orderNo, customer_name AS customerName,
                   address AS address, terminal_address AS terminalAddress, order_type AS orderType,
                   order_status AS orderStatus, finance_status AS financeStatus,
                   total_amount AS totalAmount, paid_amount AS paidAmount, receive_amount AS receiveAmount,
                   unpaid_amount AS unpaidAmount, discount_amount AS discountAmount,
                   salesman AS salesman, receive_time AS receiveTime, order_date AS orderDate
            FROM t_sales_order
            WHERE del_flag = 0
            <if test="orderStatus != null"> AND order_status = #{orderStatus} </if>
            <if test="financeStatus != null"> AND finance_status = #{financeStatus} </if>
            <if test="dateFrom != null and dateFrom != ''"> AND order_date &gt;= #{dateFrom} </if>
            <if test="dateTo != null and dateTo != ''"> AND order_date &lt;= #{dateTo} </if>
            <if test="keyword != null and keyword != ''">
                AND (order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR customer_name LIKE CONCAT('%', #{keyword}, '%')
                  OR salesman LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY order_id DESC
            </script>
            """)
    IPage<Map<String, Object>> selectReceivablePage(IPage<Map<String, Object>> page,
                                                    @Param("keyword") String keyword,
                                                    @Param("orderStatus") Integer orderStatus,
                                                    @Param("financeStatus") Integer financeStatus,
                                                    @Param("dateFrom") String dateFrom,
                                                    @Param("dateTo") String dateTo);

    /** 成品应收：统计合计（与列表同条件） */
    @Select("""
            <script>
            SELECT IFNULL(SUM(total_amount), 0) AS totalSales,
                   IFNULL(SUM(paid_amount), 0) AS totalPaid,
                   IFNULL(SUM(unpaid_amount), 0) AS totalUnpaid,
                   IFNULL(SUM(discount_amount), 0) AS totalDiscount
            FROM t_sales_order
            WHERE del_flag = 0
            <if test="orderStatus != null"> AND order_status = #{orderStatus} </if>
            <if test="financeStatus != null"> AND finance_status = #{financeStatus} </if>
            <if test="dateFrom != null and dateFrom != ''"> AND order_date &gt;= #{dateFrom} </if>
            <if test="dateTo != null and dateTo != ''"> AND order_date &lt;= #{dateTo} </if>
            <if test="keyword != null and keyword != ''">
                AND (order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR customer_name LIKE CONCAT('%', #{keyword}, '%')
                  OR salesman LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Map<String, Object> selectReceivableSummary(@Param("keyword") String keyword,
                                                @Param("orderStatus") Integer orderStatus,
                                                @Param("financeStatus") Integer financeStatus,
                                                @Param("dateFrom") String dateFrom,
                                                @Param("dateTo") String dateTo);

    /** 客户往来：按客户聚合销售/收付款（分页） */
    @Select("""
            <script>
            SELECT c.customer_id AS customerId, c.customer_name AS customerName,
                   c.phone AS phone, c.address AS address, c.remark AS remark, c.balance AS balance,
                   IFNULL(SUM(o.total_amount), 0) AS salesAmount,
                   IFNULL(SUM(o.paid_amount), 0) AS paidAmount,
                   IFNULL(SUM(o.unpaid_amount), 0) AS unpaidAmount,
                   IFNULL(SUM(o.discount_amount), 0) AS discountAmount,
                   MAX(o.order_date) AS lastOrderDate,
                   MAX(o.salesman) AS salesman
            FROM t_customer c
            LEFT JOIN t_sales_order o ON o.customer_id = c.customer_id AND o.del_flag = 0
            WHERE c.del_flag = 0
            <if test="keyword != null and keyword != ''">
                AND (c.customer_name LIKE CONCAT('%', #{keyword}, '%')
                  OR c.phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            GROUP BY c.customer_id, c.customer_name, c.phone, c.address
            ORDER BY salesAmount DESC, c.customer_id DESC
            </script>
            """)
    IPage<Map<String, Object>> selectCustomerPage(IPage<Map<String, Object>> page,
                                                  @Param("keyword") String keyword);

    /** 客户往来：统计合计（与列表同条件） */
    @Select("""
            <script>
            SELECT IFNULL(SUM(o.total_amount), 0) AS totalSales,
                   IFNULL(SUM(o.paid_amount), 0) AS totalPaid,
                   IFNULL(SUM(o.unpaid_amount), 0) AS totalUnpaid,
                   IFNULL(SUM(o.discount_amount), 0) AS totalDiscount
            FROM t_customer c
            LEFT JOIN t_sales_order o ON o.customer_id = c.customer_id AND o.del_flag = 0
            WHERE c.del_flag = 0
            <if test="keyword != null and keyword != ''">
                AND (c.customer_name LIKE CONCAT('%', #{keyword}, '%')
                  OR c.phone LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    Map<String, Object> selectCustomerSummary(@Param("keyword") String keyword);

    /** 收款明细（分页，可带账户/订单/客户关键词与时间范围） */
    @Select("""
            <script>
            SELECT record_id AS recordId, account_id AS accountId, account_name AS accountName,
                   amount AS amount, sales_order_id AS salesOrderId, order_no AS orderNo,
                   customer_name AS customerName, operator_name AS operatorName,
                   remark AS remark, create_time AS createTime, receive_time AS receiveTime
            FROM t_collection_record
            WHERE del_flag = 0
            <if test="keyword != null and keyword != ''">
                AND (order_no LIKE CONCAT('%', #{keyword}, '%')
                  OR customer_name LIKE CONCAT('%', #{keyword}, '%')
                  OR account_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="dateFrom != null and dateFrom != ''"> AND receive_time &gt;= #{dateFrom} </if>
            <if test="dateTo != null and dateTo != ''"> AND receive_time &lt;= #{dateTo} </if>
            ORDER BY record_id DESC
            </script>
            """)
    IPage<Map<String, Object>> selectRecordPage(IPage<Map<String, Object>> page,
                                                @Param("keyword") String keyword,
                                                @Param("dateFrom") String dateFrom,
                                                @Param("dateTo") String dateTo);

    /** 查询订单财务信息（收款联动用） */
    @Select("SELECT order_id AS orderId, order_no AS orderNo, customer_id AS customerId, customer_name AS customerName, " +
            "total_amount AS totalAmount, receive_amount AS receiveAmount, unpaid_amount AS unpaidAmount " +
            "FROM t_sales_order WHERE order_id = #{orderId} AND del_flag = 0")
    Map<String, Object> selectOrderForFinance(@Param("orderId") Long orderId);

    /** 更新订单财务（收款/撤销收款） */
    @Update("UPDATE t_sales_order SET receive_amount = #{receive}, paid_amount = #{receive}, " +
            "unpaid_amount = #{unpaid}, finance_status = #{status}, receive_time = #{receiveTime} " +
            "WHERE order_id = #{orderId}")
    int updateOrderFinance(@Param("orderId") Long orderId,
                           @Param("receive") BigDecimal receive,
                           @Param("unpaid") BigDecimal unpaid,
                           @Param("status") Integer status,
                           @Param("receiveTime") LocalDateTime receiveTime);

    /** 账户金额增加 */
    @Update("UPDATE t_collection_account SET balance = balance + #{amount} WHERE account_id = #{accountId}")
    int addAccountBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /** 账户金额减少 */
    @Update("UPDATE t_collection_account SET balance = balance - #{amount} WHERE account_id = #{accountId}")
    int subAccountBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /** 客户充值：余额增加 */
    @Update("UPDATE t_customer SET balance = balance + #{amount} WHERE customer_id = #{customerId} AND del_flag = 0")
    int rechargeCustomer(@Param("customerId") Long customerId, @Param("amount") BigDecimal amount);
}
