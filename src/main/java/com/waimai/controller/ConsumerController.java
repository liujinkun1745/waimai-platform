package com.waimai.controller;

import com.waimai.entity.*;
import com.waimai.repository.*;
import com.waimai.service.*;
import com.waimai.service.OrderService.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 消费者端控制器
 */
@Controller
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private static final java.util.List<String> DAILY_COUPON_NAMES = java.util.List.of(
        "满50减20","满30减10","满20减5","满100减30","满15减3","无门槛红包","满40减15","满60减25");

    private final UserService userService;
    private final MerchantService merchantService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final BalanceService balanceService;
    private final ReviewService reviewService;
    private final CouponService couponService;
    private final CouponRepository couponRepository;

    /** 获取当前登录用户 */
    private User currentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    // ============ 首页 ============

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "sales") String sort,
                         Model model,
                         jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        User user = currentUser(userDetails);
        model.addAttribute("user", user);

        List<Merchant> merchants;
        if ("rating".equals(sort)) {
            merchants = merchantService.listByRating();
        } else {
            merchants = merchantService.listBySales();
        }

        // 搜索过滤
        if (keyword != null && !keyword.isBlank()) {
            merchants = merchantService.searchOpenMerchants(keyword);
        }

        // 计算当月实时的月销量（本月已完成订单数）
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate firstOfMonth = today.withDayOfMonth(1);
        Map<Long, Integer> monthlyOrderCounts = new HashMap<>();
        for (Merchant m : merchants) {
            List<Order> orders = orderService.listByMerchant(m.getId(), null);
            long count = orders.stream()
                    .filter(o -> "已完成".equals(o.getStatus())
                            && !o.getCreatedAt().toLocalDate().isBefore(firstOfMonth))
                    .count();
            monthlyOrderCounts.put(m.getId(), (int) count);
        }

        model.addAttribute("merchants", merchants);
        model.addAttribute("monthlyOrderCounts", monthlyOrderCounts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("couponCount", couponService.countAvailable(user.getId()));
        model.addAttribute("activeTab", "home");
        return "consumer/index";
    }

    // ============ 商家详情 ============

    @GetMapping("/merchant/{id}")
    public String merchantDetail(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        User user = currentUser(userDetails);
        Merchant merchant = merchantService.findById(id);
        List<Category> categories = categoryService.listByMerchant(id);

        // 获取第一个分类的商品作为默认显示
        List<Product> products = new ArrayList<>();
        Long firstCategoryId = null;
        if (!categories.isEmpty()) {
            firstCategoryId = categories.get(0).getId();
            products = productService.listByCategory(id, firstCategoryId);
        }

        // 评价 + 评分分布 + 关联订单商品
        List<Review> reviews = reviewService.listByMerchant(id);
        long reviewCount = reviewService.countByMerchant(id);
        Map<Long, List<OrderItem>> reviewItemsMap = orderService.getOrderItemsBatch(
                reviews.stream().filter(r -> r.getOrder() != null)
                        .map(r -> r.getOrder().getId()).toList());
        int[] ratingDist = new int[5];
        for (Review r : reviews) {
            int overall = (int) Math.round(r.getOverallRating());
            if (overall >= 5) ratingDist[0]++;
            else if (overall >= 4) ratingDist[1]++;
            else if (overall >= 3) ratingDist[2]++;
            else if (overall >= 2) ratingDist[3]++;
            else ratingDist[4]++;
        }
        model.addAttribute("user", user);
        model.addAttribute("merchant", merchant);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("currentCategoryId", firstCategoryId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("ratingDist", ratingDist);
        model.addAttribute("ratingDistMax", Math.max(1, reviewCount));
        model.addAttribute("reviewItemsMap", reviewItemsMap);
        model.addAttribute("couponCount", couponService.countAvailable(user.getId()));
        return "consumer/merchant-detail";
    }

    /** 按分类加载商品（AJAX） */
    @GetMapping("/merchant/{merchantId}/category/{categoryId}")
    @ResponseBody
    public List<Product> loadProducts(@PathVariable Long merchantId,
                                       @PathVariable Long categoryId) {
        return productService.listByCategory(merchantId, categoryId);
    }

    // ============ 订单 ============

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) String status,
                          Model model,
                          jakarta.servlet.http.HttpServletResponse response) {
        // 禁止浏览器缓存，确保每次刷新拿到最新订单
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        User user = currentUser(userDetails);
        List<Order> orders = orderService.listByConsumer(user.getId(), status);
        // 预加载订单商品项
        Map<Long, List<OrderItem>> itemsMap =
                orderService.getOrderItemsBatch(orders.stream().map(Order::getId).toList());
        // 标记哪些订单已评价
        Map<Long, Boolean> reviewedMap = new HashMap<>();
        for (Order o : orders) {
            reviewedMap.put(o.getId(), reviewService.existsByOrder(o.getId()));
        }
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("itemsMap", itemsMap);
        model.addAttribute("reviewedMap", reviewedMap);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeTab", "orders");
        return "consumer/orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = currentUser(userDetails);
        Order order = orderService.findById(id);
        List<OrderItem> items = orderService.getOrderItems(id);
        boolean reviewed = reviewService.existsByOrder(id);
        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("reviewed", reviewed);
        return "consumer/order-detail";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            orderService.cancelByConsumer(id, user.getId());
            ra.addFlashAttribute("message", "订单已取消");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/consumer/orders";
    }

    @PostMapping("/order/confirm/{id}")
    public String confirmReceived(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            orderService.confirmReceived(id, user.getId());
            ra.addFlashAttribute("message", "已确认收货");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/consumer/orders";
    }

    // ============ 评价 ============

    @GetMapping("/order/{id}/review")
    public String reviewPage(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = currentUser(userDetails);
        Order order = orderService.findById(id);
        if (!order.getConsumer().getId().equals(user.getId()))
            throw new RuntimeException("无权操作");
        if (reviewService.existsByOrder(id))
            return "redirect:/consumer/orders";
        model.addAttribute("user", user);
        model.addAttribute("order", order);
        return "consumer/review";
    }

    @PostMapping("/order/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam int tasteRating,
                               @RequestParam int packagingRating,
                               @RequestParam int deliveryRating,
                               @RequestParam(required = false) String comment,
                               RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            reviewService.submit(id, user.getId(), tasteRating, packagingRating, deliveryRating, comment);
            ra.addFlashAttribute("message", "评价提交成功！感谢您的反馈");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/consumer/orders";
    }

    /** 提交订单 */
    @PostMapping("/order/submit")
    public String submitOrder(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam Long merchantId,
                               @RequestParam Long addressId,
                               @RequestParam String cartData,
                               @RequestParam BigDecimal totalAmount,
                               @RequestParam(required = false, defaultValue = "0") Long couponId,
                               RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            List<CartItem> cartItems = parseCartData(cartData);
            // 应用优惠券
            BigDecimal finalAmount = totalAmount;
            if (couponId > 0) {
                Coupon coupon = couponRepository.findById(couponId)
                        .orElseThrow(() -> new RuntimeException("优惠券不存在"));
                finalAmount = totalAmount.subtract(coupon.getAmount());
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;
                couponService.useCoupon(couponId, user.getId());
            }
            orderService.submitOrder(user.getId(), merchantId, addressId, cartItems, finalAmount);
            ra.addFlashAttribute("message", "下单成功！");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/consumer/orders";
    }

    private List<CartItem> parseCartData(String cartData) {
        // 格式: productId:quantity,productId:quantity,...
        List<CartItem> items = new ArrayList<>();
        if (cartData == null || cartData.isBlank()) return items;
        for (String part : cartData.split(",")) {
            String[] kv = part.split(":");
            Long productId = Long.valueOf(kv[0]);
            Integer quantity = Integer.valueOf(kv[1]);
            Product product = productService.findById(productId);
            items.add(new CartItem(product.getId(), product.getName(),
                    product.getImage(), product.getPrice(), quantity));
        }
        return items;
    }

    // ============ 地址管理 ============

    @GetMapping("/address")
    public String addressList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        List<Address> addresses = addressService.listByConsumer(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "consumer/address";
    }

    @PostMapping("/address/add")
    public String addAddress(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String receiverName,
                              @RequestParam String receiverPhone,
                              @RequestParam String province,
                              @RequestParam String city,
                              @RequestParam String district,
                              @RequestParam String detailAddress,
                              @RequestParam(required = false, defaultValue = "false") Boolean isDefault,
                              RedirectAttributes ra) {
        User user = currentUser(userDetails);
        addressService.add(user.getId(), receiverName, receiverPhone, province, city,
                district, detailAddress, isDefault);
        ra.addFlashAttribute("message", "地址添加成功");
        return "redirect:/consumer/address";
    }

    @PostMapping("/address/edit/{id}")
    public String editAddress(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String receiverName,
                               @RequestParam String receiverPhone,
                               @RequestParam String province,
                               @RequestParam String city,
                               @RequestParam String district,
                               @RequestParam String detailAddress,
                               @RequestParam(required = false, defaultValue = "false") Boolean isDefault,
                               RedirectAttributes ra) {
        User user = currentUser(userDetails);
        addressService.update(id, user.getId(), receiverName, receiverPhone, province,
                city, district, detailAddress, isDefault);
        ra.addFlashAttribute("message", "地址更新成功");
        return "redirect:/consumer/address";
    }

    @GetMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes ra) {
        addressService.delete(id);
        ra.addFlashAttribute("message", "地址已删除");
        return "redirect:/consumer/address";
    }

    // ============ 优惠券 ============

    @GetMapping("/coupons")
    @ResponseBody
    public List<Map<String, Object>> coupons(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        return couponService.getAvailable(user.getId()).stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("amount", c.getAmount());
            m.put("minOrder", c.getMinOrder());
            return m;
        }).toList();
    }

    // ============ 神券页面 ============

    @GetMapping("/coupons-page")
    public String couponsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("coupons", couponService.getAvailable(user.getId()));
        model.addAttribute("couponCount", couponService.countAvailable(user.getId()));
        model.addAttribute("activeTab", "coupons");
        return "consumer/coupons-page";
    }

    // ============ 每日神券 ============

    @GetMapping("/coupons/daily")
    @ResponseBody
    public Map<String, Object> dailyCoupons(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        java.time.LocalDate today = java.time.LocalDate.now();
        String[][] templates = {{"满50减20","20.00","50.00"},{"满30减10","10.00","30.00"},
                {"满20减5","5.00","20.00"},{"满100减30","30.00","100.00"},
                {"满15减3","3.00","15.00"},{"无门槛红包","6.00","0"},
                {"满40减15","15.00","40.00"},{"满60减25","25.00","60.00"}};
        // 用日期+用户ID做种子，保证同一天同一用户看到相同3张券
        long seed = today.toEpochDay() * 1000 + user.getId();
        java.util.Random rand = new java.util.Random(seed);
        java.util.List<Map<String, Object>> picks = new ArrayList<>();
        java.util.Set<Integer> used = new java.util.HashSet<>();
        for (int i = 0; i < 3; i++) {
            int idx;
            do { idx = rand.nextInt(templates.length); } while (used.contains(idx));
            used.add(idx);
            Map<String, Object> m = new HashMap<>();
            m.put("name", templates[idx][0]);
            m.put("amount", templates[idx][1]);
            m.put("minOrder", templates[idx][2]);
            // 检查该模板今天是否已被领取
            String tplName = templates[idx][0];
            boolean thisClaimed = couponRepository.findByUserId(user.getId()).stream()
                    .anyMatch(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().equals(today)
                            && c.getName().equals(tplName));
            m.put("claimed", thisClaimed);
            picks.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("coupons", picks);
        return result;
    }

    @PostMapping("/coupons/claim")
    @ResponseBody
    public Map<String, Object> claimCoupon(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam String name, @RequestParam String amount,
                                            @RequestParam String minOrder) {
        User user = currentUser(userDetails);
        // 每天只能领一张神券
        java.time.LocalDate today = java.time.LocalDate.now();
        boolean claimedToday = couponRepository.findByUserId(user.getId()).stream()
                .anyMatch(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().equals(today)
                        && DAILY_COUPON_NAMES.contains(c.getName()));
        Map<String, Object> r = new HashMap<>();
        if (claimedToday) {
            r.put("ok", false);
            r.put("msg", "今日已领取过神券，明天再来吧！");
            return r;
        }
        Coupon c = Coupon.builder().user(user).name(name)
                .amount(new BigDecimal(amount)).minOrder(new BigDecimal(minOrder))
                .used(false).expiresAt(LocalDateTime.now().plusDays(7)).build();
        couponRepository.save(c);
        r.put("ok", true);
        r.put("count", couponService.countAvailable(user.getId()));
        return r;
    }

    // ============ 搜索 ============

    @GetMapping("/search")
    public String search(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) String keyword,
                         Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "search");

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("keyword", keyword);
            model.addAttribute("merchants", merchantService.searchOpenMerchants(keyword));
        }
        model.addAttribute("hotMerchants", merchantService.listBySales());
        return "consumer/search";
    }

    // ============ 我的 ============

    @GetMapping("/my")
    public String my(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "my");
        return "consumer/my";
    }

    // ============ 个人中心 ============

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("user", user);
        return "consumer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String email,
                                 RedirectAttributes ra) {
        User user = currentUser(userDetails);
        userService.updateProfile(user.getId(), email);
        ra.addFlashAttribute("message", "信息更新成功");
        return "redirect:/consumer/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            userService.changePassword(user.getId(), oldPassword, newPassword);
            ra.addFlashAttribute("message", "密码修改成功");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/consumer/profile";
    }

    // ============ 余额 ============

    @GetMapping("/balance")
    public String balance(@AuthenticationPrincipal UserDetails userDetails, Model model,
                           jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        User user = currentUser(userDetails);
        List<BalanceRecord> records = balanceService.listRecords(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("records", records);
        return "consumer/balance";
    }

    @PostMapping("/balance/recharge")
    public String recharge(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam BigDecimal amount,
                            RedirectAttributes ra) {
        User user = currentUser(userDetails);
        userService.recharge(user.getId(), amount);
        ra.addFlashAttribute("message", "充值成功！");
        return "redirect:/consumer/balance";
    }
}
