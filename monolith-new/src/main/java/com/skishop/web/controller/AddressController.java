package com.skishop.web.controller;

import com.skishop.dao.address.UserAddressDao;
import com.skishop.domain.address.Address;
import com.skishop.domain.user.User;
import com.skishop.web.form.AddressForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class AddressController {

    private static final int MAX_ADDRESSES_PER_USER = 10;

    private final UserAddressDao userAddressDao;

    public AddressController(UserAddressDao userAddressDao) {
        this.userAddressDao = userAddressDao;
    }

    @GetMapping("/account/addresses")
    public String list(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (user == null) {
            return "redirect:/login";
        }
        List<Address> addresses = userAddressDao.listByUserId(user.getId());
        model.addAttribute("addresses", addresses);
        return "account/addresses";
    }

    @GetMapping("/account/addresses/edit")
    public String editForm(@RequestParam(value = "id", required = false) String id,
                           HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("addressForm", new AddressForm());
        return "account/address_edit";
    }

    @PostMapping("/account/addresses/save")
    public String save(@Valid @ModelAttribute AddressForm form, BindingResult bindingResult,
                       HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("addressForm", form);
            return "account/address_edit";
        }

        List<Address> existing = userAddressDao.listByUserId(user.getId());
        if (existing != null && existing.size() >= MAX_ADDRESSES_PER_USER) {
            List<String> errors = new ArrayList<>();
            errors.add("Maximum number of addresses reached.");
            model.addAttribute("errors", errors);
            model.addAttribute("addressForm", form);
            return "account/address_edit";
        }

        Address address = new Address();
        address.setId(resolveAddressId(form));
        address.setUserId(user.getId());
        address.setLabel(form.getLabel());
        address.setRecipientName(form.getRecipientName());
        address.setPostalCode(form.getPostalCode());
        address.setPrefecture(form.getPrefecture());
        address.setAddress1(form.getAddress1());
        address.setAddress2(form.getAddress2());
        address.setPhone(form.getPhone());
        address.setDefault(form.getIsDefault());
        Date now = new Date();
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        userAddressDao.save(address);

        return "redirect:/account/addresses";
    }

    private String resolveAddressId(AddressForm form) {
        if (form.getId() != null && !form.getId().isEmpty()) {
            return form.getId();
        }
        return UUID.randomUUID().toString();
    }
}
