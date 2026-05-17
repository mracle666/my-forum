package com.myforum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myforum.model.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    default boolean existsByUserAndPost(Long userId, Long postId) {
        return selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getPostId, postId)) > 0;
    }

    default long countByPost(Long postId) {
        return selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPostId, postId));
    }
}
