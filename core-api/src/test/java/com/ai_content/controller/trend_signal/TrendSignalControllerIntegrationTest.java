package com.ai_content.controller.trend_signal;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.jwt.JwtTokenProvider;
import com.ai_content.followed_channel.FollowedChannelEntity;
import com.ai_content.followed_channel.FollowedChannelJpaRepository;
import com.ai_content.followed_channel.domain.FollowedChannelPlatform;
import com.ai_content.trend_signal.TrendSignalEntity;
import com.ai_content.trend_signal.TrendSignalJpaRepository;
import com.ai_content.trend_signal.domain.PostFormat;
import com.ai_content.user.UserEntity;
import com.ai_content.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// The default test H2 has no jsonb type, so trend_signals is never created there.
// A separate in-memory DB maps jsonb to H2's JSON type.
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:trend_signal;DB_CLOSE_DELAY=-1;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON")
@Transactional
class TrendSignalControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired UserJpaRepository userJpaRepository;
    @Autowired FollowedChannelJpaRepository followedChannelJpaRepository;
    @Autowired TrendSignalJpaRepository trendSignalJpaRepository;

    private String token;
    private String otherUserToken;
    private Long channelAId;
    private Long channelBId;
    private Long oldPostId;
    private Long otherUserPostId;
    private Long deletedPostId;
    private Long deletedChannelPostId;

    /*
     * user: channel A (active), channel B (paused), channel C (deleted)
     *   A: "Hot video 50% off"  VIDEO_SCRIPT  likes 500  published 2026-10-20 10:00
     *   A: "Old text post"      TEXT          likes 50   published 2026-09-01 08:00  (used)
     *   A: "No date post"       TEXT          likes 900  published null
     *   A: "Deleted post"       TEXT          (soft deleted)
     *   B: "Paused channel img" IMAGE_TEXT    likes 100  published 2026-10-21 23:59
     *   C: "From deleted chan"  TEXT          (channel soft deleted)
     * other user: 1 post
     */
    @BeforeEach
    void setUp() {
        UserEntity user = saveUser();
        UserEntity otherUser = saveUser();
        token = jwtTokenProvider.createAccessToken(UserEntity.toDomain(user));
        otherUserToken = jwtTokenProvider.createAccessToken(UserEntity.toDomain(otherUser));

        FollowedChannelEntity channelA = saveChannel(user.getId(), "Channel A");
        FollowedChannelEntity channelB = saveChannel(user.getId(), "Channel B");
        channelB.update(null, false);
        FollowedChannelEntity channelC = saveChannel(user.getId(), "Channel C");
        channelC.delete();
        FollowedChannelEntity otherChannel = saveChannel(otherUser.getId(), "Other");
        channelAId = channelA.getId();
        channelBId = channelB.getId();

        savePost(user.getId(), channelAId, "Hot video 50% off", PostFormat.VIDEO_SCRIPT, 500,
                LocalDateTime.of(2026, 10, 20, 10, 0));
        TrendSignalEntity old = savePost(user.getId(), channelAId, "Old text post", PostFormat.TEXT, 50,
                LocalDateTime.of(2026, 9, 1, 8, 0));
        old.markUsed();
        oldPostId = old.getId();
        savePost(user.getId(), channelAId, "No date post", PostFormat.TEXT, 900, null);
        TrendSignalEntity deleted = savePost(user.getId(), channelAId, "Deleted post", PostFormat.TEXT, 1, null);
        deleted.delete();
        deletedPostId = deleted.getId();
        savePost(user.getId(), channelBId, "Paused channel img", PostFormat.IMAGE_TEXT, 100,
                LocalDateTime.of(2026, 10, 21, 23, 59));
        deletedChannelPostId = savePost(user.getId(), channelC.getId(), "From deleted chan", PostFormat.TEXT, 1,
                LocalDateTime.of(2026, 10, 1, 0, 0)).getId();
        otherUserPostId = savePost(otherUser.getId(), otherChannel.getId(), "Not yours", PostFormat.TEXT, 1,
                LocalDateTime.of(2026, 10, 1, 0, 0)).getId();

        trendSignalJpaRepository.flush();
    }

    @Test
    void list_defaultSort_isPublishedAtDescWithNullsLast_andHidesDeletedAndOtherUsers() throws Exception {
        list("")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].title").value(contains(
                        "Paused channel img", "Hot video 50% off", "Old text post", "No date post")))
                .andExpect(jsonPath("$.content[0].channelName").value("Channel B"))
                .andExpect(jsonPath("$.content[0]", not(hasKey("storyFormula"))));
    }

    @Test
    void list_sortByLikesDesc() throws Exception {
        list("?sort=likes,desc")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].likes").value(contains(900, 500, 100, 50)));
    }

    @Test
    void list_filterByChannel() throws Exception {
        list("?channelId=" + channelBId)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Paused channel img"));
    }

    @Test
    void list_filterByFormatAndIsUsed() throws Exception {
        list("?format=TEXT&isUsed=false")
                .andExpect(jsonPath("$.content[*].title").value(contains("No date post")));
    }

    @Test
    void list_filterByDateRange_isInclusiveOnBothEnds() throws Exception {
        list("?from=2026-10-20&to=2026-10-21")
                .andExpect(jsonPath("$.content[*].title").value(contains(
                        "Paused channel img", "Hot video 50% off")));
    }

    @Test
    void list_keyword_isCaseInsensitive_andTreatsPercentLiterally() throws Exception {
        list("?keyword=HOT")
                .andExpect(jsonPath("$.content[*].title").value(contains("Hot video 50% off")));
        list("?keyword=50%")
                .andExpect(jsonPath("$.content[*].title").value(contains("Hot video 50% off")));
        list("?keyword=%")
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void list_paging() throws Exception {
        list("?page=1&size=3")
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content[*].title").value(contains("No date post")));
    }

    @Test
    void list_unknownSort_returns400() throws Exception {
        list("?sort=userId,asc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.TREND_SIGNAL_INVALID_SORT.getCode()));
    }

    @Test
    void list_fromAfterTo_returns400() throws Exception {
        list("?from=2026-10-21&to=2026-10-20")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.TREND_SIGNAL_INVALID_DATE_RANGE.getCode()));
    }

    @Test
    void list_invalidFormat_returns400() throws Exception {
        list("?format=PODCAST").andExpect(status().isBadRequest());
    }

    @Test
    void list_keywordTooLong_returns400() throws Exception {
        list("?keyword=" + "a".repeat(101)).andExpect(status().isBadRequest());
    }

    @Test
    void list_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/trend-signals")).andExpect(status().isUnauthorized());
    }

    @Test
    void getOne_returnsDetailWithStoryFormulaField() throws Exception {
        detail(oldPostId, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Old text post"))
                .andExpect(jsonPath("$.channelName").value("Channel A"))
                .andExpect(jsonPath("$.isUsed").value(true))
                .andExpect(jsonPath("$", hasKey("storyFormula")));
    }

    @Test
    void getOne_notVisible_returns404() throws Exception {
        detail(otherUserPostId, token).andExpect(status().isNotFound());
        detail(deletedPostId, token).andExpect(status().isNotFound());
        detail(deletedChannelPostId, token).andExpect(status().isNotFound());
        detail(oldPostId, otherUserToken).andExpect(status().isNotFound());
        detail(999_999L, token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.TREND_SIGNAL_NOT_FOUND.getCode()));
    }

    private ResultActions list(String query) throws Exception {
        return mockMvc.perform(get("/api/v1/trend-signals" + query).header("Authorization", "Bearer " + token));
    }

    private ResultActions detail(Long id, String accessToken) throws Exception {
        return mockMvc.perform(get("/api/v1/trend-signals/" + id).header("Authorization", "Bearer " + accessToken));
    }

    private UserEntity saveUser() {
        String unique = UUID.randomUUID().toString();
        return userJpaRepository.save(UserEntity.createUser("user", unique + "@test.com", "password"));
    }

    private FollowedChannelEntity saveChannel(Long userId, String name) {
        return followedChannelJpaRepository.save(FollowedChannelEntity.create(
                userId, FollowedChannelPlatform.FANPAGE, "https://fb.com/" + UUID.randomUUID(), name));
    }

    private TrendSignalEntity savePost(Long userId, Long channelId, String title, PostFormat format,
                                       int likes, LocalDateTime publishedAt) {
        return trendSignalJpaRepository.save(TrendSignalEntity.create(
                userId, channelId, UUID.randomUUID().toString(), title, "https://fb.com/p",
                publishedAt, likes, 0, 0, format));
    }
}
