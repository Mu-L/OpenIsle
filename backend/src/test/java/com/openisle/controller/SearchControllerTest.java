package com.openisle.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.openisle.dto.PostSummaryDto;
import com.openisle.dto.UserDto;
import com.openisle.mapper.PostMapper;
import com.openisle.mapper.UserMapper;
import com.openisle.model.Comment;
import com.openisle.model.Post;
import com.openisle.model.PostStatus;
import com.openisle.model.User;
import com.openisle.service.SearchService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SearchService searchService;

  @MockBean
  private UserMapper userMapper;

  @MockBean
  private PostMapper postMapper;

  @Test
  void userSearchEndpoint() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setUsername("alice");
    Mockito.when(searchService.searchUsers("ali")).thenReturn(List.of(user));
    UserDto userDto = new UserDto();
    userDto.setId(1L);
    userDto.setUsername("alice");
    Mockito.when(userMapper.toDto(user)).thenReturn(userDto);

    mockMvc
      .perform(get("/api/search/users").param("keyword", "ali"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].username").value("alice"));
  }

  @Test
  void globalSearchAggregatesTypes() throws Exception {
    User u = new User();
    u.setId(1L);
    u.setUsername("bob");
    Post p = new Post();
    p.setId(2L);
    p.setTitle("hello");
    p.setStatus(PostStatus.PUBLISHED);
    Comment c = new Comment();
    c.setId(3L);
    c.setContent("nice");
    Mockito.when(searchService.globalSearch("n")).thenReturn(
      List.of(
        new SearchService.SearchResult("user", 1L, "bob", null, null, null, null, null, null),
        new SearchService.SearchResult("post", 2L, "hello", null, null, null, null, null, null),
        new SearchService.SearchResult("comment", 3L, "nice", null, null, null, null, null, null)
      )
    );

    mockMvc
      .perform(get("/api/search/global").param("keyword", "n"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].type").value("user"))
      .andExpect(jsonPath("$[1].type").value("post"))
      .andExpect(jsonPath("$[2].type").value("comment"));
  }

  @Test
  void searchPostsByTitle() throws Exception {
    Post p = new Post();
    p.setId(2L);
    p.setTitle("spring");
    Mockito.when(searchService.searchPostsByTitle("spr")).thenReturn(List.of(p));
    PostSummaryDto summaryDto1 = new PostSummaryDto();
    summaryDto1.setId(2L);
    summaryDto1.setTitle("spring");
    Mockito.when(postMapper.toSummaryDto(p)).thenReturn(summaryDto1);

    mockMvc
      .perform(get("/api/search/posts/title").param("keyword", "spr"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].title").value("spring"));
  }

  @Test
  void searchPosts() throws Exception {
    Post p = new Post();
    p.setId(5L);
    p.setTitle("hello");
    Mockito.when(searchService.searchPosts("he")).thenReturn(List.of(p));
    PostSummaryDto summaryDto2 = new PostSummaryDto();
    summaryDto2.setId(5L);
    summaryDto2.setTitle("hello");
    Mockito.when(postMapper.toSummaryDto(p)).thenReturn(summaryDto2);

    mockMvc
      .perform(get("/api/search/posts").param("keyword", "he"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(5));
  }
}
