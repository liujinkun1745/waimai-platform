package com.waimai.controller;

import com.waimai.entity.*;
import com.waimai.repository.*;
import com.waimai.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 商家端控制器
 */
@Controller
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final UserService userService;
    private final MerchantService merchantService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    /** 获取当前商家 */
    private Merchant currentMerchant(UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return merchantService.findByUserId(user.getId());
    }

    // ============ 首页仪表盘 ============

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        User user = merchant.getUser();
        model.addAttribute("user", user);
        model.addAttribute("merchant", merchant);

        // 待处理订单数
        List<Order> pendingOrders = orderService.listByMerchant(merchant.getId(), "待接单");
        model.addAttribute("pendingCount", pendingOrders.size());

        // 今日订单
        List<Order> todayOrders = orderService.listByMerchant(merchant.getId(), null);
        long todayCount = todayOrders.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();
        model.addAttribute("todayOrderCount", todayCount);

        // 商品总数
        List<Product> products = productService.listAll(merchant.getId());
        model.addAttribute("productCount", products.size());

        model.addAttribute("activeTab", "dashboard");
        return "merchant/index";
    }

    // ============ 店铺管理 ============

    @GetMapping("/shop/edit")
    public String editShop(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        model.addAttribute("merchant", merchant);
        return "merchant/shop-edit";
    }

    @PostMapping("/shop/update")
    public String updateShop(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String shopName,
                              @RequestParam(required = false) String shopAvatar,
                              @RequestParam String description,
                              @RequestParam String businessHours,
                              @RequestParam BigDecimal deliveryFee,
                              @RequestParam BigDecimal minOrderAmount,
                              RedirectAttributes ra) {
        Merchant merchant = currentMerchant(userDetails);
        merchantService.updateShopInfo(merchant.getId(), shopName, shopAvatar,
                description, businessHours, deliveryFee, minOrderAmount);
        ra.addFlashAttribute("message", "店铺信息更新成功");
        return "redirect:/merchant/shop/edit";
    }

    @GetMapping("/shop/toggle-status")
    public String toggleStatus(@AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        Merchant merchant = currentMerchant(userDetails);
        merchantService.toggleStatus(merchant.getId());
        ra.addFlashAttribute("message", "营业状态已切换");
        return "redirect:/merchant/orders";
    }

    // ============ 商品+分类合并页 ============

    @GetMapping("/products-all")
    public String productsAll(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        List<Product> products = productService.listAll(merchant.getId());
        List<Category> categories = categoryService.listByMerchant(merchant.getId());
        // 预计算各分类商品数，避免模板中使用复杂的 .?[] 表达式
        Map<Long, Long> categoryProductCounts = new HashMap<>();
        for (Product p : products) {
            categoryProductCounts.merge(p.getCategory().getId(), 1L, Long::sum);
        }
        model.addAttribute("merchant", merchant);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryProductCounts", categoryProductCounts);
        model.addAttribute("activeTab", "products");
        return "merchant/products-all";
    }

    @PostMapping("/product/move-category")
    @ResponseBody
    public Map<String, Object> moveProduct(@RequestParam Long productId, @RequestParam Long categoryId) {
        Product p = productService.findById(productId);
        p.setCategory(categoryRepository.getReferenceById(categoryId));
        productRepository.save(p);
        return Map.of("ok", true);
    }

    // ============ 分类管理 ============

    @GetMapping("/categories")
    public String categories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        List<Category> categories = categoryService.listByMerchant(merchant.getId());
        model.addAttribute("merchant", merchant);
        model.addAttribute("categories", categories);
        return "merchant/categories";
    }

    @PostMapping("/category/add")
    public String addCategory(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String name,
                               @RequestParam Integer sortOrder,
                               RedirectAttributes ra) {
        Merchant merchant = currentMerchant(userDetails);
        categoryService.add(merchant.getId(), name, sortOrder);
        ra.addFlashAttribute("message", "分类添加成功");
        return "redirect:/merchant/categories";
    }

    @PostMapping("/category/edit/{id}")
    public String editCategory(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam Integer sortOrder,
                                RedirectAttributes ra) {
        categoryService.update(id, name, sortOrder);
        ra.addFlashAttribute("message", "分类更新成功");
        return "redirect:/merchant/categories";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("message", "分类已删除");
        return "redirect:/merchant/categories";
    }

    // ============ 商品管理 ============

    @GetMapping("/products")
    public String products(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        List<Product> products = productService.listAll(merchant.getId());
        List<Category> categories = categoryService.listByMerchant(merchant.getId());
        model.addAttribute("merchant", merchant);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("activeTab", "products");
        return "merchant/products";
    }

    @PostMapping("/product/add")
    public String addProduct(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam Long categoryId,
                              @RequestParam String name,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer stock,
                              @RequestParam(required = false) String image,
                              @RequestParam(required = false) String description,
                              RedirectAttributes ra) {
        Merchant merchant = currentMerchant(userDetails);
        productService.add(merchant.getId(), categoryId, name, price, stock,
                image != null ? image : "/images/food1.svg",
                description != null ? description : "");
        ra.addFlashAttribute("message", "商品添加成功");
        return "redirect:/merchant/products";
    }

    @PostMapping("/product/edit/{id}")
    public String editProduct(@PathVariable Long id,
                               @RequestParam Long categoryId,
                               @RequestParam String name,
                               @RequestParam BigDecimal price,
                               @RequestParam Integer stock,
                               @RequestParam(required = false) String image,
                               @RequestParam(required = false) String description,
                               RedirectAttributes ra) {
        productService.update(id, categoryId, name, price, stock, image, description);
        ra.addFlashAttribute("message", "商品更新成功");
        return "redirect:/merchant/products";
    }

    @GetMapping("/product/toggle/{id}")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.toggleStatus(id);
        ra.addFlashAttribute("message", "商品状态已切换");
        return "redirect:/merchant/products";
    }

    // ============ 订单管理 ============

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false) String status,
                          Model model,
                          jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        Merchant merchant = currentMerchant(userDetails);
        List<Order> orders = orderService.listByMerchant(merchant.getId(), status);
        Map<Long, List<OrderItem>> itemsMap =
                orderService.getOrderItemsBatch(orders.stream().map(Order::getId).toList());
        // 仪表盘数据
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Order> allOrders = orderService.listByMerchant(merchant.getId(), null);
        long pendingCount = allOrders.stream().filter(o -> "待接单".equals(o.getStatus())).count();
        long todayCount = allOrders.stream().filter(o -> o.getCreatedAt().toLocalDate().equals(today)).count();
        BigDecimal todayEarnings = allOrders.stream()
                .filter(o -> "已完成".equals(o.getStatus()) && o.getCreatedAt().toLocalDate().equals(today))
                .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("merchant", merchant);
        model.addAttribute("orders", orders);
        model.addAttribute("itemsMap", itemsMap);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("todayEarnings", todayEarnings);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeTab", "orders");
        return "merchant/orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Merchant merchant = currentMerchant(userDetails);
        Order order = orderService.findById(id);
        List<OrderItem> items = orderService.getOrderItems(id);
        model.addAttribute("merchant", merchant);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "merchant/order-detail";
    }

    @PostMapping("/order/accept/{id}")
    public String acceptOrder(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        try {
            Merchant merchant = currentMerchant(userDetails);
            orderService.acceptOrder(id, merchant.getId());
            ra.addFlashAttribute("message", "已接单");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/merchant/orders";
    }

    @PostMapping("/order/deliver/{id}")
    public String startDelivery(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        try {
            Merchant merchant = currentMerchant(userDetails);
            orderService.startDelivery(id, merchant.getId());
            ra.addFlashAttribute("message", "已开始配送");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/merchant/orders";
    }

    @PostMapping("/order/complete/{id}")
    public String completeOrder(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        try {
            Merchant merchant = currentMerchant(userDetails);
            orderService.completeOrder(id, merchant.getId());
            ra.addFlashAttribute("message", "订单已完成");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/merchant/orders";
    }

    // ============ 评价管理 ============

    @GetMapping("/reviews")
    public String reviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        List<Review> reviews = reviewRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId());
        // 计算评分分布
        int[] ratingDist = new int[6]; // 0-5 星
        Map<Long, Integer> roundedStars = new HashMap<>();
        for (Review r : reviews) {
            int star = (int) Math.round(r.getOverallRating());
            if (star >= 1 && star <= 5) ratingDist[star]++;
            roundedStars.put(r.getId(), star);
        }
        model.addAttribute("merchant", merchant);
        model.addAttribute("reviews", reviews);
        model.addAttribute("ratingDist", ratingDist);
        model.addAttribute("roundedStars", roundedStars);
        model.addAttribute("activeTab", "orders");
        return "merchant/reviews";
    }

    // ============ 评价回复 ============

    @PostMapping("/review/reply/{id}")
    @ResponseBody
    public Map<String, Object> replyReview(@PathVariable Long id, @RequestParam String reply) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("评价不存在"));
        r.setReply(reply);
        reviewRepository.save(r);
        return Map.of("ok", true);
    }

    // ============ 我的 ============

    @GetMapping("/my")
    public String my(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        User user = merchant.getUser();
        model.addAttribute("merchant", merchant);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "my");
        return "merchant/my";
    }

    // ============ 收益统计 ============

    @GetMapping("/earnings")
    public String earnings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Merchant merchant = currentMerchant(userDetails);
        List<Order> allOrders = orderService.listByMerchant(merchant.getId(), null);
        List<Product> products = productService.listAll(merchant.getId());

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);

        // ---- 统计数据 ----
        BigDecimal todayEarnings = BigDecimal.ZERO;
        BigDecimal yesterdayEarnings = BigDecimal.ZERO;
        BigDecimal weekEarnings = BigDecimal.ZERO;
        BigDecimal monthEarnings = BigDecimal.ZERO;
        BigDecimal lastWeekEarnings = BigDecimal.ZERO;
        BigDecimal totalEarnings = BigDecimal.ZERO;
        long todayOrders = 0, weekOrders = 0, monthOrders = 0, totalOrders = 0;

        // 最近 7 天每日收益 + 订单数
        Map<LocalDate, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        Map<LocalDate, Integer> dailyCount = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dailyRevenue.put(d, BigDecimal.ZERO);
            dailyCount.put(d, 0);
        }

        // 最近 30 天每日收益
        Map<LocalDate, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) {
            monthlyRevenue.put(today.minusDays(i), BigDecimal.ZERO);
        }

        // 订单状态统计
        long pendingCount = 0, deliveringCount = 0, completedCount = 0, cancelledCount = 0;

        for (Order order : allOrders) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            BigDecimal amount = order.getTotalAmount();

            switch (order.getStatus()) {
                case "待接单", "待配送" -> pendingCount++;
                case "配送中" -> deliveringCount++;
                case "已完成" -> completedCount++;
                case "已取消" -> cancelledCount++;
            }

            if (!"已完成".equals(order.getStatus())) continue;

            totalEarnings = totalEarnings.add(amount);
            totalOrders++;

            if (orderDate.equals(today)) {
                todayEarnings = todayEarnings.add(amount);
                todayOrders++;
            }
            if (orderDate.equals(today.minusDays(1))) {
                yesterdayEarnings = yesterdayEarnings.add(amount);
            }
            if (!orderDate.isBefore(startOfWeek)) {
                weekEarnings = weekEarnings.add(amount);
                weekOrders++;
            }
            if (!orderDate.isBefore(startOfMonth)) {
                monthEarnings = monthEarnings.add(amount);
                monthOrders++;
            }
            if (!orderDate.isBefore(startOfLastWeek) && orderDate.isBefore(startOfWeek)) {
                lastWeekEarnings = lastWeekEarnings.add(amount);
            }

            // 每日统计
            if (!orderDate.isBefore(today.minusDays(6))) {
                dailyRevenue.merge(orderDate, amount, BigDecimal::add);
                dailyCount.merge(orderDate, 1, Integer::sum);
            }
            if (!orderDate.isBefore(today.minusDays(29))) {
                monthlyRevenue.merge(orderDate, amount, BigDecimal::add);
            }
        }

        // ---- 传递给模板 ----
        model.addAttribute("merchant", merchant);
        model.addAttribute("todayEarnings", todayEarnings);
        model.addAttribute("yesterdayEarnings", yesterdayEarnings);
        model.addAttribute("weekEarnings", weekEarnings);
        model.addAttribute("monthEarnings", monthEarnings);
        model.addAttribute("lastWeekEarnings", lastWeekEarnings);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("todayOrders", todayOrders);
        model.addAttribute("weekOrders", weekOrders);
        model.addAttribute("monthOrders", monthOrders);
        model.addAttribute("totalOrders", totalOrders);

        // 订单状态分布
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("deliveringCount", deliveringCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        // 7 天图表数据
        List<String> chart7Labels = new ArrayList<>();
        List<String> chart7Revenue = new ArrayList<>();
        List<Integer> chart7Orders = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : dailyRevenue.entrySet()) {
            chart7Labels.add(e.getKey().toString().substring(5));
            chart7Revenue.add(e.getValue().toString());
            chart7Orders.add(dailyCount.get(e.getKey()));
        }
        model.addAttribute("chart7Labels", chart7Labels);
        model.addAttribute("chart7Revenue", chart7Revenue);
        model.addAttribute("chart7Orders", chart7Orders);

        // 30 天图表数据
        List<String> chart30Labels = new ArrayList<>();
        List<String> chart30Revenue = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : monthlyRevenue.entrySet()) {
            chart30Labels.add(e.getKey().toString().substring(5));
            chart30Revenue.add(e.getValue().toString());
        }
        model.addAttribute("chart30Labels", chart30Labels);
        model.addAttribute("chart30Revenue", chart30Revenue);

        // 最近完成的订单列表（最多 10 条）
        List<Order> recentDone = allOrders.stream()
                .filter(o -> "已完成".equals(o.getStatus()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();
        // 最近完成订单合计
        BigDecimal recentTotal = recentDone.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("recentOrders", recentDone);
        model.addAttribute("recentOrdersTotal", recentTotal);

        // 商品销量排行
        List<Product> topProducts = products.stream()
                .filter(p -> p.getSales() > 0)
                .sorted((a, b) -> Integer.compare(b.getSales(), a.getSales()))
                .limit(5)
                .toList();
        model.addAttribute("topProducts", topProducts);

        return "merchant/earnings";
    }
}
