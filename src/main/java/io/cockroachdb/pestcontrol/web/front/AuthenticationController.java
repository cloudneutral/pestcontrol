package io.cockroachdb.pestcontrol.web.front;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.web.model.AuthenticationRequest;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthenticationController {
    @Autowired
    private ApplicationModel applicationModel;

    @ModelAttribute("authentication")
    public AuthenticationRequest authenticationRequest() {
        return new AuthenticationRequest();
    }

    @GetMapping("/login")
    public String login(
            @ModelAttribute("authentication") AuthenticationRequest authenticationRequest,
            @RequestParam(name = "loginRequired", defaultValue = "false", required = false) Boolean loginRequired,
            @RequestParam(name = "loginError", defaultValue = "false", required = false) Boolean loginError,
            @RequestParam(name = "logoutSuccess", defaultValue = "false", required = false) Boolean logoutSuccess,
            final Model model) {
        model.addAttribute("authentication", authenticationRequest);
        model.addAttribute("clusterIds", applicationModel.getClusterIds());
        if (loginRequired) {
            model.addAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("authentication.login.required"));
        } else if (logoutSuccess) {
            model.addAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("authentication.logout.success"));
        }
        if (loginError) {
            model.addAttribute(WebUtils.MSG_ERROR, WebUtils.getMessage("authentication.login.error"));
        }
        return "login";
    }

    @ModelAttribute("requestUri")
    public String getRequestServletPath(final HttpServletRequest request) {
        return request.getRequestURI();
    }
}
