package com.example.dropbox.controller;

import com.example.dropbox.dto.AdminProfileDTO;
import com.example.dropbox.dto.TeamInfoDTO;
import com.example.dropbox.service.DropboxAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class DashboardController {

    @Autowired
    private DropboxAPIService dropboxAPIService;

    @GetMapping("/dashboard")
    public String getDashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {

        log.info("Loading dashboard for user: {}", principal.getName());

        String teamMemberId = principal.getName();
        String email = (String) principal.getAttribute("admin_profile.email");
        String displayName = (String) principal.getAttribute("admin_profile.display_name");

        log.info("Team Member ID: {}, Email: {}, Display Name: {}", teamMemberId, email, displayName);

        AdminProfileDTO adminProfile = new AdminProfileDTO(teamMemberId, email, displayName);

        TeamInfoDTO teamInfo = null;
        String errorMessage = null;

        try {
            teamInfo = dropboxAPIService.getTeamInfo();
            log.info("Team info loaded successfully: {}", teamInfo);
        } catch (Exception e) {
            errorMessage = "Unable to fetch team information: " + e.getMessage();
            log.error("Failed to load team info", e);
        }

        model.addAttribute("admin", adminProfile);
        model.addAttribute("teamInfo", teamInfo);
        model.addAttribute("error", errorMessage);

        return "dashboard";
    }
}
