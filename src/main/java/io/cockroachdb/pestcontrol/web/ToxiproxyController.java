package io.cockroachdb.pestcontrol.web;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.cockroachdb.pestcontrol.web.api.toxi.ProxyForm;
import io.cockroachdb.pestcontrol.web.api.toxi.ToxicForm;
import io.cockroachdb.pestcontrol.web.api.toxi.ToxiproxyAccessException;
import io.cockroachdb.pestcontrol.web.api.toxi.ToxiproxyRestController;
import jakarta.validation.Valid;

@WebController
@RequestMapping("/proxy")
public class ToxiproxyController {
    @Autowired
    private ToxiproxyRestController toxiproxyRestController;

    @GetMapping
    public Callable<String> viewProxies(Model model) {
        model.addAttribute("proxies", toxiproxyRestController.findProxies().getBody());
        model.addAttribute("form", toxiproxyRestController.getProxyForm().getBody());
        return () -> "proxies";
    }

    @PostMapping
    public Callable<String> submitProxyForm(@Valid @ModelAttribute("form") ProxyForm form,
                                            BindingResult bindingResult,
                                            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                toxiproxyRestController.newProxy(form);
            } catch (ToxiproxyAccessException e) {
                bindingResult.addError(new ObjectError("globalError", e.getLocalizedMessage()));
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("proxies", toxiproxyRestController.findProxies().getBody());
            model.addAttribute("form", form);
            return () -> "proxies";
        }

        return () -> "redirect:proxy";
    }

    @GetMapping("/{name}/enable")
    public Callable<String> enableProxy(@PathVariable("name") String name) {
        toxiproxyRestController.enableProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/disable")
    public Callable<String> disableProxy(
            @PathVariable("name") String name) {
        toxiproxyRestController.disableProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/delete")
    public Callable<String> deleteProxy(@PathVariable("name") String name) {
        toxiproxyRestController.deleteProxy(name);
        return () -> "redirect:/proxy";
    }

    @GetMapping("/{name}/toxic")
    public Callable<String> viewToxics(@PathVariable("name") String name, Model model) {
        model.addAttribute("proxy", toxiproxyRestController.findProxy(name).getBody());
        model.addAttribute("toxics", toxiproxyRestController.findProxyToxics(name).getBody());
        model.addAttribute("form", toxiproxyRestController.getToxicForm(name).getBody());
        return () -> "toxics";
    }

    @PostMapping("/{name}/toxic")
    public Callable<String> submitToxicForm(@PathVariable("name") String name,
                                            @Valid @ModelAttribute("form") ToxicForm form,
                                            Model model,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return () -> "toxics";
        }
        toxiproxyRestController.newProxyToxic(name, form);
        model.addAttribute("form", form);
        return () -> "redirect:/proxy/" + name + "/toxic";
    }

    @GetMapping("/{name}/toxic/{toxic}/delete")
    public Callable<String> deleteProxyToxic(
            @PathVariable("name") String name,
            @PathVariable("toxic") String toxic) {
        toxiproxyRestController.deleteProxyToxic(name, toxic);
        return () -> "redirect:/proxy/" + name + "/toxic";
    }

}
