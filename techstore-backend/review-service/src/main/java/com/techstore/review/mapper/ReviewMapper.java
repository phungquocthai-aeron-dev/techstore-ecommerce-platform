package com.techstore.review.mapper;

import org.mapstruct.*;

import com.techstore.review.dto.response.*;
import com.techstore.review.entity.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ReviewMapper {

    @Mapping(target = "reply", expression = "java(mapReply(review.getReply()))")
    ReviewResponse toResponse(Review review);

    ReplyResponse toReplyResponse(Reply reply);

    default ReplyResponse mapReply(Reply reply) {
        if (reply == null || !"ACTIVE".equals(reply.getStatus())) {
            return null;
        }
        return toReplyResponse(reply);
    }
}
