use mall_system;
-- 创建测试商品A和B（使用IGNORE避免重复）
INSERT IGNORE INTO product (name, description, price, stock_quantity, image_url, is_deleted) VALUES
('测试商品A - 202412订单测试', '用于2024年12月开始的订单测试，避免干扰现有商品', 50.00, 1000, 'http://example.com/testA_202412.jpg', 0),
('测试商品B - 202412订单测试', '用于2024年12月开始的订单测试，避免干扰现有商品', 80.00, 1000, 'http://example.com/testB_202412.jpg', 0);

-- 获取商品ID（通过商品名称查询，确保只返回一个结果）
SELECT id INTO @productA_id FROM product WHERE name = '测试商品A - 202412订单测试' ORDER BY id DESC LIMIT 1;
SELECT id INTO @productB_id FROM product WHERE name = '测试商品B - 202412订单测试' ORDER BY id DESC LIMIT 1;

-- 添加新顾客（使用IGNORE避免重复）
INSERT IGNORE INTO customer (username, password, email, phone, is_admin) VALUES
('shaoxinyu', 'password123', 'shaoxinyu@example.com', '13800000010', 0), -- 邵新宇
('xuzirui', 'password123', 'xuzirui@example.com', '13800000011', 0),   -- 徐子锐
('zhaoshendi', 'password123', 'zhaoshendi@example.com', '13800000012', 0), -- 赵绅迪
('renxingyu', 'password123', 'renxingyu@example.com', '13800000013', 0), -- 任星宇
('lihaoran', 'password123', 'lihaoran@example.com', '13800000014', 0), -- 李浩然
('wangjianing', 'password123', 'wangjianing@example.com', '13800000015', 0), -- 王佳宁
('chensiyuan', 'password123', 'chensiyuan@example.com', '13800000016', 0), -- 陈思远
('liusihan', 'password123', 'liusihan@example.com', '13800000017', 0); -- 刘思涵

-- 生成从2024年12月1日到2025年12月31日的订单数据
-- 订单数量：200个
DELIMITER $$
CREATE PROCEDURE generate_test_orders()
BEGIN
    -- 声明变量
    DECLARE i INT DEFAULT 0;
    DECLARE customer_count INT DEFAULT 0;
    DECLARE random_index INT DEFAULT 0;
    DECLARE random_status INT DEFAULT 0;
    DECLARE random_address INT DEFAULT 0;
    
    -- 创建临时表存储所有顾客ID
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_customer_ids (
        id INT AUTO_INCREMENT PRIMARY KEY,
        customer_id INT
    );
    
    -- 插入所有顾客ID到临时表
    INSERT INTO temp_customer_ids (customer_id)
    SELECT id FROM customer;
    
    -- 获取顾客总数
    SELECT COUNT(*) INTO customer_count FROM temp_customer_ids;
    
    -- 循环生成200个订单
    WHILE i < 200 DO
        -- 随机选择顾客ID
        SET random_index = FLOOR(1 + RAND() * customer_count);
        SELECT customer_id INTO @customer_id FROM temp_customer_ids WHERE id = random_index;
        
        -- 随机选择订单状态 (PENDING, PAID, SHIPPED, COMPLETED, CANCELLED)
        SET random_status = FLOOR(1 + RAND() * 5);
        SET @status = CASE random_status
            WHEN 1 THEN 'PENDING'
            WHEN 2 THEN 'PAID'
            WHEN 3 THEN 'SHIPPED'
            WHEN 4 THEN 'COMPLETED'
            WHEN 5 THEN 'CANCELLED'
        END;
        
        -- 随机选择收货地址
        SET random_address = FLOOR(1 + RAND() * 10);
        SET @address = CASE random_address
            WHEN 1 THEN '北京市海淀区中关村大街1号'
            WHEN 2 THEN '上海市浦东新区张江高科技园区'
            WHEN 3 THEN '广州市天河区珠江新城'
            WHEN 4 THEN '深圳市南山区科技园'
            WHEN 5 THEN '杭州市西湖区文三路'
            WHEN 6 THEN '成都市高新区天府大道'
            WHEN 7 THEN '武汉市洪山区光谷大道'
            WHEN 8 THEN '南京市玄武区珠江路'
            WHEN 9 THEN '天津市南开区学府路'
            WHEN 10 THEN '重庆市渝中区解放碑'
        END;
        
        -- 随机生成2024-12-01到2025-12-31之间的日期
        SET @days = FLOOR(RAND() * 396); -- 396天（2024-12-01到2025-12-31）
        SET @order_date = DATE_ADD('2024-12-01', INTERVAL @days DAY);
        SET @order_date = CONCAT(@order_date, ' ', FLOOR(RAND() * 24), ':', FLOOR(RAND() * 60), ':', FLOOR(RAND() * 60));
        
        -- 随机生成订单商品数量（1-2个商品）
        SET @product_count = FLOOR(1 + RAND() * 2);
        SET @total_amount = 0;
        
        -- 创建订单主表记录
        INSERT INTO order_master (customer_id, order_date, total_amount, shipping_address, order_status)
        VALUES (@customer_id, @order_date, 0, @address, @status);
        
        SET @order_id = LAST_INSERT_ID();
        
        -- 生成订单详情项
        SET @j = 0;
        WHILE @j < @product_count DO
            -- 随机选择商品A或B
            IF RAND() > 0.5 THEN
                SET @product_id = @productA_id;
                SET @product_name = '测试商品A - 202412订单测试';
                SET @price = 50.00;
            ELSE
                SET @product_id = @productB_id;
                SET @product_name = '测试商品B - 202412订单测试';
                SET @price = 80.00;
            END IF;
            
            -- 随机生成购买数量（1-5件）
            SET @quantity = FLOOR(1 + RAND() * 5);
            
            -- 计算商品总价
            SET @item_total = @price * @quantity;
            SET @total_amount = @total_amount + @item_total;
            
            -- 插入订单详情
            INSERT INTO order_item (order_id, product_id, product_name, quantity, price_at_purchase)
            VALUES (@order_id, @product_id, @product_name, @quantity, @price);
            
            SET @j = @j + 1;
        END WHILE;
        
        -- 更新订单总金额
        UPDATE order_master SET total_amount = @total_amount WHERE order_id = @order_id;
        
        SET i = i + 1;
    END WHILE;
    
    -- 删除临时表
    DROP TEMPORARY TABLE IF EXISTS temp_customer_ids;
END $$
DELIMITER ;

-- 调用存储过程生成测试数据
CALL generate_test_orders();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_test_orders;


-- 调用方法
CALL generate_test_orders();