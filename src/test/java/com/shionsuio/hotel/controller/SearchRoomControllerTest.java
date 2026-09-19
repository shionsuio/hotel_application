package com.shionsuio.hotel.controller;

import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchRoomControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser //テスト中だけ認証ユーザーとして扱うもの
    @DisplayName("チェックイン日が未指定なら400を返す")
    void returnBadRequestWithCheckInIsMissing () throws Exception {
        mockMvc.perform(
                post("/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"checkOutDate": "2026-10-12"}
                                """)
        ).andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser //テスト中だけ認証ユーザーとして扱うもの
    @DisplayName("チェックイン、チェックアウトが指定なら200を返す")
    void returnAvailableRoomsForValidDates () throws Exception {
        mockMvc.perform(
                post("/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "checkInDate": "2026-10-10",
                                "checkOutDate": "2026-10-12"
                                }
                                """)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));
    }


}
