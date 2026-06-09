package com.waimai.config;

import com.waimai.entity.*;
import com.waimai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 数据初始化器 — 启动时插入测试数据
 * 仅在数据为空时执行，可重复启动不会重复插入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化测试数据...");

        // ===== 消费者 =====
        User consumer = userRepository.save(User.builder()
                .username("consumer1")
                .phone("13800138000")
                .password(passwordEncoder.encode("123456"))
                .email("consumer@test.com")
                .role("ROLE_CONSUMER")
                .balance(new BigDecimal("1000.00"))
                .build());
        log.info("消费者创建: 13800138000 / 123456 (余额 1000)");

        // 为消费者创建默认收货地址
        addressRepository.save(Address.builder()
                .consumer(consumer)
                .receiverName("张三")
                .receiverPhone("13800138000")
                .province("北京市")
                .city("北京市")
                .district("朝阳区")
                .detailAddress("建国路88号 SOHO现代城A座 1508室")
                .isDefault(true)
                .build());
        log.info("默认收货地址已创建");

        // ===== 商家1: 美味家常菜 =====
        User merchantUser1 = userRepository.save(User.builder()
                .username("merchant1")
                .phone("13900139000")
                .password(passwordEncoder.encode("123456"))
                .email("merchant1@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());

        Merchant m1 = merchantRepository.save(Merchant.builder()
                .user(merchantUser1)
                .shopName("美味家常菜")
                .shopAddress("北京市朝阳区建国路88号")
                .businessLicense("SC10000000000001")
                .description("地道家常菜，食材新鲜，价格实惠，每日新鲜烹制！")
                .rating(new BigDecimal("4.8"))
                .monthlySales(2580)
                .deliveryFee(new BigDecimal("2.00"))
                .minOrderAmount(new BigDecimal("15.00"))
                .businessHours("09:00-22:00")
                .status("营业中")
                .build());
        log.info("商家1创建: 13900139000 / 123456 — 美味家常菜");

        // 商家1分类和商品
        Category c11 = categoryRepository.save(Category.builder().merchant(m1).name("热销推荐").sortOrder(1).build());
        Category c12 = categoryRepository.save(Category.builder().merchant(m1).name("荤菜系列").sortOrder(2).build());
        Category c13 = categoryRepository.save(Category.builder().merchant(m1).name("素菜系列").sortOrder(3).build());
        Category c14 = categoryRepository.save(Category.builder().merchant(m1).name("汤类").sortOrder(4).build());
        Category c15 = categoryRepository.save(Category.builder().merchant(m1).name("主食").sortOrder(5).build());

        productRepository.save(Product.builder().merchant(m1).category(c11).name("红烧肉").image("/images/food1.svg").price(new BigDecimal("28.00")).stock(100).sales(520).description("精选五花肉，文火慢炖").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c11).name("宫保鸡丁").image("/images/food2.svg").price(new BigDecimal("22.00")).stock(100).sales(480).description("花生鸡丁，麻辣鲜香").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c12).name("糖醋排骨").image("/images/food3.svg").price(new BigDecimal("32.00")).stock(80).sales(350).description("酸甜可口，肉质鲜嫩").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c12).name("鱼香肉丝").image("/images/food4.svg").price(new BigDecimal("24.00")).stock(90).sales(410).description("经典川菜，下饭神器").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c12).name("回锅肉").image("/images/food5.svg").price(new BigDecimal("26.00")).stock(70).sales(300).description("肥而不腻，蒜香浓郁").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c13).name("清炒时蔬").image("/images/food6.svg").price(new BigDecimal("14.00")).stock(120).sales(550).description("当季新鲜时蔬").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c13).name("麻婆豆腐").image("/images/food7.svg").price(new BigDecimal("16.00")).stock(80).sales(390).description("麻辣鲜香嫩烫").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c13).name("番茄炒蛋").image("/images/food8.svg").price(new BigDecimal("12.00")).stock(100).sales(600).description("家常经典，营养美味").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c14).name("紫菜蛋花汤").image("/images/food9.svg").price(new BigDecimal("8.00")).stock(150).sales(700).description("清淡爽口").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c14).name("排骨玉米汤").image("/images/food10.svg").price(new BigDecimal("18.00")).stock(60).sales(260).description("滋补养生，汤汁浓郁").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c15).name("米饭").image("/images/food11.svg").price(new BigDecimal("2.00")).stock(999).sales(3000).description("东北优质大米").status("上架").build());
        productRepository.save(Product.builder().merchant(m1).category(c15).name("蛋炒饭").image("/images/food12.svg").price(new BigDecimal("10.00")).stock(80).sales(430).description("粒粒分明，蛋香浓郁").status("上架").build());

        // ===== 商家2: 甜蜜时光烘焙 =====
        User merchantUser2 = userRepository.save(User.builder()
                .username("merchant2")
                .phone("13900139001")
                .password(passwordEncoder.encode("123456"))
                .email("merchant2@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());

        Merchant m2 = merchantRepository.save(Merchant.builder()
                .user(merchantUser2)
                .shopName("甜蜜时光烘焙")
                .shopAddress("北京市海淀区中关村大街15号")
                .businessLicense("SC10000000000002")
                .description("新鲜手作甜品，采用进口原料，每日限量供应")
                .rating(new BigDecimal("4.9"))
                .monthlySales(3200)
                .deliveryFee(new BigDecimal("5.00"))
                .minOrderAmount(new BigDecimal("20.00"))
                .businessHours("10:00-21:00")
                .status("营业中")
                .build());

        Category c21 = categoryRepository.save(Category.builder().merchant(m2).name("人气蛋糕").sortOrder(1).build());
        Category c22 = categoryRepository.save(Category.builder().merchant(m2).name("面包").sortOrder(2).build());
        Category c23 = categoryRepository.save(Category.builder().merchant(m2).name("饮品").sortOrder(3).build());

        productRepository.save(Product.builder().merchant(m2).category(c21).name("草莓慕斯蛋糕").image("/images/dessert1.svg").price(new BigDecimal("38.00")).stock(30).sales(680).description("新鲜草莓搭配丝滑慕斯").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c21).name("提拉米苏").image("/images/dessert2.svg").price(new BigDecimal("32.00")).stock(25).sales(550).description("经典意式甜品").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c21).name("芒果千层蛋糕").image("/images/dessert3.svg").price(new BigDecimal("42.00")).stock(20).sales(420).description("层层芒果，香甜可口").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c22).name("牛角可颂").image("/images/dessert4.svg").price(new BigDecimal("15.00")).stock(50).sales(800).description("香酥层层分明").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c22).name("全麦吐司").image("/images/dessert5.svg").price(new BigDecimal("18.00")).stock(40).sales(350).description("健康全麦，低糖低油").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c23).name("经典拿铁").image("/images/dessert6.svg").price(new BigDecimal("22.00")).stock(100).sales(620).description("浓郁咖啡与丝滑牛奶").status("上架").build());
        productRepository.save(Product.builder().merchant(m2).category(c23).name("杨枝甘露").image("/images/dessert7.svg").price(new BigDecimal("20.00")).stock(60).sales(480).description("芒果西柚椰奶经典搭配").status("上架").build());

        // ===== 商家3: 鲜果鲜生 =====
        User merchantUser3 = userRepository.save(User.builder()
                .username("merchant3")
                .phone("13900139002")
                .password(passwordEncoder.encode("123456"))
                .email("merchant3@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());

        Merchant m3 = merchantRepository.save(Merchant.builder()
                .user(merchantUser3)
                .shopName("鲜果鲜生")
                .shopAddress("北京市西城区金融街10号")
                .businessLicense("SC10000000000003")
                .description("产地直采新鲜水果，品质保证")
                .rating(new BigDecimal("4.7"))
                .monthlySales(1850)
                .deliveryFee(new BigDecimal("3.00"))
                .minOrderAmount(new BigDecimal("10.00"))
                .businessHours("08:00-20:00")
                .status("营业中")
                .build());

        Category c31 = categoryRepository.save(Category.builder().merchant(m3).name("时令水果").sortOrder(1).build());
        Category c32 = categoryRepository.save(Category.builder().merchant(m3).name("进口水果").sortOrder(2).build());
        Category c33 = categoryRepository.save(Category.builder().merchant(m3).name("果切拼盘").sortOrder(3).build());

        productRepository.save(Product.builder().merchant(m3).category(c31).name("新疆哈密瓜").image("/images/fruit1.svg").price(new BigDecimal("25.00")).stock(50).sales(340).description("甜蜜多汁").status("上架").build());
        productRepository.save(Product.builder().merchant(m3).category(c31).name("海南芒果").image("/images/fruit2.svg").price(new BigDecimal("18.00")).stock(40).sales(280).description("香甜软糯").status("上架").build());
        productRepository.save(Product.builder().merchant(m3).category(c32).name("智利车厘子").image("/images/fruit3.svg").price(new BigDecimal("58.00")).stock(20).sales(150).description("进口新鲜").status("上架").build());
        productRepository.save(Product.builder().merchant(m3).category(c32).name("泰国金枕头榴莲").image("/images/fruit4.svg").price(new BigDecimal("128.00")).stock(10).sales(85).description("树上熟榴莲").status("上架").build());
        productRepository.save(Product.builder().merchant(m3).category(c33).name("鲜果拼盘 A").image("/images/fruit5.svg").price(new BigDecimal("30.00")).stock(30).sales(220).description("5种时令水果拼配").status("上架").build());

        // ===== 商家4: 深夜烧烤 =====
        User merchantUser4 = userRepository.save(User.builder()
                .username("merchant4")
                .phone("13900139003")
                .password(passwordEncoder.encode("123456"))
                .email("merchant4@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());
        Merchant m4 = merchantRepository.save(Merchant.builder()
                .user(merchantUser4)
                .shopName("深夜烧烤")
                .shopAddress("北京市朝阳区望京街道108号")
                .businessLicense("SC10000000000004")
                .description("炭火现烤，秘制酱料，夜宵首选！营业至凌晨2点")
                .rating(new BigDecimal("4.6"))
                .monthlySales(3200)
                .deliveryFee(new BigDecimal("3.00"))
                .minOrderAmount(new BigDecimal("25.00"))
                .businessHours("17:00-02:00")
                .status("营业中")
                .build());
        Category c41 = categoryRepository.save(Category.builder().merchant(m4).name("招牌烤串").sortOrder(1).build());
        Category c42 = categoryRepository.save(Category.builder().merchant(m4).name("锡纸系列").sortOrder(2).build());
        Category c43 = categoryRepository.save(Category.builder().merchant(m4).name("酒水饮料").sortOrder(3).build());
        productRepository.save(Product.builder().merchant(m4).category(c41).name("羊肉串(10串)").image("/images/food1.svg").price(new BigDecimal("35.00")).stock(200).sales(1200).description("内蒙古鲜羊现切现串").status("上架").build());
        productRepository.save(Product.builder().merchant(m4).category(c41).name("牛肉串(10串)").image("/images/food2.svg").price(new BigDecimal("38.00")).stock(180).sales(950).description("上等牛里脊，嫩滑多汁").status("上架").build());
        productRepository.save(Product.builder().merchant(m4).category(c41).name("鸡翅中(5串)").image("/images/food3.svg").price(new BigDecimal("22.00")).stock(150).sales(800).description("蜜汁腌制，外焦里嫩").status("上架").build());
        productRepository.save(Product.builder().merchant(m4).category(c42).name("锡纸金针菇").image("/images/food6.svg").price(new BigDecimal("15.00")).stock(100).sales(600).description("蒜蓉粉丝金针菇").status("上架").build());
        productRepository.save(Product.builder().merchant(m4).category(c42).name("锡纸花甲粉").image("/images/food9.svg").price(new BigDecimal("28.00")).stock(80).sales(450).description("鲜活花甲配红薯粉").status("上架").build());
        productRepository.save(Product.builder().merchant(m4).category(c43).name("青岛啤酒").image("/images/food11.svg").price(new BigDecimal("8.00")).stock(500).sales(2000).description("冰镇青岛，烧烤绝配").status("上架").build());

        // ===== 商家5: 瑞幸咖啡 =====
        User merchantUser5 = userRepository.save(User.builder()
                .username("merchant5")
                .phone("13900139004")
                .password(passwordEncoder.encode("123456"))
                .email("merchant5@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());
        Merchant m5 = merchantRepository.save(Merchant.builder()
                .user(merchantUser5)
                .shopName("瑞幸咖啡")
                .shopAddress("北京市海淀区中关村软件园C座")
                .businessLicense("SC10000000000005")
                .description("专业精品咖啡，新鲜烘焙，每日直送")
                .rating(new BigDecimal("4.8"))
                .monthlySales(5600)
                .deliveryFee(new BigDecimal("0"))
                .minOrderAmount(new BigDecimal("15.00"))
                .businessHours("07:00-21:00")
                .status("营业中")
                .build());
        Category c51 = categoryRepository.save(Category.builder().merchant(m5).name("经典咖啡").sortOrder(1).build());
        Category c52 = categoryRepository.save(Category.builder().merchant(m5).name("果茶系列").sortOrder(2).build());
        Category c53 = categoryRepository.save(Category.builder().merchant(m5).name("烘焙轻食").sortOrder(3).build());
        productRepository.save(Product.builder().merchant(m5).category(c51).name("美式咖啡").image("/images/dessert6.svg").price(new BigDecimal("15.00")).stock(200).sales(1800).description("经典美式，醇厚回甘").status("上架").build());
        productRepository.save(Product.builder().merchant(m5).category(c51).name("拿铁").image("/images/dessert6.svg").price(new BigDecimal("18.00")).stock(200).sales(2200).description("意式浓缩与丝滑牛奶").status("上架").build());
        productRepository.save(Product.builder().merchant(m5).category(c51).name("生椰拿铁").image("/images/dessert7.svg").price(new BigDecimal("22.00")).stock(150).sales(1600).description("爆款椰子风味拿铁").status("上架").build());
        productRepository.save(Product.builder().merchant(m5).category(c52).name("葡萄冰萃").image("/images/dessert7.svg").price(new BigDecimal("20.00")).stock(120).sales(900).description("手剥葡萄，清爽冰萃").status("上架").build());
        productRepository.save(Product.builder().merchant(m5).category(c52).name("芒果茉莉茶").image("/images/fruit2.svg").price(new BigDecimal("18.00")).stock(100).sales(750).description("芒果与茉莉花茶融合").status("上架").build());
        productRepository.save(Product.builder().merchant(m5).category(c53).name("巧克力麦芬").image("/images/dessert4.svg").price(new BigDecimal("12.00")).stock(80).sales(500).description("浓郁巧克力，松软可口").status("上架").build());

        // ===== 商家6: 麦香鸡汉堡 =====
        User merchantUser6 = userRepository.save(User.builder()
                .username("merchant6")
                .phone("13900139005")
                .password(passwordEncoder.encode("123456"))
                .email("merchant6@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());
        Merchant m6 = merchantRepository.save(Merchant.builder()
                .user(merchantUser6)
                .shopName("麦香鸡汉堡")
                .shopAddress("北京市东城区王府井大街201号")
                .businessLicense("SC10000000000006")
                .description("美式快餐，现点现做，30分钟送达")
                .rating(new BigDecimal("4.5"))
                .monthlySales(7800)
                .deliveryFee(new BigDecimal("2.00"))
                .minOrderAmount(new BigDecimal("20.00"))
                .businessHours("09:00-22:00")
                .status("营业中")
                .build());
        Category c61 = categoryRepository.save(Category.builder().merchant(m6).name("超值套餐").sortOrder(1).build());
        Category c62 = categoryRepository.save(Category.builder().merchant(m6).name("汉堡单品").sortOrder(2).build());
        Category c63 = categoryRepository.save(Category.builder().merchant(m6).name("小食饮品").sortOrder(3).build());
        productRepository.save(Product.builder().merchant(m6).category(c61).name("巨无霸套餐").image("/images/food2.svg").price(new BigDecimal("35.00")).stock(300).sales(2800).description("巨无霸+薯条+可乐").status("上架").build());
        productRepository.save(Product.builder().merchant(m6).category(c61).name("麦辣鸡腿堡套餐").image("/images/food3.svg").price(new BigDecimal("32.00")).stock(250).sales(2400).description("辣堡+鸡翅+可乐").status("上架").build());
        productRepository.save(Product.builder().merchant(m6).category(c62).name("巨无霸").image("/images/food2.svg").price(new BigDecimal("22.00")).stock(200).sales(1500).description("双层牛肉，经典之选").status("上架").build());
        productRepository.save(Product.builder().merchant(m6).category(c62).name("麦辣鸡腿堡").image("/images/food4.svg").price(new BigDecimal("18.00")).stock(200).sales(1800).description("香辣脆皮鸡腿肉").status("上架").build());
        productRepository.save(Product.builder().merchant(m6).category(c63).name("薯条(大)").image("/images/food11.svg").price(new BigDecimal("12.00")).stock(400).sales(3500).description("金黄酥脆，外酥里嫩").status("上架").build());
        productRepository.save(Product.builder().merchant(m6).category(c63).name("可口可乐").image("/images/food11.svg").price(new BigDecimal("8.00")).stock(500).sales(4000).description("冰爽畅快").status("上架").build());

        // ===== 商家7: 幸福西饼 =====
        User merchantUser7 = userRepository.save(User.builder()
                .username("merchant7")
                .phone("13900139006")
                .password(passwordEncoder.encode("123456"))
                .email("merchant7@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());
        Merchant m7 = merchantRepository.save(Merchant.builder()
                .user(merchantUser7)
                .shopName("幸福西饼")
                .shopAddress("北京市西城区西单北大街88号")
                .businessLicense("SC10000000000007")
                .description("现场烘焙，动物奶油，生日蛋糕定制")
                .rating(new BigDecimal("4.9"))
                .monthlySales(4200)
                .deliveryFee(new BigDecimal("5.00"))
                .minOrderAmount(new BigDecimal("30.00"))
                .businessHours("08:00-20:00")
                .status("营业中")
                .build());
        Category c71 = categoryRepository.save(Category.builder().merchant(m7).name("生日蛋糕").sortOrder(1).build());
        Category c72 = categoryRepository.save(Category.builder().merchant(m7).name("甜品切块").sortOrder(2).build());
        Category c73 = categoryRepository.save(Category.builder().merchant(m7).name("面包吐司").sortOrder(3).build());
        productRepository.save(Product.builder().merchant(m7).category(c71).name("经典奶油蛋糕(6寸)").image("/images/dessert1.svg").price(new BigDecimal("168.00")).stock(20).sales(350).description("动物奶油，新鲜水果夹心").status("上架").build());
        productRepository.save(Product.builder().merchant(m7).category(c71).name("黑森林蛋糕(8寸)").image("/images/dessert2.svg").price(new BigDecimal("198.00")).stock(15).sales(280).description("巧克力碎与樱桃的完美搭配").status("上架").build());
        productRepository.save(Product.builder().merchant(m7).category(c72).name("提拉米苏切块").image("/images/dessert2.svg").price(new BigDecimal("28.00")).stock(50).sales(680).description("经典马斯卡彭，入口即化").status("上架").build());
        productRepository.save(Product.builder().merchant(m7).category(c72).name("芒果慕斯杯").image("/images/dessert3.svg").price(new BigDecimal("22.00")).stock(60).sales(550).description("新鲜芒果泥配慕斯").status("上架").build());
        productRepository.save(Product.builder().merchant(m7).category(c73).name("北海道吐司").image("/images/dessert5.svg").price(new BigDecimal("18.00")).stock(80).sales(450).description("绵软拉丝，奶香浓郁").status("上架").build());

        // ===== 商家8: 天天超市 =====
        User merchantUser8 = userRepository.save(User.builder()
                .username("merchant8")
                .phone("13900139007")
                .password(passwordEncoder.encode("123456"))
                .email("merchant8@test.com")
                .role("ROLE_MERCHANT")
                .balance(BigDecimal.ZERO)
                .build());
        Merchant m8 = merchantRepository.save(Merchant.builder()
                .user(merchantUser8)
                .shopName("天天超市")
                .shopAddress("北京市丰台区方庄路55号")
                .businessLicense("SC10000000000008")
                .description("日用百货，零食饮料，一站购齐")
                .rating(new BigDecimal("4.4"))
                .monthlySales(9500)
                .deliveryFee(new BigDecimal("1.00"))
                .minOrderAmount(new BigDecimal("10.00"))
                .businessHours("07:00-23:00")
                .status("营业中")
                .build());
        Category c81 = categoryRepository.save(Category.builder().merchant(m8).name("零食").sortOrder(1).build());
        Category c82 = categoryRepository.save(Category.builder().merchant(m8).name("饮料").sortOrder(2).build());
        Category c83 = categoryRepository.save(Category.builder().merchant(m8).name("日用").sortOrder(3).build());
        productRepository.save(Product.builder().merchant(m8).category(c81).name("乐事薯片大礼包").image("/images/food11.svg").price(new BigDecimal("29.90")).stock(200).sales(1200).description("3种口味组合装").status("上架").build());
        productRepository.save(Product.builder().merchant(m8).category(c81).name("良品铺子坚果礼盒").image("/images/fruit5.svg").price(new BigDecimal("88.00")).stock(100).sales(680).description("6种坚果混合装").status("上架").build());
        productRepository.save(Product.builder().merchant(m8).category(c82).name("农夫山泉(24瓶)").image("/images/food11.svg").price(new BigDecimal("28.00")).stock(300).sales(1800).description("天然矿泉水整箱装").status("上架").build());
        productRepository.save(Product.builder().merchant(m8).category(c82).name("元气森林(6罐)").image("/images/dessert7.svg").price(new BigDecimal("19.90")).stock(250).sales(950).description("0糖0卡气泡水").status("上架").build());
        productRepository.save(Product.builder().merchant(m8).category(c83).name("维达纸巾(10包装)").image("/images/food11.svg").price(new BigDecimal("25.90")).stock(150).sales(720).description("超韧三层，不掉屑").status("上架").build());

        log.info("测试数据初始化完成！共创建 8 个商家、{} 个商品", 59);
    }
}
