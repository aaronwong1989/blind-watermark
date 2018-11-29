package cn.com.zybank.blindwatermark.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Aaron
 */
@Controller
public class IndexController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }
}
