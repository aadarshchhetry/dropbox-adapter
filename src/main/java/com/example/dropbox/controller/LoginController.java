package com.example.dropbox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;


    @GetMapping("/login")
    public String getLoginPage(Model model) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("dbx");
        String authorizationUri = "/oauth2/authorization/" + registration.getRegistrationId();

        model.addAttribute("authorizationUri", authorizationUri);

        return "login";
    }

    @GetMapping("/success")
    public String getSuccessPage() {
        return "success";
    }
}
