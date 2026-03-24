package com.skishop.web.controller;

import com.skishop.domain.address.Address;
import com.skishop.domain.user.User;
import com.skishop.service.address.AddressService;
import com.skishop.web.form.AddressForm;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AddressController {
    private static final int MAX_ADDRESSES = 10;
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/account/addresses")
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("addresses", addressService.listByUserId(user.getId()));
        return "account/addresses";
    }

    @GetMapping("/account/addresses/edit")
    public String edit(@RequestParam(required = false) String id, HttpSession session, Model model) {
        AddressForm form = new AddressForm();
        if (id != null && !id.isEmpty()) {
            Address addr = addressService.findById(id);
            if (addr != null) {
                form.setId(addr.getId()); form.setLabel(addr.getLabel());
                form.setRecipientName(addr.getRecipientName()); form.setPostalCode(addr.getPostalCode());
                form.setPrefecture(addr.getPrefecture()); form.setAddress1(addr.getAddress1());
                form.setAddress2(addr.getAddress2()); form.setPhone(addr.getPhone());
                form.setIsDefault(addr.isDefault());
            }
        }
        model.addAttribute("addressForm", form);
        return "account/address_edit";
    }

    @PostMapping("/account/addresses/save")
    public String save(@ModelAttribute AddressForm form, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (addressService.countByUserId(user.getId()) >= MAX_ADDRESSES && (form.getId() == null || form.getId().isEmpty())) {
            model.addAttribute("errorMessage", "住所は最大10件です");
            model.addAttribute("addressForm", form);
            return "account/address_edit";
        }
        Address address = new Address();
        address.setId(form.getId() != null && !form.getId().isEmpty() ? form.getId() : UUID.randomUUID().toString());
        address.setUserId(user.getId());
        address.setLabel(form.getLabel()); address.setRecipientName(form.getRecipientName());
        address.setPostalCode(form.getPostalCode()); address.setPrefecture(form.getPrefecture());
        address.setAddress1(form.getAddress1()); address.setAddress2(form.getAddress2());
        address.setPhone(form.getPhone()); address.setDefault(form.getIsDefault());
        Date now = new Date();
        address.setCreatedAt(now); address.setUpdatedAt(now);
        addressService.save(address);
        return "redirect:/account/addresses";
    }
}
