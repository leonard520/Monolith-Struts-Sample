package com.skishop.web.controller;

import com.skishop.domain.address.Address;
import com.skishop.domain.user.User;
import com.skishop.service.address.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AddressService addressService;

    private MockHttpSession userSession() {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("_csrfToken", "token");
        return session;
    }

    @Test
    void listAddresses_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/account/addresses"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void listAddresses_authenticated_returns200() throws Exception {
        when(addressService.listByUserId("u-1")).thenReturn(List.of());
        mockMvc.perform(get("/account/addresses").session(userSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("account/addresses"))
            .andExpect(model().attributeExists("addresses"));
    }

    @Test
    void editAddress_newForm_returns200() throws Exception {
        mockMvc.perform(get("/account/addresses/edit").session(userSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("account/address_edit"))
            .andExpect(model().attributeExists("addressForm"));
    }

    @Test
    void editAddress_existingAddress_returns200() throws Exception {
        Address addr = new Address();
        addr.setId("addr-1");
        addr.setLabel("Home");
        addr.setRecipientName("Test User");
        addr.setPostalCode("100-0001");
        addr.setPrefecture("Tokyo");
        addr.setAddress1("Chiyoda");
        when(addressService.findById("addr-1")).thenReturn(addr);

        mockMvc.perform(get("/account/addresses/edit").param("id", "addr-1").session(userSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("account/address_edit"))
            .andExpect(model().attributeExists("addressForm"));
    }

    @Test
    void saveAddress_validData_redirects() throws Exception {
        when(addressService.countByUserId("u-1")).thenReturn(0);

        mockMvc.perform(post("/account/addresses/save")
                .session(userSession())
                .param("label", "Home")
                .param("recipientName", "Test User")
                .param("postalCode", "100-0001")
                .param("prefecture", "Tokyo")
                .param("address1", "Chiyoda 1-1")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account/addresses"));
    }

    @Test
    void saveAddress_atLimit_returnsError() throws Exception {
        when(addressService.countByUserId("u-1")).thenReturn(10);

        mockMvc.perform(post("/account/addresses/save")
                .session(userSession())
                .param("label", "Home")
                .param("recipientName", "Test User")
                .param("postalCode", "100-0001")
                .param("prefecture", "Tokyo")
                .param("address1", "Chiyoda 1-1")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("account/address_edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
