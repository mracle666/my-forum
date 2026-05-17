package com.myforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myforum.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
