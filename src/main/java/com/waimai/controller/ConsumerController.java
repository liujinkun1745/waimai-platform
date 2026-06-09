package com.waimai.controller;

import com.waimai.entity.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消费者端控制器
 */
@Controller
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final UserService userService;
    private final MerchantService merchantService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final BalanceService balanceService;

    /** 获取当前登录用户 */
    private User currentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    // ============ 首页 ============

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "sales") String sort,
                         Model model) {
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

        model.addAttribute("merchants", merchants);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
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

        model.addAttribute("user", user);
        model.addAttribute("merchant", merchant);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("currentCategoryId", firstCategoryId);
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
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("itemsMap", itemsMap);
        model.addAttribute("currentStatus", status);
        return "consumer/orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = currentUser(userDetails);
        Order order = orderService.findById(id);
        List<OrderItem> items = orderService.getOrderItems(id);
        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
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

    /** 提交订单 */
    @PostMapping("/order/submit")
    public String submitOrder(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam Long merchantId,
                               @RequestParam Long addressId,
                               @RequestParam String cartData,
                               @RequestParam BigDecimal totalAmount,
                               RedirectAttributes ra) {
        try {
            User user = currentUser(userDetails);
            List<CartItem> cartItems = parseCartData(cartData);
            orderService.submitOrder(user.getId(), merchantId, addressId, cartItems, totalAmount);
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
