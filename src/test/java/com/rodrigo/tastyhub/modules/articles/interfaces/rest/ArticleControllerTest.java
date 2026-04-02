package com.rodrigo.tastyhub.modules.articles.interfaces.rest;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(ArticleController.class)
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

}