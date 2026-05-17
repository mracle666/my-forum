package com.myforum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myforum.model.entity.Like;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {

    default List<Long> findLikedTargetIds(Long userId, Integer targetType, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return Collections.emptyList();
        return selectList(new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .in(Like::getTargetId, targetIds))
                .stream().map(Like::getTargetId).collect(Collectors.toList());
    }

    default long countByTarget(Integer targetType, Long targetId) {
        return selectCount(new LambdaQueryWrapper<Like>()
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId));
    }

    default boolean existsByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return selectCount(new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId)) > 0;
    }

    default long countByTargetIds(Integer targetType, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return 0L;
        return selectCount(new LambdaQueryWrapper<Like>()
                .eq(Like::getTargetType, targetType)
                .in(Like::getTargetId, targetIds));
    }
}
