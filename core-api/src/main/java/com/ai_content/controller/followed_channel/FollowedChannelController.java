package com.ai_content.controller.followed_channel;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.common.PagedResponse;
import com.ai_content.controller.followed_channel.request.CreateFollowedChannelRequest;
import com.ai_content.controller.followed_channel.request.UpdateFollowedChannelRequest;
import com.ai_content.controller.followed_channel.response.FollowedChannelResponse;
import com.ai_content.followed_channel.service.FollowedChannelService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/followed-channels")
@RequiredArgsConstructor
public class FollowedChannelController {

    private final FollowedChannelService followedChannelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowedChannelResponse create(
            @CurrentUser User user,
            @RequestBody @Valid CreateFollowedChannelRequest request
    ) {
        return FollowedChannelResponse.from(
                followedChannelService.create(request.toCommand(user.id()))
        );
    }

    @GetMapping
    public PagedResponse<FollowedChannelResponse> getAll(
            @CurrentUser User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return PagedResponse.from(
                followedChannelService.getAll(user.id(), pageable),
                FollowedChannelResponse::from
        );
    }

    @GetMapping("/{id}")
    public FollowedChannelResponse getOne(
            @CurrentUser User user,
            @PathVariable Long id
    ) {
        return FollowedChannelResponse.from(
                followedChannelService.getOne(id, user.id())
        );
    }

    @PatchMapping("/{id}")
    public FollowedChannelResponse update(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestBody @Valid UpdateFollowedChannelRequest request
    ) {
        return FollowedChannelResponse.from(
                followedChannelService.update(request.toCommand(id, user.id()))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @CurrentUser User user,
            @PathVariable Long id
    ) {
        followedChannelService.delete(id, user.id());
    }
}
