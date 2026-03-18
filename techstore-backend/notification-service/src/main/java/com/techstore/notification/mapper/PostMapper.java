package com.techstore.notification.mapper;

import org.mapstruct.Mapper;

import com.techstore.notification.dto.response.PostResponse;
import com.techstore.notification.entity.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
