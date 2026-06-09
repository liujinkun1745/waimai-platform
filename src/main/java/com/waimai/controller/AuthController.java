package com.waimai.controller;

import com.waimai.entity.User;
import com.waimai.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 认证控制器 — 登录、注册、登出、角色路由
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 首页 — 已登录按角色跳转，未登录去登录页
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return redirectAfterLogin(authentication);
        }
        return "redirect:/login";
    }

    /**
     * 登录后根据角色重定向
     */
    @GetMapping("/auth/redirect")
    public String redirectAfterLogin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_CONSUMER".equals(role)) {
                return "redirect:/consumer/index";
            } else if ("ROLE_MERCHANT".equals(role)) {
                return "redirect:/merchant/index";
            }
        }
        return "redirect:/";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        if (logout != null) {
            model.addAttribute("message", "您已安全登出");
        }
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 消费者注册
     */
    @PostMapping("/auth/register/consumer")
    public String registerConsumer(@RequestParam String username,
                                    @RequestParam String phone,
                                    @RequestParam String password,
                                    @RequestParam(required = false) String email,
                                    RedirectAttributes ra) {
        try {
            userService.registerConsumer(username, phone, password,
                    email != null ? email : "");
            ra.addFlashAttribute("message", "注册成功，请登录");
            return "redirect:/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * 商家注册
     */
    @PostMapping("/auth/register/merchant")
    public String registerMerchant(@RequestParam String username,
                                    @RequestParam String phone,
                                    @RequestParam String password,
                                    @RequestParam String shopName,
                                    @RequestParam String shopAddress,
                                    @RequestParam String businessLicense,
                                    @RequestParam(required = false) String description,
                                    RedirectAttributes ra) {
        try {
            userService.registerMerchant(username, phone, password, shopName,
                    shopAddress, businessLicense,
                    description != null ? description : "");
            ra.addFlashAttribute("message", "商家注册成功，请登录");
            return "redirect:/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
